package com.github.jhu_oose11.calendue.models;

import com.github.jhu_oose11.calendue.repositories.UsersRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {
    private static Map<String, Object> testData = new HashMap<>();


    @BeforeAll
    static void setUpOnce() {
        testData.put("email", "testemail@test.com");
    }

    @Test
    void getEmail() {
        User user = new User((String) testData.get("email"));
        assertEquals(user.getEmail(), testData.get("email"));
    }

    @Test
    void setId() {
        User user = new User(0, (String) testData.get("email"));
        int id = 5;
        user.setId(id);
        assertEquals(user.getId(), id);
    }

    @Test
    void getId() {
        int id = 3;
        User user = new User(id, (String) testData.get("email"));
        assertEquals(user.getId(), id);
    }
}
