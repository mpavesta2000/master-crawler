/*
Template Name: tinnnews - Admin & Dashboard Template
Author: Themesbrand
Website: https://Themesbrand.com/
Contact: Themesbrand@gmail.com
File: Editor Analytics Dashboard init js
*/

// get colors array from the string
function getChartColorsArray(chartId) {
    if (document.getElementById(chartId) !== null) {
        var colors = document.getElementById(chartId).getAttribute("data-colors");
        if (colors) {
            colors = JSON.parse(colors);
            return colors.map(function (value) {
                var newValue = value.replace(" ", "");
                if (newValue.indexOf(",") === -1) {
                    var color = getComputedStyle(document.documentElement).getPropertyValue(
                        newValue
                    );
                    if (color) return color;
                    else return newValue;
                } else {
                    var val = value.split(",");
                    if (val.length == 2) {
                        var rgbaColor = getComputedStyle(
                            document.documentElement
                        ).getPropertyValue(val[0]);
                        rgbaColor = "rgba(" + rgbaColor + "," + val[1] + ")";
                        return rgbaColor;
                    } else {
                        return newValue;
                    }
                }
            });
        }
    }
}

// Real data for editor analytics - will be injected from Thymeleaf template
var realEditorData = {
    editorId: null,
    editorName: null,
    myNews: 0,
    myEdits: 0,
    aiUsagePercent: 0,
    avgSeoScore: 0,
    monthlyActivity: {
        labels: [],
        newsCountData: [],
        seoScoreData: []
    },
    recentActions: []
};

// Function to update data from backend
function updateRealData(backendData) {
    if (backendData) {
        realEditorData.myNews = backendData.totalNewsAdded || 0;
        realEditorData.myEdits = backendData.totalEdits || 0;
        realEditorData.aiUsagePercent = backendData.aiAssistancePercentage || 0;
        realEditorData.avgSeoScore = backendData.avgSeoScore || 0; // Use real average from backend
        realEditorData.recentSeoScores = backendData.recentSeoScores || []; // Real SEO scores for sparkline
        realEditorData.monthlyActivity = backendData.chartData || realEditorData.monthlyActivity;
        realEditorData.recentActions = backendData.recentNews || [];
        
        // Store additional data for sparklines
        realEditorData.monthlyEdits = backendData.chartData.editsCountData || [];
        realEditorData.monthlyAiUsage = backendData.chartData.aiUsageData || [];
        
        // Store weekly data
        realEditorData.weeklyActivity = backendData.weeklyData || {
            weekLabels: [],
            weeklyNewsData: [],
            weeklyEditsData: [],
            weeklyChapChinData: [],
            weeklySeoData: []
        };
        
    }
}

