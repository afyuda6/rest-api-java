import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpServer;
import database.Sqlite;
import handlers.User;

public class Main {
    public static void main(String[] args) {
        try {
            String portEnv = System.getenv("PORT");
            int port = (portEnv != null) ? Integer.parseInt(portEnv) : 6002;
            Sqlite.initializeDatabase();
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new User());
            server.setExecutor(null);
            server.start();
        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}