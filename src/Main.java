import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class Main {
    public static void main(String[] args) {
        try {
            Database.initializeDatabase();

            HttpServer server = HttpServer.create(new InetSocketAddress(6002), 0);
            server.createContext("/users", new UserHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}