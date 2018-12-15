package com.github.jhu_oose11.calendue.repositories;

import com.github.jhu_oose11.calendue.models.Assignment;
import com.github.jhu_oose11.calendue.models.Course;
import com.github.jhu_oose11.calendue.models.User;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AssignmentsRepository {
    private DataSource database;

    public AssignmentsRepository(DataSource database) throws SQLException {
        this.database = database;
        var connection = database.getConnection();
        var statement = connection.createStatement();
        if (database instanceof PGSimpleDataSource) {
            statement.execute("CREATE TABLE IF NOT EXISTS assignments (id SERIAL PRIMARY KEY, title varchar(255) NOT NULL, due_date DATE NOT NULL, course_id INTEGER NOT NULL REFERENCES courses ON DELETE CASCADE, completed BOOLEAN NOT NULL DEFAULT 'false')");
            statement.execute("CREATE TABLE IF NOT EXISTS assignments_users " +
                    "(id SERIAL PRIMARY KEY, " +
                    "assignment_id integer NOT NULL REFERENCES assignments ON DELETE CASCADE, " +
                    "user_id INTEGER NOT NULL REFERENCES users ON DELETE CASCADE, " +
                    "completion_time INTEGER NOT NULL, " +
                    "grade REAL NOT NULL, " +
                    "added_to_statistics BOOLEAN NOT NULL DEFAULT 'false', " +
                    "UNIQUE(assignment_id, user_id))");
            statement.execute("CREATE TABLE IF NOT EXISTS statistics " +
                    "(id SERIAL PRIMARY KEY, " +
                    "title varchar(255) NOT NULL, " +
                    "num_submissions INTEGER NOT NULL, " +
                    "sum_of_grades REAL NOT NULL, " +
                    "grades_std REAL NOT NULL, " +
                    "num_comp_time INTEGER NOT NULL, " +
                    "sum_comp_time INTEGER NOT NULL, " +
                    "comp_time_std REAL NOT NULL)");
        }
        statement.close();
        connection.close();
    }

    public Assignment create(Assignment assignment) throws SQLException {
        String title = assignment.getTitle();
        int course_id = assignment.getCourseId();
        LocalDate dueDate = assignment.getDueDate();

        var connection = database.getConnection();
        var statement = connection.prepareStatement("INSERT INTO assignments (title, course_id, due_date) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, title);
        statement.setInt(2, course_id);
        statement.setDate(3, Date.valueOf(dueDate));
        statement.executeUpdate();

        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            int id = rs.getInt(1);
            assignment = new Assignment(id, title, dueDate, course_id, false);
        }


        statement.close();
        connection.close();

        return assignment;
    }

    public void addAssignmentForUser(int assignmentId, int userId) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("INSERT INTO assignments_users (assignment_id, user_id, completion_time, grade) VALUES (?, ?, ?, ?)");

        int completionTime = 30; // to do: get actual completion time
        double grade = 90; // to do: get actual grade

        statement.setInt(1, assignmentId);
        statement.setInt(2, userId);
        statement.setInt(3, completionTime);
        statement.setDouble(4, grade);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }


    public void addStatistic(String title, int assignmentId, int userId) throws SQLException, NonExistingAssignmentException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT * FROM statistics WHERE title = "+"'"+title+"'");
        var rs = statement.executeQuery();
        if (!rs.next()) { // checks if row in statistics already exists
            createStatistic(connection, title);
        }

        if (notAddedToStatistics(assignmentId, userId)) {
            updateStatistics(connection, title, assignmentId, userId);
        }
        statement.close();
        connection.close();
    }

    private void createStatistic(Connection connection, String title) throws SQLException {
        var statement = connection.prepareStatement("INSERT INTO statistics (title, num_submissions, " +
                "sum_of_grades, grades_std, num_comp_time, sum_comp_time, comp_time_std) VALUES (?, 0, 0, 0, 0, 0, 0)");
        statement.setString(1, title);
        statement.executeUpdate();
        statement.close();
    }

    private void updateStatistics(Connection connection, String title, int assignmentId, int userId) throws SQLException {
        // note: still need to check if assignment was already added
        // currently this code assumes it was not added
        var statement = connection.prepareStatement("SELECT * FROM statistics WHERE title = "+"'"+title+"'");
        var rs = statement.executeQuery();
        rs.next();

        // to do: change these to actual values
        double grade = 90;
        int compTime = 60; //minutes

        // Prepare variables
        int numSubmissions = rs.getInt("num_submissions") + 1;
        double sumGrades = rs.getDouble("sum_of_grades") + grade;
        double[] grades = {90, 100, 80.3, 55, 68.5}; // to do: get actual grades
        double gradesSTD = stdDev(grades);
        int numCompTime = rs.getInt("num_comp_time") + 1;
        int sumCompTime = rs.getInt("sum_comp_time") + compTime;
        double[] compTimes = {90, 100, 80.3, 55, 68.5}; // to do: get actual completion times
        double compTimeSTD = stdDev(compTimes);

        var stm = connection.createStatement();
        stm.executeUpdate("UPDATE statistics set num_submissions = "+numSubmissions+"WHERE title="+"'"+title+"'");
        stm.executeUpdate("UPDATE statistics set sum_of_grades = "+sumGrades+"WHERE title="+"'"+title+"'");
        stm.executeUpdate("UPDATE statistics set grades_std = "+gradesSTD+"WHERE title="+"'"+title+"'");
        stm.executeUpdate("UPDATE statistics set num_comp_time = "+numCompTime+"WHERE title="+"'"+title+"'");
        stm.executeUpdate("UPDATE statistics set sum_comp_time = "+sumCompTime+"WHERE title="+"'"+title+"'");
        stm.executeUpdate("UPDATE statistics set comp_time_std = "+compTimeSTD+"WHERE title="+"'"+title+"'");

        stm.close();

        markAsAddedToStatistic(assignmentId, userId);
    }

    private double stdDev(double[] values) {
        double mean = 0.0;
        double num = 0.0;
        for (double value : values) {
            mean += value;
            num++;
        }
        mean = mean / num;

        double sum = 0;
        for (double value : values) {
            sum += ((value - mean) * (value - mean));
        }
        return Math.sqrt(sum / num);
    }

    private boolean notAddedToStatistics(int assignmentId, int userId) throws SQLException, NonExistingAssignmentException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT added_to_statistic FROM assignments_users WHERE user_id = ? AND assignment_id = ?");
        statement.setInt(1, userId);
        statement.setInt(2, assignmentId);
        var rs = statement.executeQuery();

        if (!rs.next()) throw new NonExistingAssignmentException();
        boolean added = rs.getBoolean("added_to_statistic");

        statement.close();
        connection.close();

        return added;
    }

    private void markAsAddedToStatistic(int assignmentId, int userId) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.createStatement();
        statement.executeUpdate("UPDATE assignments_users set added_to_statistics = 'false' WHERE user_id = "+userId+" AND assignment_id = "+assignmentId);

        statement.close();
        connection.close();
    }

    public List<Assignment> getAssignmentsForUser(int userId) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT a.id, title, due_date, course_id, completed FROM assignments a INNER JOIN assignments_users au ON au.user_id = ?");
        statement.setInt(1, userId);
        ResultSet results = statement.executeQuery();

        List<Assignment> assignments = new ArrayList<>();
        while (results.next()) {
            System.out.println(results.getInt("id"));
            LocalDate dueDate = results.getDate("due_date").toLocalDate();
            assignments.add(new Assignment(results.getInt("id"), results.getString("title"), dueDate, results.getInt("course_id"), results.getBoolean("completed")));
        }

        return assignments;
    }

    public Assignment getAssignmentById(int assignment_id) throws SQLException, NonExistingAssignmentException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT id, title, due_date, course_id, completed FROM assignments WHERE assignments.id = ?");
        statement.setInt(1, assignment_id);
        ResultSet rs = statement.executeQuery();
        if (!rs.next()) throw new NonExistingAssignmentException();
        LocalDate dueDate = rs.getDate("due_date").toLocalDate();
        Assignment assignment = new Assignment(rs.getInt("id"), rs.getString("title"), dueDate, rs.getInt("course_id"), rs.getBoolean("completed"));
        statement.close();
        connection.close();

        return assignment;
    }

    void deleteAssignment(Assignment assignment) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("DELETE FROM assignments WHERE id=?");
        statement.setInt(1, assignment.getId());
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    void deleteStatistic(String title) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("DELETE FROM statistics WHERE title=?");
        //statement.setString(1, "'"+title+"'");
        statement.setString(1, title);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public class NonExistingAssignmentException extends Exception {}
}
