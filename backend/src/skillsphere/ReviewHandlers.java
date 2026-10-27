package skillsphere;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

// HTTP endpoint for reviews:
//   POST /api/reviews/create  (userId = the learner, requestId, rating, comment)
public class ReviewHandlers {

    static class CreateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (RegisterServer.handlePreflightAndMethod(exchange)) return;
                Map<String, String> p = RegisterServer.readForm(exchange);

                int userId = Integer.parseInt(p.get("userId"));
                int requestId = Integer.parseInt(p.get("requestId"));
                int rating = Integer.parseInt(p.get("rating"));
                String result = new ReviewDAO().createReview(requestId, userId, rating, p.get("comment"));

                int code = result.startsWith("SUCCESS") ? 200 : result.startsWith("FAILED") ? 400 : 500;
                RegisterServer.sendResponse(exchange, code, "text/plain", result);

            } catch (NumberFormatException e) {
                RegisterServer.sendResponse(exchange, 400, "text/plain", "FAILED: Invalid user, request or rating");
            } catch (Exception e) {
                e.printStackTrace();
                RegisterServer.sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }
}