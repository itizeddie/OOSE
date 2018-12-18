package com.github.jhu_oose11.calendue.repositories;

import com.github.jhu_oose11.calendue.models.Assignment;
import com.github.jhu_oose11.calendue.models.Course;
import com.github.jhu_oose11.calendue.models.Term;
import com.github.jhu_oose11.calendue.models.User;
import org.junit.jupiter.api.*;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AssignmentsRepositoryTest {
    private static AssignmentsRepository repo;
    private static CoursesRepository courseRepo;
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
            repo = new AssignmentsRepository(database);
            courseRepo = new CoursesRepository(database);
            userRepo = new UsersRepository(database);
            termsRepository = new TermsRepository(database);

            Term term = new Term("Test Term", LocalDate.now(), LocalDate.now().plusDays(1));
            term = termsRepository.create(term);

            Course course = new Course("Test Course", term.getId(), 1);
            course = courseRepo.create(course);

            testData.put("course", course);
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        String title = "Test Course";
        LocalDate dueDate = LocalDate.now();
        Assignment assignment = new Assignment(title, dueDate, ((Course) testData.get("course")).getId());
        assignment = repo.create(assignment);

        testData.put("assignment", assignment);
        testData.put("title", title);
        testData.put("due_date", dueDate);
    }

    @AfterEach
    void tearDown() {
        try {
            Assignment assignment = (Assignment)(testData.get("assignment"));
            repo.deleteAssignment(assignment);
        }
        catch(SQLException ignored) {}
    }

    @AfterAll
    static void tearDownAll() throws SQLException, TermsRepository.NonExistingTermException {
        Course course = (Course) testData.get("course");
        termsRepository.deleteTerm(course.getTermId()); // Cascades to assignments & courses
    }

    @Test
    void create() throws SQLException {
        String title = "Test Course";
        int course_id = ((Course) testData.get("course")).getId();
        LocalDate dueDate = (LocalDate) testData.get("due_date");
        Assignment assignment = new Assignment(title, dueDate, course_id);

        int priorCount = countAssignments(title, dueDate, course_id);
        assignment = repo.create(assignment);

        assertEquals(priorCount + 1, countAssignments(title, dueDate, course_id));

        repo.deleteAssignment(assignment);
    }

    @Test
    void addAssignmentForUser() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Assignment assignment = (Assignment) testData.get("assignment");
        repo.addAssignmentForUser(assignment.getId(), user.getId());
        assertEquals(1, countAssignmentUsers(assignment.getId(), user.getId()));

        userRepo.deleteUser(user);
    }

    @Test
    void getAssignmentById() throws SQLException, AssignmentsRepository.NonExistingAssignmentException {
        String title = "Test Assignment";
        int course_id = ((Course) testData.get("course")).getId();
        LocalDate dueDate = (LocalDate) testData.get("due_date");
        Assignment assignment = new Assignment(title, dueDate, course_id, false);
        assignment = repo.create(assignment);

        Assignment result = repo.getAssignmentById(assignment.getId());
        assertEquals(result.getId(), assignment.getId());
        repo.deleteAssignment(assignment);
    }


    @Test
    void getAssignmentsForUser() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Assignment assignment = (Assignment) testData.get("assignment");
        repo.addAssignmentForUser(assignment.getId(), user.getId());

        List<Assignment> result = repo.getAssignmentsForUser(user.getId());
        assertTrue(result.size() > 0);
        assertEquals(result.get(0).getId(), assignment.getId());

        userRepo.deleteUser(user);
    }

    @Test
    void AssignmentUsersUnique() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Assignment assignment = (Assignment) testData.get("assignment");
        repo.addAssignmentForUser(assignment.getId(), user.getId());
        assertEquals(1, countAssignmentUsers(assignment.getId(), user.getId()));

        try {
            repo.addAssignmentForUser(assignment.getId(), user.getId());
            fail("assignments_users UNIQUE constraint not working");
        } catch (SQLException e) {
            if (!e.getSQLState().equals("23505")) throw e;
        }

        userRepo.deleteUser(user);
    }

    @Test
    void deleteAssignmentDeletesAssignmentUsers() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Assignment assignment = (Assignment) testData.get("assignment");
        repo.addAssignmentForUser(assignment.getId(), user.getId());

        assertEquals(1, countAssignmentUsers(assignment.getId(), user.getId()));
        repo.deleteAssignment(assignment);

        assertThrows(AssignmentsRepository.NonExistingAssignmentException.class, () -> repo.getAssignmentById(assignment.getId()));

        assertEquals(0, countAssignmentUsers(assignment.getId(), user.getId()));
        userRepo.deleteUser(user);
    }

    @Test
    void deleteUserDeletesAssignmentUsers() throws SQLException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Assignment assignment = (Assignment) testData.get("assignment");
        repo.addAssignmentForUser(assignment.getId(), user.getId());

        assertEquals(1, countAssignmentUsers(assignment.getId(), user.getId()));
        userRepo.deleteUser(user);

        assertEquals(0, countAssignmentUsers(assignment.getId(), user.getId()));
    }

    @Test
    void addStatistic() throws SQLException, UsersRepository.NonExistingUserException, AssignmentsRepository.NonExistingAssignmentException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Assignment assignment = (Assignment) testData.get("assignment");
        repo.addAssignmentForUser(assignment.getId(), user.getId());

        repo.addStatistic(assignment.getTitle(), assignment.getId(), user.getId());

        assertEquals(1, countStatistics(assignment.getTitle()));
        repo.deleteStatistic(assignment.getTitle());
        userRepo.deleteUser(user);
    }

   @Test
    void addSameStatisticTwice() throws SQLException, AssignmentsRepository.NonExistingAssignmentException, UsersRepository.NonExistingUserException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Assignment assignment = (Assignment) testData.get("assignment");
        repo.addAssignmentForUser(assignment.getId(), user.getId());

        repo.addStatistic(assignment.getTitle(), assignment.getId(), user.getId());
        repo.addStatistic(assignment.getTitle(), assignment.getId(), user.getId());
        assertEquals(1, countStatistics(assignment.getTitle()));

        repo.deleteStatistic(assignment.getTitle());
        userRepo.deleteUser(user.getId());
    }

    @Test
    void addTwoDifferentAssignmentStatistics() throws SQLException, UsersRepository.NonExistingUserException, AssignmentsRepository.NonExistingAssignmentException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Assignment assignment1 = (Assignment) testData.get("assignment");
        repo.addAssignmentForUser(assignment1.getId(), user.getId());

        repo.addStatistic(assignment1.getTitle(), assignment1.getId(), user.getId());

        String title = "Different Course";
        int course_id = ((Course) testData.get("course")).getId();
        LocalDate dueDate = (LocalDate) testData.get("due_date");
        Assignment assignment2 = new Assignment(title, dueDate, course_id);
        assignment2 = repo.create(assignment2);
        repo.addAssignmentForUser(assignment2.getId(), user.getId());

        repo.addStatistic(assignment2.getTitle(), assignment2.getId(), user.getId());

        assertEquals(1, countStatistics(assignment1.getTitle()));
        assertEquals(1, countStatistics(assignment2.getTitle()));

        repo.deleteStatistic(assignment1.getTitle());
        repo.deleteStatistic(assignment2.getTitle());
        userRepo.deleteUser(user.getId());
        repo.deleteAssignment(assignment1);
        repo.deleteAssignment(assignment2);
    }

    @Test
    void statisticsHasRightSums() throws SQLException, AssignmentsRepository.NonExistingAssignmentException, UsersRepository.NonExistingUserException {
        // User 1
        String email1 = "test1234235@testing.com";
        User user1 = new User(email1);
        userRepo.create(user1);
        user1 = userRepo.getByEmail(email1);

        // User 2
        String email2 = "test67890@testing.com";
        User user2 = new User(email2);
        userRepo.create(user2);
        user2 = userRepo.getByEmail(email2);

        Assignment assignment = (Assignment) testData.get("assignment");
        repo.addAssignmentForUser(assignment.getId(), user1.getId());
        repo.addAssignmentForUser(assignment.getId(), user2.getId());

        repo.addStatistic(assignment.getTitle(), assignment.getId(), user1.getId());
        repo.addStatistic(assignment.getTitle(), assignment.getId(), user2.getId());

        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT * FROM statistics s WHERE s.title = ?");
        statement.setString(1, assignment.getTitle());
        ResultSet rs = statement.executeQuery();
        rs.next();

        double grades = 180;
        double completionTime = 60;

        assertEquals(grades, rs.getInt("sum_of_grades"));
        assertEquals(completionTime, rs.getInt("sum_comp_time"));

        repo.deleteStatistic(assignment.getTitle());
        userRepo.deleteUser(user1.getId());
        userRepo.deleteUser(user2.getId());
    }

    @Test
    void statisticsHasRightSTD() throws SQLException, UsersRepository.NonExistingUserException, AssignmentsRepository.NonExistingAssignmentException {
        String email = "test1234235@testing.com";
        User user = new User(email);
        userRepo.create(user);
        user = userRepo.getByEmail(email);

        Assignment assignment = (Assignment) testData.get("assignment");
        repo.addAssignmentForUser(assignment.getId(), user.getId());

        repo.addStatistic(assignment.getTitle(), assignment.getId(), user.getId());

        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT * FROM statistics WHERE title = ?");
        statement.setString(1, assignment.getTitle());
        ResultSet rs = statement.executeQuery();
        rs.next();

        double std = 0;
        /*double[] blah = repo.getDoubleArrayFromAssmtUsers("grade", assignment.getId());
        double uh = 90.0;
        for (double x : blah) {
            assertEquals(x, uh);
        }*/
        assertEquals(std, rs.getDouble("grades_std"));
        repo.deleteStatistic(assignment.getTitle());
        userRepo.deleteUser(user);
    }

    private int countAssignmentUsers(int assignmentId, int userId) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT COUNT(*) FROM assignments_users cu WHERE cu.assignment_id = ? AND cu.user_id = ?");
        statement.setInt(1, assignmentId);
        statement.setInt(2, userId);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    private int countAssignments(String title, LocalDate dueDate, int course_id) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT COUNT(*) FROM assignments a WHERE a.title = ? AND a.course_id = ? AND a.due_date = ?");
        statement.setString(1, title);
        statement.setInt(2, course_id);
        statement.setDate(3, Date.valueOf(dueDate));
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    private int countStatistics(String title) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT * FROM statistics WHERE title = ?");
        statement.setString(1, title);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt("num_submissions");
    }
}
