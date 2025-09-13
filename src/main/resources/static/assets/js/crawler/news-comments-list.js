document.addEventListener('DOMContentLoaded', function () {
    let table = new DataTable('#news-datatables', {
        dom: '<"d-flex justify-content-between align-items-center"lfB>rtip',
        buttons: ['copy', 'csv', 'excel', 'print'],
        lengthMenu: [10, 25, 50, 100],
        serverSide: true,
        processing: true,
        ajax: {
            url: '/api/comments/data',
            type: 'GET',
            error: function (xhr, error, code) {
                console.error("Error loading data: ", error, code);
                alert("An error occurred while loading data.");
            },
            dataSrc: function (json) {
                json.data.forEach(function (comment) {

                    comment.content = `<a href="#" class="view-content" data-bs-toggle="modal" data-bs-target="#contentModal" data-content="${comment.content}">دیدن متن نظر</a>`;

                    comment.status = comment.status
                        ? `<span class="badge bg-primary">${'انتشار'}</span>`
                        : `<span class="badge bg-danger">${'انتشار نشده'}</span>`;


                    const postedDate = new Date(comment.postedDate);
                    const formattedDate = new Intl.DateTimeFormat('fa-IR').format(postedDate);
                    comment.postedDate = formattedDate;

                    comment.actions = `
                        <div class="dropdown">
                            <button class="btn btn-link p-0 link-primary fs-15 dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-expanded="false">
                                انتخاب وضیعت
                            </button>
                            <ul class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                                <li>
                                    <form id="changeStatusForm${comment.id}" action="/admin/comments/changeStatus/${comment.id}" method="post" style="display: inline;">
                                        <button type="button" class="dropdown-item" onclick="changeStatus(event, ${comment.id})">
                                           تغییر وضیعت
                                        </button>
                                    </form>
                                </li>
                            </ul>
                        </div>
                    `;

                    comment.erase = `
                        <form id="deleteForm${comment.id}" action="/admin/comments/delete/${comment.id}" method="post" style="display: inline;">
                            <button type="submit" class="btn btn-link p-0 link-danger fs-15" style="border: none; background: none;" onclick="confirmDelete(event, ${comment.id})">
                                <i class="ri-delete-bin-line"></i>
                            </button>
                        </form>
                    `;
                });
                return json.data;
            }
        },
        columns: [
            { data: 'id' },
            { data: 'name' },
            { data: 'email' },
            { data: 'content' },
            { data: 'postedDate' },
            { data: 'newsId'},
            { data: 'status' },
            { data: 'actions', render: function(data) { return data; } },
            { data: 'erase', render: function(data) { return data; } }
        ],
        language: {
            search: "جستجو:",
            lengthMenu: "نشان دادن _MENU_ در هر صفحه",
            info: "نشان دادن _START_ تا _END_ از _TOTAL_ نظرات",
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

    $('#news-datatables').on('click', '.view-content', function(event) {
        event.preventDefault();
        var content = $(this).data('content');
        $('#modalContent').text(content);
        $('#contentModal').modal('show');
    });


    window.confirmDelete = function(event, commentId) {
        event.preventDefault();
        const form = document.getElementById('deleteForm' + commentId);
        Swal.fire({
            title: 'آیا مطمئن هستید؟',
            text: 'این عمل قابل بازگشت نیست!',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'بله، حذف کن',
            cancelButtonText: 'لغو',
            reverseButtons: true
        }).then((result) => {
            if (result.isConfirmed) {
                form.submit();
            }
        });
    }


    window.changeStatus = function(event, commentId) {
        event.preventDefault();
        const form = document.getElementById('changeStatusForm' + commentId);
        Swal.fire({
            title: 'تغییر وضعیت',
            text: 'آیا مطمئن هستید که می‌خواهید وضعیت این نظر را تغییر دهید؟',
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'بله، تغییر وضعیت کن',
            cancelButtonText: 'لغو',
            reverseButtons: true
        }).then((result) => {
            if (result.isConfirmed) {
                form.submit();
            }
        });
    }
});
