package com.github.jhu_oose11.calendue.repositories;

import com.github.jhu_oose11.calendue.models.Term;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class TermsRepository {
    private DataSource database;

    public TermsRepository(DataSource database) throws SQLException {
        this.database = database;
        var connection = database.getConnection();
        var statement = connection.createStatement();
        if (database instanceof PGSimpleDataSource) {
            statement.execute("CREATE TABLE IF NOT EXISTS terms (id SERIAL PRIMARY KEY, title varchar(255) NOT NULL, start_date DATE, end_date DATE CONSTRAINT start_before_end CHECK (end_date >= start_date))");
            statement.execute("CREATE TABLE IF NOT EXISTS terms_users (id SERIAL PRIMARY KEY, term_id integer NOT NULL REFERENCES terms ON DELETE CASCADE, user_id INTEGER NOT NULL REFERENCES users ON DELETE CASCADE, UNIQUE(term_id, user_id))");
        }
        statement.close();
        connection.close();
    }

    public Term create(Term term) throws SQLException {
        Date startDate = Date.valueOf(term.getStartDate());
        Date endDate = Date.valueOf(term.getEndDate());
        String title = term.getTitle();

        var connection = database.getConnection();
        var statement = connection.prepareStatement("INSERT INTO terms (title, start_date, end_date) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, title);
        statement.setDate(2, startDate);
        statement.setDate(3, endDate);
        statement.executeUpdate();

        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            int id = rs.getInt(1);
            term = new Term(id, title, term.getStartDate(), term.getEndDate());
        }


        statement.close();
        connection.close();

        return term;
    }

    void addTermForUser(int term_id, int user_id) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("INSERT INTO terms_users (term_id, user_id) VALUES (?, ?)");
        statement.setInt(1, term_id);
        statement.setInt(2, user_id);
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    void deleteTerm(Term term) throws SQLException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("DELETE FROM terms WHERE id=?");
        statement.setInt(1, term.getId());
        statement.executeUpdate();
        statement.close();
        connection.close();
    }

    public void deleteTerm(int term_id) throws SQLException, NonExistingTermException {
        Term term = getTermById(term_id);
        deleteTerm(term);
    }

    public Term getTermById(int term_id) throws SQLException, NonExistingTermException {
        var connection = database.getConnection();
        var statement = connection.prepareStatement("SELECT id, title, start_date, end_date FROM terms WHERE terms.id = ?");
        statement.setInt(1, term_id);
        ResultSet rs = statement.executeQuery();
        if (!rs.next()) throw new NonExistingTermException();
        LocalDate startDate = rs.getDate("start_date").toLocalDate();
        LocalDate endDate = rs.getDate("end_date").toLocalDate();
        Term term = new Term(rs.getInt("id"), rs.getString("title"), startDate, endDate);
        statement.close();
        connection.close();

        return term;
    }

    public class NonExistingTermException extends Exception {}
}