package skillsphere;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// A tiny built-in web server (no external framework) that listens for
// registration requests from register.html and forwards them to UserDAO.
public class RegisterServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/register", new RegisterHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("SkillSphere backend running at http://localhost:8080");
    }

    static class RegisterHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // Allow the browser (opened as a local file) to call this server
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            // Browsers send a preflight OPTIONS request before POST - just approve it
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Only POST is allowed");
                return;
            }

            // Read the form data sent from the browser
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);

            User newUser = new User(
                    params.get("fullname"),
                    params.get("email"),
                    params.get("password"),
                    "" // bio is empty at registration time
            );

            UserDAO userDAO = new UserDAO();
            boolean success = userDAO.registerUser(newUser);

            if (success) {
                sendResponse(exchange, 200, "SUCCESS: Account created");
            } else {
                sendResponse(exchange, 400, "FAILED: Email may already be registered or input was invalid");
            }
        }

        // Turns "fullname=Arjun&email=a%40b.com&password=1234" into a Map
        private Map<String, String> parseFormData(String body) {
            Map<String, String> map = new HashMap<>();
            for (String pair : body.split("&")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    map.put(key, value);
                }
            }
            return map;
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }
}
