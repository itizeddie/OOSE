package com.github.jhu_oose11.calendue.models;

public class Course {
    private String title;
    private int id = 0;
    private int termId;
    private int gradeScopeId;

    public Course(String title, int term_id, int gradeScope_id) {
        this.title = title;
        this.termId = term_id;
        this.gradeScopeId = gradeScope_id;
    }

    public Course(int id, String title, int term_id, int gradeScope_Id) {
        this(title, term_id, gradeScope_Id);
        this.id = id;
    }

    public int getId() { return this.id; }

    public String getTitle() {
        return this.title;
    }

    public int getTermId() {
        return termId;
    }

    public int getGradeScopeId() {return gradeScopeId;}
}
