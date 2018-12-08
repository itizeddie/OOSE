package com.github.jhu_oose11.calendue.models;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssignmentTest {
    private static Map<String, Object> testData = new HashMap<>();


    @BeforeAll
    static void setUpOnce() {
        String title = "Test Term";
        int courseId = 5;
        LocalDate dueDate = LocalDate.now();
        Assignment assignment = new Assignment(title, dueDate, courseId);

        testData.put("course_id", courseId);
        testData.put("title", title);
        testData.put("due_date", dueDate);
        testData.put("assignment", assignment);
    }

    @Test
    void getId() {
        int id = 5;
        String title = (String) testData.get("title");
        int courseId = (int) testData.get("course_id");
        LocalDate dueDate = (LocalDate) testData.get("due_date");
        Assignment assignment = new Assignment(id, title, dueDate, courseId, false);

        assertEquals(assignment.getId(), id);
    }

    @Test
    void getTitle() {
        Assignment assignment = (Assignment) testData.get("assignment");
        assertEquals(assignment.getTitle(), testData.get("title"));
    }

    @Test
    void getTermId() {
        Assignment assignment = (Assignment) testData.get("assignment");
        assertEquals(assignment.getCourseId(), testData.get("course_id"));
    }

    @Test
    void getDueDate() {
        Assignment assignment = (Assignment) testData.get("assignment");
        assertEquals(assignment.getDueDate(), testData.get("due_date"));
    }
}
