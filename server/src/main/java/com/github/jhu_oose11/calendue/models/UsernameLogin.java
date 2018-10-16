package com.github.jhu_oose11.calendue.models;

import org.mindrot.jbcrypt.BCrypt;

public class UsernameLogin extends LoginCredential {
    private int id = 0;
    private int userId;
    private String username;
    private String passwordHash;

    public UsernameLogin(int user_id, String username, String password) {
        this.userId = user_id;
        this.username = username;
        this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public UsernameLogin(int id, int user_id, String username, String passwordHash) {
        this.id = id;
        this.userId = user_id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getId() {
        return id;
    }

    public int getUserId() { return userId; }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() { return passwordHash; }

    @Override
    public boolean authenticate(String password) {
        return BCrypt.checkpw(password, passwordHash);
    }
}