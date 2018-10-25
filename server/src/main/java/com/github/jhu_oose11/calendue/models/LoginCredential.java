package com.github.jhu_oose11.calendue.models;

import io.javalin.UnauthorizedResponse;

public abstract class LoginCredential {
    public abstract boolean authenticate(String password);
}
