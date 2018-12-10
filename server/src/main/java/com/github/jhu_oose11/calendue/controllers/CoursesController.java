package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.Server;
import com.github.jhu_oose11.calendue.controllers.Helpers.Auth;
import com.github.jhu_oose11.calendue.controllers.Helpers.Strings;
import com.github.jhu_oose11.calendue.controllers.Helpers.Validator;
import com.github.jhu_oose11.calendue.models.Course;
import com.github.jhu_oose11.calendue.models.Term;
import io.javalin.BadRequestResponse;
import io.javalin.Context;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class CoursesController {
    public static void newCourse(Context ctx) throws SQLException {
        if (!Auth.ensureLoggedIn(ctx)) return;
        int current_user_id = ctx.sessionAttribute("current_user");

        ensureNewParamsValid(ctx);

        String title = ctx.formParam("title");

        int term_id;
        try {
            term_id = Integer.parseInt(ctx.formParam("term_id"));
        } catch(NumberFormatException e) {
            throw new BadRequestResponse("Term ID is invalid.");
        }
        int gradeScope_id;
        try {
            gradeScope_id = Integer.parseInt(ctx.formParam("gradeScope_id"));
        } catch(NumberFormatException e) {
            throw new BadRequestResponse("Grade Scope ID is invalid.");
        }

        Course course = new Course(title, term_id, gradeScope_id);

        try {
            course = Server.getCoursesRepository().create(course);
            Server.getCoursesRepository().addCourseForUser(course.getId(), current_user_id);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23503")) {
                throw new BadRequestResponse("Term does not exist.");
            } else {
                throw e;
            }
        }

        ctx.status(201);
        ctx.result("" + course.getId());
    }

    private static void ensureNewParamsValid(Context ctx) {
        String[] fields = {"title", "term_id"};

        String field = Validator.validateNotBlank(ctx, fields);
        if (field != null) {
            throw new BadRequestResponse(Strings.humanize(field) + " cannot be blank.");
        }
    }
}
