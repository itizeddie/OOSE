package com.github.jhu_oose11.calendue.controllers;

import io.javalin.Context;

import java.io.*;


public class ScrapeController {

    public static void main (Context ctx) {
        String formatedHtm;
        String parsedHtm;
        try {
            formatedHtm = runPerl(ctx.formParam("document"), "formatHtm.pl");
            parsedHtm = runPerl(formatedHtm, "htm_parser.pl");
            //System.out.print(formatedHtm);
            System.out.print(parsedHtm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String runPerl(String file, String perlscript) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("perl", System.getProperty("user.dir")+"\\src\\main\\perl_parsing_scripts\\"+perlscript);
        StringBuilder returnString = new StringBuilder();
        try {
            Process p=pb.start();
            BufferedReader stdout = new BufferedReader(
                    new InputStreamReader(p.getInputStream())
            );

            BufferedWriter stdin = new BufferedWriter(
                    new OutputStreamWriter(p.getOutputStream())
            );
            //write to perl script's stdin
            stdin.write(file);
            //assure that that the data is written and does not remain in the buffer
            stdin.flush();
            //send eof by closing the scripts stdin
            stdin.close();

            //read the first output line from the perl script's stdout
            String output;
            while((output = stdout.readLine()) != null) {
                returnString.append(output+"\n");
                //System.out.println(output);
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        //System.out.printf("%s", ctx.formParam("document"));
        return returnString.toString();
    }

}
