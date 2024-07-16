package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(System.getenv("DB_CONNECTION_STRING"));
    }
}