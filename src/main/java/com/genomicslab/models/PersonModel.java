package com.genomicslab.models;

public abstract class PersonModel {
    protected String id;
    protected String name;
    protected String email;
    protected String phone;
    protected byte[] picture; // Binary data for picture

    public PersonModel(String id, String name, String email, String phone, byte[] picture) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.picture = picture;
    }

    public PersonModel(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public byte[] getPicture() {
        return picture;
    }


    // Abstract method
    public abstract String getRole();
}
