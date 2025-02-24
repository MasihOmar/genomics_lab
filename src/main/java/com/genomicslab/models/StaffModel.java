package com.genomicslab.models;

public class StaffModel extends PersonModel {
    private String role;
    private String department;

    public StaffModel(String id, String name, String email, String department, String phone, byte[] picture, String role) {
        super(id, name, email, phone, picture);
        this.role = role;
        this.department = department;
    }

    // Getters and Setters
    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }
}
