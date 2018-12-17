package com.github.jhu_oose11.calendue.repositories;

import com.github.jhu_oose11.calendue.models.Course;
import com.github.jhu_oose11.calendue.models.Term;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class CoursesRepository {
    private DataSource database;

    public CoursesRepository(DataSource database) throws SQLException {
        this.database = database;
        var connection = database.getConnection();
        var statement = connection.createStatement();
        if (database instanceof PGSimpleDataSource) {
            statement.execute("CREATE TABLE IF NOT EXISTS courses (id SERIAL PRIMARY KEY, title varchar(255) NOT NULL, term_id INTEGER NOT NULL REFERENCES terms ON DELETE CASCADE, gradeScope_id INTEGER NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS courses_users (id SERIAL PRIMARY KEY, course_id integer NOT NULL REFERENCES courses ON DELETE CASCADE, user_id INTEGER NOT NULL REFERENCES users ON DELETE CASCADE, UNIQUE(course_id, user_id))");
        }
        statement.close();
        connection.close();
    }

    public Course create(Course course) throws SQLException {
        String title = course.getTitle();
        int term_id = course.getTermId();
        int gradeScope_id = course.getGradeScopeId();

        var connection = database.getConnection();
        var statement = connection.prepareStatement("INSERT INTO courses (title, term_id, gradeScope_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, title);
        statement.setInt(2, term_id);
        statement.setInt(3, gradeScope_id);
        statement.executeUpdate();

        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            int id = rs.getInt(1);
            course = new Course(id, title, term_id, gradeScope_id);
        }


        statement.close();
        connection.close();

        return course;
    }

    public void addCourseForUser(int course_id, int user_id) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("INSERT INTO courses_users (course_id, user_id) VALUES (?, ?)");
        statement.setInt(1, course_id);
        statement.setInt(2, user_id);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    void deleteCourse(Course course) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("DELETE FROM courses WHERE id=?");
        statement.setInt(1, course.getId());
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    void deleteCourse(int course_id) throws SQLException, CoursesRepository.NonExistingCourseException {
        Course course = getCourseById(course_id);
        deleteCourse(course);
    }

    Course getCourseById(int course_id) throws SQLException, CoursesRepository.NonExistingCourseException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT id, title, term_id, gradeScope_id FROM courses WHERE courses.id = ?");
        statement.setInt(1, course_id);
        ResultSet rs = statement.executeQuery();
        if (!rs.next()) throw new CoursesRepository.NonExistingCourseException();
        Course course = new Course(rs.getInt("id"), rs.getString("title"), rs.getInt("term_id"), rs.getInt("gradeScope_id"));
        statement.close();
        connection.close();

        return course;
    }

    public class NonExistingCourseException extends Exception {}
}
