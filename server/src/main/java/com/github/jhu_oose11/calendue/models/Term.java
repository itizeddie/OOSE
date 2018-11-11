package com.github.jhu_oose11.calendue.models;

import java.time.LocalDate;
import java.util.Date;

public class Term {
    private String title;
    private int id = 0;
    private LocalDate startDate;
    private LocalDate endDate;

    public Term(String title, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Term(int id, String title, LocalDate startDate, LocalDate endDate) {
        this(title, startDate, endDate);
        this.id = id;
    }

    public int getId() { return this.id; }

    public String getTitle() {
        return this.title;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }
}