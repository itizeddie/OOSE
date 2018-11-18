package com.github.jhu_oose11.calendue.models;

import java.time.LocalDate;

public class Assignment {
    private String title;
    private LocalDate dueDate;
    private int courseId;
    Boolean completed = false;
    private int id;

    public Assignment(String title, LocalDate dueDate, int courseId) {
        this.title = title;
        this.courseId = courseId;
        this.dueDate = dueDate;
    }

    public Assignment(int id, String title, LocalDate dueDate, int courseId) {
        this(title, dueDate, courseId);
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public int getId() {
        return id;
    }

    public int getCourseId() {
        return courseId;
    }
}
