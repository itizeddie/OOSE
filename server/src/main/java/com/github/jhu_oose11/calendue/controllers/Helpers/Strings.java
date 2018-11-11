package com.github.jhu_oose11.calendue.controllers.Helpers;

public class Strings {

    /*
    This function humanizes a given string by capitalizing the first letter
    and replacing underscores with spaces.
     */
    public static String humanize(String string) {
        string = string.replace('_', ' ');

        StringBuilder capitalized = new StringBuilder();
        String[] words = string.split("\\s");
        for (String word : words) {
            capitalized.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }

        return capitalized.toString().trim();
    }
}
