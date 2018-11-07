package com.github.jhu_oose11.calendue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jhu_oose11.calendue.controllers.AccountsController;
import com.github.jhu_oose11.calendue.repositories.CredentialsRepository;
import com.github.jhu_oose11.calendue.repositories.UsersRepository;
import io.javalin.Javalin;
import io.javalin.JavalinEvent;
import io.javalin.staticfiles.Location;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Server {
    private static ObjectMapper json = new ObjectMapper();
    private static DataSource database;
    private static UsersRepository usersRepository;
    private static CredentialsRepository credentialsRepository;

    public static void main(String[] args) {
        Javalin.create()
                .enableStaticFiles("/public")
                .enableStaticFiles(System.getProperty("user.dir") + "/src/main/resources/public", Location.EXTERNAL)
                .routes(() -> path("accounts", () -> {
                    post(AccountsController::newAccount);
                    get(AccountsController::getAccount);
                    path(":user_id", ()-> delete(AccountsController::deleteAccount));
                }))
                .event(JavalinEvent.SERVER_STARTING, () -> {
                    if (System.getenv("JDBC_DATABASE_URL") != null) {
                        var postgresDatabase = new PGSimpleDataSource();
                        postgresDatabase.setURL(System.getenv("JDBC_DATABASE_URL"));
                        database = postgresDatabase;
                    }
                    usersRepository = new UsersRepository(database);
                    credentialsRepository = new CredentialsRepository(database);
                })
                .exception(UsersRepository.NonExistingUserException.class, (e, ctx) -> ctx.status(404))
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
}
