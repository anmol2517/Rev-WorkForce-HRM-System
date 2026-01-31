package com.revature.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DBConnection {

    private static final String PROPERTIES_FILE = "database.properties";
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String DRIVER;

    private static ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {

            Properties props = new Properties();

            if (input == null) {
                URL = "jdbc:mysql://localhost:3306/hrm_console";
                USERNAME = "root";
                PASSWORD = "Scar2511@#";
                DRIVER = "com.mysql.cj.jdbc.Driver";
            } else {
                props.load(input);
                URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/hrm_console");
                USERNAME = props.getProperty("db.username", "root");
                PASSWORD = props.getProperty("db.password", "Scar2511@#");
                DRIVER = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            }

        } catch (IOException e) {
            System.err.println("Error loading properties : " + e.getMessage());
        }

        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found : " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static Connection getTransactionConnection() throws SQLException {
        Connection conn = threadLocalConnection.get();
        if (conn == null || conn.isClosed()) {
            conn = getConnection();
            conn.setAutoCommit(false);
            threadLocalConnection.set(conn);
        }
        return conn;
    }

    public static void beginTransaction() throws SQLException { getTransactionConnection(); }

    public static void commitTransaction() throws SQLException {
        Connection conn = threadLocalConnection.get();
        if (conn != null && !conn.isClosed()) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    public static void rollbackTransaction() {
        Connection conn = threadLocalConnection.get();
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) { }
        }
    }

    public static void closeTransactionConnection() {
        Connection conn = threadLocalConnection.get();
        if (conn != null) {
            try {
                if (!conn.isClosed()) conn.close();
            } catch (SQLException e) {
            } finally {
                threadLocalConnection.remove();
            }
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) conn.close();
            } catch (SQLException e) { }
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

