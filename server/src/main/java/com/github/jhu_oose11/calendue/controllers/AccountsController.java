package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.Server;
import com.github.jhu_oose11.calendue.controllers.Helpers.Strings;
import com.github.jhu_oose11.calendue.controllers.Helpers.Validator;
import com.github.jhu_oose11.calendue.models.User;
import com.github.jhu_oose11.calendue.models.UsernameLogin;
import com.github.jhu_oose11.calendue.repositories.UsersRepository;
import io.javalin.BadRequestResponse;
import io.javalin.Context;
import io.javalin.NotFoundResponse;

import java.sql.SQLException;

public class AccountsController {
    private final static int MIN_PASSWORD_LENGTH = 5;

    public static void newAccount(Context ctx) throws UsersRepository.NonExistingUserException {
        ensureNewParamsValid(ctx);
        String email = ctx.formParam("email");
        User user = new User(email);
        try {
            Server.getUsersRepository().create(user);

            user = Server.getUsersRepository().getByEmail(email);

            UsernameLogin credential = new UsernameLogin(user.getId(), ctx.formParam("username"), ctx.formParam("password"));
            Server.getCredentialsRepository().create(credential);

            ctx.status(201);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                throw new BadRequestResponse("Email is already taken");
            }
        }
    }

    public static void getAccount(Context ctx) throws UsersRepository.NonExistingUserException, SQLException {
        var email = ctx.queryParam("email");
        User user = Server.getUsersRepository().getByEmail(email);
        ctx.result("" + user.getId());
        ctx.status(200);
    }

    // TODO: TEMPORARILY INSECURE FOR TESTING WITH POSTMAN...ADD SESSION AUTHENTICATION LATER
    public static void deleteAccount(Context ctx) throws SQLException {
        int user_id;
        try {
            user_id = Integer.parseInt(ctx.pathParam("user_id"));
        } catch(NumberFormatException e) {
            throw new NotFoundResponse();
        }
        Server.getUsersRepository().deleteUser(user_id);
        ctx.status(204);
    }

    private static boolean deleteParamsValid(Context ctx) {
        return Validator.validateNotBlank(ctx,"email");
    }

    private static void ensureNewParamsValid(Context ctx) {
        String[] fields = {"email", "username", "password"};

        String field = Validator.validateNotBlank(ctx, fields);
        if (field != null) {
            throw new BadRequestResponse(Strings.humanize(field) + " cannot be blank.");
        }

        if (!Validator.validateLength(ctx, "password", true, MIN_PASSWORD_LENGTH-1)) {
            throw new BadRequestResponse("Password must be at least 5 characters");
        }
    }
}
