package com.github.jhu_oose11.calendue.models;

import java.time.LocalDate;

public class Assignment {
    private String title;
    private LocalDate dueDate;
    private int courseId;
    boolean completed = false;
    private int id;

    public Assignment(String title, LocalDate dueDate, int courseId, boolean completed) {
        this.title = title;
        this.courseId = courseId;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    public Assignment(String title, LocalDate dueDate, int courseId) {
        this(title, dueDate, courseId, false);
    }

    public Assignment(int id, String title, LocalDate dueDate, int courseId, boolean completed) {
        this(title, dueDate, courseId, completed);
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
