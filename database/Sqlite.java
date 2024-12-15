package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Sqlite {
    private static final String DB_URL = "jdbc:sqlite:rest_api_java.db";

    public static void initializeDatabase() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        Connection connection = DriverManager.getConnection(DB_URL);
        String dropUsersTableSQL = "DROP TABLE IF EXISTS users;";
        Statement statement = connection.createStatement();
        statement.execute(dropUsersTableSQL);
        connection = DriverManager.getConnection(DB_URL);
        String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT NOT NULL);";
        statement = connection.createStatement();
        statement.execute(createUsersTableSQL);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