// Sparkline charts for stats cards
function renderSparklineCharts() {
    // Daily News Creation Sparkline (Weekly Trend)
    var myNewsColors = getChartColorsArray("my_news_sparkline");
    if (myNewsColors) {
        
        // Use weekly data if available, otherwise fall back to monthly data sliced to 7 days
        let chartData = [];
        if (realEditorData.weeklyActivity && realEditorData.weeklyActivity.weeklyNewsData.length > 0) {
            chartData = realEditorData.weeklyActivity.weeklyNewsData;
        } else if (realEditorData.monthlyActivity && realEditorData.monthlyActivity.newsCountData.length > 0) {
            chartData = realEditorData.monthlyActivity.newsCountData.slice(-7);
        } else {
            chartData = [0, 1, 0, 2, 1, 0, 1]; // fallback mock data
        }
        
        // Ensure all values are valid numbers
        chartData = chartData.map(val => val != null && !isNaN(val) ? Number(val) : 0);
        
        var options = {
            series: [{
                data: chartData
            }],
            chart: {
                type: 'line',
                width: 80,
                height: 35,
                sparkline: {
                    enabled: true
                }
            },
            stroke: {
                curve: 'smooth',
                width: 2
            },
            colors: myNewsColors,
            tooltip: {
                fixed: {
                    enabled: false
                },
                x: {
                    show: false
                },
                y: {
                    title: {
                        formatter: function (seriesName) {
                            return 'اخبار: '
                        }
                    }
                },
                marker: {
                    show: false
                }
            }
        };
        var chartElement = document.querySelector("#my_news_sparkline");
        if (chartElement) {
            var chart = new ApexCharts(chartElement, options);
            chart.render();
        }
    }

    // My Edits Sparkline
    var myEditsColors = getChartColorsArray("my_edits_sparkline");
    if (myEditsColors) {
        var options = {
            series: [{
                data: realEditorData.monthlyEdits.length > 0 
                    ? realEditorData.monthlyEdits.slice(-7) 
                    : [10, 20, 14, 27, 19, 35, 28]
            }],
            chart: {
                type: 'line',
                width: 80,
                height: 35,
                sparkline: {
                    enabled: true
                }
            },
            stroke: {
                curve: 'smooth',
                width: 2,
            },
            colors: myEditsColors,
            tooltip: {
                fixed: {
                    enabled: false
                },
                x: {
                    show: false
                },
                y: {
                    title: {
                        formatter: function (seriesName) {
                            return ''
                        }
                    }
                },
                marker: {
                    show: false
                }
            }
        };
        var chartElement = document.querySelector("#my_edits_sparkline");
        if (chartElement) {
            var chart = new ApexCharts(chartElement, options);
            chart.render();
        }
    }

    // My AI Usage Sparkline
    var myAiUsageColors = getChartColorsArray("my_ai_usage_sparkline");
    if (myAiUsageColors) {
        var options = {
            series: [{
                data: realEditorData.monthlyAiUsage.length > 0 
                    ? realEditorData.monthlyAiUsage.slice(-7) 
                    : [5, 12, 8, 17, 14, 22, 18]
            }],
            chart: {
                type: 'line',
                width: 80,
                height: 35,
                sparkline: {
                    enabled: true
                }
            },
            stroke: {
                curve: 'smooth',
                width: 2,
            },
            colors: myAiUsageColors,
            tooltip: {
                fixed: {
                    enabled: false
                },
                x: {
                    show: false
                },
                y: {
                    title: {
                        formatter: function (seriesName) {
                            return ''
                        }
                    }
                },
                marker: {
                    show: false
                }
            }
        };
        var chartElement = document.querySelector("#my_ai_usage_sparkline");
        if (chartElement) {
            var chart = new ApexCharts(chartElement, options);
            chart.render();
        }
    }

    // My SEO Score Sparkline
    var mySeoScoreColors = getChartColorsArray("my_seo_score_sparkline");
    if (mySeoScoreColors) {
        var options = {
            series: [{
                data: realEditorData.recentSeoScores.length > 0 
                    ? realEditorData.recentSeoScores 
                    : [65, 72, 68, 85, 78, 91, 83]
            }],
            chart: {
                type: 'line',
                width: 80,
                height: 35,
                sparkline: {
                    enabled: true
                }
            },
            stroke: {
                curve: 'smooth',
                width: 2,
            },
            colors: mySeoScoreColors,
            tooltip: {
                fixed: {
                    enabled: false
                },
                x: {
                    show: false
                },
                y: {
                    title: {
                        formatter: function (seriesName) {
                            return ''
                        }
                    }
                },
                marker: {
                    show: false
                }
            }
        };
        var chartElement = document.querySelector("#my_seo_score_sparkline");
        if (chartElement) {
            var chart = new ApexCharts(chartElement, options);
            chart.render();
        }
    }
}

