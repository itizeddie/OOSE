use strict;
use warnings;

    ##
    # Given a string reprensentation of a HTML
    # Format and print string to file formated
    #
    ##
    my $outputName = "formated";
    #my @allLines = $ARGV[0];
    my @allLines = <STDIN>;
    #open my $input,  '<',  $fileToFormat       or die "Can't read old file: $!";
    #open my $out, '>', $outputName		or die "Can't write new file: $!";

    #used if input is the name of a .htm file
    #my @allLines = do { local $/; <$input> }; #slurp up file into array
    #print $out
    print "Formated with formatHtm.pl\n";

    foreach my $line (@allLines){
    my @splitLine = split( /[<>]+/, $line); 
        
	foreach my $word (@splitLine) {  
	    #print $out "<$word>\n";
	    print "<$word>\n";
        }
    }

    #close $out;
