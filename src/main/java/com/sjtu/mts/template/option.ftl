{
title: {
text: '${title}',
left: 'center',
},
xAxis: {
type: 'category',
boundaryGap: false,
data: ${xValue},
},
yAxis: { type: 'value' },
series: [{
data: ${yValue},
type: 'line',
areaStyle: {},
label: {
'normal': {
'show': true,
'textStyle': {
'fontSize': 16 },
},
'emphasis': {
'show': true,
},
},
smooth: 0.6,
}],
}