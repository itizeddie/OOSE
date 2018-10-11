#!/usr/local/bin/perl -w

#
# This program parses through a .htm file. 
#
# Note: you must use formatHtm.pl on the htm
# 	you wish to format otherwise parsing is 
# 	not guarenteed to be correct. 

use strict;
use warnings; 
use v5.10;

    my $fileToParse = $ARGV[0];
    open my $input, '<', $fileToParse	or die "Can't read the file I want to parse: $!";
    open my $output, '>', "$fileToParse.parsed" or die "Can't write new file: $!";

    #slup of all lines of input file
    my @allLines = do { local $/; <$input> }; 
    close $input; 

    #split allLines by line \n
    my @splitLines = split(/\n/, $allLines[0]); 
    #Expected tag for files that are formated with formatHtm.pl
    my $formatTag = "Formated with formatHtm.pl"; 

    my $courseNumber; 

    ##
    #Can and will just make an Assignment Object and use
    #name, grade, release, and due, timeTakeToComplete as values for it. 
    ##
    my @assignmentNames; 
    my @assignmentGrades;
    my @assignmentRelease; 
    my @assignmentDue; 

    my $assignmentCount = 0;
    my $lineCount = 0; 


    ##check if input file is formated correctly
    if(!isFileFormatted($splitLines[0])){
	die ".htm was not formatted with formatHtm.pl: $!";
    } 

    $courseNumber = getCourseNumber(@splitLines); 
    print $output "$courseNumber\n";
    my $temp; 
    foreach my $line (@splitLines) {

	if($temp = getAssignment($line, @splitLines, $lineCount) ne 0) {$assignmentNames[$assignmentCount];}
	if($temp = getGrades($line, @splitLines, $lineCount) ne 0) {	$assignmentGrades[$assignmentCount];}
	if($temp = getRelease($line, @splitLines, $lineCount) ne 0) { 	$assignmentRelease[$assignmentCount];}
	if($temp = getDue($line, @splitLines, $lineCount) ne 0) {	$assignmentDue[$assignmentCount++];}

	$lineCount++; 
    }

    for( my $i = 0; $i < $assignmentCount; $i++) {
	print $output "$assignmentNames[$i] $assignmentGrades[$i] $assignmentRelease[$i] $assignmentDue[$i]\n";
    } 
    close $output; 
    
    exit; 

    sub isFileFormatted {
	my @params = @_;  
	if($params[0] eq $formatTag) { 
		return 1;	
	}else {
		return 0;
	} 
    }
    ##
    #Use a regex to find Gradescope [anything] [3numbers . 3 numbers]anything Dashboard
    #return 3 numbers.3numbers + anything up until a space
    ##
    sub getCourseNumber {
    	my @lotsOfLines = @_;
	foreach my $line (@lotsOfLines) {
	    if ($line =~ /[0-9]{3}.[0-9]{3}(\/[0-9]{3})?/) {
	   
		#Returning $line gives the line with the course number now try and only get the numbers
	 	return $line=~ s/([a-z])//;
	    }
    	}
    }

    ##
    #Finds class="table--primaryLink"
    #Checks if the next line has aria-label="View ... 
    #if so then the Assignemnt name is the word after View. 
    #OR
    #if not then the next line is the assignment name. 
    ##
    sub getAssignment {
    	return 0;
    }

    ##
    #find submissionStatus--Score OR submissionStatus--text
    #next line is the grade or word No Submission
    ##
    sub getGrades {
	return 0;
    }
    ##
    #submissionTimeChart--releaseDate
    #next line will hold the release date
    ##
    sub getRelease{
	return 0;
    }
    ##
    #submissionTimeChart--dueDate
    ##
    sub getDue {
	return 0;
    }

