package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    private static final String SELECT_LOGS =
            "SELECT log_id, user_id, action_type, description, timestamp FROM activity_logs ";

    /** Records one action. A logging failure is reported but never stops the main action. */
    public boolean logAction(int userId, String actionType, String description){
        String sql = "INSERT INTO activity_logs(user_id, action_type, description) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getAllLogs(){
        // log_id breaks ties when two actions happen in the same second
        return query(SELECT_LOGS + "ORDER BY timestamp DESC, log_id DESC", null);
    }

    public List<ActivityLog> getLogsByUserId(int userId){
        return query(SELECT_LOGS + "WHERE user_id = ? ORDER BY timestamp DESC, log_id DESC", userId);
    }

    /** Deletes every log entry. Returns how many were deleted, or -1 if it failed. */
    public int clearLogs(){
        String sql = "DELETE FROM activity_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()){

            return stmt.executeUpdate(sql);

        } catch (SQLException e) {
            System.err.println("Error clearing logs: " + e.getMessage());
            return -1;
        }
    }

    private List<ActivityLog> query(String sql, Integer userId){
        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            if(userId != null){
                pstmt.setInt(1, userId);
            }

            try (ResultSet rs = pstmt.executeQuery()){
                while (rs.next()){
                    logs.add(new ActivityLog(
                            rs.getInt("log_id"),
                            rs.getInt("user_id"),
                            rs.getString("action_type"),
                            rs.getString("description"),
                            rs.getString("timestamp")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }

        return logs;
    }

}