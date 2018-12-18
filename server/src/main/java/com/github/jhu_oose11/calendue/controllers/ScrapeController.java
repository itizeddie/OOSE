package com.github.jhu_oose11.calendue.controllers;

import com.github.jhu_oose11.calendue.Server;
import com.github.jhu_oose11.calendue.models.Assignment;
import com.github.jhu_oose11.calendue.models.Course;
import com.github.jhu_oose11.calendue.models.Term;
import com.github.jhu_oose11.calendue.repositories.CoursesRepository;
import io.javalin.BadRequestResponse;
import io.javalin.Context;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;



public class ScrapeController {

    public static void main (Context ctx) {
        String formatedHtm;
        String parsedHtm;

        try {
            formatedHtm = runPerl(ctx.formParam("document"), "formatHtm.pl");
            parsedHtm = runPerl(formatedHtm, "htm_parser.pl");

            String[] lines = parsedHtm.split("\\n");

            String[] assignmentParams;
            int userId = ctx.sessionAttribute("current_user");

            Term term = new Term("Term_Title", formatDate(" Jan 01"), formatDate(" Dec 31"));
            term = Server.getTermsRepository().create(term);
            Server.getTermsRepository().addTermForUser(term.getId(), userId);

            int gradescopeId = 0;
            if (!lines[0].equals(""))
                gradescopeId = Integer.parseInt(lines[0]);

            Course course = new Course("template", term.getId(), gradescopeId);
            course = Server.getCoursesRepository().create(course);
            Server.getCoursesRepository().addCourseForUser(course.getId(),userId);


            //
            //This for loop is going through each assignment that was parsed and creating
            //assignment objects.
            //
            for( int i =1; i < lines.length; i++)
            {
                assignmentParams = lines[i].split(",");
                boolean completed = !assignmentParams[1].equals("0");
                LocalDate date = formatDate(assignmentParams[3]);
                double grade = 0;

                if (completed) {
                    double score = Integer.parseInt(assignmentParams[1].split("/")[0]);
                    double total = Integer.parseInt(assignmentParams[1].split("/")[1]);
                    grade = score/total * 100;
                }

                Assignment assignment = new Assignment(assignmentParams[0], date, course.getId(), completed);
                assignment = Server.getAssignmentsRepository().create(assignment);
                Server.getAssignmentsRepository().addAssignmentForUser(assignment.getId(), userId, grade, completed);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static LocalDate formatDate(String stringDate)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd");
        formatter = formatter.withLocale(Locale.US);  // Locale specifies human language for translating, and cultural norms for lowercase/uppercase and abbreviations and such. Example: Locale.US or Locale.CANADA_FRENCH
        LocalDate date;
        if(stringDate.equals(" na"))
        {
            date = LocalDate.parse("2018 Jan 01", formatter);
        }else {
            date = LocalDate.parse("2018" + stringDate, formatter);
        }
        return date;
    }

    //Runs perl script.
    //Read STDOUT of perlscript and save output into a string
    //@param    String file: s the input for perl script via STDIN
    private static String runPerl(String file, String perlscript) throws IOException {
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
