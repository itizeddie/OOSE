package com.github.jhu_oose11.calendue.controllers.Helpers;

import io.javalin.Context;

public class Validator {
    public static boolean validateNotBlank(Context ctx, String field) {
        return ctx.formParam(field) != null && validateLength(ctx, field, true, 0);
    }

    public static String validateNotBlank(Context ctx, String[] fields) {
        for (String field : fields) {
            if (!validateNotBlank(ctx, field)) {
                return field;
            }
        }
        return null;
    }

    public static boolean validateLength(Context ctx, String field, boolean greater, int length) {
        String fieldVal = ctx.formParam(field);
        if (fieldVal == null) return true;

        if (greater) {
            return fieldVal.length() > length;
        } else {
            return fieldVal.length() < length;
        }
    }
}
