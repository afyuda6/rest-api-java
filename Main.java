import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import database.Sqlite;
import handlers.User;

public class Main {
    public static void main(String[] args) {
        try {
            Sqlite.initializeDatabase();

            HttpServer server = HttpServer.create(new InetSocketAddress(6002), 0);
            server.createContext("/users", new User());
            server.createContext("/", new ErrorHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ErrorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();

            if (!requestPath.equals("/")) {
                String response = "{\"status\": \"Not Found\", \"code\": 404 }";
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(404, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
}