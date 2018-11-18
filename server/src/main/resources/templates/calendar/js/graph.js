var x = [0, 10, 20, 30, 40, 50];
var y = [0, 1, 2, 7, 11, 14];
var trace = {
    x: x,
    y: y,
    type: 'histogram',
  };
var data = [trace];
Plotly.newPlot('assignment', data);