package com.github.jhu_oose11.calendue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jhu_oose11.calendue.repositories.GamesRepository;
import io.javalin.Javalin;
import io.javalin.staticfiles.Location;

import java.sql.SQLException;

public class Server {
    private static GamesRepository gamesRepository;
    private static ObjectMapper json = new ObjectMapper();

    public static void main(String[] args) throws SQLException {
        Javalin.create()
                .enableStaticFiles("/public")
                .enableStaticFiles(System.getProperty("user.dir") + "/src/main/resources/public", Location.EXTERNAL)

                .start(System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 7000);
    }

    public static ObjectMapper getJson() {
        return json;
    }

    public static GamesRepository getGamesRepository() {
        return gamesRepository;
    }
}
