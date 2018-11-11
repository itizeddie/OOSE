package com.github.jhu_oose11.calendue.repositories;

import com.github.jhu_oose11.calendue.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UsersRepositoryTest {
    private static UsersRepository repo;
    private static Map<String, Object> testData = new HashMap<>();

    @BeforeAll
    static void setUpOnce() throws SQLException {
        DataSource database;
        if (System.getenv("JDBC_DATABASE_URL") != null) {
            var postgresDatabase = new PGSimpleDataSource();
            postgresDatabase.setURL(System.getenv("JDBC_DATABASE_URL"));
            database = postgresDatabase;
            repo = new UsersRepository(database);

            testData.put("email", "test123456@testi1234ng.com");
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        String testEmail = (String)(testData.get("email"));
        User u = new User(testEmail);
        repo.create(u);
    }

    @AfterEach
    void tearDown() {
        try {
            String testEmail = (String)(testData.get("email"));
            User u = repo.getByEmail(testEmail);
            repo.deleteUser(u);
        }
        catch(SQLException | UsersRepository.NonExistingUserException ignored) {}
    }


    @Test
    void getByEmail() {
        try {
            String testEmail = (String)(testData.get("email"));
            User u = repo.getByEmail(testEmail);
            assertEquals(u.getEmail(), testEmail);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void create() throws SQLException, UsersRepository.NonExistingUserException {
        String testEmail = (String)(testData.get("email"));
        User u = repo.getByEmail(testEmail);
        assertEquals(u.getEmail(), testEmail);
    }

    @Test
    void deleteUser() throws SQLException, UsersRepository.NonExistingUserException {
        String testEmail = (String)(testData.get("email"));
        User u = repo.getByEmail(testEmail);
        repo.deleteUser(u);

        assertThrows(UsersRepository.NonExistingUserException.class, () -> repo.getByEmail(testEmail));
    }
}