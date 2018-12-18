package com.github.jhu_oose11.calendue;

import java.sql.SQLException;

public interface Scraper {
    void scrape(String document, int userId) throws SQLException;
}
