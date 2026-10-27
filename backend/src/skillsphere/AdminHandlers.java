package skillsphere;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// HTTP endpoints for the admin page (all POST):
//   /api/admin/login         (email, password)
//   /api/admin/users         (adminId)
//   /api/admin/skills        (adminId)
//   /api/admin/skills/delete (adminId, skillId)
public class AdminHandlers {

    // Turns a list of maps into JSON. Numbers stay numbers, everything else is a quoted string.
    private static String toJson(List<Map<String, Object>> rows) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) json.append(",");
            json.append("{");
            boolean first = true;
            for (Map.Entry<String, Object> e : rows.get(i).entrySet()) {
                if (!first) json.append(",");
                first = false;
                json.append("\"").append(e.getKey()).append("\":");
                Object v = e.getValue();
                if (v instanceof Integer) {
                    json.append(v);
                } else {
                    json.append("\"").append(RegisterServer.jsonEscape(v == null ? "" : v.toString())).append("\"");
                }
            }
            json.append("}");
        }
        return json.append("]").toString();
    }

    // ---------- /api/admin/login ----------
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (RegisterServer.handlePreflightAndMethod(exchange)) return;
                Map<String, String> p = RegisterServer.readForm(exchange);

                String[] admin = new AdminDAO().login(p.get("email"), p.get("password"));
                if (admin == null) {
                    RegisterServer.sendResponse(exchange, 401, "text/plain", "FAILED: Wrong email or password, or not an admin");
                } else {
                    String json = "{\"userId\":" + admin[0] + ",\"name\":\""
                            + RegisterServer.jsonEscape(admin[1]) + "\"}";
                    RegisterServer.sendResponse(exchange, 200, "application/json", json);
                }
            } catch (java.sql.SQLException e) {
                System.out.println("Error in admin login: " + e.getMessage());
                RegisterServer.sendResponse(exchange, 500, "text/plain",
                        "ERROR: Server could not log in. Check the backend console.");
            } catch (Exception e) {
                e.printStackTrace();
                RegisterServer.sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }

    // ---------- /api/admin/users, /skills and /skills/delete ----------
    static class DataHandler implements HttpHandler {
        private final String mode; // "users", "skills" or "deleteSkill"

        DataHandler(String mode) {
            this.mode = mode;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (RegisterServer.handlePreflightAndMethod(exchange)) return;
                Map<String, String> p = RegisterServer.readForm(exchange);

                AdminDAO dao = new AdminDAO();
                int adminId = Integer.parseInt(p.get("adminId"));
                if (!dao.isAdmin(adminId)) {
                    RegisterServer.sendResponse(exchange, 403, "text/plain", "FAILED: Admin access required");
                    return;
                }

                if (mode.equals("users")) {
                    RegisterServer.sendResponse(exchange, 200, "application/json", toJson(dao.getAllUsers()));
                } else if (mode.equals("skills")) {
                    RegisterServer.sendResponse(exchange, 200, "application/json", toJson(dao.getAllSkills()));
                } else {
                    String result = dao.deleteSkill(Integer.parseInt(p.get("skillId")));
                    int code = result.startsWith("SUCCESS") ? 200 : result.startsWith("FAILED") ? 400 : 500;
                    RegisterServer.sendResponse(exchange, code, "text/plain", result);
                }

            } catch (NumberFormatException e) {
                RegisterServer.sendResponse(exchange, 400, "text/plain", "FAILED: Invalid admin or skill");
            } catch (java.sql.SQLException e) {
                System.out.println("Error in admin request: " + e.getMessage());
                RegisterServer.sendResponse(exchange, 500, "text/plain",
                        "ERROR: Server could not load data. Check the backend console.");
            } catch (Exception e) {
                e.printStackTrace();
                RegisterServer.sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }
}