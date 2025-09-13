function filterSites() {
    let input = document.getElementById('searchInput').value.toLowerCase();
    let items = document.getElementsByClassName('site-item');

    for (let i = 0; i < items.length; i++) {
        let siteName = items[i].getElementsByClassName('site-name')[0].innerText.toLowerCase();
        if (siteName.includes(input)) {
            items[i].style.display = "block";
        } else {
            items[i].style.display = "none";
        }
    }
}


document.querySelector("#chapchinForm").addEventListener("submit", function (event) {
    event.preventDefault();

    const url = document.querySelector('#url').value;

    if (!url.trim()) {
        Swal.fire({
            icon: 'warning',
            title: 'خطا!',
            text: 'لینک نمی‌تواند خالی باشد!',
            confirmButtonText: 'باشه'
        });
        return;
    }

    const saving = Swal.fire({
        title: 'در حال ارسال...',
        html: '<div class="spinner"></div>',
        allowOutsideClick: false,
        showConfirmButton: false,
        didOpen: () => {
            Swal.showLoading();
        },
    });

    console.log(url.trim());

    fetch("/api/chapchin/send", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ url: url.trim() })
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Unknown error occurred');
                });
            }
            return response.json();
        })
        .then(data => {
            saving.close();
            Swal.fire({
                icon: 'success',
                title: 'ذخیره شد!',
                text: 'خبر با موفقیت ذخیره شد.',
                confirmButtonText: 'باشه'
            });
        })
        .catch(error => {
            console.error("Error:", error);
            saving.close();
            Swal.fire({
                icon: 'error',
                title: 'خطا!',
                text: error.message,
                confirmButtonText: 'باشه'
            });
        });
});

