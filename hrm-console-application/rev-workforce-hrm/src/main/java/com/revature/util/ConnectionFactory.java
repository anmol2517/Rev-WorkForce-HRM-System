package com.revature.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private static ConnectionFactory instance;


//   Database Credentials

    private final String url = "jdbc:mysql://localhost:3306/hrm_console";
    private final String user = "root";
    private final String password = "Scar2511@#";

    private ConnectionFactory() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


//   Global Access Point

    public static synchronized ConnectionFactory getInstance() {
        if (instance == null) {
            instance = new ConnectionFactory();
        }
        return instance;
    }


//    Connection Provides Methods

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("DB Connection Failed..!! Reason : " + e.getMessage());
            throw e;
        }
    }
}

