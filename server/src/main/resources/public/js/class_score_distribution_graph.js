var classAssignment1Scores = {
  y: [15, 25, 25, 30, 30, 30, 32.5, 35, 35, 35, 37.5, 37.5, 40, 40, 40, 40, 40, 45, 45, 45, 45, 45, 47.5, 47.5, 47.5, 50, 50],
  name: 'Assignment 1',
  type: 'box',
  marker: {
    color: 'rgb(10,140,208)'
  },
  boxmean: 'sd'
};
var classAssignment2Scores = {
  y: [10, 15, 25, 25, 25, 30, 32.5, 32.5, 37.5, 35, 37.5, 37.5, 42.5, 42.5, 40, 40, 40, 45, 47.5, 47.5, 45, 45, 47.5, 47.5, 47.5, 47.5, 50],
  name: 'Assignment 2',
  type: 'box',
  marker: {
    color: 'rgb(100,80,230)'
  },
  boxmean: 'sd'
};
var classAssignment3Scores = {
  y: [20, 20, 20, 20, 20, 30, 32.5, 35, 35, 35, 37.5, 37.5, 40, 40, 42.5, 42.5, 42.5, 45, 45, 45, 50, 50, 50, 50, 50, 50, 50],
  name: 'Assignment 3',
  type: 'box',
  marker: {
    color: 'rgb(190,100,70)'
  },
  boxmean: 'sd'
};

var classAssignmentData = [classAssignment1Scores, classAssignment2Scores, classAssignment3Scores];

var classAssignmentScoresLayout = {
  title: 'Class Score Distribution'
};

Plotly.newPlot('classAssignmentScoresDistribution', classAssignmentData, classAssignmentScoresLayout);

