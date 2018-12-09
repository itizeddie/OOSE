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
import java.time.LocalDate;
import java.sql.Connection;

public class AssignmentsRepository {
    private DataSource database;

    public AssignmentsRepository(DataSource database) throws SQLException {
        this.database = database;
        var connection = database.getConnection();
        var statement = connection.createStatement();
        if (database instanceof PGSimpleDataSource) {
            statement.execute("CREATE TABLE IF NOT EXISTS assignments (id SERIAL PRIMARY KEY, title varchar(255) NOT NULL, due_date DATE NOT NULL, course_id INTEGER NOT NULL REFERENCES courses ON DELETE CASCADE)");
            statement.execute("CREATE TABLE IF NOT EXISTS assignments_users (id SERIAL PRIMARY KEY, assignment_id integer NOT NULL REFERENCES assignments ON DELETE CASCADE, user_id INTEGER NOT NULL REFERENCES users ON DELETE CASCADE, UNIQUE(assignment_id, user_id))");
            statement.execute("CREATE TABLE IF NOT EXISTS statistics " +
                    "(id SERIAL PRIMARY KEY, " +
                    "assignment_id integer NOT NULL REFERENCES assignments ON DELETE CASCADE, " +
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
            assignment = new Assignment(id, title, dueDate, course_id);
        }


        statement.close();
        connection.close();

        return assignment;
    }

    public void addAssignmentForUser(int assignmentId, int userId) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("INSERT INTO assignments_users (assignment_id, user_id) VALUES (?, ?)");
        statement.setInt(1, assignmentId);
        statement.setInt(2, userId);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public void addStatistic(int assignmentId) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT * FROM statistics WHERE assignment_id = "+assignmentId);
        var rs = statement.executeQuery();
        if (!rs.next()) {
            createStatistic(connection, assignmentId);
        }
        updateStatistics(connection, assignmentId);
        statement.close();
        connection.close();
    }

    private void createStatistic(Connection connection, int assignmentId) throws SQLException {
        var statement = connection.prepareStatement("INSERT INTO statistics (assignment_id, num_submissions, " +
                "sum_of_grades, grades_std, num_comp_time, sum_comp_time, comp_time_std) VALUES (?, 0, 0, 0, 0, 0, 0)");
        statement.setInt(1, assignmentId);
        statement.executeUpdate();
        statement.close();
    }

    private void updateStatistics(Connection connection, int assignmentId) throws SQLException {
        // note: still need to check if assignment was already added (maybe add a field to assignments to keep track)
        // currently we are assuming it was not added
        var statement = connection.prepareStatement("SELECT * FROM statistics WHERE assignment_id = "+assignmentId);
        var rs = statement.executeQuery();
        rs.next();

        // to do: change these to actual values
        int grade = 90;
        int compTime = 60; //minutes

        // Prepare variables
        int numSubmissions = rs.getInt("num_submissions") + 1;
        double sumGrades = rs.getInt("sum_of_grades") + grade;
        double gradesSTD = 1.0;
        int numCompTime = rs.getInt("num_comp_time") + 1;
        int sumCompTime = rs.getInt("sum_comp_time") + compTime;
        double compTimeSTD = 1.0;

        var stm = connection.createStatement();
        stm.executeUpdate("UPDATE statistics set num_submissions = "+numSubmissions+"WHERE assignment_id="+assignmentId);
        stm.executeUpdate("UPDATE statistics set sum_of_grades = "+sumGrades+"WHERE assignment_id="+assignmentId);
        stm.executeUpdate("UPDATE statistics set grades_std = "+gradesSTD+"WHERE assignment_id="+assignmentId);
        stm.executeUpdate("UPDATE statistics set num_comp_time = "+numCompTime+"WHERE assignment_id="+assignmentId);
        stm.executeUpdate("UPDATE statistics set sum_comp_time = "+sumCompTime+"WHERE assignment_id="+assignmentId);
        stm.executeUpdate("UPDATE statistics set comp_time_std = "+compTimeSTD+"WHERE assignment_id="+assignmentId);

        stm.close();
    }

    void deleteAssignment(Assignment assignment) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("DELETE FROM assignments WHERE id=?");
        statement.setInt(1, assignment.getId());
        statement.executeUpdate();
        statement.close();
        connection.close();
    }
}
