package com.github.jhu_oose11.calendue.repositories;

import com.github.jhu_oose11.calendue.models.Course;
import com.github.jhu_oose11.calendue.models.Term;
import com.github.jhu_oose11.calendue.models.User;
import org.junit.jupiter.api.*;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.util.PSQLException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class CoursesRepositoryTest {
    private static CoursesRepository repo;
    private static UsersRepository userRepo;
    private static TermsRepository termsRepository;
    private static Map<String, Object> testData = new HashMap<>();
    private static DataSource database;

    @BeforeAll
    static void setUpOnce() throws SQLException {
        if (System.getenv("JDBC_DATABASE_URL") != null) {
            var postgresDatabase = new PGSimpleDataSource();
            postgresDatabase.setURL(System.getenv("JDBC_DATABASE_URL"));
            database = postgresDatabase;
            userRepo = new UsersRepository(database);
            termsRepository = new TermsRepository(database);
            repo = new CoursesRepository(database);

            Term term = new Term("Test Term", LocalDate.now(), LocalDate.now().plusDays(1));
            term = termsRepository.create(term);

            testData.put("term_id", term.getId());
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        String title = "Test Course";
        int gradeScope_id = 5;
        Course course = new Course(title, (int) testData.get("term_id"), gradeScope_id);
        course = repo.create(course);

        testData.put("course", course);
        testData.put("title", title);
    }

    @AfterAll
    static void tearDownAll() throws SQLException, TermsRepository.NonExistingTermException {
        termsRepository.deleteTerm((int) testData.get("term_id"));
    }

    @Test
    void create() throws SQLException {
        String title = "Test Course";
        int gradeScope_id = 5;
        int term_id = (int) testData.get("term_id");
        Course course = new Course(title, term_id, gradeScope_id);

        int priorCount = countCourses(title, term_id);
        course = repo.create(course);

        assertEquals(priorCount + 1, countCourses(title, term_id));

        repo.deleteCourse(course);
    }

    @Test
    void validTermIdCheck() throws SQLException {
        String title = "Test Course";
        int gradeScope_id = 5;
        Course course = new Course(title, 0, gradeScope_id);

        try {
            repo.create(course);
            fail("Invalid Term ID still inserts");
            repo.deleteCourse(course);
        } catch (PSQLException e) {
            if (!(e.getSQLState().equals("23503") && e.getMessage().contains("courses_term_id_fkey"))) {
                throw e;
            }
        }
    }

    @Test
    void addCourseForUser() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Course course = (Course) testData.get("course");
        repo.addCourseForUser(course.getId(), user.getId());
        assertEquals(1, countCourseUsers(course.getId(), user.getId()));

        userRepo.deleteUser(user);
    }

    @Test
    void CourseUsersUnique() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Course course = (Course) testData.get("course");
        repo.addCourseForUser(course.getId(), user.getId());
        assertEquals(1, countCourseUsers(course.getId(), user.getId()));

        try {
            repo.addCourseForUser(course.getId(), user.getId());
            fail("courses_users UNIQUE constraint not working");
        } catch (SQLException e) {
            if (!e.getSQLState().equals("23505")) throw e;
        }

        userRepo.deleteUser(user);
    }

    @Test
    void getCourseByIdNonExistent() {
        assertThrows(CoursesRepository.NonExistingCourseException.class, () -> repo.getCourseById(0));
    }

    @Test
    void deleteCourseWithId() throws SQLException, UsersRepository.NonExistingUserException, CoursesRepository.NonExistingCourseException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Course course = (Course) testData.get("course");
        int count = countCourses(course.getTitle(), course.getTermId());
        repo.deleteCourse(course.getId());
        assertEquals(count - 1, countCourses(course.getTitle(), course.getTermId()));
        userRepo.deleteUser(user);
    }

    @Test
    void deleteCourseDeletesCourseUsers() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Course course = (Course) testData.get("course");
        repo.addCourseForUser(course.getId(), user.getId());

        assertEquals(1, countCourseUsers(course.getId(), user.getId()));
        repo.deleteCourse(course);

        assertEquals(0, countCourseUsers(course.getId(), user.getId()));
        userRepo.deleteUser(user);
    }

    @Test
    void deleteCourse() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Course course = (Course) testData.get("course");
        int count = countCourses(course.getTitle(), course.getTermId());
        repo.deleteCourse(course);
        assertEquals(count - 1, countCourses(course.getTitle(), course.getTermId()));
        userRepo.deleteUser(user);
    }

    @Test
    void deleteUserDeletesCourseUsers() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Course course = (Course) testData.get("course");
        repo.addCourseForUser(course.getId(), user.getId());

        assertEquals(1, countCourseUsers(course.getId(), user.getId()));
        userRepo.deleteUser(user);

        assertEquals(0, countCourseUsers(course.getId(), user.getId()));
    }

    @AfterEach
    void tearDown() {
        try {
            Course course = (Course)(testData.get("course"));
            repo.deleteCourse(course);
        }
        catch(SQLException ignored) {}
    }

    private int countCourseUsers(int courseId, int userId) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT COUNT(*) FROM courses_users cu WHERE cu.course_id = ? AND cu.user_id = ?");
        statement.setInt(1, courseId);
        statement.setInt(2, userId);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    private int countCourses(String title, int term_id) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT COUNT(*) FROM courses c WHERE c.title = ? AND c.term_id = ?");
        statement.setString(1, title);
        statement.setInt(2, term_id);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt(1);
    }
}