// Function to render main charts (weekly activity and performance donut)
function renderMainCharts() {
    // Weekly Activity Chart
    var weeklyActivityColors = getChartColorsArray("weekly_activity_chart");
    if (weeklyActivityColors) {
        // Prepare chart data with debugging
        var newsData = realEditorData.weeklyActivity.weeklyNewsData.length > 0 
            ? realEditorData.weeklyActivity.weeklyNewsData 
            : [5, 8, 3, 7, 6, 4, 9];
            
        var editsData = realEditorData.weeklyActivity.weeklyEditsData.length > 0 
            ? realEditorData.weeklyActivity.weeklyEditsData 
            : [3, 6, 2, 5, 4, 3, 7];
            
        var chapChinData = realEditorData.weeklyActivity.weeklyChapChinData.length > 0 
            ? realEditorData.weeklyActivity.weeklyChapChinData 
            : [1, 3, 1, 4, 2, 1, 3];
        
        
        var options = {
            series: [{
                name: 'اخبار اضافه شده',
                data: newsData
            }, {
                name: 'ویرایش‌ها',
                data: editsData
            }, {
                name: 'چپ چین استفاده شده',
                data: chapChinData
            }],
            chart: {
                type: 'line',
                height: 350,
                toolbar: {
                    show: true
                }
            },
            colors: ['#8E0100', '#6c757d', '#ffc107'], // Red for news, grey for edits, yellow for ChapChin
            dataLabels: {
                enabled: false
            },
            stroke: {
                curve: 'smooth',
                width: 3
            },
            xaxis: {
                categories: realEditorData.weeklyActivity.weekLabels.length > 0 
                    ? realEditorData.weeklyActivity.weekLabels 
                    : ['شنبه', 'یک‌شنبه', 'دوشنبه', 'سه‌شنبه', 'چهارشنبه', 'پنج‌شنبه', 'جمعه']
            },
            grid: {
                borderColor: '#f1f1f1',
            },
            legend: {
                position: 'top'
            },
            tooltip: {
                y: {
                    formatter: function (val) {
                        return val + " عدد"
                    }
                }
            }
        };

        var chartElement = document.querySelector("#weekly_activity_chart");
        if (chartElement) {
            var chart = new ApexCharts(chartElement, options);
            chart.render();
        }
    }

    // Performance Donut Chart
    var performanceDonutColors = getChartColorsArray("performance_donut_chart");
    if (performanceDonutColors) {
        // Calculate activity breakdown percentages
        let totalNews = realEditorData.myNews || 0;
        let totalEdits = realEditorData.myEdits || 0;
        let totalAI = Math.round((totalNews * realEditorData.aiUsagePercent) / 100) || 0;
        
        let totalActivities = totalNews + totalEdits + totalAI;
        
        
        let newsPercent = totalActivities > 0 ? ((totalNews / totalActivities) * 100).toFixed(1) : 0;
        let editsPercent = totalActivities > 0 ? ((totalEdits / totalActivities) * 100).toFixed(1) : 0;
        let aiPercent = totalActivities > 0 ? ((totalAI / totalActivities) * 100).toFixed(1) : 0;
        
        // For users with no activity, show zeros (no mock data)
        if (totalActivities === 0) {
            newsPercent = 0;
            editsPercent = 0;
            aiPercent = 0;
        }
        
        var chartSeries = [parseFloat(newsPercent), parseFloat(editsPercent), parseFloat(aiPercent)];
        
        var options = {
            series: chartSeries,
            chart: {
                type: 'donut',
                height: 240
            },
            labels: ['اخبار اضافه شده', 'ویرایش‌ها', 'استفاده از AI'],
            colors: ['#6c757d', '#8E0100', '#ffc107'], // Grey for news, Red for edits, Yellow for AI
            plotOptions: {
                pie: {
                    donut: {
                        size: '70%'
                    }
                }
            },
            dataLabels: {
                enabled: false
            },
            legend: {
                show: false
            },
            tooltip: {
                y: {
                    formatter: function (val) {
                        return val + "%"
                    }
                }
            }
        };

        var chartElement = document.querySelector("#performance_donut_chart");
        if (chartElement) {
            // Clear any existing chart content
            chartElement.innerHTML = '';
            
            var chart = new ApexCharts(chartElement, options);
            chart.render().then(function() {
                // Update the percentage labels below the chart
                const newsPercentageElement = document.getElementById('news-percentage');
                const editsPercentageElement = document.getElementById('edits-percentage');
                const aiPercentageElement = document.getElementById('ai-percentage');
                
                if (newsPercentageElement) newsPercentageElement.textContent = newsPercent + '%';
                if (editsPercentageElement) editsPercentageElement.textContent = editsPercent + '%';
                if (aiPercentageElement) aiPercentageElement.textContent = aiPercent + '%';
            });
        }
    }
}

