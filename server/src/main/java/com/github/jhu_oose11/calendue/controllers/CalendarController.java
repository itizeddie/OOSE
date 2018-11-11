package com.github.jhu_oose11.calendue.controllers;

import io.javalin.Context;

public class CalendarController {
    public static void index(Context ctx) {
        LoginController.ensureLoggedIn(ctx);

        ctx.html("You are logged in. <a href='/logout'>Logout</a>");
    }
}
