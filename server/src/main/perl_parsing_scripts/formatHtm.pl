use strict;
use warnings;

##
# Given a .htm file this will insert newlines after every brackets
# which makes it easier to read and parse later
#
##

my $fileToFormat = $ARGV[0];
open my $input,  '<',  $fileToFormat      or die "Can't read old file: $!";
open my $out, '>', "$fileToFormat.new" or die "Can't write new file: $!";


my @allLines = do { local $/; <$input> }; #slurp up file into array

print $out "Formated with formatHtm.pl\n";

foreach my $line (@allLines)
    {
    my @splitLine = split( /[<>]+/, $line); 
        foreach my $word (@splitLine) {   
	print $out "<$word>\n";
        }
    }

close $out; 
