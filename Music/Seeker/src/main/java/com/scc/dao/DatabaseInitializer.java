package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initializer() {
        new UserDao();
        createActivityLogsTable();
        System.out.println(" -- Database tables verified");
    }

    private static void createActivityLogsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS activity_logs ("
                + "log_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "action_type TEXT NOT NULL, "
                + "description TEXT NOT NULL, "
                + "timestamp DATETIME DEFAULT (datetime('now', 'localtime')), "
                + "FOREIGN KEY (user_id) REFERENCES Users(user_id)"
                + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            System.err.println("Error creating activity_logs table: " + e.getMessage());
        }
    }
}
