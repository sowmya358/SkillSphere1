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

// A tiny built-in web server (no external framework). It listens for
// requests from the frontend and forwards them to UserDAO.
//   POST /api/register  -> create an account
//   POST /api/login     -> check email + password
public class RegisterServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());
                server.createContext("/api/skills/list", new SkillHandlers.ListHandler());
        server.createContext("/api/skills/add", new SkillHandlers.ActionHandler("add"));
        server.createContext("/api/skills/update", new SkillHandlers.ActionHandler("update"));
        server.createContext("/api/skills/delete", new SkillHandlers.ActionHandler("delete"));
        server.createContext("/api/skills/search", new SkillHandlers.SearchHandler());
                server.createContext("/api/requests/create", new RequestHandlers.CreateHandler());
        server.createContext("/api/requests/incoming", new RequestHandlers.ListHandler(true));
        server.createContext("/api/requests/outgoing", new RequestHandlers.ListHandler(false));
        server.createContext("/api/requests/accept", new RequestHandlers.RespondHandler(true));
        server.createContext("/api/requests/reject", new RequestHandlers.RespondHandler(false));
        server.setExecutor(null);
        server.start();
        System.out.println("SkillSphere backend running at http://localhost:8080");
        System.out.println("Press Ctrl+C to stop.");
    }

    // ---------------- /api/register ----------------
    static class RegisterHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (handlePreflightAndMethod(exchange)) return;

                Map<String, String> params = readForm(exchange);

                User newUser = new User(
                        params.get("fullname"),
                        params.get("email"),
                        params.get("password"),
                        "" // bio is empty at registration time
                );

                String result = new UserDAO().registerUser(newUser);

                if (result.startsWith("SUCCESS")) {
                    sendResponse(exchange, 200, "text/plain", result);
                } else if (result.startsWith("FAILED")) {
                    sendResponse(exchange, 400, "text/plain", result);   // user's mistake
                } else {
                    sendResponse(exchange, 500, "text/plain", result);   // server/database problem
                }

            } catch (Exception e) {
                // Never let an unexpected error silently drop the connection
                e.printStackTrace();
                sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }

    // ---------------- /api/login ----------------
    static class LoginHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (handlePreflightAndMethod(exchange)) return;

                Map<String, String> params = readForm(exchange);

                User user = new UserDAO().loginUser(params.get("email"), params.get("password"));

                if (user == null) {
                    sendResponse(exchange, 401, "text/plain", "FAILED: Invalid email or password");
                    return;
                }

                // Send the logged-in user's details back as JSON (never the password)
                String json = "{"
                        + "\"userId\":" + user.getUserId() + ","
                        + "\"name\":\"" + jsonEscape(user.getName()) + "\","
                        + "\"email\":\"" + jsonEscape(user.getEmail()) + "\","
                        + "\"credits\":" + user.getCredits()
                        + "}";
                sendResponse(exchange, 200, "application/json", json);

            } catch (java.sql.SQLException e) {
                System.out.println("Error during login: " + e.getMessage());
                sendResponse(exchange, 500, "text/plain",
                        "ERROR: Server could not check your login. Check the backend console.");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "text/plain", "ERROR: Something went wrong on the server");
            }
        }
    }

    // ---------------- shared helpers ----------------

    // Adds CORS headers, approves OPTIONS preflight, rejects non-POST.
    // Returns true if the request was already answered (caller should stop).
    static boolean handlePreflightAndMethod(HttpExchange exchange) throws IOException {
        // Allow the browser (opened as a local file) to call this server
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        // Browsers may send a preflight OPTIONS request before POST - just approve it
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "text/plain", "Only POST is allowed");
            return true;
        }
        return false;
    }

     static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return parseFormData(body);
    }

    // Turns "email=a%40b.com&password=1234" into a Map
    private static Map<String, String> parseFormData(String body) {
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

     static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

     static void sendResponse(HttpExchange exchange, int statusCode,
                                     String contentType, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}