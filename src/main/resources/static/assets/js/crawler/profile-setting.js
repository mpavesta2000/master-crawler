/*
Template Name: Velzon - Admin & Dashboard Template
Author: Themesbrand
Website: https://Themesbrand.com/
Contact: Themesbrand@gmail.com
File: Profile-setting init js
*/


if (document.querySelector("#profile-img-file-input")) {
    document.querySelector("#profile-img-file-input").addEventListener("change", function () {
        var preview = document.querySelector(".user-profile-image");
        var file = document.querySelector(".profile-img-file-input").files[0];

        if (file) {
            var reader = new FileReader();
            reader.addEventListener(
                "load",
                function () {
                    preview.src = reader.result;
                },
                false
            );
            reader.readAsDataURL(file);
        }

        if (file) {
            var formData = new FormData();
            formData.append("image", file);
            Swal.fire({
                title: "در حال آپلود...",
                text: "لطفا منتظر بمانید",
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                },
            });

            fetch("/api/profile/picture/upload", {
                method: "POST",
                body: formData,
                headers: {
                    "X-Requested-With": "XMLHttpRequest",
                },
            })
                .then((response) => {
                    if (!response.ok) {
                        throw new Error("Network response was not ok " + response.statusText);
                    }
                    return response.json();
                })
                .then((data) => {
                    Swal.close();
                    if (data.success) {
                        Swal.fire({
                            icon: "success",
                            text: data.success,
                            confirmButtonText: "باشه",
                        }).then(() => {
                            location.reload();
                        });
                    } else {
                        Swal.fire({
                            icon: "error",
                            title: "خطا",
                            text: data.error,
                            confirmButtonText: "باشه",
                        });
                    }
                })
                .catch((error) => {
                    console.error("Error uploading image:", error);
                    alert("خطایی رخ داد. لطفا دوباره تلاش کنید.");
                });
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('form');
    const newPasswordInput = document.querySelector('#newpasswordInput');
    const confirmPasswordInput = document.querySelector('#confirmpasswordInput');
    const submitButton = document.querySelector('button[type="submit"]');

    form.addEventListener('submit', function (event) {
        if (newPasswordInput.value !== confirmPasswordInput.value) {
            event.preventDefault();
            Swal.fire({
                title: 'خطا!',
                text: 'رمز عبور جدید و تایید رمز عبور مطابقت ندارد.',
                icon: 'error',
                confirmButtonText: 'باشه'
            });
        }
    });
});