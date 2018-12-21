# Iteration 6 Evaluation - Group 11 Calendue (Yash)

### Presentation Style [15 points]
(Was the presentation itself done well - was it well-organized, easy to understand, and convincing?)

The presentation didn’t satisfy our expectations. For example, when showing code, the slide was a wall of text, and it wasn’t well explained. Also, it wasn’t good code. For example, it was using old JavaScript APIs for HTTP requests (XHR, instead of `fetch`, as we covered in class extensively). One other code snippet had a clear smell that was even a question in a homework (`if (a == "something") doSomething(); else if (a == "something-else") doSomethingElse(); ...`).

Don't show sketches in the slides, show screenshots or even better show us in the app.

The server code you showed was good. The `Validator` and `"flash"` seemed to be well factored.

**REDEMO**: The demo worked and the promised features were there. The application still wasn’t polished, however. For example, there was an URL ending `.php`, but the project isn’t in PHP.

-4

### App demo [35 points]
(Did app work well?  Did any errors arise during the demo?  How did the demo'd implementation line up with features proposed? Was it a live demo?)

Scraping gave a 500 error during demo, it’s not working.  Should have at least showed that scraper was working.

The plots you showed were **very** basic, only including things like bar graphs with basic statistics (median, average, and so forth).

You need some more data in database to really show predicter is working.

-5

### UI quality based on the demo [10 points]
(Was it well-designed? Was it usable?  Did it have glitches?)

Nice views of Gradescope data.  It was still too simple for the scope of project we were looking for.


### Testing and Coverage [5 points]
(Continue to write more tests for new features and keep high coverage?)

Done, despite the TravisCI build queue issues.

### Build/Package/Deploy and git usage [10 points]
(Same as previous iterations)

Done

### Non-CRUD feature [10 points]
(Did this make it across the finish line as a usable feature in the final app?)

The non-CRUD feature the group had initially proposed was to aggregate data from all students and use it to correlate the grades with the time spent into the assignments. Also, predict how long future assignments could take.

But the only non-CRUD feature left in the end is the Gradescope scraping. This was over-engineered and done poorly. Over-engineered because the Firefox extension sends raw HTML for the server to parse, and the server parses it using a Perl subprocess for what could easily be done in Java. Done poorly because the data is extracted from HTML using [regular expressions](https://stackoverflow.com/questions/1732348/regex-match-open-tags-except-xhtml-self-contained-tags/1732454#1732454) instead of a parser. Parsing the data directly on the Firefox extension using JavaScript would have made more sense and would have been a lot easier. Or doing the whole capturing on the server-side, just asking for the user credentials.

-4

### Iteration progress reporting [5 points]
(Final CHANGELOG.md and project boards updated?)

Done

### Evaluation of project difficulty [10 points]
(in terms of lines of code, conceptual difficulty, non-CRUD features, degree of completion)

### Overall Remarks

You guys didn't quite get it across the finish line.

**Grade: 87/100**

