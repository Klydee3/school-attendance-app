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
import java.util.ArrayList;
public class AttendanceServer {
    static Map<String,String> parseQuery(HttpExchange exchange) {
        Map<String,String> params=new HashMap<>();
        String query=exchange.getRequestURI().getQuery();
        if (query!=null) {
            for (String pair:query.split("&")) {
                String[] kv=pair.split("=");
                if (kv.length==2) {
                    params.put(kv[0],URLDecoder.decode(kv[1],StandardCharsets.UTF_8));
                }
            }
        }
        return params;
    }
    static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes=json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type","application/json;charset=UTF-8");
        exchange.sendResponseHeaders(200,bytes.length);
        OutputStream os=exchange.getResponseBody();
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
            Map<String, String> params = parseQuery(exchange);
            String name=params.get("name");
            String surname=params.get("surname");
            if (name==null||surname==null||name.isEmpty()||surname.isEmpty()) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"нужны имя и фамилия\"}");
                return;
            }
            String key=surname+" "+name;
            if (AttendanceApp.students.containsKey(key)) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"уже есть в списках\"}");
                return;
            }
            AttendanceApp.students.put(key,new Student(name,surname));
            sendJson(exchange,"{\"result\":\"ok\",\"name\":\""+key+"\"}");
        }
    }
    static class ReviewHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params=parseQuery(exchange);
            String name=params.get("name");
            String surname=params.get("surname");
            String decision=params.get("decision");
            String key=surname+" "+name;
            Student target=AttendanceApp.students.get(key);
            if (target==null) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"студент не найден\"}");
            } else if (!"APPROVED".equalsIgnoreCase(decision)&&!"REJECTED".equalsIgnoreCase(decision)) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"bad decision\"}");
            } else {
                target.setStatus(RegistrationStatus.valueOf(decision.toUpperCase()));
                sendJson(exchange,"{\"result\":\"ok\",\"name\":\""+key+"\",\"status\":\""+target.getStatus()+"\"}");
            }
        }
    }
    static class SaveHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            FileStorage.saveStudents(new ArrayList<>(AttendanceApp.students.values()),"students.txt");
            sendJson(exchange, "{\"result\":\"ok\",\"message\":\"saved\"}");
        }
    }
    static class PageHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String path=exchange.getRequestURI().getPath();
            if(path.equals("/")) {
                path="/index.html";
            }
            java.io.File file=new java.io.File("web"+path);
            if(!file.exists()||!file.isFile()) {
                byte[] msg="404 Not Found".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404,msg.length);
                OutputStream os=exchange.getResponseBody();
                os.write(msg);
                os.close();
                return;
            }
                byte[] bytes=java.nio.file.Files.readAllBytes(file.toPath());
                String contentType=path.endsWith(".css")?"text/css; charset=UTF-8"
                :path.endsWith("js")?"text/javascript; charset=UTF-8"
                :"text/html; charset=UTF-8"; 
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os=exchange.getResponseBody();
                os.write(bytes);
                os.close();
        }
    }
    static class AttendHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            Map<String,String> params=parseQuery(exchange);
            String name=params.get("name");
            String surname=params.get("surname");
            String key=surname+" "+name;
            Student target=AttendanceApp.students.get(key);
            if (target==null) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"Ты не найден в списках школы\"}");
            } else {
                target.setStatus(RegistrationStatus.PENDING);
                sendJson(exchange, "{\"result\":\"ok\",\"name\":\""+key+"\"}");
            }
        }
    }
    public static void main(String[] args) throws IOException {
        AttendanceApp.loadRegistry();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/students", new StudentsHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/review", new ReviewHandler());
        server.createContext("/save", new SaveHandler());
        server.createContext("/", new PageHandler());
        server.createContext("/attend", new AttendHandler());
        server.start();
        System.out.println("Сервер запущен на порту 8080!");
    }
}