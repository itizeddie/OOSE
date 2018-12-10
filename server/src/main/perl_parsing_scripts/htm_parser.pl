


use strict;
use warnings;
use v5.10;

    my @allLines = <STDIN>;

    #Expected tag for files that are formated with formatHtm.pl
    my $formatTag = 	"Formated with formatHtm.pl\n";
    ## Final regex patterns

    my $REGEXDUE =	    "submissionTimeChart--dueDate";
    my $REGEXRELEASE =	"submissionTimeChart--releaseDate";
    my $REGEXNOGRADES =	"submissionStatus--text";
    my $REGEXGRADES = 	"submissionStatus--score";
    my $ERROR = -1;

    ##
    #Can and will just make an Assignment Object and use
    #name, grade, release, and due, timeTakeToComplete as values for it. 
    ##
    my $courseNumber;
    my @assignmentNames; 
    my @assignmentGrades;
    my @assignmentRelease; 
    my @assignmentDue; 

    my $assignmentCount = 0;
    my $lineCount = 0; 



    ##check if input file is formatted correctly
    if(!isFileFormatted($allLines[0])){
	die ".htm was not formatted with formatHtm.pl: $!";
    }

    $courseNumber = getCourseNumber(@allLines);
    print "$courseNumber\n";
    if($courseNumber eq $ERROR) { die "Can't read course number: $!";}
    my $temp;

    foreach my $line (@allLines) {

	    if(($temp =  getAssignment($lineCount, $line, @allLines)) ne $ERROR)
	        {$assignmentNames[$assignmentCount] = $temp;$assignmentCount++;}
	    elsif(($temp = getGrades($lineCount,$line, @allLines)) ne $ERROR)
	        {$assignmentGrades[$assignmentCount] = $temp;}
	    elsif(($temp = getRelease($lineCount, $line, @allLines)) ne $ERROR) {
	        $assignmentRelease[$assignmentCount] = $temp;}
	    elsif(($temp = getDue($lineCount, $line, @allLines)) ne $ERROR) {
       	    $assignmentDue[$assignmentCount] = $temp;}
	    $lineCount++;
    }
    ##
    #These two checks are only needed for Release and Due
    #dates because some assignments can not have a Release
    #and Due date like Final, Quiz, and Midterms
    ##
    for(my $i = 1; $i < $assignmentCount+1; $i++) {
    	if (!defined $assignmentRelease[$i]) {
	        $assignmentRelease[$i] = 'na';
	    }
	    if (!defined $assignmentDue[$i]) {
	        $assignmentDue[$i] = 'na';
	    }
    }
    ##
    #Printing to stdout file to be read in by java and placed into database
    ##
    for( my $i = 0; $i < $assignmentCount; $i++) {
	print  "$assignmentNames[$i], $assignmentGrades[$i+1], $assignmentRelease[$i+1], $assignmentDue[$i+1]\n";
    }
    exit;
#END#OF#MAIN##END#OF#MAIN##END#OF#MAIN##END#OF#MAIN##END#OF#MAIN##END#OF#MAIN##END#OF#MAIN##END#OF#MAIN##END#OF#MAIN#

#####################################################################################################################
#FUNCTIONS##FUNCTIONS##FUNCTIONS##FUNCTIONS##FUNCTIONS##FUNCTIONS##FUNCTIONS##FUNCTIONS##FUNCTIONS##FUNCTIONS##FUNCTI
#BELOW##BELOW#BELOW##BELOW##BELOW##BELOW##BELOW##BELOW##BELOW##BELOW##BELOW##BELOW##BELOW##BELOW##BELOW##BELOW##BELOW


    sub isFileFormatted
    {
	my @params = @_;  
	    if( $params[0] eq $formatTag )
	    {
		    return 1;
	    }else {
		    return 0;
	    }
    }
    ##
    #Use a regex to find Gradescope [anything] [3numbers . 3 numbers]anything Dashboard
    #return 3 numbers.3numbers + anything up until a space
    ##
    sub getCourseNumber
    {
    	my @lotsOfLines = @_;

	    my $regexPattern = "[0-9]{3}.[0-9]{3}(\/[0-9]{3})?";
	    foreach my $line ( @lotsOfLines )
	    {
	        if ( $line =~ /$regexPattern/ )
	        {
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
    #if so then the Assignment name is the word after View.
    #OR
    #if not then the next line is the assignment name. 
    #
    # I KNOW THIS IS RETURN WHAT I WANT IT TO . IE ONLY THE ASSIGNMENT NAME
    ##
    sub getAssignment
    {
        my($count, $line, @completeHtm) = @_;
	    my $primaryRegexPattern = "table--primaryLink";
	    my $assignmentSubmittedPattern = "a aria-label=\"View";
	    if ( $line =~ /$primaryRegexPattern/ )
	    {
		    if( $completeHtm[$count+1] =~ /$assignmentSubmittedPattern/ )
		    {
		        my ($justAssignment) = $completeHtm[$count+1] =~ /View [^\"|\n]*/i;
		        return substr($&, 5); #removes word view
		    }
		    else #might not need this# experiment with this later
		    {
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
    sub getGrades
    {
	my ($count, $line, @completeHtm) = @_;
	    if ($line =~ /$REGEXNOGRADES/ )
	    {
	    return 0;
	    }
	    elsif ( $line =~ /$REGEXGRADES/ )
	    {
	        my($result) = $completeHtm[$count+1] =~ /[0-9]*.?[0-9]*\s\/\s[0-9]*.?[0-9]*/gi;
	        return $&;
	    }
	    else
	    {
	        return $ERROR;
	    }
	}

    ##
    #submissionTimeChart--releaseDate
    #next line will hold the release date
    ##
    sub getRelease
    {
	    my ($count, $line, @completeHtm) = @_;
	    if ( $line =~ /$REGEXRELEASE/ )
	    {
	        my($result) = $completeHtm[$count+1] =~ /[A-Z][a-z]{2} [0-9]{2}/gi;
	        return $&;
	    }
	    else
	    {
	        return $ERROR;
	    }
	}

    ##
    #submissionTimeChart--dueDate
    ##
    sub getDue
    {
	    my ( $count, $line, @completeHtm ) = @_;
	    if ( $line =~ /$REGEXDUE/ )
	    {
	        my($result) = $completeHtm[$count+1] =~ /[A-Z][a-z]{2} [0-9]{2}/gi;
	        return $&;
	    } else
	    {
	        return $ERROR;
	    }
    }



