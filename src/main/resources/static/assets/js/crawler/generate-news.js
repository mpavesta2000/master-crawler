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

document.querySelector("#generateForm").addEventListener("submit", function (event) {
    event.preventDefault();

    const keywords = document.querySelector("#keywords").value;
    const seo = document.querySelector("#seo").value;
    const editorInstance = CKEDITOR.instances.editor;
    const outcomeInstance = CKEDITOR.instances.outcome;
    const prompt = editorInstance.getData();

    let errorMessage = "";

    if (keywords === "") errorMessage += "کلمات کلیدی را وارد کنید.\n";
    if (seo === "") errorMessage += "نکات SEO را وارد کنید.\n";
    if (prompt === "") errorMessage += "توضیحات به هوش مصنوعی را وارد کنید.\n";

    if (errorMessage !== "") {
        Swal.fire({
            icon: "error",
            title: "خطا!",
            text: errorMessage,
            confirmButtonText: "باشه"
        });
        return;
    }

    if (prompt == null) {
        Swal.fire({
            icon: 'warning',
            title: 'خطا!',
            text: 'توضیحات به هوش مصنوعی نمی‌تواند خالی باشد!',
            confirmButtonText: 'باشه'
        });
        return;
    }

    const requestData = {
        keywords: keywords,
        seo: seo,
        generate: prompt
    };

    Swal.fire({
        title: 'لطفاً صبر کنید...',
        html: '<div class="spinner"></div>',
        allowOutsideClick: false,
        showConfirmButton: false,
        didOpen: () => {
            Swal.showLoading();
        },
    });

    fetch("/api/gemini/generate", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(requestData),
    })
        .then(response => response.json())
        .then(data => {
            const htmlContent = data.content.replace(/```html|```/g, '').trim();
            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlContent, 'text/html');
            const answer = doc.querySelector('#answer')?.innerHTML || htmlContent;
            const answerForm = document.querySelector('#sendToCms');
            answerForm.removeAttribute('hidden');

            if (outcomeInstance) {
                outcomeInstance.setData(answer);
            }

            // Close loading alert and show success
            Swal.close();
            Swal.fire({
                icon: 'success',
                title: 'مقاله شما ایجاد شد.',
                text: 'مقاله جدید با موفقیت ایجاد شد.',
                confirmButtonText: 'باشه'
            });
        })
        .catch(error => {
            console.error("Error:", error);
            Swal.close();
            Swal.fire({
                icon: 'error',
                title: 'خطا!',
                text: 'مشکلی در ایجاد مقاله پیش آمد. لطفاً دوباره تلاش کنید.',
                confirmButtonText: 'باشه'
            });
        });
});

document.querySelector("#sendToCms").addEventListener("submit", function (event) {
    event.preventDefault();

    const outcomeInstance = CKEDITOR.instances.outcome;
    const newsContent = outcomeInstance.getData();

    if (!newsContent.trim()) {
        Swal.fire({
            icon: 'warning',
            title: 'خطا!',
            text: 'محتوای مقاله نمی‌تواند خالی باشد!',
            confirmButtonText: 'باشه'
        });
        return;
    }

    const saving = Swal.fire({
        title: 'در حال ذخیره...',
        html: '<div class="spinner"></div>',
        allowOutsideClick: false,
        showConfirmButton: false,
        didOpen: () => {
            Swal.showLoading();
        },
    });

    fetch("/api/generatedNews/save", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({ body: newsContent })
    })
        .then(response => response.json())
        .then(data => {
            saving.close();
            Swal.fire({
                icon: 'success',
                title: 'ذخیره شد!',
                text: 'مقاله با موفقیت ذخیره شد.',
                confirmButtonText: 'باشه'
            });
        })
        .catch(error => {
            console.error("Error:", error);
            saving.close();
            Swal.fire({
                icon: 'error',
                title: 'خطا!',
                text: 'مشکلی در ذخیره‌سازی مقاله پیش آمد. لطفاً دوباره تلاش کنید.',
                confirmButtonText: 'باشه'
            });
        });
});
