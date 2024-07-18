package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(System.getenv("DB_CONNECTION_STRING"));
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
        return null;
    }
}