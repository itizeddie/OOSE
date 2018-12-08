package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.TemplatePath;
import com.github.jhu_oose11.calendue.controllers.Helpers.Auth;
import com.github.jhu_oose11.calendue.controllers.Helpers.Render;
import io.javalin.Context;

public class CalendarController {
    public static void index(Context ctx) {
        if (!Auth.ensureLoggedIn(ctx)) return;
        Render.render(ctx, TemplatePath.CALENDAR);
    }
}
