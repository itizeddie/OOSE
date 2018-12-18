package com.github.jhu_oose11.calendue.models;

import java.sql.SQLException;

public interface Scraper {
    void scrape(String document, int userId) throws SQLException;
}
