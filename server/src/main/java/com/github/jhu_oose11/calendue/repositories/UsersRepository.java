package com.github.jhu_oose11.calendue.repositories;

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
}