// Function to render recent actions table
function renderRecentActionsTable() {
    if (document.getElementById("recent-actions-table")) {
        
        new gridjs.Grid({
            columns: [
                {
                    name: 'عنوان',
                    formatter: (cell) => gridjs.html(`<span class="fw-medium">${cell}</span>`)
                },
            {
                name: 'وضعیت ارسال',
                formatter: (cell) => {
                    let badgeClass = 'badge-soft-secondary';
                    if (cell === 'ارسال شده به تین نیوز') {
                        badgeClass = 'badge-soft-success';
                    } else if (cell === 'ارسال نشده') {
                        badgeClass = 'badge-soft-warning';
                    }
                    return gridjs.html(`<span class="badge ${badgeClass}">${cell}</span>`);
                }
            },
                'تاریخ',
                {
                    name: 'امتیاز SEO',
                    formatter: (cell) => {
                        let badgeClass = 'badge-soft-danger';
                        if (cell >= 80) badgeClass = 'badge-soft-success';
                        else if (cell >= 60) badgeClass = 'badge-soft-warning';
                        return gridjs.html(`<span class="badge ${badgeClass}">${cell}</span>`);
                    }
                }
            ],
            data: realEditorData.recentActions.length > 0 
                ? realEditorData.recentActions.map(item => [
                    item.title || 'عنوان نامشخص',
                    item.sendToTinn ? 'ارسال شده به تین نیوز' : 'ارسال نشده',
                    item.createdAt ? new Date(item.createdAt).toLocaleDateString('fa-IR') : 'تاریخ نامشخص',
                    item.yoastSeoPoint || 0
                ])
                : [], // Empty table when no news
            sort: true,
            search: true,
            pagination: {
                limit: 5
            },
            language: {
                search: {
                    placeholder: ' جستجو...'
                },
                pagination: {
                    previous: 'قبلی',
                    next: 'بعدی',
                    showing: 'نمایش',
                    of: 'از',
                    to: 'تا',
                    results: () => 'نتایج'
                },
                noRecordsFound: 'هیچ رکوردی یافت نشد',
                error: 'خطا در بارگذاری اطلاعات'
            }
        }).render(document.getElementById("recent-actions-table"));
    }
}

// Initialize all charts
document.addEventListener('DOMContentLoaded', function() {
    // Charts will be initialized when real data is loaded from backend
});

// Function to initialize analytics with real data (called from HTML template)
function initializeEditorAnalytics(backendData) {
    // Update real data from backend
    updateRealData(backendData);
    
    // Update the statistics in the UI
    updateStatisticsDisplay();
    
    // Clear any existing charts before rendering new ones
    clearExistingCharts();
    
    // Re-render all charts with new data
    renderSparklineCharts();
    renderMainCharts();
    
    // Re-render table if needed
    renderRecentActionsTable();
}

// Function to clear existing charts to prevent overlapping
function clearExistingCharts() {
    // Clear chart containers
    const chartContainers = [
        '#my_news_sparkline',
        '#my_edits_sparkline', 
        '#my_ai_usage_sparkline',
        '#my_seo_score_sparkline',
        '#weekly_activity_chart',
        '#performance_donut_chart'
    ];
    
    chartContainers.forEach(container => {
        const element = document.querySelector(container);
        if (element) {
            element.innerHTML = '';
        }
    });
}

// Function to update statistics display
function updateStatisticsDisplay() {
    // The counter values are updated via Thymeleaf th:data-target attributes
    // This function can be extended for additional dynamic updates if needed
}
