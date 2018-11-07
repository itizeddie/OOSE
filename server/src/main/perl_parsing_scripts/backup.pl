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
    my $ERROR = -1; 
    my $fileToParse = $ARGV[0];
    open my $input, '<', $fileToParse	or die "Can't read the file I want to parse: $!";
    open my $output, '>', "$fileToParse.parsed" or die "Can't write new file: $!";

    #slup of all lines of input file
    my @allLines = do { local $/; <$input> }; 
    close $input; 

    #split allLines by line \n
    my @splitLines = split(/\n/, $allLines[0]); 
    #Expected tag for files that are formated with formatHtm.pl
    my $formatTag = 	"Formated with formatHtm.pl"; 
    ## Final regex patterns
    my $REGEXDUE =	 "submissionTimeChart--dueDate";
    my $REGEXRELEASE =	 "submissionTimeChart--releaseDate";
    my $REGEXNOGRADES =	 "submissionStatus--text";
    my $REGEXGRADES = 	 "submissionStatus--score";

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
    if($courseNumber eq $ERROR) { die "Can't read couse number: $!";}
    print $output "$courseNumber\n";
    my $temp; 
    foreach my $line (@splitLines) {

	if(($temp =  getAssignment($lineCount, $line, @splitLines)) ne $ERROR)
	    {$assignmentNames[$assignmentCount] = $temp;$assignmentCount++;}
	elsif(($temp = getGrades($lineCount,$line, @splitLines)) ne $ERROR)
	   {$assignmentGrades[$assignmentCount] = $temp;}
	elsif(($temp = getRelease($lineCount, $line, @splitLines)) ne $ERROR) { 
	    $assignmentRelease[$assignmentCount] = $temp;}
	elsif(($temp = getDue($lineCount, $line, @splitLines)) ne $ERROR) {
       	    $assignmentDue[$assignmentCount] = $temp;}

	$lineCount++; 
    }
    for( my $i = 0; $i < $assignmentCount; $i++) {
	print $output "$assignmentNames[$i], $assignmentGrades[$i+1]\n";# $assignmentRelease[$i] $assignmentDue[$i]\n";
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
	my $regexPattern = "[0-9]{3}.[0-9]{3}(\/[0-9]{3})?";
	foreach my $line (@lotsOfLines) {
	    if ($line =~ /$regexPattern/) { 
		#Returning $line gives the line with the course number now try and only get the numbers
		my ($justCourse) = $line =~ /$regexPattern/i;	
		return $&;
	    }
    	}
	return $ERROR; 
    }

    ##
    #Finds class="table--primaryLink"
    #Checks if the next line has aria-label="View ... 
    #if so then the Assignemnt name is the word after View. 
    #OR
    #if not then the next line is the assignment name. 
    #
    # I KNOW THIS IS RETURN WHAT I WANT IT TO . IE ONLY THE ASSIGNMENT NAME
    ##
    sub getAssignment {
    	my($count, $line, @completeHtm) = @_; 
	#print "$_[0] is the count";
	my $primaryRegexPattern = "table--primaryLink"; 
	my $assignmentSubmittedPattern = "a aria-label=\"View";     
	    if ($line =~/$primaryRegexPattern/) {
		if($completeHtm[$count+1] =~ /$assignmentSubmittedPattern/) {
		    my ($justAssignment) = $completeHtm[$count+1] =~ /View [A-Za-z0-9]+/i;
		    return substr($&, 5);
		} else {
		    my ($justAssignment) = $completeHtm[$count+1] =~ /[A-Za-z0-9 ]+/i;
		    return $&;
		}
	    }		
	return $ERROR;
    }

    ##
    #find submissionStatus--Score OR submissionStatus--text
    #next line is the grade or word No Submission
    ##
    sub getGrades {
	my($count, $line, @completeHtm) = @_;
	if($line =~/$REGEXNOGRADES/) {return 0;}
	elsif($line =~/$REGEXGRADES/){
	    my($result) = $completeHtm[$count+1] =~ /[0-9]*.?[0-9]*\s\/\s[0-9]*.?[0-9]*/gi;
	    return $&;
	} else {return $ERROR; }
	#return getInfo($count, "grades", $line, @completeHtm); 
    }
    ##
    #Returns a due Date if param 1 is 'due'
    #returns a release date if param 1 is 'release'
    ##
    sub getInfo {
	my($count, $type, $line, @completeHtm) = @_;
	my $primaryRegexPattern;
	if( $type eq "due") {
	    $primaryRegexPattern = $REGEXDUE;
	}elsif( $type eq "release") {
	    $primaryRegexPattern = $REGEXRELEASE;
	
	}elsif($type eq "grades") {
		$primaryRegexPattern = $REGEXGRADES; 

	} else { return $ERROR; }
	if ($line =~/$REGEXNOGRADES/) {		return 0;}
	if ($line =~/$primaryRegexPattern/) {
	    my($result) = $completeHtm[$count+1] =~ /[^<>]/i;
		return $&;

	}

	
    }
    ##
    #submissionTimeChart--releaseDate
    #next line will hold the release date
    ##
    sub getRelease{

	my($count, $line, @completeHtm) = @_;

	return getInfo($count, "release", $line, @completeHtm); 
    }
    ##
    #submissionTimeChart--dueDate
    ##
    sub getDue {
	my($count, $line, @completeHtm) = @_;

	return getInfo($count, "due", $line, @completeHtm);
    }

