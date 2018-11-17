package com.github.jhu_oose11.calendue.controllers.Helpers;

import io.javalin.Context;

public class Auth {
    public static void ensureLoggedIn(Context ctx) {
        if (ctx.sessionAttribute("current_user") == null) {
            ctx.sessionAttribute("flash", "Must be logged in to view this page.");
            ctx.redirect("/login", 303);
        }
    }
}
