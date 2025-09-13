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
                alert("An error occurred while loading data.");
            },
            dataSrc: function (json) {
                json.data.forEach(function(tags) {
                    tags.actions = `
                        <td class="due_date">
                            <div class="hstack gap-3 flex-wrap">
                                <form id="deleteForm" action="/admin/tags/delete/${tags.id}" method="post" style="display: inline;">
                                    <button type="submit" class="btn btn-link p-0 link-danger fs-15" style="border: none; background: none;" onclick="confirmDelete(event)">
                                        <i class="ri-delete-bin-line"></i>
                                    </button>
                                </form>
                            </div>
                        </td>
                    `;
                });

                return json.data;
            }
        },
        columns: [
            { data: 'id' },
            { data: 'name' },
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





