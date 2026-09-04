import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
public class AttendanceServer {
    static Map<String, String> parseQuery(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    params.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            }
        }
        return params;
    }
    static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    static class StudentsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, AttendanceApp.studentsToJson());
        }
    }
    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String name = parseQuery(exchange).get("name");
            if (name == null || name.isEmpty()) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"name required\"}");
            } else {
                AttendanceApp.students.put(name, new Student(name));
                sendJson(exchange, "{\"result\":\"ok\",\"name\":\"" + name + "\"}");
            }
        }
    }
    static class ReviewHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange);
            String name = params.get("name");
            String decision = params.get("decision");
            Student target = AttendanceApp.students.get(name);
            if (target == null) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"student not found\"}");
            } else if (!"APPROVED".equalsIgnoreCase(decision) && !"REJECTED".equalsIgnoreCase(decision)) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"bad decision\"}");
            } else {
                target.setStatus(RegistrationStatus.valueOf(decision.toUpperCase()));
                sendJson(exchange, "{\"result\":\"ok\",\"name\":\"" + name + "\",\"status\":\"" + target.getStatus() + "\"}");
            }
        }
    }
    public static void main(String[] args) throws IOException {
        AttendanceApp.loadRegistry();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/students", new StudentsHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/review", new ReviewHandler());
        server.start();
        System.out.println("Сервер запущен на порту 8080!");
    }
}