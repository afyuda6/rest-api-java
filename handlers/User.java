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
import java.util.*;

public class User implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                handleReadUsers(exchange);
            case "POST":
                handleCreateUser(exchange);
            case "PUT":
                handleUpdateUser(exchange);
            case "DELETE":
                handleDeleteUser(exchange);
            default:
                String response = "{\"status\": \"Method Not Allowed\", \"code\": 405}";
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(405, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
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

    private void handleReadUsers(HttpExchange exchange) throws IOException {
        List<UserModel> users = new ArrayList<>();
        try (Connection connection = Sqlite.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM users");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(new UserModel(resultSet.getInt("id"), resultSet.getString("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String usersString = users.toString();
        String response = "{\"status\":\"OK\", \"code\": 200, \"data\":" + usersString + "}";

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void handleCreateUser(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> parameters = parseUrlEncoded(requestBody);

        String name = parameters.get("name");

        if (name == null || name.trim().isEmpty()) {
            String errorResponse = "{\"status\": \"Bad Request\", \"code\": 400, \"errors\": \"Missing 'name' parameter\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorResponse.getBytes());
            }
            return;
        }

        try (Connection connection = Sqlite.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO users (name) VALUES (?)")) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String response = "{\"status\":\"Created\", \"code\": 201}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void handleUpdateUser(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> parameters = parseUrlEncoded(requestBody);

        String name = parameters.get("name");
        String id = parameters.get("id");

        if (name == null || name.trim().isEmpty() || id == null || id.trim().isEmpty()) {
            String errorResponse = "{\"status\": \"Bad Request\", \"code\": 400, \"errors\": \"Missing 'id' or 'name' parameter\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorResponse.getBytes());
            }
            return;
        }

        try (Connection connection = Sqlite.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE users SET name = ? WHERE id = ?")) {
            statement.setString(1, name);
            statement.setInt(2, Integer.parseInt(id));
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String response = "{\"status\":\"OK\", \"code\": 200}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void handleDeleteUser(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, String> parameters = parseUrlEncoded(requestBody);

        String id = parameters.get("id");
        if (id == null || id.trim().isEmpty()) {
            String errorResponse = "{\"status\": \"Bad Request\", \"code\": 400, \"errors\": \"Missing 'id' parameter\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorResponse.getBytes());
            }
            return;
        }

        try (Connection connection = Sqlite.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            statement.setInt(1, Integer.parseInt(id));
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String response = "{\"status\":\"OK\", \"code\": 200}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

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
}