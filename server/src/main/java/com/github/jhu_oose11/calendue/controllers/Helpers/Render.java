package com.github.jhu_oose11.calendue.controllers.Helpers;

import io.javalin.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public class Render {
    public static void render(Context ctx, String path, Map<String, Object> model) {
        List<String> flashes = new ArrayList<>();
        if (ctx.sessionAttribute("displayFlash") != null)
            flashes.add(ctx.sessionAttribute("displayFlash"));
        if (model.get("flash") != null) flashes.add((String) model.get("flash"));
        if (flashes.size() == 0) flashes = null;

        Map<String, Object> args = model("flashes", flashes);
        ctx.render(path, args);
    }

    public static void render(Context ctx, String path) {
        render(ctx, path, model());
    }
}
