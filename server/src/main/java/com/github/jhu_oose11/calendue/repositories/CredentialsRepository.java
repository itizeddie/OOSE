package com.github.jhu_oose11.calendue.repositories;

import com.github.jhu_oose11.calendue.models.UsernameLogin;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public class CredentialsRepository {
    private DataSource database;

    public CredentialsRepository(DataSource database) throws SQLException {
        this.database = database;
        var connection = database.getConnection();
        var statement = connection.createStatement();
        if (database instanceof PGSimpleDataSource) {
            statement.execute("CREATE TABLE IF NOT EXISTS credentials (id SERIAL PRIMARY KEY, username VARCHAR(255) UNIQUE NOT NULL, password_hash VARCHAR(255) NOT NULL, user_id integer NOT NULL REFERENCES users ON DELETE CASCADE)");
        }
        statement.close();
        connection.close();
    }

    public UsernameLogin getByUsername(String username) throws SQLException, UsersRepository.NonExistingUserException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT id, user_id, username, password_hash FROM credentials WHERE username = ?");
        statement.setString(1, username);
        var result = statement.executeQuery();
        if (!result.next()) throw new UsersRepository.NonExistingUserException();
        var credential = new UsernameLogin(result.getInt("id"), result.getInt("user_id"), result.getString("username"), result.getString("password_hash"));
        result.close();
        statement.close();
        connection.close();
        return credential;
    }

    public void create(UsernameLogin credential) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("INSERT INTO credentials (username, password_hash, user_id) VALUES (?, ?, ?)");
        statement.setString(1, credential.getUsername());
        statement.setString(2, credential.getPasswordHash());
        statement.setInt(3, credential.getUserId());
        statement.executeUpdate();
        statement.close();
        connection.close();
    }
}
