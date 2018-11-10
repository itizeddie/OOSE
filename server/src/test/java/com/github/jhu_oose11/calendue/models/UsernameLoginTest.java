package com.github.jhu_oose11.calendue.models;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsernameLoginTest {
    private static Map<String, Object> testData = new HashMap<>();
    private static UsernameLogin usernameLogin;


    @BeforeAll
    static void setUpOnce() {
        int user_id = 5;
        String username = "test_user";
        String password = "ab23sc#sjr";
        usernameLogin = new UsernameLogin(user_id, username, password);

        testData.put("username", username);
        testData.put("user_id", user_id);
        testData.put("password", password);
        testData.put("usernameLogin", usernameLogin);
    }

    @Test
    void getUserId() {
        assertEquals(usernameLogin.getUserId(), (int) testData.get("user_id"));
    }

    @Test
    void getUsername() {
        assertEquals(usernameLogin.getUsername(), testData.get("username"));
    }

    @Test
    void getPasswordHash() {
        String password = (String) testData.get("password");
        assertTrue(BCrypt.checkpw(password, usernameLogin.getPasswordHash()));
    }

    @Test
    void getId() {
        int userId = usernameLogin.getUserId();
        String username = usernameLogin.getUsername();
        String passwordHash = usernameLogin.getPasswordHash();

        UsernameLogin usernameLogin = new UsernameLogin(0, userId, username, passwordHash);
        assertEquals(usernameLogin.getId(), 0);
    }

    @Test
    void authenticate() {
        String password = (String) testData.get("password");
        assertTrue(usernameLogin.authenticate(password));
    }
}
