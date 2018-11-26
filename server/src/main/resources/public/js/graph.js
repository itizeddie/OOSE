window.onload = function() {
var x = [15, 25, 25, 30, 30, 30, 32.5, 35, 35, 35, 37.5, 37.5, 40, 40, 40, 40, 40, 45, 45, 45, 45, 45, 47.5, 47.5, 47.5, 50, 50];
var sum = 0, avg = 0;
for(var i = 0; i < x.length; i++) {
    sum += x[i];
}
avg = (sum / x.length).toFixed(2);
var mean = "Average: " + avg.toString() + "     ";
var percentile = "Percentile: 80";
var toc = "Avg Completion Time: 3.5 hours";
var rec = "Estimated Completion Time: 4.5 hours";
var trace = {
    x: x,
    type: 'histogram',
  };
var data = [trace];
var layout = {
    xaxis: {
        range: [0, 50]
    }
}

Plotly.newPlot('myDiv', data, layout);
}
function details() {
var details = "(Details of assignment including title, description, etc.)"
}
