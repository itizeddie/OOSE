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

public class AssignmentsRepository {
    private DataSource database;

    public AssignmentsRepository(DataSource database) throws SQLException {
        this.database = database;
        var connection = database.getConnection();
        var statement = connection.createStatement();
        if (database instanceof PGSimpleDataSource) {
            statement.execute("CREATE TABLE IF NOT EXISTS assignments (id SERIAL PRIMARY KEY, title varchar(255) NOT NULL, due_date DATE NOT NULL, course_id INTEGER NOT NULL REFERENCES courses ON DELETE CASCADE)");
            statement.execute("CREATE TABLE IF NOT EXISTS assignments_users (id SERIAL PRIMARY KEY, assignment_id integer NOT NULL REFERENCES assignments ON DELETE CASCADE, user_id INTEGER NOT NULL REFERENCES users ON DELETE CASCADE, UNIQUE(assignment_id, user_id))");
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

    void deleteAssignment(Assignment assignment) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("DELETE FROM assignments WHERE id=?");
        statement.setInt(1, assignment.getId());
        statement.executeUpdate();
        statement.close();
        connection.close();
    }
}
