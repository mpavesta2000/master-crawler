
document.addEventListener('DOMContentLoaded', function () {
    // Initialize DataTable
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
                        return 'جزئیات برای ' + data[0] + ' ' + data[1];
                    }
                }),
                renderer: $.fn.dataTable.Responsive.renderer.tableAll({
                    tableClass: 'table'
                })
            }
        },
        language: {
            search: "جستجو:",
            lengthMenu: "نمایش _MENU_ مورد در هر صفحه",
            info: "نمایش _START_ تا _END_ از _TOTAL_ کاربر",
            infoEmpty: "هیچ اطلاعاتی یافت نشد",
            infoFiltered: "(فیلتر شده از _MAX_ رکورد)",
            zeroRecords: "هیچ رکوردی یافت نشد",
            paginate: {
                next: "بعدی",
                previous: "قبلی"
            },
            buttons: {
                copy: "Copy",
                csv: "CSV",
                excel: "Excel",
                print: "Print"
            }
        }
    });

    // Role Filter Logic
    $('#role-filter').on('change', function () {
        const filterValue = $(this).val();
        if (filterValue === '') {
            table.search('').draw();
        } else {
            table.column(2).search(filterValue).draw();
        }
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const dateElements = document.querySelectorAll('.registration-date');
    dateElements.forEach(el => {
        const rawDate = el.getAttribute('data-date');
        if (rawDate) {
            const postedDate = new Date(rawDate);
            const formattedDate = new Intl.DateTimeFormat('fa-IR').format(postedDate);
            el.textContent = formattedDate;
        }
    });
});

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.edit-item-btn').forEach(button => {
        button.addEventListener('click', async (event) => {
            event.preventDefault();
            const userId = button.getAttribute('data-id');

            try {
                const response = await fetch(`/api/users/edit/${userId}`);
                if (!response.ok) {
                    throw new Error(`Failed to fetch user data. Status: ${response.status}`);
                }

                const user = await response.json();

                // Populate modal fields
                document.getElementById('userId').value = user.userId;
                document.getElementById('userEmail').value = user.email;
                document.getElementById('isActive').value = user.isActive.toString();

                // Set the correct user type in the dropdown
                const userTypeSelect = document.getElementById('userTypeName');
                Array.from(userTypeSelect.options).forEach(option => {
                    if (option.textContent.trim() === user.userTypeName) {
                        option.selected = true;
                    } else {
                        option.selected = false;
                    }
                });
            } catch (error) {
                console.error('Error fetching user data:', error);
                alert('خطا در دریافت اطلاعات کاربر');
            }
        });
    });
});



