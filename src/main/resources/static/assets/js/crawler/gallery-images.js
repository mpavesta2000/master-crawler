/*
Template Name: Velzon - Admin & Dashboard Template
Author: Themesbrand
Website: https://Themesbrand.com/
Contact: Themesbrand@gmail.com
File: Gallery init
*/



//Filepond Config
FilePond.registerPlugin(
    FilePondPluginFileEncode,
    FilePondPluginFileValidateSize,
    FilePondPluginImageExifOrientation,
    FilePondPluginImagePreview
);
const input = document.querySelector('input.filepond');
const pond = FilePond.create(input, {
    storeAsFile: true,
    labelIdle: '.فایل خود را در<span class="filepond--label-action">اینجا </span> آپلود کنید',
});

pond.setOptions({
    server: {
        process: (fieldName, file, metadata, load, error, progress, abort) => {
            const formData = new FormData();
            formData.append(fieldName, file);

            const xhr = new XMLHttpRequest();
            xhr.open("POST", "/api/gallery/images/upload", true);


            xhr.onload = function () {
                if (xhr.status === 200) {
                    Swal.fire({
                        icon: 'success',
                        title: 'آپلود موفق',
                        text: 'فایل شما با موفقیت آپلود شد!',
                        confirmButtonText: 'تایید',
                    }).then(() => {
                        location.reload();
                    });
                    load(xhr.responseText);
                } else {
                    Swal.fire({
                        icon: 'error',
                        title: 'خطا',
                        text: 'مشکلی در آپلود فایل رخ داد!',
                        confirmButtonText: 'تایید',
                    });
                    error("Upload failed.");
                }
            };

            xhr.onerror = function () {
                Swal.fire({
                    icon: 'error',
                    title: 'خطا',
                    text: 'مشکلی در ارتباط با سرور رخ داد!',
                    confirmButtonText: 'تایید',
                });
                error("Upload error.");
            };

            xhr.send(formData);

            return {
                abort: () => {
                    xhr.abort();
                    abort();
                },
            };
        },
    },
});

//Edit image name
const editImageModal = document.getElementById('editImage');
editImageModal.addEventListener('show.bs.modal', function (event) {
    const button = event.relatedTarget; // Button that triggered the modal
    const imageId = button.getAttribute('data-image-id'); // Extract image ID
    const imageName = button.getAttribute('data-image-name'); // Extract image name

    // Populate the input fields
    const editImageIdInput = document.getElementById('editImageId');
    const editImageNameInput = document.getElementById('editImageName');
    editImageIdInput.value = imageId;
    editImageNameInput.value = imageName;
});



// GLightbox Popup
var lightbox = GLightbox({
    selector: '.image-popup',
    title: false,
});