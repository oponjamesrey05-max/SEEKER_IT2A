package com.scc.dao;

import java.sql.*;

public class UserDao {

    public UserDao(){

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement()){

            defaultAdminGenerator(conn);

        }catch(SQLException e){
                System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public void defaultAdminGenerator(Connection conn){
        String checkSql = "SELECT COUNT(*) FROM Users WHERE role = 'Administrator'";
        String insertSql = "INSERT INTO Users(full_name, email, password, role, status) " +
                "VALUES('admin', 'admin@seeker.com', 'admin123', 'Administrator', 'Active')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)){
            if (rs.next() && rs.getInt(1) == 0){
                stmt.executeUpdate(insertSql);
                System.out.println("-> Default Admin Created (Username: admin | Password: admin123)");
            }
        } catch (SQLException e){
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public boolean registerUser(String fullName, String email, String password, String role) {

        String sql = "INSERT INTO Users(full_name, email, password, role, status) VALUES(?, ?, ?, ?, 'Pending')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, role);
            pstmt.executeUpdate();

            System.out.println("-- Successful Registration, Waiting for Approval");
            return true;

        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }




}
