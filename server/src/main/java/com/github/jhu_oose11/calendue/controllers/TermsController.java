package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.Server;
import com.github.jhu_oose11.calendue.controllers.Helpers.Auth;
import com.github.jhu_oose11.calendue.controllers.Helpers.Strings;
import com.github.jhu_oose11.calendue.controllers.Helpers.Validator;
import com.github.jhu_oose11.calendue.models.Term;
import com.github.jhu_oose11.calendue.repositories.TermsRepository;
import io.javalin.BadRequestResponse;
import io.javalin.Context;
import io.javalin.NotFoundResponse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class TermsController {
    public static void newTerm(Context ctx) throws SQLException {
        Auth.ensureLoggedIn(ctx);

        int current_user_id = ctx.sessionAttribute("current_user");

        ensureNewParamsValid(ctx);

        String title = ctx.formParam("title");
        LocalDate start_date = LocalDate.parse(Objects.requireNonNull(ctx.formParam("start_date")));
        LocalDate end_date = LocalDate.parse(Objects.requireNonNull(ctx.formParam("end_date")));
        Term term = new Term(title, start_date, end_date);

        try {
            term = Server.getTermsRepository().create(term);
            Server.getTermsRepository().addTermForUser(term.getId(), current_user_id);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23514")) {
                throw new BadRequestResponse("End Date cannot be before Start Date.");
            } else {
                throw e;
            }
        }

        ctx.status(201);
        ctx.result("" + term.getId());
    }

    public static void getTerm(Context ctx) throws SQLException, TermsRepository.NonExistingTermException {
        int term_id;
        try {
            term_id = Integer.parseInt(ctx.pathParam("term_id"));
        } catch(NumberFormatException e) {
            throw new NotFoundResponse();
        }
        Term term = Server.getTermsRepository().getTermById(term_id);
        ctx.result("" + term.getId());
        ctx.status(200);
    }

    public static void deleteTerm(Context ctx) throws SQLException, TermsRepository.NonExistingTermException {
        int term_id;
        try {
            term_id = Integer.parseInt(ctx.pathParam("term_id"));
        } catch(NumberFormatException e) {
            throw new NotFoundResponse();
        }
        Server.getTermsRepository().deleteTerm(term_id);
        ctx.status(204);
    }

    private static void ensureNewParamsValid(Context ctx) {
        String[] fields = {"title", "start_date", "end_date"};

        String field = Validator.validateNotBlank(ctx, fields);
        if (field != null) {
            throw new BadRequestResponse(Strings.humanize(field) + " cannot be blank.");
        }
    }
}