package com.genomicslab.models;


public class PatientsModel extends PersonModel {
    private String dob;
    private String gender;
    private String medicalHistory;

    public PatientsModel(String id, String name, String email, String phone, byte[] picture, String dob, String gender, String medicalHistory) {
        super(id, name, email, phone, picture);
        this.dob = dob;
        this.gender = gender;
        this.medicalHistory = medicalHistory;
    }

    // Getters and Setters
    public String getDob() {
        return dob;
    }


    public String getGender() {
        return gender;
    }


    public String getMedicalHistory() {
        return medicalHistory;
    }


    @Override
    public String getRole() {
        return "Patient";
    }
}