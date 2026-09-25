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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
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
    static void sendJson(HttpExchange exchange,int code,String json) throws IOException {
        byte[] bytes=json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type","application/json;charset=UTF-8");
        exchange.sendResponseHeaders(code,bytes.length);
        OutputStream os=exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    static void sendJson(HttpExchange exchange,String json) throws IOException {
        sendJson(exchange,200,json);
    }
    static class StudentsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            Account account=accountByToken(exchange);
            if (account==null) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"не авторизован\"}");
                return;
            }
            sendJson(exchange, AttendanceApp.studentsToJson());
        }
    }
    static class RegisterHandler implements HttpHandler {
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
            Map<String,String>params=parseBody(exchange);
            String name=params.get("name");
            String surname=params.get("surname");
			String className=params.get("className");
            if (name==null||surname==null||name.isEmpty()||surname.isEmpty()||className.isEmpty()) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"нужны имя, фамилия и класс\"}");
                return;
            }
            String key=surname+" "+name;
            if (AttendanceApp.students.containsKey(key)) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"уже есть в списках\"}");
                return;
            }
            AttendanceApp.students.put(key,new Student(name,surname,className));
			String password="12345678";
            String salt=AttendanceApp.newSalt();
			String hash=AttendanceApp.hashPassword(password,salt);
			AttendanceApp.accounts.put(key,new Account(key,Role.STUDENT,salt,hash));
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
            Account account=accountByToken(exchange);
            if (account==null) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"не авторизован\"}");
                return;
            }
            if (account.getRole()!=Role.STUDENT) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"нет доступа\"}");
                return;
            }
            String key=account.getLogin();
            Student target=AttendanceApp.students.get(key);
            if (target==null) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"Ты не найден в списках школы\"}");
                return;
            }
            Map<String,String> params=parseQuery(exchange);
            String answer=params.get("answer");
			FileStorage.addAttendanceMark(account.getLogin(),answer);
            if ("no".equals(answer)) {
                target.setStatus(RegistrationStatus.REJECTED);
            } else {
                target.setStatus(RegistrationStatus.PENDING);
            }
            sendJson(exchange,"{\"result\":\"ok\",\"name\":\"" + key + "\"}");
        }
    }
    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if(!exchange.getRequestMethod().equals("POST")) {
                sendJson(exchange,405,"{\"result\":\"error\",\"message\":\"нужен POST\"}");
                return;
            }
            Map<String,String> params=parseBody(exchange);
            String login=params.get("login");
            String password=params.get("password");
            if (login==null||password==null) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"нужны логин и пароль\"}");
                return;
            }
            Account account=AttendanceApp.accounts.get(login);
            if (account==null||!AttendanceApp.hashPassword(password,account.getSalt()).equals(account.getPasswordHash())) {
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
            Map<String, String> params=parseBody(exchange);
            String oldPassword=params.get("oldPassword");
            String newPassword=params.get("newPassword");
            if (oldPassword==null||newPassword==null) {
                sendJson(exchange, "{\"result\":\"error\",\"message\":\"нужны старый и новый пароль\"}");
                return;
            }
            if (!AttendanceApp.hashPassword(oldPassword,account.getSalt()).equals(account.getPasswordHash())) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"неверный текущий пароль\"}");
                return;
            }
            if(newPassword.length()<8) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"новый пароль слишком короткий(не менее 8 символов)\"}");
                return;
            }
            String salt=AttendanceApp.newSalt();
			account.setSalt(salt);
			account.setPasswordHash(AttendanceApp.hashPassword(newPassword,salt));
            FileStorage.saveAccounts(new ArrayList<>(AttendanceApp.accounts.values()),"accounts.txt");
            sendJson(exchange,"{\"result\":\"ok\",\"message\":\"пароль успешно изменён!\"}");
        }
    }
    static Account accountByToken(HttpExchange exchange) {
        String auth=exchange.getRequestHeaders().getFirst("Authorization");
        if(auth==null||!auth.startsWith("Bearer")) {
            return null;
        }
        String token=auth.substring(7);
        return AttendanceApp.sessions.get(token);
    }
    static class DeleteStudentHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            Account account=accountByToken(exchange);
            if(account==null) {
                sendJson(exchange,"{\"result\":error\",\"message\":\"не авторизован\"}");
                return;
            }
            if(account.getRole()!=Role.ADMIN) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"недостаточно прав\"}");
                return;
            }
            Map<String,String> params=parseQuery(exchange);
            String name=params.get("name");
            String surname=params.get("surname");
            String key=surname+" "+name;
            if(AttendanceApp.students.remove(key)==null) {
                sendJson(exchange,"{\"result\":\"error\",\"message\":\"ученик не найден\"}");
                return;
            }
            AttendanceApp.accounts.remove(key);
            FileStorage.saveStudents(new ArrayList<>(AttendanceApp.students.values()),"students.txt");
            FileStorage.saveAccounts(new ArrayList<>(AttendanceApp.accounts.values()),"accounts.txt");
            sendJson(exchange,"{\"result\":\"ok\",\"message\":\"ученик удален\"}");
        }
    }
	static class SummaryHandler implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			Account account=accountByToken(exchange);
			if (account==null) {
				sendJson(exchange,"{\"result\":\"error\",\"message\":\"не авторизован\"}");
				return;
			}
			if (account.getRole()==Role.STUDENT) {
				sendJson(exchange,"{\"result\":\"error\",\"message\":\"нет доступа\"}");
				return;
			}
			Map<String,String> params=parseQuery(exchange);
			String date=params.get("date");
			if(date==null||date.isEmpty()) {
				date=java.time.LocalDate.now().toString();
			}
			java.util.Set<String> present=new java.util.HashSet<>();
			java.io.File f=new java.io.File("attendance.txt");
			if(f.exists()) {
				BufferedReader reader=new BufferedReader(
					new InputStreamReader(new FileInputStream(f),StandardCharsets.UTF_8));
				String line;
				while((line=reader.readLine())!=null) {
					String[] p=line.split(";");
					if (p.length==3&&p[0].equals(date)&&p[2].equals("yes")) {
						present.add(p[1]);
					}
				}
				reader.close();
			}
			java.util.Map<String,int[]> byClass=new java.util.TreeMap<>();
			for (Student s:AttendanceApp.students.values()) {
				int[] c=byClass.get(s.getClassName());
				if (c==null) {
					c=new int[2];
					byClass.put(s.getClassName(),c);
				}
				c[0]++;
				if(present.contains(s.fullName())) {
					c[1]++;
				}
			}
			StringBuilder sb=new StringBuilder();
			sb.append("{\"result\":\"ok\",\"date\":\"").append(date).append("\",\"rows\":[");
			boolean first=true;
			int totalAll=0,presentAll=0;
			for (Map.Entry<String,int[]> e:byClass.entrySet()) {
				if(!first) sb.append(",");
				first=false;
				int total=e.getValue()[0],pr=e.getValue()[1];
				totalAll+=total;
				presentAll+=pr;
				int percent=total==0?0:pr*100/total;
				sb.append("{\"class\":\"").append(e.getKey())
				  .append("\",\"total\":").append(total)
				  .append(",\"present\":").append(pr)
				  .append(",\"absent\":").append(total-pr)
				  .append(",\"percent\":").append(percent).append("}");
			}
			sb.append("],\"totalAll\":").append(totalAll)
			  .append(",\"presentAll\":").append(presentAll).append("}");
			sendJson(exchange,sb.toString());
		}
	}
    static Map<String,String> parseBody(HttpExchange exchange) throws IOException {
        String body=new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String,String> result=new HashMap<>();
        if(body.isEmpty()) {
            return result;
        }
        for(String pair:body.split("&")) {
            String kv[]=pair.split("=",2);
            if(kv.length==2) {
                result.put(URLDecoder.decode(kv[0],"UTF-8"),URLDecoder.decode(kv[1],"UTF-8"));
            }
        }
        return result;
    }
    public static void main(String[] args) throws IOException {
        AttendanceApp.loadRegistry();
		if (AttendanceApp.accounts.isEmpty()) {
			String salt=AttendanceApp.newSalt();
			Account admin=new Account("admin",Role.ADMIN,salt,AttendanceApp.hashPassword("12345678",salt));
			AttendanceApp.accounts.put("admin",admin);
			FileStorage.saveAccounts(new ArrayList<>(AttendanceApp.accounts.values()),"accounts.txt");
		}
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/students", new StudentsHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/review", new ReviewHandler());
        server.createContext("/api/save", new SaveHandler());
        server.createContext("/", new PageHandler());
        server.createContext("/api/attend", new AttendHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/change-password", new ChangePasswordHandler());
        server.createContext("/api/delete-student",new DeleteStudentHandler());
		server.createContext("/api/summary",new SummaryHandler());
        server.start();
        System.out.println("Сервер запущен на порту 8080!");
    }
}