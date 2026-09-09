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
            Account account = accountByToken(exchange);
            if (account == null) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"не авторизован\"}");
                return;
            }
            sendJson(exchange, AttendanceApp.studentsToJson());
        }
    }
    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            Account account = accountByToken(exchange);
            if (account == null) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"не авторизован\"}");
                return;
            }
            if (account.getRole() != Role.ADMIN) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"нет доступа\"}");
                return;
            }
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
            AttendanceApp.accounts.put(key,new Account(key,"1234",Role.STUDENT));
            sendJson(exchange,"{\"result\":\"ok\",\"name\":\""+key+"\"}");
        }
    }
    static class ReviewHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            Account account=accountByToken(exchange);
            if (account==null) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"не авторизован\"}");
                return;
            }
            if (account.getRole()!=Role.TEACHER) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"нет доступа\"}");
                return;
            }
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
            Account account=accountByToken(exchange);
            if (account==null) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"не авторизован\"}");
                return;
            }
            if (account.getRole()!=Role.ADMIN) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"нет доступа\"}");
                return;
            }
            FileStorage.saveStudents(new ArrayList<>(AttendanceApp.students.values()),"students.txt");
            FileStorage.saveAccounts(new ArrayList<>(AttendanceApp.accounts.values()),"accounts.txt");
            sendJson(exchange,"{\"result\":\"ok\",\"message\":\"saved\"}");
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
            Account account = accountByToken(exchange);
            if (account == null) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"не авторизован\"}");
                return;
            }
            if (account.getRole() != Role.STUDENT) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"нет доступа\"}");
                return;
            }
            String key = account.getLogin();
            Student target = AttendanceApp.students.get(key);
            if (target == null) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"Ты не найден в списках школы\"}");
                return;
            }
            Map<String, String> params = parseQuery(exchange);
            String answer = params.get("answer");
            if ("no".equals(answer)) {
                target.setStatus(RegistrationStatus.REJECTED);
            } else {
                target.setStatus(RegistrationStatus.PENDING);
            }
            sendJson(exchange, "{\"result\":\"ok\",\"name\":\"" + key + "\"}");
        }
    }
    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            Map<String,String> params=parseQuery(exchange);
            String login=params.get("login");
            String password=params.get("password");
            if (login==null||password==null) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"нужны логин и пароль\"}");
                return;
            }
            Account account=AttendanceApp.accounts.get(login);
            if (account==null||!account.getPassword().equals(password)) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"неверный логин или пароль\"}");
                return;
            }
            String token=java.util.UUID.randomUUID().toString();
            AttendanceApp.sessions.put(token,account);
            sendJson(exchange,"{\"result\":\"ok\",\"token\":\""+token+"\",\"role\":\""+account.getRole()+"\"}");
        }
    }
    static class ChangePasswordHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            Account account=accountByToken(exchange);
            if(account==null) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"не авторизован\"}");
                return;
            }
            Map<String, String> params=parseQuery(exchange);
            String oldPassword=params.get("oldPassword");
            String newPassword=params.get("newPassword");
            if (oldPassword==null||newPassword==null) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"нужны старый и новый пароль\"}");
                return;
            }
            if(!account.getPassword().equals(oldPassword)) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"неверный текущий пароль\"}");
                return;
            }
            if(newPassword.length()<8) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"новый пароль слишком короткий(не менее 8 символов)\"}");
                return;
            }
            account.setPassword(newPassword);
            FileStorage.saveAccounts(new ArrayList<>(AttendanceApp.accounts.values()),"accounts.txt");
            sendJson(exchange,"{\"result\":\"ok\",\"message\":\"пароль успешно изменён!\"}");
        }
    }
    static Account accountByToken(HttpExchange exchange) {
        String token = parseQuery(exchange).get("token");
        if (token == null) {
            return null;
        }
        return AttendanceApp.sessions.get(token);
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
        server.createContext("/login", new LoginHandler());
        server.createContext("/change-password", new ChangePasswordHandler());
        server.start();
        System.out.println("Сервер запущен на порту 8080!");
    }
}