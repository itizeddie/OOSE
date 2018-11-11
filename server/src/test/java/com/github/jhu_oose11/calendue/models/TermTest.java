package com.github.jhu_oose11.calendue.models;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TermTest {
    private static Map<String, Object> testData = new HashMap<>();


    @BeforeAll
    static void setUpOnce() {
        String title = "Test Term";
        LocalDate startDate = LocalDate.of(2018, 9, 5);
        LocalDate endDate = LocalDate.of(2018, 11, 5);
        Term term = new Term(title, startDate, endDate);

        testData.put("term", term);
        testData.put("title", title);
        testData.put("startDate", startDate);
        testData.put("endDate", endDate);
    }

    @Test
    void getId() {
        int id = 5;
        String title = (String) testData.get("title");
        LocalDate startDate = (LocalDate) testData.get("startDate");
        LocalDate endDate = (LocalDate) testData.get("endDate");
        Term term = new Term(id, title, startDate, endDate);

        assertEquals(term.getId(), id);
    }

    @Test
    void getTitle() {
        Term term = (Term) testData.get("term");
        assertEquals(term.getTitle(), testData.get("title"));
    }

    @Test
    void getStartDate() {
        Term term = (Term) testData.get("term");
        assertEquals(term.getStartDate(), testData.get("startDate"));
    }

    @Test
    void getEndDate() {
        Term term = (Term) testData.get("term");
        assertEquals(term.getEndDate(), testData.get("endDate"));
    }
}