package com.github.jhu_oose11.calendue.repositories;

import com.github.jhu_oose11.calendue.models.User;
import com.github.jhu_oose11.calendue.models.UsernameLogin;
import org.junit.jupiter.api.*;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CredentialsRepositoryTest {
    private static CredentialsRepository repo;
    private static UsersRepository usersRepo;
    private static Map<String, Object> testData = new HashMap<>();

    @BeforeAll
    static void setUpOnce() throws SQLException {
        DataSource database;
        if (System.getenv("JDBC_DATABASE_URL") != null) {
            var postgresDatabase = new PGSimpleDataSource();
            postgresDatabase.setURL(System.getenv("JDBC_DATABASE_URL"));
            database = postgresDatabase;
            repo = new CredentialsRepository(database);
            usersRepo = new UsersRepository(database);

            testData.put("email", "test@testing.com");
            testData.put("username", "testing_username");
            testData.put("password", "12345");
        }
    }

    @BeforeEach
    void setUp() throws SQLException, UsersRepository.NonExistingUserException {
        String testEmail = (String) testData.get("email");
        User user = new User(testEmail);
        usersRepo.create(user);
        user = usersRepo.getByEmail(testEmail);
        testData.put("user", user);
    }

    @AfterEach
    void tearDown() {
        String testEmail = (String) testData.get("email");
        User user;
        try {
            user = usersRepo.getByEmail(testEmail);
            usersRepo.deleteUser(user);
        }
        catch (SQLException | UsersRepository.NonExistingUserException ignored) {}
    }

    @Test
    void deleteUserDeletesCredential() throws SQLException {
        String testUsername = (String) testData.get("username");
        String testPassword = (String) testData.get("password");
        User user = (User) testData.get("user");
        UsernameLogin credential = new UsernameLogin(user.getId(), testUsername, testPassword);
        repo.create(credential);
        usersRepo.deleteUser(user);

        assertThrows(UsersRepository.NonExistingUserException.class, () -> repo.getByUsername(testUsername));
    }

    @Test
    void getByUsername() throws SQLException {
        String testUsername = (String) testData.get("username");
        String testPassword = (String) testData.get("password");
        User user = (User) testData.get("user");
        UsernameLogin credential = new UsernameLogin(user.getId(), testUsername, testPassword);
        repo.create(credential);

        try {
            credential = repo.getByUsername(testUsername);
            assertEquals(credential.getUsername(), testUsername);
        } catch(UsersRepository.NonExistingUserException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void create() {

    }
}