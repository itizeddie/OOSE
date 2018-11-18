package com.github.jhu_oose11.calendue.models;

public class Course {
    private String title;
    private int id = 0;
    private int termId;

    public Course(String title, int term_id) {
        this.title = title;
        this.termId = term_id;
    }

    public Course(int id, String title, int term_id) {
        this(title, term_id);
        this.id = id;
    }

    public int getId() { return this.id; }

    public String getTitle() {
        return this.title;
    }

    public int getTermId() {
        return termId;
    }
}
