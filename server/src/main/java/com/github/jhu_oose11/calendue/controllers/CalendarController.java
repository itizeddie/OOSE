package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.TemplatePath;
import com.github.jhu_oose11.calendue.controllers.Helpers.Render;
import io.javalin.Context;

public class CalendarController {
    public static void index(Context ctx) {
        LoginController.ensureLoggedIn(ctx);
        Render.render(ctx, TemplatePath.CALENDAR);
    }
}
