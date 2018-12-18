package com.github.jhu_oose11.calendue.models;

import com.github.jhu_oose11.calendue.Server;
import com.github.jhu_oose11.calendue.repositories.AssignmentsRepository;
import com.github.jhu_oose11.calendue.repositories.CoursesRepository;
import com.github.jhu_oose11.calendue.repositories.TermsRepository;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class GradescopeScraper implements Scraper {
    public void scrape(String document, int userId) throws SQLException {
        String formatedHtm;
        String parsedHtm;

        try {
            formatedHtm = runPerl(document, "formatHtm.pl");
            parsedHtm = runPerl(formatedHtm, "htm_parser.pl");

            String[] lines = parsedHtm.split("\\n");

            String term_title = "Unknown Term";
            if (!lines[1].equals("")) term_title = lines[1];

            Term term = getOrCreateTerm(term_title);
            addUserToTerm(userId, term);

            int gradescopeId = 0;
            if (!lines[0].equals(""))
                gradescopeId = Integer.parseInt(lines[0]);

            Course course = getOrCreateCourse(term, gradescopeId);
            addUserToCourse(userId, course);

            String[] assignmentParams;
            //
            //This for loop is going through each assignment that was parsed and creating
            //assignment objects.
            //
            for( int i =2; i < lines.length; i++)
            {
                assignmentParams = lines[i].split(",");
                boolean completed = !assignmentParams[1].trim().equals("0");
                LocalDate date = formatDate(assignmentParams[3]);

                double grade = 0;

                if (completed) {
                    var test = assignmentParams[1].split(" / ");
                    double score = Double.parseDouble(test[0]);
                    double total = Double.parseDouble(test[1]);
                    grade = score/total * 100;
                }

                String title = assignmentParams[0];

                Assignment assignment = getOrCreateAssignment(course, date, title);
                addUserToAssignment(userId, assignment, grade, completed);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Runs perl script.
    //Read STDOUT of perlscript and save output into a string
    //@param    String file: s the input for perl script via STDIN
    private static String runPerl(String file, String perlscript) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("perl", System.getProperty("user.dir")+"/src/main/perl_parsing_scripts/"+perlscript);
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
    private static void addUserToAssignment(int userId, Assignment assignment, double grade, boolean completed) throws SQLException {
        try {
            Server.getAssignmentsRepository().addAssignmentForUser(assignment.getId(), userId, grade, completed);
        } catch (SQLException e) {
            // If the user already has the assignment then ignore this exception
            if (!e.getSQLState().equals("23505")) throw e;
        }
    }

    private static Assignment getOrCreateAssignment(Course course, LocalDate date, String title) throws SQLException {
        Assignment assignment;
        try {
            assignment = Server.getAssignmentsRepository().getAssignmentByTitleAndCourse(title, course.getId());
        } catch (AssignmentsRepository.NonExistingAssignmentException e) {
            assignment = new Assignment(title, date, course.getId());
            assignment = Server.getAssignmentsRepository().create(assignment);
        }
        return assignment;
    }

    private static void addUserToCourse(int userId, Course course) throws SQLException {
        try {
            Server.getCoursesRepository().addCourseForUser(course.getId(), userId);
        } catch (SQLException e) {
            // If the user already has the course then ignore this exception
            if (!e.getSQLState().equals("23505")) throw e;
        }
    }

    private static Course getOrCreateCourse(Term term, int gradescopeId) throws SQLException {
        Course course;
        try {
            course = Server.getCoursesRepository().getCourseByGradescopeId(gradescopeId);
        } catch (CoursesRepository.NonExistingCourseException e) {
            course = new Course("template", term.getId(), gradescopeId);
            course = Server.getCoursesRepository().create(course);
        }
        return course;
    }

    private static void addUserToTerm(int userId, Term term) throws SQLException {
        try {
            Server.getTermsRepository().addTermForUser(term.getId(), userId);
        } catch (SQLException e) {
            // If the user already has the term then ignore this exception
            if (!e.getSQLState().equals("23505")) throw e;
        }
    }

    private static Term getOrCreateTerm(String term_title) throws SQLException {
        Term term;
        try {
            term = Server.getTermsRepository().getTermByTitle(term_title);
        } catch(TermsRepository.NonExistingTermException e) {
            term = new Term(term_title, formatDate(" Jan 01"), formatDate(" Dec 31"));
            term = Server.getTermsRepository().create(term);
        }
        return term;
    }
}
