document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#customers-datatables', {
        dom: 'Bfrtip',
        buttons: ['copy', 'csv', 'excel', 'print'],
        lengthMenu: [10, 25, 50, 100],
        serverSide: true,
        processing: true,
        ajax: {
            url: '/api/users/admin/data',
            type: 'GET',
            data: function (d) {
                d['search[value]'] = $('#search-input').val();
            },
            error: function (xhr, error, code) {
                console.error("Error loading data: ", error, code);
                alert("هنگام بارگذاری اطلاعات کاربران خطایی رخ داده است.");
            },
            dataSrc: function (json) {
                json.data.forEach(function(user) {
                    user.createdAt = str_dt(user.createdAt);
                    if (user.updatedAt) {
                        user.updatedAt = str_dt(user.updatedAt);
                    } else {
                        user.updatedAt = '—';
                    }

                    user.status = '<span class="badge ' +
                        (user.status ? 'badge-soft-success' : 'badge-soft-danger') + '">' +
                        (user.status ? 'فعال' : 'غیرفعال') + '</span>';
                });

                return json.data;
            }
        },
        columns: [
            { data: 'userId' },
            { data: 'email' },
            { data: 'status' },
            { data: 'createdAt' },
            { data: 'updatedAt' },
            { data: 'actions', orderable: false, searchable: false }
        ],
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.modal({
                    header: function (row) {
                        return 'کاربر: ' + row.data().email;
                    }
                }),
                renderer: $.fn.dataTable.Responsive.renderer.tableAll({
                    tableClass: 'table'
                })
            }
        },
        language: {
            search: "جستجو:",
            lengthMenu: "نمایش _MENU_ کاربر",
            info: "نمایش _START_ تا _END_ از _TOTAL_ کاربر",
            infoEmpty: "هیچ کاربری یافت نشد",
            infoFiltered: "(فیلتر شده از _MAX_ کاربر)",
            zeroRecords: "کاربری پیدا نشد",
            paginate: {
                next: "بعدی",
                previous: "قبلی"
            },
            processing: "در حال بارگذاری..."
        }
    });

    $('#search-input').on('input', function () {
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




$(document).on('click', '.edit-user', function () {
    const userId = $(this).data('user-id');
    console.log("Edit button clicked. User ID:", userId);

    $.get('/api/users/admins/' + userId, function (user) {
        console.log("User loaded:", user);
        $('#edit-user-id').val(user.id);
        $('#edit-email').val(user.email);
        $('#edit-password').val('');
        $('#edit-status').val(user.active.toString());

        $('#editUserModal').modal('show');
    }).fail(function () {
        console.error("Failed to fetch user data for ID:", userId);
    });
});



$('#editUserForm').submit(function (e) {
    e.preventDefault();

    const formData = {
        id: $('#edit-user-id').val(),
        email: $('#edit-email').val(),
        password: $('#edit-password').val(),
        active: $('#edit-status').val() === "true",
    };

    $.ajax({
        url: '/api/users/admins/edit/save',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function () {
            $('#editUserModal').modal('hide');
            $('#customers-datatables').DataTable().ajax.reload(null, false);
            Swal.fire({
                title: 'موفق!',
                text: 'اطلاعات ادمین بروزرسانی شد.',
                icon: 'success',
                confirmButtonText: 'تایید'
            });

        },
        error: function () {
            Swal.fire({
                title: 'خطا!',
                text: 'بروزرسانی با مشکل مواجه شد.',
                icon: 'error',
                confirmButtonText: 'تایید'
            });

        }
    });
});

