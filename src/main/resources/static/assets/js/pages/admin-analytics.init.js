/*
Template Name: tinnnews - Admin & Dashboard Template
Author: Themesbrand
Website: https://Themesbrand.com/
Contact: Themesbrand@gmail.com
File: Admin Analytics Dashboard init js
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
        } else {
            console.warn('data-colors attributes not found on', chartId);
        }
    }
}

// Mock
const mockAdminData = {
    totalNews: 1247,
    totalEdits: 892,
    aiArticles: 456,
    aiPercentage: 37,
    newsPerDay: [
        { date: '2024-01-01', added: 45, edited: 23 },
        { date: '2024-01-02', added: 52, edited: 31 },
        { date: '2024-01-03', added: 38, edited: 19 },
        { date: '2024-01-04', added: 61, edited: 27 },
        { date: '2024-01-05', added: 44, edited: 35 },
        { date: '2024-01-06', added: 33, edited: 18 },
        { date: '2024-01-07', added: 49, edited: 29 }
    ],
    editorContributions: [
        { name: 'علی احمدی', articles: 89, edits: 45 },
        { name: 'سارا محمدی', articles: 76, edits: 52 },
        { name: 'حسن رضایی', articles: 65, edits: 38 },
        { name: 'فاطمه کریمی', articles: 58, edits: 29 },
        { name: 'محمد نوری', articles: 43, edits: 21 }
    ],
    recentEdits: [
        { title: 'اخبار اقتصادی جدید ایران', editor: 'علی احمدی', date: '1403/10/15', type: 'ویرایش' },
        { title: 'گزارش ورزشی از مسابقات', editor: 'سارا محمدی', date: '1403/10/15', type: 'اضافه' },
        { title: 'تحلیل بازار بورس تهران', editor: 'حسن رضایی', date: '1403/10/14', type: 'ویرایش' },
        { title: 'فناوری‌های نوین در ایران', editor: 'فاطمه کریمی', date: '1403/10/14', type: 'اضافه' },
        { title: 'بررسی روند قیمت مسکن', editor: 'محمد نوری', date: '1403/10/13', type: 'ویرایش' }
    ]
};

var realAdminData = {
    totalNewsAdded: 0,
    totalEdits: 0,
    totalChapChinUsage: 0,
    aiPercentage: 0
};

// Function to update real data from backend
function updateAdminData(backendData) {
    if (backendData) {
        realAdminData.totalNewsAdded = backendData.totalNewsAdded || 0;
        realAdminData.totalEdits = backendData.totalEdits || 0;
        realAdminData.totalChapChinUsage = backendData.totalChapChinUsage || 0;
        realAdminData.aiPercentage = backendData.aiPercentage || 0;
        
        // Add weekly data
        if (backendData.weeklyData) {
            realAdminData.weeklyData = {
                weekLabels: backendData.weeklyData.weekLabels || [],
                weeklyNewsData: backendData.weeklyData.weeklyNewsData || [],
                weeklyEditsData: backendData.weeklyData.weeklyEditsData || [],
                weeklyChapChinData: backendData.weeklyData.weeklyChapChinData || []
            };
        }
        
        // Add editor contributions data
        if (backendData.editorContributions) {
            realAdminData.editorContributions = backendData.editorContributions;
        }
        
        // Add recent news data
        if (backendData.recentNewsSimple) {
            realAdminData.recentNewsSimple = backendData.recentNewsSimple;
        }
    }
}

// Sparkline charts for stats cards
function renderSparklineCharts() {
    // Total News Sparkline
    var totalNewsColors = getChartColorsArray("total_news_sparkline");
    if (totalNewsColors) {
        var options = {
            series: [{
                data: [12, 25, 18, 35, 28, 42, realAdminData.totalNewsAdded || 25]
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
            colors: totalNewsColors,
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
        var chart = new ApexCharts(document.querySelector("#total_news_sparkline"), options);
        chart.render();
    }

    // Total Edits Sparkline
    var totalEditsColors = getChartColorsArray("total_edits_sparkline");
    if (totalEditsColors) {
        var options = {
            series: [{
                data: [8, 18, 15, 28, 22, 35, realAdminData.totalEdits || 30]
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
            colors: totalEditsColors,
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
        var chart = new ApexCharts(document.querySelector("#total_edits_sparkline"), options);
        chart.render();
    }

    // AI Articles Sparkline
    var aiArticlesColors = getChartColorsArray("ai_articles_sparkline");
    if (aiArticlesColors) {
        var options = {
            series: [{
                data: [3, 8, 5, 12, 9, 18, realAdminData.totalChapChinUsage || 15]
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
            colors: aiArticlesColors,
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
        var chart = new ApexCharts(document.querySelector("#ai_articles_sparkline"), options);
        chart.render();
    }

    // AI Percentage Sparkline
    var aiPercentageColors = getChartColorsArray("ai_percentage_sparkline");
    if (aiPercentageColors) {
        var options = {
            series: [{
                data: [15, 25, 20, 30, 28, 35, realAdminData.aiPercentage || 25]
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
            colors: aiPercentageColors,
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
        var chart = new ApexCharts(document.querySelector("#ai_percentage_sparkline"), options);
        chart.render();
    }
}

// News Per Day Chart
function renderNewsPerDayChart() {
    var newsPerDayColors = getChartColorsArray("news_per_day_chart");
    if (newsPerDayColors) {
        var options = {
            series: [{
                name: 'اخبار اضافه شده',
                data: realAdminData.weeklyData ? realAdminData.weeklyData.weeklyNewsData : [45, 52, 38, 61, 44, 33, 49]
            }, {
                name: 'تعداد کل اخبار های ویرایش شده',
                data: realAdminData.weeklyData ? realAdminData.weeklyData.weeklyEditsData : [23, 31, 19, 27, 35, 18, 29]
            }, {
                name: 'تعداد کل اخبار چپ چین',
                data: realAdminData.weeklyData ? realAdminData.weeklyData.weeklyChapChinData : [12, 18, 10, 22, 15, 8, 16]
            }],
            chart: {
                type: 'line',
                height: 350,
                toolbar: {
                    show: true
                }
            },
            colors: ['#6c757d', '#8E0100', '#ffc107'], // Grey for news, Red for edits, Yellow for AI
            dataLabels: {
                enabled: false
            },
            stroke: {
                curve: 'smooth',
                width: 3
            },
            xaxis: {
                categories: realAdminData.weeklyData ? realAdminData.weeklyData.weekLabels : ['شنبه', 'یکشنبه', 'دوشنبه', 'سه‌شنبه', 'چهارشنبه', 'پنج‌شنبه', 'جمعه']
            },
            grid: {
                borderColor: '#f1f1f1',
            },
            legend: {
                position: 'top',
                horizontalAlign: 'left',
                offsetX: 0,
                offsetY: 0,
                markers: {
                    width: 12,
                    height: 12,
                    radius: 6
                },
                itemMargin: {
                    horizontal: 15,
                    vertical: 5
                }
            }
        };

        var chart = new ApexCharts(document.querySelector("#news_per_day_chart"), options);
        chart.render();
    }
}

// Editor Contribution Chart
function renderEditorContributionChart() {
    var editorContributionColors = getChartColorsArray("editor_contribution_chart");
    if (editorContributionColors) {
        var contributionsData = realAdminData.editorContributions || mockAdminData.editorContributions;
        
        var options = {
            series: [{
                name: 'اخبار تولید شده',
                data: contributionsData.map(item => item.articles)
            }, {
                name: 'ویرایش‌ها',
                data: contributionsData.map(item => item.edits)
            }],
            chart: {
                type: 'bar',
                height: 350,
                toolbar: {
                    show: true
                }
            },
            colors: editorContributionColors,
            plotOptions: {
                bar: {
                    horizontal: false,
                    columnWidth: '55%',
                    endingShape: 'rounded'
                },
            },
            dataLabels: {
                enabled: false
            },
            stroke: {
                show: true,
                width: 2,
                colors: ['transparent']
            },
            xaxis: {
                categories: contributionsData.map(item => item.name)
            },
            yaxis: {
                title: {
                    text: 'تعداد'
                }
            },
            fill: {
                opacity: 1
            },
            tooltip: {
                y: {
                    formatter: function (val) {
                        return val + " عدد"
                    }
                }
            },
            legend: {
                position: 'top'
            }
        };

        var chart = new ApexCharts(document.querySelector("#editor_contribution_chart"), options);
        chart.render();
    }
}

// Recent News Table
function renderRecentNewsTable() {
    if (document.getElementById("recent-edits-table")) {
        const newsData = realAdminData.recentNewsSimple || [];
        
        new gridjs.Grid({
            columns: [
                {
                    name: 'عنوان خبر',
                    formatter: (cell) => gridjs.html(`<span class="fw-medium">${cell}</span>`)
                },
                'خبرنگار',
                {
                    name: 'تاریخ',
                    formatter: (cell) => {
                        try {
                            const date = new Date(cell);
                            const formattedDate = new Intl.DateTimeFormat('fa-IR', {dateStyle: 'short', calendar: 'persian'}).format(date);
                            return formattedDate;
                        } catch (e) {
                            return cell; // Return original if conversion fails
                        }
                    }
                },
                {
                    name: 'امتیاز SEO',
                    formatter: (cell) => gridjs.html(`<span class="badge badge-soft-info">${cell}</span>`)
                },
                {
                    name: 'ارسال به تین نیوز',
                    formatter: (cell) => {
                        const badgeClass = cell ? 'badge-soft-success' : 'badge-soft-warning';
                        const statusText = cell ? 'ارسال شده به تین نیوز' : 'ارسال نشده';
                        return gridjs.html(`<span class="badge ${badgeClass}">${statusText}</span>`);
                    }
                }
            ],
            data: newsData.length > 0 ? newsData.map(item => [
                item.title,
                item.editorName, 
                item.createdAt,
                item.yoastSeoPoint,
                item.sendToTinn
            ]) : [],
            sort: true,
            search: true,
            pagination: {
                limit: 5,
                summary: true
            },
            language: {
                search: {
                    placeholder: 'جستجو...'
                },
                pagination: {
                    previous: 'قبلی',
                    next: 'بعدی',
                    showing: 'نمایش',
                    of: 'از',
                    to: 'تا',
                    results: 'نتایج'
                },
                loading: 'در حال بارگیری...',
                noRecordsFound: 'هیچ رکوردی یافت نشد',
                error: 'خطا در بارگیری داده‌ها'
            }
        }).render(document.getElementById("recent-edits-table"));
    }
}

// Initialize all charts
document.addEventListener('DOMContentLoaded', function() {
    // Initialize admin analytics with real data if available
    if (typeof adminData !== 'undefined') {
        updateAdminData(adminData);
    }
    
    renderSparklineCharts();
    renderNewsPerDayChart();
    renderEditorContributionChart();
    renderRecentNewsTable();
});
