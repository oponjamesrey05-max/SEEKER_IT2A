package com.scc.model;

public record User(int user_id, String full_name, String email, String password, String role, String status,
        String date_created) {
    @Override
    public String toString(){
        return String.format("[%d] %s - Role: %s, Status: %s", user_id, full_name, role, status);
    }
}
