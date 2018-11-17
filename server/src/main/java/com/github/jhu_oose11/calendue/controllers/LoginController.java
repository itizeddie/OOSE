package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.Server;
import com.github.jhu_oose11.calendue.TemplatePath;
import com.github.jhu_oose11.calendue.controllers.Exceptions.InvalidInputException;
import com.github.jhu_oose11.calendue.controllers.Helpers.Auth;
import com.github.jhu_oose11.calendue.controllers.Helpers.Render;
import com.github.jhu_oose11.calendue.controllers.Helpers.Strings;
import com.github.jhu_oose11.calendue.controllers.Helpers.Validator;
import com.github.jhu_oose11.calendue.models.UsernameLogin;
import com.github.jhu_oose11.calendue.repositories.UsersRepository;
import io.javalin.Context;

import java.sql.SQLException;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public class LoginController {
    public static void loginView(Context ctx) {
        if (ctx.sessionAttribute("current_user") != null) {
            ctx.redirect("/");
        } else {
            Render.render(ctx, TemplatePath.LOGIN);
        }
    }

    public static void login(Context ctx) throws SQLException {
        try {
            ensureLoginParamsValid(ctx);
        } catch(InvalidInputException e) {
            Map<String, Object> args = model("flash", e.getMessage());
            ctx.status(400);
            Render.render(ctx, TemplatePath.LOGIN, args);
            return;
        }

        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        UsernameLogin credential;
        if ((credential = authenticateRetrieveCredential(username, password)) != null) {
            setSessionCookies(ctx, credential);
            ctx.redirect("/", 303);
        } else {
            Map<String, Object> args = model("flash", "Invalid Username or Password");
            ctx.status(401);
            Render.render(ctx, TemplatePath.LOGIN, args);
        }
    }

    public static void logout(Context ctx) {
        Auth.ensureLoggedIn(ctx);
        ctx.sessionAttribute("current_user", null);
        ctx.sessionAttribute("flash", "Successfully been logged out.");
        ctx.redirect("/login", 303);
    }

    private static void setSessionCookies(Context ctx, UsernameLogin credential) {
        ctx.sessionAttribute("current_user", credential.getUserId());
    }

    private static void ensureLoginParamsValid(Context ctx) throws InvalidInputException {
        String[] fields = {"username", "password"};
        String field = Validator.validateNotBlank(ctx, fields);
        if (field != null) {
            throw new InvalidInputException(Strings.humanize(field) + " cannot be blank.");
        }
    }

    private static UsernameLogin authenticateRetrieveCredential(String username, String password) throws SQLException {
        try {
            UsernameLogin credential = Server.getCredentialsRepository().getByUsername(username);
            if (credential.authenticate(password)) return credential;
            return null;
        } catch (UsersRepository.NonExistingUserException e) {
            return null;
        }
    }
}
