import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpServer;
import database.Sqlite;
import handlers.User;

public class Main {
    public static void main(String[] args) {
        try {
            Sqlite.initializeDatabase();
            HttpServer server = HttpServer.create(new InetSocketAddress(6002), 0);
            server.createContext("/", new User());
            server.setExecutor(null);
            server.start();
        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}