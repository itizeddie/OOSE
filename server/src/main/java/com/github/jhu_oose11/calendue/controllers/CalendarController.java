package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.Server;
import com.github.jhu_oose11.calendue.TemplatePath;
import com.github.jhu_oose11.calendue.controllers.Helpers.Auth;
import com.github.jhu_oose11.calendue.controllers.Helpers.Render;
import com.github.jhu_oose11.calendue.models.Assignment;
import io.javalin.Context;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarController {
    public static void index(Context ctx) {
        if (!Auth.ensureLoggedIn(ctx)) return;
        Render.render(ctx, TemplatePath.CALENDAR);
    }

    public static void getAssignments(Context ctx) throws SQLException {
        Auth.ensureLoggedIn(ctx);

        int userId = ctx.sessionAttribute("current_user");

        List<Assignment> assignments = Server.getAssignmentsRepository().getAssignmentsForUser(userId);

        ctx.json(mapAssignmentsByDueDate(assignments));
    }

    public static void getStatistics(Context ctx) throws SQLException {
        Auth.ensureLoggedIn(ctx);

        int userId = ctx.sessionAttribute("current_user");
    }

    private static Map<String, List<Assignment>> mapAssignmentsByDueDate(List<Assignment> assignments) {
        Map<String, List<Assignment>> assignmentList = new HashMap<>();
        for (Assignment assignment : assignments) {
            String date = assignment.getDueDate().format(DateTimeFormatter.ISO_DATE);
            List<Assignment> onDate = assignmentList.getOrDefault(date, new ArrayList<>());
            onDate.add(assignment);
            assignmentList.put(date, onDate);
        }

        return assignmentList;
    }
}