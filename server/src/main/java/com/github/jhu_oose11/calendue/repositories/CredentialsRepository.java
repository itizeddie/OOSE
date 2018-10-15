package com.github.jhu_oose11.calendue.repositories;

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
            statement.execute("CREATE TABLE IF NOT EXISTS credentials (id SERIAL PRIMARY KEY, username VARCHAR(255) UNIQUE NOT NULL, password_hash VARCHAR(255) NOT NULL, user_id integer NOT NULL REFERENCES users)");
        }
        statement.close();
        connection.close();
    }
}
