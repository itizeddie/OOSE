package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.controllers.Helpers.Auth;
import com.github.jhu_oose11.calendue.models.GradescopeScraper;
import com.github.jhu_oose11.calendue.models.Scraper;
import io.javalin.Context;

import java.sql.SQLException;



public class ScrapeController {
    public static void main (Context ctx) throws SQLException {
        if (!Auth.ensureLoggedIn(ctx)) return;

        int userId = ctx.sessionAttribute("current_user");

        Scraper scraper = new GradescopeScraper();

        scraper.scrape(ctx.formParam("document"), userId);
    }
}