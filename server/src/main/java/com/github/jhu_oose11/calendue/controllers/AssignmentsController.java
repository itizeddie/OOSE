package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.Server;
import com.github.jhu_oose11.calendue.controllers.Helpers.Auth;
import com.github.jhu_oose11.calendue.controllers.Helpers.Strings;
import com.github.jhu_oose11.calendue.controllers.Helpers.Validator;
import com.github.jhu_oose11.calendue.models.Assignment;
import com.github.jhu_oose11.calendue.models.Course;
import com.github.jhu_oose11.calendue.models.User;
import com.github.jhu_oose11.calendue.repositories.AssignmentsRepository;
import io.javalin.BadRequestResponse;
import io.javalin.Context;
import io.javalin.NotFoundResponse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class AssignmentsController {
    public static void newAssignment(Context ctx) throws SQLException {
        if (!Auth.ensureLoggedIn(ctx)) return;
        int current_user_id = ctx.sessionAttribute("current_user");

        ensureNewParamsValid(ctx);

        String title = ctx.formParam("title");
        LocalDate dueDate = LocalDate.parse(Objects.requireNonNull(ctx.formParam("due_date")));
        String gradeString = ctx.formParam("grade");
        double score = Integer.parseInt(gradeString.split("/")[0]);
        double total = Integer.parseInt(gradeString.split("/")[1]);
        double grade = score/total * 100;

        int course_id;
        try {
            course_id = Integer.parseInt(Objects.requireNonNull(ctx.formParam("course_id")));
        } catch(NumberFormatException e) {
            throw new BadRequestResponse("Course ID is invalid.");
        }

        Assignment assignment = new Assignment(title, dueDate, course_id);

        try {
            assignment = Server.getAssignmentsRepository().create(assignment);
            Server.getAssignmentsRepository().addAssignmentForUser(assignment.getId(), current_user_id, grade, false);
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

    public static void markAssignmentComplete(Context ctx) throws SQLException {
        if (!Auth.ensureLoggedIn(ctx)) return;
        int current_user_id = ctx.sessionAttribute("current_user");

        int assignment_id;
        int completion_time;
        try {
            assignment_id = Integer.parseInt(ctx.pathParam("assignment_id"));
        } catch(NumberFormatException e) {
            throw new NotFoundResponse("Assignment not found.");
        }
        try {
            completion_time = Integer.parseInt(ctx.formParam("time_spent"));
        } catch(NumberFormatException e) {
            throw new NotFoundResponse("Completion time must be a valid number.");
        }
        Server.getAssignmentsRepository().markAssignmentAsCompleted(assignment_id, current_user_id, completion_time);

        ctx.status(200);
    }

    public static void getAssignment(Context ctx) throws AssignmentsRepository.NonExistingAssignmentException, SQLException {
        int assignment_id;
        try {
            assignment_id = Integer.parseInt(ctx.pathParam("assignment_id"));
        } catch(NumberFormatException e) {
            throw new NotFoundResponse("Assignment not found.");
        }
        Assignment assignment = Server.getAssignmentsRepository().getAssignmentById(assignment_id);
        ctx.result("" + assignment.getId());
        ctx.status(200);
    }

    private static void ensureNewParamsValid(Context ctx) {
        String[] fields = {"title", "course_id", "due_date"};

        String field = Validator.validateNotBlank(ctx, fields);
        if (field != null) {
            throw new BadRequestResponse(Strings.humanize(field) + " cannot be blank.");
        }
        String valid = Validator.validateDate(ctx, "due_date");
        if(valid != null) {
            throw new BadRequestResponse(Strings.humanize(valid) + " must be a valid date.");
        }

    }

}
