/*
Template Name: Velzon - Admin & Dashboard Template
Author: Themesbrand
Website: https://Themesbrand.com/
Contact: Themesbrand@gmail.com
File: datatables init js
*/

document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#example',);
});

document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#scroll-vertical', {
        "scrollY":        "210px",
        "scrollCollapse": true,
        "paging":         false
    });

});

document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#scroll-horizontal', {
        "scrollX": true
    });
});

document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#alternative-pagination', {
        "pagingType": "full_numbers"
    });
});



$(document).ready(function() {
    var t = $('#add-rows').DataTable();
    var counter = 1;

    $('#addRow').on( 'click', function () {
        t.row.add( [
            counter +'.1',
            counter +'.2',
            counter +'.3',
            counter +'.4',
            counter +'.5',
            counter +'.6',
            counter +'.7',
            counter +'.8',
            counter +'.9',
            counter +'.10',
            counter +'.11',
            counter +'.12'
        ] ).draw( false );

        counter++;
    } );

    // Automatically add a first row of data
    $('#addRow').click();
});


$(document).ready(function() {
    $('#example').DataTable();
});

//fixed header
document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#fixed-header', {
        "fixedHeader": true
    });

});

//modal data datables
document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#model-datatables', {
        dom: 'Bfrtip',
        buttons: [
            'copy',
            'csv',
            'excel',
            'print',
        ],
        responsive: {
            details: {
                display: $.fn.dataTable.Responsive.display.modal({
                    header: function (row) {
                        var data = row.data();
                        return 'مشخصات ' + data[0] + ' ' + data[1];
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
            info: "نشان دادن _START_ تا _END_ از _TOTAL_ دسته بندی ها",
            infoEmpty: "هیچ اطلاعاتی موجود نیست",
            infoFiltered: "(فیلتر شده از _MAX_ رکورد)",
            zeroRecords: "رکوردی یافت نشد",
            paginate: {
                next: "بعدی",
                previous: "قبلی"
            }
        }
    });

    $('#status-filter').on('change', function () {
        const statusValue = $(this).val();
        table.column(4).search(statusValue).draw();
    });
});



//buttons exmples
document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#buttons-datatables', {
        dom: 'Bfrtip',
        buttons: [
            'copy', 'csv', 'excel', 'print', 'pdf'
        ]
    });
});

//buttons exmples
document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#ajax-datatables', {
        "ajax": 'assets/json/datatable.json'
    });
});