CKEDITOR.replace('outcome', {
    language: 'fa',
    height: 600,
    extraPlugins: 'sendtoapi',
    toolbar: [
        { name: 'basicstyles', items: ['Bold', 'Italic', 'Underline', 'Strike'] },
        { name: 'paragraph', items: ['NumberedList', 'BulletedList', '-', 'Blockquote'] },
        { name: 'links', items: ['Link', 'Unlink'] },
        { name: 'clipboard', items: ['Undo', 'Redo'] },
        { name: 'editing', items: ['Scayt'] },
        { name: 'tools', items: ['Maximize'] }
    ],
    filebrowserUploadUrl: '/upload-image',
    filebrowserBrowseUrl: '/gallery/gallery-browser',
    filebrowserUploadMethod: 'form',
    on: {
        fileUploadResponse: function (evt) {
            var data = evt.data;
            try {
                var response = JSON.parse(data.fileLoader.xhr.responseText);
                console.log("File upload response received:", response);

                if (response.uploaded) {
                    var imageUrl = response.url;
                    console.log("Image URL:", imageUrl);

                    var editor = evt.editor;
                    editor.insertHtml('<img src="' + imageUrl + '" alt="Uploaded Image"/>');

                    setTimeout(function () {
                        var iframe = document.querySelector('.cke_dialog_ui_input_text[id^="cke_"][type="text"]');
                        if (iframe) {
                            iframe.value = imageUrl;
                            console.log("URL input field updated successfully:", imageUrl);
                        } else {
                            console.warn("Could not find the image URL input field.");
                        }
                    }, 100);
                } else {
                    alert('File upload failed.');
                }
            } catch (error) {
                console.error("Error processing upload response:", error);
            }
        }
    }
});

FilePond.registerPlugin(
    FilePondPluginFileEncode,
    FilePondPluginFileValidateSize,
    FilePondPluginImageExifOrientation,
    FilePondPluginImagePreview
);

const input = document.querySelector('input.filepond');

const pond = FilePond.create(input, {
    storeAsFile: true,
    labelIdle: 'فایل خود را در<span class="filepond--label-action">اینجا </span> آپلود کنید',
});

