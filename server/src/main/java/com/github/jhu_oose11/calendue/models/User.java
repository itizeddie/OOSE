package com.github.jhu_oose11.calendue.models;

public class User {
    private String email;
    private int id = 0;

    public User(String email) {
        this.email = email;
    }

    public User(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
