import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
public class AttendanceServer {
    static class StudentsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String json=AttendanceApp.studentsToJson();
            byte[] bytes=json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json; charset=UTF_8");
            exchange.sendResponseHeaders(200,bytes.length);
            OutputStream os=exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
    public static void main(String[] args) throws IOException {
        AttendanceApp.loadRegistry();
        HttpServer server=HttpServer.create(new InetSocketAddress(8080),0);
        server.createContext("/students",new StudentsHandler());
        server.start();
        System.out.println("Сервер запущен на порту 8080!");
    }
}