pond.setOptions({
    server: {
        process: (fieldName, file, metadata, load, error, progress, abort) => {
            const formData = new FormData();
            formData.append(fieldName, file);

            const xhr = new XMLHttpRequest();
            xhr.open("POST", "/api/upload/voice", true);


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

document.getElementById('voiceForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const selectedFile = document.querySelector('input[name="exampleRadios"]:checked');
    if (!selectedFile) {
        Swal.fire({
            text: 'لطفاً یک فایل را انتخاب کنید.',
            icon: 'error',
            confirmButtonText: 'تایید',
            confirmButtonColor: '#d33'
        });
        return;
    }

    const fileName = selectedFile.value;

    const sending = Swal.fire({
        title: 'در حال ارسال...',
        html: '<div class="spinner"></div>',
        allowOutsideClick: false,
        showConfirmButton: false,
        didOpen: () => {
            Swal.showLoading();
        },
    });

    fetch('/api/getvoice', {
        method: 'POST',
        body: new URLSearchParams({
            'fileName': fileName
        })
    })
        .then(response => response.json())
        .then(data => {
            const transcript = data.transcript;

            CKEDITOR.instances.outcome.setData(transcript);
            sending.close();

            Swal.fire({
                text: 'متن با موفقیت تولید شد.',
                icon: 'success',
                confirmButtonText: 'تایید',
                confirmButtonColor: '#556ee6'
            });
        })
        .catch(error => {
            sending.close();
            Swal.fire({
                text: 'خطا در تبدیل فایل به متن.',
                icon: 'error',
                confirmButtonText: 'تایید',
                confirmButtonColor: '#d33'
            });
        });
});

document.addEventListener('DOMContentLoaded', function() {
    let mediaRecorder;
    let audioChunks = [];
    let audioBlob;
    let timerInterval;
    let recordingTime = 0;

    const startButton = document.getElementById('start-recording');
    const stopButton = document.getElementById('stop-recording');
    const uploadButton = document.getElementById('upload-audio');
    const audioElement = document.getElementById('recorded-audio');
    const audioContainer = document.getElementById('audio-container');
    const recordingStatus = document.getElementById('recording-status');
    const recordingTimeDisplay = document.getElementById('recording-time');

    startButton.addEventListener('click', startRecording);
    stopButton.addEventListener('click', stopRecording);
    uploadButton.addEventListener('click', uploadAudio);

    function startRecording() {
        audioChunks = [];
        recordingTime = 0;  // Reset recording time
        recordingTimeDisplay.textContent = formatTime(recordingTime);

        navigator.mediaDevices.getUserMedia({ audio: true })
            .then(stream => {
                mediaRecorder = new MediaRecorder(stream);

                mediaRecorder.ondataavailable = event => {
                    audioChunks.push(event.data);
                };

                mediaRecorder.onstop = () => {
                    audioBlob = new Blob(audioChunks, { type: 'audio/mp4' });
                    const audioUrl = URL.createObjectURL(audioBlob);
                    audioElement.src = audioUrl;
                    audioContainer.classList.remove('d-none');
                    recordingStatus.textContent = 'ضبط متوقف شد';
                    recordingStatus.classList.remove('bg-danger');
                    recordingStatus.classList.add('bg-light', 'text-dark');

                    clearInterval(timerInterval);
                };

                mediaRecorder.start();
                startButton.disabled = true;
                stopButton.disabled = false;
                recordingStatus.textContent = 'در حال ضبط...';
                recordingStatus.classList.remove('bg-light', 'text-dark');
                recordingStatus.classList.add('bg-danger');

                timerInterval = setInterval(() => {
                    recordingTime++;
                    recordingTimeDisplay.textContent = formatTime(recordingTime);
                }, 1000);
            })
            .catch(error => {
                console.error('Error accessing microphone:', error);
                Swal.fire({
                    icon: 'error',
                    title: 'خطا!',
                    text: 'دسترسی به میکروفن امکان‌پذیر نیست. لطفاً مجوز دسترسی را بررسی کنید.',
                    confirmButtonText: 'متوجه شدم'
                });
            });
    }

    function stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.stream.getTracks().forEach(track => track.stop());
        startButton.disabled = false;
        stopButton.disabled = true;
    }

    function uploadAudio() {
        if (!audioBlob) {
            return;
        }

        const filenameInput = document.getElementById('filename');
        const filename = filenameInput.value.trim();

        if (!filename) {
            Swal.fire({
                icon: 'warning',
                title: 'خطا!',
                text: 'لطفاً نام فایل را وارد کنید.'
            });
            return;
        }

        const formData = new FormData();
        formData.append('audio', audioBlob, filename + '.mp4');
        formData.append('filename', filename);

        Swal.fire({
            title: 'در حال آپلود...',
            text: 'لطفاً صبر کنید',
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });

        fetch('/api/upload/audio', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('مشکلی در آپلود پیش آمده است');
                }
                return response.text();
            })
            .then(message => {
                Swal.fire({
                    icon: 'success',
                    title: 'آپلود موفق',
                    text: message,
                    confirmButtonText: 'باشه'
                })
                    .then(() => {
                        location.reload();
                    });
            })
            .catch(error => {
                console.error('Error uploading audio:', error);

                Swal.fire({
                    icon: 'error',
                    title: 'خطا!',
                    text: 'مشکلی در آپلود فایل پیش آمده است. لطفاً دوباره امتحان کنید.',
                    confirmButtonText: 'متوجه شدم'
                });
            });
    }

    function formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${String(minutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`;
    }
});

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.delete-button').forEach(button => {
        button.addEventListener('click', function () {
            const filename = this.getAttribute('data-filename');

            Swal.fire({
                title: "آیا مطمئن هستید؟",
                text: "این فایل برای همیشه حذف خواهد شد!",
                icon: "warning",
                showCancelButton: true,
                confirmButtonText: "بله، حذف شود",
                cancelButtonText: "لغو"
            }).then((result) => {
                if (result.isConfirmed) {
                    fetch(`/api/delete/audio?filename=${filename}`, {
                        method: 'DELETE'
                    })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error('حذف فایل ناموفق بود');
                            }
                            return response.text();
                        })
                        .then(message => {
                            Swal.fire({
                                icon: "success",
                                title: "حذف موفق!",
                                text: message,
                                confirmButtonText: "تایید",
                            }).then(() => {
                                location.reload();
                            });
                        })
                        .catch(error => {
                            Swal.fire({
                                icon: "error",
                                title: "خطا!",
                                text: "مشکلی در حذف فایل پیش آمده است. لطفاً دوباره امتحان کنید.",
                                confirmButtonText: "تایید",
                            });
                        });
                }
            });
        });
    });
});

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.play-button').forEach(button => {
        button.addEventListener('click', function () {
            const filename = this.getAttribute('data-filename');
            const audioUrl = `/news/voices/${filename}`;

            const modal = new bootstrap.Modal(document.getElementById('audioModal'));
            const audioPlayer = document.getElementById('audio-player');
            const filenameElement = document.getElementById('audio-filename');

            filenameElement.textContent = filename;

            const existingAudio = document.getElementById('audio-player');
            if (existingAudio) {
                existingAudio.remove();
            }

            const audio = document.createElement('audio');
            audio.id = 'audio-player';
            audio.controls = true;
            audio.src = audioUrl;
            audio.style.width = '100%';

            const modalBody = document.querySelector('#audioModal .modal-body');
            modalBody.appendChild(audio);

            modal.show();

            audio.play().catch(error => {
                Swal.fire({
                    icon: 'error',
                    title: 'خطا!',
                    text: 'مشکلی در پخش فایل صوتی پیش آمده است.'
                });
            });

            const modalElement = document.getElementById('audioModal');
            modalElement.addEventListener('hidden.bs.modal', function () {
                audio.pause();
                audio.currentTime = 0;
            });
        });
    });
});

document.getElementById("copyButton").addEventListener("click", function () {
    const copyButton = this;
    const editorInstance = CKEDITOR.instances.outcome;

    if (editorInstance) {
        const text = editorInstance.getData().trim();

        if (!text) {
            Swal.fire({
                text: "متنی برای کپی وجود ندارد!",
                icon: "error",
                confirmButtonText: "تایید",
                confirmButtonColor: "#d33"
            });
            return;
        }

        navigator.clipboard.writeText(text).then(() => {
            copyButton.textContent = "✅ متن کپی شد!";
            copyButton.classList.add("btn-success");

            setTimeout(() => {
                copyButton.textContent = "کپی کردن متن";
                copyButton.classList.remove("btn-success");
            }, 2000);
        }).catch(err => {
            copyButton.textContent = "❌ خطا در کپی!";
            copyButton.classList.add("btn-danger");

            setTimeout(() => {
                copyButton.textContent = "کپی کردن متن";
                copyButton.classList.remove("btn-danger");
            }, 2000);
        });
    }
});


