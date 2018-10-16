package com.github.jhu_oose11.calendue.repositories;

import com.github.jhu_oose11.calendue.models.User;
import com.github.jhu_oose11.calendue.models.UsernameLogin;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public class UsersRepository {
    private DataSource database;

    public UsersRepository(DataSource database) throws SQLException {
        this.database = database;
        var connection = database.getConnection();
        var statement = connection.createStatement();
        if (database instanceof PGSimpleDataSource) {
            statement.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, email varchar(255) UNIQUE NOT NULL)");
        }
        statement.close();
        connection.close();
    }

    public void create(User user) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("INSERT INTO users (email) VALUES (?)");
        statement.setString(1, user.getEmail());
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public User getByEmail(String email) throws SQLException, NonExistingUserException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT id, email FROM users WHERE email = ?");
        statement.setString(1, email);
        var result = statement.executeQuery();
        if (!result.next()) throw new UsersRepository.NonExistingUserException();
        var user = new User(result.getInt("id"), result.getString("email"));
        result.close();
        statement.close();
        connection.close();
        return user;
    }

    public void deleteUser(User user) throws SQLException {
        deleteUser(user.getId());
    }

    public void deleteUser(int user_id) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("DELETE FROM users WHERE id=?");
        statement.setInt(1, user_id);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public static class NonExistingUserException extends Exception {}
}
