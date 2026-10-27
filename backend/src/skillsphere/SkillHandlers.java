package skillsphere;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// HTTP endpoints for skill management:
//   POST /api/skills/list    (userId)
//   POST /api/skills/add     (userId, name, category, description)
//   POST /api/skills/update  (userId, skillId, name, category, description)
//   POST /api/skills/delete  (userId, skillId)
public class SkillHandlers {

    // ---------- /api/skills/list ----------
    static class ListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (RegisterServer.handlePreflightAndMethod(exchange)) return;
                Map<String, String> p = RegisterServer.readForm(exchange);

                int userId = Integer.parseInt(p.get("userId"));
                List<Skill> skills = new SkillDAO().getSkillsByUser(userId);

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < skills.size(); i++) {
                    Skill s = skills.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"skillId\":").append(s.getSkillId()).append(",")
                        .append("\"name\":\"").append(RegisterServer.jsonEscape(s.getName())).append("\",")
                        .append("\"category\":\"").append(RegisterServer.jsonEscape(s.getCategory())).append("\",")
                        .append("\"description\":\"").append(RegisterServer.jsonEscape(s.getDescription())).append("\"")
                        .append("}");
                }
                json.append("]");
                RegisterServer.sendResponse(exchange, 200, "application/json", json.toString());

            } catch (NumberFormatException e) {
                RegisterServer.sendResponse(exchange, 400, "text/plain", "FAILED: Invalid user");
            } catch (java.sql.SQLException e) {
                System.out.println("Error listing skills: " + e.getMessage());
                RegisterServer.sendResponse(exchange, 500, "text/plain",
                        "ERROR: Server could not load skills. Check the backend console.");
            } catch (Exception e) {
                e.printStackTrace();
                RegisterServer.sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }

    // ---------- /api/skills/add, /update, /delete ----------
    static class ActionHandler implements HttpHandler {
        private final String action; // "add", "update" or "delete"

        ActionHandler(String action) {
            this.action = action;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (RegisterServer.handlePreflightAndMethod(exchange)) return;
                Map<String, String> p = RegisterServer.readForm(exchange);

                int userId = Integer.parseInt(p.get("userId"));
                SkillDAO dao = new SkillDAO();
                String result;

                if (action.equals("add")) {
                    result = dao.addSkill(new Skill(userId, p.get("name"),
                            p.get("category"), p.get("description")));
                } else if (action.equals("update")) {
                    Skill s = new Skill(userId, p.get("name"), p.get("category"), p.get("description"));
                    s.setSkillId(Integer.parseInt(p.get("skillId")));
                    result = dao.updateSkill(s);
                } else {
                    result = dao.deleteSkill(Integer.parseInt(p.get("skillId")), userId);
                }

                if (result.startsWith("SUCCESS")) {
                    RegisterServer.sendResponse(exchange, 200, "text/plain", result);
                } else if (result.startsWith("FAILED")) {
                    RegisterServer.sendResponse(exchange, 400, "text/plain", result);
                } else {
                    RegisterServer.sendResponse(exchange, 500, "text/plain", result);
                }

            } catch (NumberFormatException e) {
                RegisterServer.sendResponse(exchange, 400, "text/plain", "FAILED: Invalid user or skill");
            } catch (Exception e) {
                e.printStackTrace();
                RegisterServer.sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }
}