
document.addEventListener("DOMContentLoaded", function () {
    const languageSelect = document.getElementById("choices-privacy-status-input");
    const directionElements = [
        document.getElementById("translated-headline"),
        document.getElementById("translated-title"),
        document.getElementById("translated-subheadline"),
        document.getElementById("translated-lead"),
    ];

    function updateDirection(language) {
        const rtlLanguages = ['ar', 'fa', 'he', 'ur', 'ps', 'ha'];
        const direction = rtlLanguages.includes(language) ? 'rtl' : 'ltr';

        directionElements.forEach(element => {
            if (element) {
                element.dir = direction;
                element.style.textAlign = rtlLanguages.includes(language) ? 'right' : 'left';
            }
        });

        if (CKEDITOR.instances.editor) {
            CKEDITOR.instances.editor.config.contentsLangDirection = direction;

            const editorData = CKEDITOR.instances.editor.getData();
            CKEDITOR.instances.editor.setData(editorData);

            const editorElement = CKEDITOR.instances.editor.container;
            if (editorElement) {
                editorElement.dir = direction;
            }
        }
    }

    updateDirection(languageSelect.value);

    languageSelect.addEventListener("change", function() {
        updateDirection(this.value);
    });

    document.getElementById("voiceForm").addEventListener("submit", function (event) {
        event.preventDefault();

        const selectedNews = document.querySelector("input[name='exampleRadios']:checked");
        const language = document.getElementById("choices-privacy-status-input").value;

        if (!selectedNews) {
            Swal.fire({
                icon: "warning",
                title: "انتخاب خبر الزامی است!",
                text: "لطفاً یک خبر را انتخاب کنید.",
                confirmButtonText: "باشه",
            });
            return;
        }

        const newsId = selectedNews.value;

        Swal.fire({
            title: "در حال ترجمه...",
            text: "لطفاً صبر کنید",
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });

        fetch(`/api/translate/news?newsId=${newsId}&language=${language}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    Swal.fire({
                        icon: "error",
                        title: "خطا!",
                        text: data.error,
                        confirmButtonText: "متوجه شدم",
                    });
                    return;
                }

                document.getElementById("translated-headline").value = data.headline || "";
                document.getElementById("translated-title").value = data.title || "";
                document.getElementById("translated-subheadline").value = data.subHeadline || "";
                document.getElementById("translated-lead").value = data.lead || "";
                const editorInstance = CKEDITOR.instances.editor;
                editorInstance.setData(data.body);

                updateDirection(language);

                Swal.fire({
                    icon: "success",
                    title: "ترجمه با موفقیت انجام شد!",
                    text: "نتیجه در فرم نمایش داده شد.",
                    confirmButtonText: "باشه"
                });
            })
            .catch(error => {
                console.error("Error fetching translation:", error);
                Swal.fire({
                    icon: "error",
                    title: "خطا!",
                    text: "مشکلی پیش آمده است. لطفاً دوباره امتحان کنید.",
                    confirmButtonText: "متوجه شدم",
                });
            });
    });
});

CKEDITOR.replace('editor', {
    language: 'fa',
    height: 600,
    extraPlugins: 'sendtoapi',
    toolbar: [
        { name: 'basicstyles', items: ['Bold', 'Italic', 'Underline', 'Strike'] },
        { name: 'paragraph', items: ['NumberedList', 'BulletedList', '-', 'Blockquote'] },
        { name: 'links', items: ['Link', 'Unlink'] },
        { name: 'clipboard', items: ['Undo', 'Redo'] },
        { name: 'editing', items: ['Scayt'] },
        { name: 'tools', items: ['Maximize'] },
        { name: 'direction', items: ['BidiLtr', 'BidiRtl'] }
    ],

    contentsLangDirection: 'rtl',
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

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('sendTranslate');
    const saveButton = form.querySelector('button[type="submit"]');

    saveButton.addEventListener('click', function(event) {
        event.preventDefault();

        const headline = document.getElementById('translated-headline').value.trim() || null;
        const title = document.getElementById('translated-title').value.trim();
        const subheadline = document.getElementById('translated-subheadline').value.trim() || null;
        const lead = document.getElementById('translated-lead').value.trim();
        const editorData = CKEDITOR.instances.editor.getData();

        if (!title || !lead || !editorData) {
            Swal.fire({
                icon: 'error',
                title: 'خطا!',
                text: 'لطفاً تمام فیلدهای ضروری را پر کنید.'
            });
            return;
        }

        const formData = new FormData();
        formData.append('headline', headline);
        formData.append('title', title);
        formData.append('subheadline', subheadline);
        formData.append('lead', lead);
        formData.append('body', editorData);

        Swal.fire({
            title: 'در حال ذخیره...',
            text: 'لطفاً صبر کنید',
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });

        fetch('/api/translate/save', {
            method: 'POST',
            body: formData
        })
            .then(response => response.json())
            .then(data => {
                Swal.close();
                if (data.message) {
                    Swal.fire({
                        icon: 'success',
                        title: 'موفق!',
                        text: data.message
                    }).then(() => {
                        location.reload();
                    });
                } else if (data.error) {
                    Swal.fire({
                        icon: 'error',
                        title: 'خطا!',
                        text: data.error
                    });
                }
            })
            .catch(error => {
                Swal.close();
                Swal.fire({
                    icon: 'error',
                    title: 'خطا!',
                    text: 'مشکلی در ارسال داده‌ها پیش آمده است. لطفاً دوباره امتحان کنید.'
                });
                console.error('Error:', error);
            });
    });
});

