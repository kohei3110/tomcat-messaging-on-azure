package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DatabaseService {
    private final Connection connection;

    public DatabaseService(Connection connection) {
        this.connection = connection;
    }

    public void createTable() {
        try (Scanner scanner = new Scanner(Subscriber.class.getClassLoader().getResourceAsStream("schema.sql"));
             Statement statement = connection.createStatement()) {
            while (scanner.hasNextLine()) {
                statement.execute(scanner.nextLine());
            }
        } catch (SQLException e) {
            System.err.println("Failed to create table: " + e.getMessage());
        }
    }

    public void insertData(String id, String message) {
        try (PreparedStatement insertStatement = connection
                .prepareStatement("INSERT INTO messages (id, message) VALUES (?, ?);")) {
            insertStatement.setString(1, id);
            insertStatement.setString(2, message);
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert data: " + e.getMessage());
        }
    }
}