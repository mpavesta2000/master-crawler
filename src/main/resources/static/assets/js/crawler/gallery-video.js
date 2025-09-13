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
            xhr.open("POST", "/api/video/upload", true);


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

//Edit video name
const editImageModal = document.getElementById('editVideo');
editImageModal.addEventListener('show.bs.modal', function (event) {
    const button = event.relatedTarget;
    const videoId = button.getAttribute('data-video-id');
    const videoName = button.getAttribute('data-video-name');

    // Populate the input fields
    const editVideoInput = document.getElementById('editVideoId');
    const editVideoNameInput = document.getElementById('editVideoName');
    editVideoInput.value = videoId;
    editVideoNameInput.value = videoName;
});


// GLightbox Popup
var lightbox = GLightbox({
    selector: '.image-popup',
    title: false,
});