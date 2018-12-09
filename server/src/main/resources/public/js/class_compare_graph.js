var assignments = ['Assignment 1', 'Assignment 2', 'Assignment 3'];
var userScores = [76, 85, 80];
var classScores = [75.23, 82.13, 84.45];

var userData = {
    x: assignments,
    y: userScores,
    name: 'Your Scores',
    type: 'bar',
    text: userScores.map(String),
    textposition: 'auto'
};
var classData = {
    x: assignments,
    y: classScores,
    name: 'Class Scores',
    type: 'bar',
    text: classScores.map(String),
    textposition: 'auto'
};

var data = [userData, classData];

var layout = {barmode: 'group'};

Plotly.newPlot('classCompareDiv', data, layout);