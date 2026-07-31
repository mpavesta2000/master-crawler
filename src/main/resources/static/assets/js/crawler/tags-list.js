document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#news-datatables', {
        dom: 'Bfrtip',
        buttons: ['copy', 'csv', 'excel', 'print'],
        serverSide: true,
        processing: true,
        ajax: {
            url: '/api/tags/data',
            type: 'GET',
            data: function (d) {
            },
            error: function (xhr, error, code) {
                console.error("Error loading data: ", error, code);
                Swal.fire({
                    icon: 'error',
                    title: 'خطا',
                    text: 'خطایی در بارگذاری برچسب‌ها رخ داد.',
                    confirmButtonText: 'تایید'
                });
            },
            dataSrc: function (json) {
                json.data.forEach(function(tags) {
                    tags.actions = `
                        <div class="tl-actions">
                            <form action="/admin/tags/delete/${tags.id}" method="post">
                                <button type="submit" class="tl-action" onclick="confirmDelete(event)" title="حذف">
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
            { data: 'id' },
            { data: 'name', render: function(data) {
                    return '<span class="tl-tag"><i class="ri-price-tag-3-line"></i>' + data + '</span>';
                }},
            { data: 'actions', render: function(data) {
                    return data;
                }}
        ],
        language: {
            search: "جستجو:",
            lengthMenu: "نشان دادن _MENU_ در هر صفحه",
            info: "نشان دادن _START_ تا _END_ از _TOTAL_ برچسب ها",
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

});





