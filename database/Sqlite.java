package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Sqlite {
    private static final String DB_URL = "jdbc:sqlite:rest_api_java.db";

    public static void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");

            try (Connection connection = DriverManager.getConnection(DB_URL)) {
                if (connection != null) {
                    String dropUsersTableSQL = """
                            DROP TABLE IF EXISTS users;
                            """;
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(dropUsersTableSQL);
                    }
                }
            }

            try (Connection connection = DriverManager.getConnection(DB_URL)) {
                if (connection != null) {
                    String createUsersTableSQL = """
                            CREATE TABLE IF NOT EXISTS users (
                                id INTEGER PRIMARY KEY,
                                name TEXT NOT NULL
                            );
                            """;
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(createUsersTableSQL);
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("SQL error (open database): " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
