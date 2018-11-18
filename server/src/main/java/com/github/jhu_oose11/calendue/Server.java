package com.github.jhu_oose11.calendue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jhu_oose11.calendue.controllers.*;
import com.github.jhu_oose11.calendue.models.Course;
import com.github.jhu_oose11.calendue.repositories.CoursesRepository;
import com.github.jhu_oose11.calendue.repositories.CredentialsRepository;
import com.github.jhu_oose11.calendue.repositories.TermsRepository;
import com.github.jhu_oose11.calendue.repositories.UsersRepository;
import io.javalin.Javalin;
import io.javalin.JavalinEvent;
import io.javalin.staticfiles.Location;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Server {
    private static ObjectMapper json = new ObjectMapper();
    private static DataSource database;
    private static UsersRepository usersRepository;
    private static CredentialsRepository credentialsRepository;
    private static CoursesRepository coursesRepository;
    private static TermsRepository termsRepository;

    public static void main(String[] args) {
        Javalin.create()
                .enableStaticFiles("/public")
                .enableStaticFiles(System.getProperty("user.dir") + "/src/main/resources/public", Location.EXTERNAL)
                .routes(() -> {
                    before(ctx -> {
                        ctx.sessionAttribute("displayFlash", null);
                        String flash = ctx.sessionAttribute("flash");
                        ctx.sessionAttribute("flash", null);
                        ctx.sessionAttribute("displayFlash", flash);
                    });

                    path("/", () -> get(CalendarController::index));

                    path("accounts", () -> {
                        post(AccountsController::newAccount);
                        get(AccountsController::getAccount);
                        path(":user_id", () -> delete(AccountsController::deleteAccount));
                    });
                    path("scrape", () -> post(ScrapeController::main));
                    path("login", () -> {
                        get(LoginController::loginView);
                        post(LoginController::login);
                    });
                    path("logout", () -> get(LoginController::logout));
                    path("term", () -> {
                        post(TermsController::newTerm);
                        path(":term_id", () -> {
                            delete(TermsController::deleteTerm);
                            get(TermsController::getTerm);
                        });
                    });
                    path("course", () -> post(CoursesController::newCourse));
                })
                .event(JavalinEvent.SERVER_STARTING, () -> {
                    if (System.getenv("JDBC_DATABASE_URL") != null) {
                        var postgresDatabase = new PGSimpleDataSource();
                        postgresDatabase.setURL(System.getenv("JDBC_DATABASE_URL"));
                        database = postgresDatabase;
                    }
                    usersRepository = new UsersRepository(database);
                    credentialsRepository = new CredentialsRepository(database);
                    coursesRepository = new CoursesRepository(database);
                    termsRepository = new TermsRepository(database);
                })
                .exception(UsersRepository.NonExistingUserException.class, (e, ctx) -> ctx.status(404))
                .exception(TermsRepository.NonExistingTermException.class, (e, ctx) -> ctx.status(404))
                .exception(CoursesRepository.NonExistingCourseException.class, (e, ctx) -> ctx.status(404))
                .start(System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 7000);
    }

    public static ObjectMapper getJson() {
        return json;
    }

    public static UsersRepository getUsersRepository() {
        return usersRepository;
    }

    public static CredentialsRepository getCredentialsRepository() {
        return credentialsRepository;
    }

    public static TermsRepository getTermsRepository() { return termsRepository; }

    public static CoursesRepository getCoursesRepository() { return coursesRepository; }
}
