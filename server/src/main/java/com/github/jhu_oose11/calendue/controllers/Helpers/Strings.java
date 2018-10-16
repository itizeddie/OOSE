package com.github.jhu_oose11.calendue.controllers.Helpers;

public class Strings {

    /*
    This function humanizes a given string by capitalizing the first letter
    and replacing underscores with spaces.
     */
    public static String humanize(String string) {
        char firstChar = string.toUpperCase().charAt(0);
        string = string.replace('_', ' ');
        return firstChar + string.substring(1);
    }
}
