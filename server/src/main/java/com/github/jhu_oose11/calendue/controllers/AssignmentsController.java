package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.Server;
import com.github.jhu_oose11.calendue.controllers.Helpers.Auth;
import com.github.jhu_oose11.calendue.controllers.Helpers.Strings;
import com.github.jhu_oose11.calendue.controllers.Helpers.Validator;
import com.github.jhu_oose11.calendue.models.Assignment;
import com.github.jhu_oose11.calendue.models.Course;
import io.javalin.BadRequestResponse;
import io.javalin.Context;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class AssignmentsController {
    public static void newAssignment(Context ctx) throws SQLException {
        Auth.ensureLoggedIn(ctx);
        int current_user_id = ctx.sessionAttribute("current_user");

        ensureNewParamsValid(ctx);

        String title = ctx.formParam("title");
        LocalDate dueDate = LocalDate.parse(Objects.requireNonNull(ctx.formParam("due_date")));

        int course_id;
        try {
            course_id = Integer.parseInt(Objects.requireNonNull(ctx.formParam("course_id")));
        } catch(NumberFormatException e) {
            throw new BadRequestResponse("Course ID is invalid.");
        }

        Assignment assignment = new Assignment(title, dueDate, course_id);

        try {
            assignment = Server.getAssignmentsRepository().create(assignment);
            Server.getAssignmentsRepository().addAssignmentForUser(assignment.getId(), current_user_id);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23503")) {
                throw new BadRequestResponse("Course does not exist.");
            } else {
                throw e;
            }
        }

        ctx.status(201);
        ctx.result("" + assignment.getId());
    }

    private static void ensureNewParamsValid(Context ctx) {
        String[] fields = {"title", "course_id", "due_date"};

        String field = Validator.validateNotBlank(ctx, fields);
        if (field != null) {
            throw new BadRequestResponse(Strings.humanize(field) + " cannot be blank.");
        }
    }
}
