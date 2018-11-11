package com.github.jhu_oose11.calendue.repositories;

import com.github.jhu_oose11.calendue.models.Term;
import com.github.jhu_oose11.calendue.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.jdbc.PSQLSavepoint;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TermsRepositoryTest {
    private static TermsRepository repo;
    private static UsersRepository userRepo;
    private static Map<String, Object> testData = new HashMap<>();
    private static DataSource database;

    @BeforeAll
    static void setUpOnce() throws SQLException {
        if (System.getenv("JDBC_DATABASE_URL") != null) {
            var postgresDatabase = new PGSimpleDataSource();
            postgresDatabase.setURL(System.getenv("JDBC_DATABASE_URL"));
            database = postgresDatabase;
            repo = new TermsRepository(database);
            userRepo = new UsersRepository(database);
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        String title = "Test Term";
        LocalDate startDate = LocalDate.of(2018, 9, 5);
        LocalDate endDate = LocalDate.of(2018, 11, 5);
        Term term = new Term(title, startDate, endDate);
        term = repo.create(term);

        testData.put("term", term);
        testData.put("title", title);
        testData.put("startDate", startDate);
        testData.put("endDate", endDate);
    }

    @Test
    void create() throws SQLException {
        String title = "Test Term";
        LocalDate startDate = LocalDate.of(2018, 9, 5);
        LocalDate endDate = LocalDate.of(2018, 11, 5);
        Term term = new Term(title, startDate, endDate);

        int priorCount = countTerms(title);
        term = repo.create(term);

        assertEquals(priorCount + 1, countTerms(title));

        repo.deleteTerm(term);
    }

    @Test
    void StartBeforeEndCheck() throws SQLException {
        String title = "Test Term";
        LocalDate startDate = LocalDate.of(2018, 9, 5);
        LocalDate endDate = LocalDate.of(2018, 8, 5);
        Term term = new Term(title, startDate, endDate);

        try {
            repo.create(term);
            fail("Check constraint for Start before End is incorrect");
        } catch (PSQLException e) {
            // If check constraint violation
            if (!(e.getSQLState().equals("23514") && e.getMessage().contains("start_before_end"))) {
                throw e;
            }
        }
    }

    @Test
    void addTermForUser() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Term term = (Term) testData.get("term");
        repo.addTermForUser(term.getId(), user.getId());
        assertEquals(1, countTermUsers(term.getId(), user.getId()));

        userRepo.deleteUser(user);
    }

    @Test
    void TermUsersUnique() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Term term = (Term) testData.get("term");
        repo.addTermForUser(term.getId(), user.getId());
        assertEquals(1, countTermUsers(term.getId(), user.getId()));

        try {
            repo.addTermForUser(term.getId(), user.getId());
            fail("terms_users UNIQUE constraint not working");
        } catch (SQLException e) {
            if (!e.getSQLState().equals("23505")) throw e;
        }

        userRepo.deleteUser(user);
    }

    @Test
    void getTermByIdNonExistent() {
        assertThrows(TermsRepository.NonExistingTermException.class, () -> repo.getTermById(0));
    }

    @Test
    void deleteTermWithId() throws SQLException, UsersRepository.NonExistingUserException, TermsRepository.NonExistingTermException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Term term = (Term) testData.get("term");
        int count = countTerms(term.getTitle());
        repo.deleteTerm(term.getId());
        assertEquals(count - 1, countTerms(term.getTitle()));
        userRepo.deleteUser(user);
    }

    @Test
    void deleteTermDeletesTermUsers() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Term term = (Term) testData.get("term");
        repo.addTermForUser(term.getId(), user.getId());

        assertEquals(1, countTermUsers(term.getId(), user.getId()));
        repo.deleteTerm(term);

        assertEquals(0, countTermUsers(term.getId(), user.getId()));
        userRepo.deleteUser(user);
    }

    @Test
    void deleteTerm() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Term term = (Term) testData.get("term");
        int count = countTerms(term.getTitle());
        repo.deleteTerm(term);
        assertEquals(count - 1, countTerms(term.getTitle()));
        userRepo.deleteUser(user);
    }

    @Test
    void deleteUserDeletesTermUsers() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Term term = (Term) testData.get("term");
        repo.addTermForUser(term.getId(), user.getId());

        assertEquals(1, countTermUsers(term.getId(), user.getId()));
        userRepo.deleteUser(user);

        assertEquals(0, countTermUsers(term.getId(), user.getId()));
    }

    @AfterEach
    void tearDown() {
        try {
            Term term = (Term)(testData.get("term"));
            repo.deleteTerm(term);
        }
        catch(SQLException ignored) {}
    }

    private int countTermUsers(int termId, int userId) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT COUNT(*) FROM terms_users tu WHERE tu.term_id = ? AND tu.user_id = ?");
        statement.setInt(1, termId);
        statement.setInt(2, userId);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    private int countTerms(String title) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT COUNT(*) FROM terms t WHERE t.title = ?");
        statement.setString(1, title);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt(1);
    }
}
