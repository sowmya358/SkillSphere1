package skillsphere;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// HTTP endpoints for exchange requests (all POST):
//   /api/requests/create    (userId, skillId)
//   /api/requests/incoming  (userId)
//   /api/requests/outgoing  (userId)
//   /api/requests/accept    (userId, requestId)
//   /api/requests/reject    (userId, requestId)
public class RequestHandlers {

    // Maps "SUCCESS/FAILED/ERROR: ..." to a status code and sends it
    private static void sendResult(HttpExchange exchange, String result) throws IOException {
        int code = result.startsWith("SUCCESS") ? 200 : result.startsWith("FAILED") ? 400 : 500;
        RegisterServer.sendResponse(exchange, code, "text/plain", result);
    }

    // ---------- /api/requests/create ----------
    static class CreateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (RegisterServer.handlePreflightAndMethod(exchange)) return;
                Map<String, String> p = RegisterServer.readForm(exchange);

                int userId = Integer.parseInt(p.get("userId"));
                int skillId = Integer.parseInt(p.get("skillId"));
                sendResult(exchange, new RequestDAO().createRequest(skillId, userId));

            } catch (NumberFormatException e) {
                RegisterServer.sendResponse(exchange, 400, "text/plain", "FAILED: Invalid user or skill");
            } catch (Exception e) {
                e.printStackTrace();
                RegisterServer.sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }

    // ---------- /api/requests/incoming and /outgoing ----------
    static class ListHandler implements HttpHandler {
        private final boolean incoming;

        ListHandler(boolean incoming) {
            this.incoming = incoming;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (RegisterServer.handlePreflightAndMethod(exchange)) return;
                Map<String, String> p = RegisterServer.readForm(exchange);

                int userId = Integer.parseInt(p.get("userId"));
                List<ExchangeRequest> list = new RequestDAO().getRequests(userId, incoming);

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    ExchangeRequest r = list.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"requestId\":").append(r.getRequestId()).append(",")
                        .append("\"skillName\":\"").append(RegisterServer.jsonEscape(r.getSkillName())).append("\",")
                        .append("\"otherName\":\"").append(RegisterServer.jsonEscape(r.getOtherName())).append("\",")
                        .append("\"status\":\"").append(RegisterServer.jsonEscape(r.getStatus())).append("\",")
                        .append("\"createdAt\":\"").append(RegisterServer.jsonEscape(r.getCreatedAt())).append("\"")
                        .append("}");
                }
                json.append("]");
                RegisterServer.sendResponse(exchange, 200, "application/json", json.toString());

            } catch (NumberFormatException e) {
                RegisterServer.sendResponse(exchange, 400, "text/plain", "FAILED: Invalid user");
            } catch (java.sql.SQLException e) {
                System.out.println("Error listing requests: " + e.getMessage());
                RegisterServer.sendResponse(exchange, 500, "text/plain",
                        "ERROR: Server could not load requests. Check the backend console.");
            } catch (Exception e) {
                e.printStackTrace();
                RegisterServer.sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }

    // ---------- /api/requests/accept and /reject ----------
    static class RespondHandler implements HttpHandler {
        private final boolean accept;

        RespondHandler(boolean accept) {
            this.accept = accept;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (RegisterServer.handlePreflightAndMethod(exchange)) return;
                Map<String, String> p = RegisterServer.readForm(exchange);

                int userId = Integer.parseInt(p.get("userId"));
                int requestId = Integer.parseInt(p.get("requestId"));
                sendResult(exchange, new RequestDAO().respond(requestId, userId, accept));

            } catch (NumberFormatException e) {
                RegisterServer.sendResponse(exchange, 400, "text/plain", "FAILED: Invalid user or request");
            } catch (Exception e) {
                e.printStackTrace();
                RegisterServer.sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }
        // ---------- /api/requests/complete ----------
    //   POST (userId = the teacher, requestId)
    static class CompleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (RegisterServer.handlePreflightAndMethod(exchange)) return;
                Map<String, String> p = RegisterServer.readForm(exchange);

                int userId = Integer.parseInt(p.get("userId"));
                int requestId = Integer.parseInt(p.get("requestId"));
                sendResult(exchange, new RequestDAO().completeSession(requestId, userId));

            } catch (NumberFormatException e) {
                RegisterServer.sendResponse(exchange, 400, "text/plain", "FAILED: Invalid user or request");
            } catch (Exception e) {
                e.printStackTrace();
                RegisterServer.sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }

    // ---------- /api/credits ----------
    //   POST (userId)  ->  plain number, e.g. 110
    static class CreditsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (RegisterServer.handlePreflightAndMethod(exchange)) return;
                Map<String, String> p = RegisterServer.readForm(exchange);

                int userId = Integer.parseInt(p.get("userId"));
                int credits = new RequestDAO().getCredits(userId);
                if (credits < 0) {
                    RegisterServer.sendResponse(exchange, 400, "text/plain", "FAILED: User not found");
                } else {
                    RegisterServer.sendResponse(exchange, 200, "text/plain", String.valueOf(credits));
                }

            } catch (NumberFormatException e) {
                RegisterServer.sendResponse(exchange, 400, "text/plain", "FAILED: Invalid user");
            } catch (java.sql.SQLException e) {
                System.out.println("Error loading credits: " + e.getMessage());
                RegisterServer.sendResponse(exchange, 500, "text/plain",
                        "ERROR: Server could not load credits. Check the backend console.");
            } catch (Exception e) {
                e.printStackTrace();
                RegisterServer.sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }
}