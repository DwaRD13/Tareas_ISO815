package com.unapec.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {

    private static final String URL = "jdbc:mysql://localhost:3306/NOMINA?useSSL=false&serverTimezone=UTC";
    private static final String USER = "dward";
    private static final String PASS = "DuM#12345@";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL Driver not found: " + e.getMessage());
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
