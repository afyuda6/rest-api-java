package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.Sqlite;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class User implements HttpHandler {
    public class UserModel {
        private final int id;
        private final String name;
        public UserModel(int id, String name) {
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() {
            return String.format("{\"id\":%d,\"name\":\"%s\"}", id, name);
        }
    }

    private Map<String, String> parseUrlEncoded(String requestBody) {
        Map<String, String> parameters = new HashMap<>();
        String[] pairs = requestBody.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = java.net.URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                parameters.put(key, value);
            }
        }
        return parameters;
    }

    private void handleReadUsers(HttpExchange exchange) throws IOException, SQLException {
        List<UserModel> users = new ArrayList<>();
        Connection connection = Sqlite.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM users");
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            users.add(new UserModel(resultSet.getInt("id"), resultSet.getString("name")));
        }
        String usersString = users.toString();
        String response = "{\"status\":\"OK\", \"code\": 200, \"data\":" + usersString + "}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void handleCreateUser(HttpExchange exchange) throws IOException, SQLException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> parameters = parseUrlEncoded(requestBody);
        String name = parameters.get("name");
        if (name == null || name.trim().isEmpty()) {
            String errorResponse = "{\"status\": \"Bad Request\", \"code\": 400, \"errors\": \"Missing 'name' parameter\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(errorResponse.getBytes());
            os.close();
            return;
        }
        Connection connection = Sqlite.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO users (name) VALUES (?)");
        statement.setString(1, name);
        statement.executeUpdate();
        String response = "{\"status\":\"Created\", \"code\": 201}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void handleUpdateUser(HttpExchange exchange) throws IOException, SQLException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> parameters = parseUrlEncoded(requestBody);
        String name = parameters.get("name");
        String id = parameters.get("id");
        if (name == null || name.trim().isEmpty() || id == null || id.trim().isEmpty()) {
            String errorResponse = "{\"status\": \"Bad Request\", \"code\": 400, \"errors\": \"Missing 'id' or 'name' parameter\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(errorResponse.getBytes());
            os.close();
            return;
        }
        Connection connection = Sqlite.getConnection();
        PreparedStatement statement = connection.prepareStatement("UPDATE users SET name = ? WHERE id = ?");
        statement.setString(1, name);
        statement.setInt(2, Integer.parseInt(id));
        statement.executeUpdate();
        String response = "{\"status\":\"OK\", \"code\": 200}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void handleDeleteUser(HttpExchange exchange) throws IOException, SQLException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> parameters = parseUrlEncoded(requestBody);
        String id = parameters.get("id");
        if (id == null || id.trim().isEmpty()) {
            String errorResponse = "{\"status\": \"Bad Request\", \"code\": 400, \"errors\": \"Missing 'id' parameter\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(errorResponse.getBytes());
            os.close();
            return;
        }
        Connection connection = Sqlite.getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?");
        statement.setInt(1, Integer.parseInt(id));
        statement.executeUpdate();
        String response = "{\"status\":\"OK\", \"code\": 200}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public void userHandler(HttpExchange exchange) throws IOException, SQLException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                handleReadUsers(exchange);
                break;
            case "POST":
                handleCreateUser(exchange);
                break;
            case "PUT":
                handleUpdateUser(exchange);
                break;
            case "DELETE":
                handleDeleteUser(exchange);
                break;
            default:
                String response = "{\"status\": \"Method Not Allowed\", \"code\": 405}";
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(405, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        if ("/users".equals(requestPath) || "/users/".equals(requestPath)) {
            try {
                userHandler(exchange);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            String response = "{\"status\": \"Not Found\", \"code\": 404}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(404, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}