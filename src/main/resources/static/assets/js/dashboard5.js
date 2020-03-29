/*
Template Name: Material Pro Admin
Author: Themedesigner
Email: niravjoshi87@gmail.com
File: js
*/
$(function () {
    "use strict";
    
    // ============================================================== 
    // Realtime chart
    // ============================================================== 
    var data = []
        , totalPoints = 300;

    function getRandomData() {
        if (data.length > 0) data = data.slice(1);
        // Do a random walk
        while (data.length < totalPoints) {
            var prev = data.length > 0 ? data[data.length - 1] : 50
                , y = prev + Math.random() * 10 - 5;
            if (y < 0) {
                y = 0;
            }
            else if (y > 100) {
                y = 100;
            }
            data.push(y);
        }
        // Zip the generated y values with the x values
        var res = [];
        for (var i = 0; i < data.length; ++i) {
            res.push([i, data[i]])
        }
        return res;
    }
    // Set up the control widget
    var updateInterval = 250;
    $("#updateInterval").val(updateInterval).change(function () {
        var v = $(this).val();
        if (v && !isNaN(+v)) {
            updateInterval = +v;
            if (updateInterval < 1) {
                updateInterval = 1;
            }
            else if (updateInterval > 3000) {
                updateInterval = 3000;
            }
            $(this).val("" + updateInterval);
        }
    });
    var plot = $.plot("#placeholder", [getRandomData()], {
        series: {
            shadowSize: 0 // Drawing is faster without shadows
        }
        , yaxis: {
            min: 0
            , max: 100
        }
        , xaxis: {
            show: false
        }
        , colors: ["#26c6da"]
        , grid: {
            color: "#AFAFAF"
            , hoverable: true
            , borderWidth: 0
            , backgroundColor: '#FFF'
        }
        , tooltip: true
        , tooltipOpts: {
            content: "Visit: %y"
            , defaultTheme: false
        }
    });
    $(window).resize(function () {
        $.plot($('#placeholder'), data);
    });

    function update() {
        plot.setData([getRandomData()]);
        // Since the axes don't change, we don't need to call plot.setupGrid()
        plot.draw();
        setTimeout(update, updateInterval);
    }
    update();
    // ============================================================== 
    // Ample vs Pixel
    // ============================================================== 
    new Chartist.Line('.andvios', {
        labels: ['0', '4', '8', '12', '16', '20', '24', '30', '16', '20', '24', '30', '34', '38', '42', '46', '50', '54']
        , series: [
        [0, 4, 3, 24, 9, 10, 18, 15, 44, 17, 19, 26, 31, 8, 37, 10, 24, 51]
        , [0, 1, 1, 10, 24, 6, 12, 4, 21, 15, 44, 24, 28, 4, 10, 21, 5, 47]
      ]
    }, {
        low: 0
        , showArea: true
        , fullWidth: true
        , chartPadding: 30
         
        , axisX: {
            showLabel: true
            , divisor: 2
            
            , showGrid: false
            , offset: 50
        }
        , plugins: [
        Chartist.plugins.tooltip()
      ], // As this is axis specific we need to tell Chartist to use whole numbers only on the concerned axis
        axisY: {
            onlyInteger: true
            , showLabel: true
            , scaleMinSpace: 50 
            , showGrid: true
            , offset: 10
        }
    });
    // ============================================================== 
    // Badnwidth usage
    // ============================================================== 
    new Chartist.Line('.usage', {
        labels: ['0', '4', '8', '12', '16', '20', '24', '30']
        , series: [
        [5, 0, 12, 1, 8, 3, 12, 15]

      ]
    }, {
        high: 13
        , low: 0
        , showArea: true
        , fullWidth: true
        , plugins: [
        Chartist.plugins.tooltip()
      ], // As this is axis specific we need to tell Chartist to use whole numbers only on the concerned axis
        axisY: {
            onlyInteger: true
            , offset: 20
            , showLabel: false
            , showGrid: false
            , labelInterpolationFnc: function (value) {
                return (value / 1) + 'k';
            }
        }
        , axisX: {
            showLabel: false
            , divisor: 2
            , showGrid: false
            , offset: 0
        }
    });
    // ============================================================== 
    // Download count
    // ============================================================== 
    var sparklineLogin = function () {
        $('.spark-count').sparkline([4, 5, 0, 10, 9, 12, 4, 9, 4, 5, 3, 10, 9, 12, 10, 9, 12, 4, 9], {
            type: 'bar'
            , width: '100%'
            , height: '100'
            , barWidth: '4'
            , resize: true
            , barSpacing: '5'
            , barColor: 'rgba(255, 255, 255, 0.3)'
        });
    }
    var sparkResize;
    $(window).resize(function (e) {
        clearTimeout(sparkResize);
        sparkResize = setTimeout(sparklineLogin, 500);
    });
    sparklineLogin();
    // ============================================================== 
    // Download count
    // ============================================================== 
    new Chartist.Bar('.download-state', {
        labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        , series: [
        [5, 4, 3, 7, 5, 10, 3]
        , [3, 2, 9, 5, 4, 6, 4]
      ]
    }, {
        high: 11
        , low: 0
        , showArea: true
        , seriesBarDistance: 10
        , fullWidth: true
        , plugins: [
        Chartist.plugins.tooltip()
      ]
        , axisX: {
            // On the x-axis start means top and end means bottom
            showGrid: false
        }
    , }, {});
});
// ============================================================== 
// doughnut chart option
// ============================================================== 
var doughnutChart = echarts.init(document.getElementById('m-piechart'));
// specify chart configuration item and data
option = {
    tooltip: {
        trigger: 'item'
        , formatter: "{a} <br/>{b} : {c} ({d}%)"
    }
    , legend: {
        orient: 'horizontal'
        , x: 'center'
        , show: false
        , y: 'bottom'
        , data: ['80', '60', '20', '140']
    }
    , toolbox: {
        show: false
        , feature: {
            dataView: {
                show: true
                , readOnly: false
            }
            , magicType: {
                show: false
                , type: ['pie', 'funnel']
                , option: {
                    funnel: {
                        x: '25%'
                        , width: '50%'
                        , funnelAlign: 'center'
                        , max: 1548
                    }
                }
            }
            , restore: {
                show: true
            }
            , saveAsImage: {
                show: true
            }
        }
    }
    , color: ["#745af2", "#f62d51", "#26c6da", "#dadada"]
    , calculable: true
    , series: [
        {
            name: 'Source'
            , type: 'pie'
            , radius: ['80%', '90%']
            , itemStyle: {
                normal: {
                    label: {
                        show: false
                    }
                    , labelLine: {
                        show: false
                    }
                }
                , emphasis: {
                    label: {
                        show: true
                        , position: 'center'
                        , textStyle: {
                            fontSize: '30'
                            , fontWeight: 'bold'
                        }
                    }
                }
            }
            , data: [
                {
                    value: 335
                    , name: '80%'
                }
                , {
                    value: 110
                    , name: '60%'
                }
                , {
                    value: 234
                    , name: '20%'
                }
                , {
                    value: 300
                    , name: '140%'
                }
                ]
            }
        ]
};
// use configuration item and data specified to show chart
doughnutChart.setOption(option, true), $(function () {
    function resize() {
        setTimeout(function () {
            doughnutChart.resize()
        }, 100)
    }
    $(window).on("resize", resize), $(".sidebartoggler").on("click", resize)
});