package com.github.jhu_oose11.calendue.models;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CourseTest {
    private static Map<String, Object> testData = new HashMap<>();


    @BeforeAll
    static void setUpOnce() {
        String title = "Test Term";
        int term_id = 5;
        Course course = new Course(title, term_id);

        testData.put("term_id", term_id);
        testData.put("title", title);
        testData.put("course", course);
    }

    @Test
    void getId() {
        int id = 5;
        String title = (String) testData.get("title");
        int termId = (int) testData.get("term_id");
        Course course = new Course(id, title, termId);

        assertEquals(course.getId(), id);
    }

    @Test
    void getTitle() {
        Course course = (Course) testData.get("course");
        assertEquals(course.getTitle(), testData.get("title"));
    }

    @Test
    void getTermId() {
        Course course = (Course) testData.get("course");
        assertEquals(course.getTermId(), testData.get("term_id"));
    }
}