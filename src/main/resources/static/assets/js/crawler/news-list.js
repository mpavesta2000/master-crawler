function getNewsTypePath(newsType) {
    switch (newsType) {
        case 'گالری':
            return 'images-news/edit';
        case 'ویدئویی':
            return 'video/edit';
        default:
            return 'edit';
    }
}


document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#news-datatables', {
        dom: 'Bfrtip',
        buttons: ['copy', 'csv', 'excel', 'print'],
        lengthMenu: [10, 25, 50, 100],
        serverSide: true,
        processing: true,
        ajax: {
            url: '/api/data',
            type: 'GET',
            data: function (d) {
                d.statusFilter = $('#status-filter').val();
                d.newsTypeFilter = $('#idNewsType').val();
                d.userFilter = $('#idUser').val();
                d.idFilter = $('#idFilter').val();

            },
            error: function (xhr, error, code) {
                console.error("Error loading data: ", error, code);
                Swal.fire({
                    icon: 'error',
                    title: 'خطا',
                    text: 'خطایی در بارگذاری اخبار رخ داد.',
                    confirmButtonText: 'تایید'
                });
            },
            dataSrc: function (json) {
                json.data.forEach(function(news) {
                    if (news.newsType === 'Video') {
                        news.newsType = 'ویدئویی';
                    } else if (news.newsType === 'Default') {
                        news.newsType = 'استاندارد';
                    } else if (news.newsType === 'Image') {
                        news.newsType = 'گالری';
                    } else {
                        news.newsType = 'نامشخص';
                    }

                    if (news.status === 'Draft') {
                        news.status = 'پیش نویس';
                    } else if (news.status === 'Published') {
                        news.status = 'منتشر شده';
                    }

                    if (typeof news.categories === 'string') {
                        news.categories = news.categories.split(',').map(function(category) {
                            return '<span class="nl-cat">' + category.trim() + '</span>';
                        }).join(' ');
                    } else {
                        news.categories = '';
                    }

                    news.createdAt = str_dt(news.createdAt);

                    news.actions = `
                        <div class="nl-actions">
                            <a href="/admin/news/${getNewsTypePath(news.newsType.toLowerCase())}/${news.newsId}" class="nl-action nl-action-edit" id="editLink-${news.newsId}" title="ویرایش">
                                <i class="ri-edit-2-line"></i>
                            </a>
                            <form action="/admin/news/delete/${news.newsId}" method="post">
                                <button type="submit" class="nl-action nl-action-delete" onclick="confirmDelete(event)" title="حذف">
                                    <i class="ri-delete-bin-line"></i>
                                </button>
                            </form>
                        </div>
                    `;
                });

                return json.data;
            }
        },
        columns: [
            { data: 'newsId' },
            { data: 'title',
                render: function(data, type, row) {
                    return `
                <form action="/admin/news/show/${row.newsId}" method="get" class="news-title-form nl-title-form">
                    <button type="submit" class="nl-title-btn" title="${data}">${data}</button>
                </form>
            `;}
            },
            { data: 'newsType' },
            { data: 'author' },
            { data: 'createdAt' },
            {
                data: 'status',
                render: function(data) {
                    return '<span class="nl-status ' + (data === 'پیش نویس' ? 'is-draft' : 'is-published') + '">' + data + '</span>';
                }
            },
            { data: 'categories', render: function(data) {
                    return data;
                }},
            { data: 'actions', render: function(data) {
                    return data;
                }}
        ],
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.modal({
                    header: function (row) {
                        var data = row.data();
                        return data.newsId + ' - ' + data.title;
                    }
                }),
                renderer: $.fn.dataTable.Responsive.renderer.tableAll({
                    tableClass: 'table'
                })
            }
        },

        language: {
            search: "جستجو:",
            lengthMenu: "نشان دادن _MENU_ در هر صفحه",
            info: "نشان دادن _START_ تا _END_ از _TOTAL_ اخبار",
            infoEmpty: "هیچ اطلاعاتی موجود نیست",
            infoFiltered: "(فیلتر شده از _MAX_ رکورد)",
            zeroRecords: "رکوردی یافت نشد",
            paginate: {
                next: "بعدی",
                previous: "قبلی"
            },
            processing: "لطفاً منتظر بمانید..."
        }
    });

    $('#status-filter, #idNewsType, #idUser, #idFilter').on('change', function () {
        table.ajax.reload();
    });
});


var str_dt = function formatDate(dateStr) {
    const monthNames = ["فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"];

    function leap_gregorian(year) {
        return ((year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0)) ? 1 : 0;
    }

    function toJalali(g_y, g_m, g_d) {
        var g_days_in_month = [31, 28 + leap_gregorian(g_y), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
        var j_days_in_month = [31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29];

        function div(a, b) { return ~~(a / b); }

        var gy = g_y - 1600;
        var gm = g_m - 1;
        var gd = g_d - 1;

        var g_day_no = 365 * gy + div(gy + 3, 4) - div(gy + 99, 100) + div(gy + 399, 400);
        for (var i = 0; i < gm; ++i)
            g_day_no += g_days_in_month[i];
        g_day_no += gd;

        var j_day_no = g_day_no - 79;
        var j_np = div(j_day_no, 12053);
        j_day_no %= 12053;

        var jy = 979 + 33 * j_np + 4 * div(j_day_no, 1461);
        j_day_no %= 1461;

        if (j_day_no >= 366) {
            jy += div(j_day_no - 1, 365);
            j_day_no = (j_day_no - 1) % 365;
        }

        for (var i = 0; i < 11 && j_day_no >= j_days_in_month[i]; ++i)
            j_day_no -= j_days_in_month[i];

        var jm = i + 1;
        var jd = j_day_no + 1;

        return { jy: jy, jm: jm, jd: jd };
    }

    // 📌 تبدیل ورودی string به Date (مثلاً "2025-07-22 17:25:47.801994")
    let date = new Date(dateStr.replace(" ", "T").substring(0, 23)); // ISO-compatible format

    var g_y = date.getFullYear();
    var g_m = date.getMonth() + 1;
    var g_d = date.getDate();
    var jalali = toJalali(g_y, g_m, g_d);

    var hours = date.getHours();
    var minutes = date.getMinutes();
    var newformat = hours >= 12 ? 'ب.ظ' : 'ق.ظ';
    hours = hours % 12;
    hours = hours ? hours : 12;
    minutes = minutes < 10 ? '0' + minutes : minutes;

    var day = jalali.jd < 10 ? '0' + jalali.jd : jalali.jd;
    var month = monthNames[jalali.jm - 1];
    var year = jalali.jy;

    return `${day} ${month} ${year} <small class='text-muted'>${hours}:${minutes} ${newformat}</small>`;
};





