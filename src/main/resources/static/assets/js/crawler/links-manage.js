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
                        return 'Details for ' + data[0] + ' ' + data[1];
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
            info: "نشان دادن _START_ تا _END_ از _TOTAL_ پیوند ها",
            infoEmpty: "هیچ اطلاعاتی موجود نیست",
            infoFiltered: "(فیلتر شده از _MAX_ رکورد)",
            zeroRecords: "رکوردی یافت نشد",
            paginate: {
                next: "بعدی",
                previous: "قبلی"
            }
        }
    });
});