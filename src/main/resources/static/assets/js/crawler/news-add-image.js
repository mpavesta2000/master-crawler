const metaInputField = document.getElementById("meta-keywords-input");
const metaChoices = new Choices(metaInputField, {
    removeItemButton: true,
    duplicateItemsAllowed: false,
})

let choices;
document.addEventListener("DOMContentLoaded", function () {
    const selectField = document.getElementById("choices-text-input");
    let debounceTimer;
    choices = new Choices(selectField, {
        removeItemButton: true,
        duplicateItemsAllowed: false,
        shouldSort: false,
        noResultsText: "نتیجه‌ای یافت نشد. اینتر بزنید تا اضافه شود.",
        allowAdditions: true,
        placeholder: true,
    });


    async function fetchTags(query) {
        if (!query || query.length < 3) return;

        console.log("Fetching tags for:", query);

        try {
            const response = await fetch(`/api/tags/search?query=${encodeURIComponent(query)}`);
            if (!response.ok) throw new Error("Failed to fetch tags");

            const tags = await response.json();
            updateChoices(tags);

            if (tags.length === 0) {
                addCustomTag(query);
            }
        } catch (error) {
            console.error("Error fetching tags:", error);
        }
    }

    function updateChoices(tags) {
        choices.clearChoices();
        choices.setChoices(tags.map(tag => ({ value: tag, label: tag })), 'value', 'label', true);
    }

    function addCustomTag(tag) {
        choices.setChoices([{ value: tag, label: tag }], 'value', 'label', true);
    }

    selectField.addEventListener("search", (event) => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            fetchTags(event.detail.value);
        }, 500);
    });

});

document.addEventListener("DOMContentLoaded", function () {
    let selectedTitle = "";
    document.querySelectorAll(".list-group-item").forEach(item => {
        item.addEventListener("click", function () {

            document.querySelectorAll(".list-group-item").forEach(i => i.classList.remove("selected"));

            this.classList.add("selected");

            let fullTitle = this.querySelector("strong").innerText;
            fullTitle = fullTitle.replace(/^\d+\.\s*/, "");
            switch (fullTitle) {
                case 'زیرتیتر توضیحی':
                    selectedTitle = 'DescriptiveSubheadline';
                    break;
                case 'زیرتیتر تکمیلی':
                    selectedTitle = 'SupplementarySubheadline';
                    break;
                case 'زیرتیتر عددی':
                    selectedTitle = 'NumericalSubheadline';
                    break;
                case 'زیرتیتر نقل قولی':
                    selectedTitle = 'QuoteSubheadline';
                    break;
                case 'زیرتیتر حسی':
                    selectedTitle = 'EmotionalSubheadline';
                    break;
                case 'زیرتیتر پرسشی':
                    selectedTitle = 'QuestionSubheadline';
                    break;
                case 'زیرتیتر مقایسه‌ای':
                    selectedTitle = 'ComparativeSubheadline';
                    break;
                case 'زیرتیتر شگفت‌انگیز':
                    selectedTitle = 'AmazingSubheadline';
                    break;
                case 'زیرتیتر معمولی':
                    selectedTitle = 'subheadlineSeo';
                    break;
                default:
                    selectedTitle = '';
                    break;
            }
        });
    });

    document.querySelector("#saveSubtitle").addEventListener("click", async function () {
        if (selectedTitle) {
            await createSubHeadline(selectedTitle);
            const modalElement = document.getElementById('subtitleModal');
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            modalInstance.hide();
        } else {
            Swal.fire({
                title: "خطا",
                text: "لطفاً یک نوع زیرتیتر را انتخاب کنید.",
                icon: "warning",
                confirmButtonText: "باشه"
            });
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    let selectedTitle = "";
    document.querySelectorAll(".list-group-headline .list-group-item").forEach(item => {
        item.addEventListener("click", function () {

            document.querySelectorAll(".list-group-headline .list-group-item").forEach(i => i.classList.remove("selected"));

            this.classList.add("selected");

            let fullTitle = this.querySelector("strong").innerText;
            fullTitle = fullTitle.replace(/^\d+\.\s*/, "");

            switch (fullTitle) {
                case 'روتیتر توضیحی':
                    selectedTitle = 'DescriptiveHeadline';
                    break;
                case 'روتیتر تکمیلی':
                    selectedTitle = 'SupplementaryHeadline';
                    break;
                case 'روتیتر عددی':
                    selectedTitle = 'NumericalHeadline';
                    break;
                case 'روتیتر نقل قولی':
                    selectedTitle = 'QuoteHeadline';
                    break;
                case 'روتیتر حسی':
                    selectedTitle = 'EmotionalHeadline';
                    break;
                case 'روتیتر پرسشی':
                    selectedTitle = 'QuestionHeadline';
                    break;
                case 'روتیتر مقایسه‌ای':
                    selectedTitle = 'ComparativeHeadline';
                    break;
                case 'روتیتر شگفت‌انگیز':
                    selectedTitle = 'AmazingHeadline';
                    break;
                case 'روتیتر معمولی':
                    selectedTitle = 'headlineSeo';
                    break;
                default:
                    selectedTitle = '';
                    break;
            }
        });
    });

    document.querySelector("#saveHeadline").addEventListener("click", async function () {
        if (selectedTitle) {
            await createHeadline(selectedTitle);
            const modalElement = document.getElementById('headlineModal');
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            modalInstance.hide();
        } else {
            Swal.fire({
                title: "خطا",
                text: "لطفاً یک نوع روتیتر را انتخاب کنید.",
                icon: "warning",
                confirmButtonText: "باشه"
            });
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    let selectedTitleType = "";
    document.querySelectorAll("#titleModal .list-group-item").forEach(item => {
        item.addEventListener("click", function () {
            document.querySelectorAll("#titleModal .list-group-item").forEach(i => i.classList.remove("selected"));
            this.classList.add("selected");
            let fullTitle = this.querySelector("strong").innerText;
            fullTitle = fullTitle.replace(/^[0-9]+\.\s*/, "");
            switch (fullTitle) {
                case 'عنوان خبری':
                    selectedTitleType = 'NewsTitle';
                    break;
                case 'عنوان توصیفی':
                    selectedTitleType = 'DescriptiveTitle';
                    break;
                case 'عنوان جذاب':
                    selectedTitleType = 'CatchyTitle';
                    break;
                case 'عنوان سوالی':
                    selectedTitleType = 'QuestionTitle';
                    break;
                case 'عنوان مقایسه‌ای':
                    selectedTitleType = 'ComparativeTitle';
                    break;
                case 'عنوان عددی':
                    selectedTitleType = 'NumericalTitle';
                    break;
                case 'عنوان شگفت‌انگیز':
                    selectedTitleType = 'AmazingTitle';
                    break;
                default:
                    selectedTitleType = '';
                    break;
            }
        });
    });

    document.querySelector("#saveTitle").addEventListener("click", async function () {
        if (selectedTitleType) {
            await createTitle(selectedTitleType);
            const modalElement = document.getElementById('titleModal');
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            modalInstance.hide();
        } else {
            Swal.fire({
                title: "خطا",
                text: "لطفاً یک نوع عنوان را انتخاب کنید.",
                icon: "warning",
                confirmButtonText: "باشه"
            });
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    let selectedLead = "";
    document.querySelectorAll(".list-group-lead .list-group-item").forEach(item => {
        item.addEventListener("click", function () {

            document.querySelectorAll(".list-group-lead .list-group-item").forEach(i => i.classList.remove("selected"));

            this.classList.add("selected");

            let fullTitle = this.querySelector("strong").innerText;
            fullTitle = fullTitle.replace(/^\d+\.\s*/, "");

            switch (fullTitle) {
                case 'سرنخ توضیحی':
                    selectedLead = 'DescriptiveLead';
                    break;
                case 'سرنخ تکمیلی':
                    selectedLead = 'SupplementaryLead';
                    break;
                case 'سرنخ عددی':
                    selectedLead = 'NumericalLead';
                    break;
                case 'سرنخ نقل قولی':
                    selectedLead = 'QuoteLead';
                    break;
                case 'سرنخ حسی':
                    selectedLead = 'EmotionalLead';
                    break;
                case 'سرنخ پرسشی':
                    selectedLead = 'QuestionLead';
                    break;
                case 'سرنخ مقایسه‌ای':
                    selectedLead = 'ComparativeLead';
                    break;
                case 'سرنخ شگفت‌انگیز':
                    selectedLead = 'AmazingLead';
                    break;
                case 'سرنخ معمولی':
                    selectedLead = 'leadSeo';
                    break;
                default:
                    selectedLead = '';
                    break;
            }
        });
    });

    document.querySelector("#saveLead").addEventListener("click", async function () {
        if (selectedLead) {
            await createLead(selectedLead);
            const modalElement = document.getElementById('leadModal');
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            modalInstance.hide();
        } else {
            Swal.fire({
                title: "خطا",
                text: "لطفاً یک نوع سرنخ را انتخاب کنید.",
                icon: "warning",
                confirmButtonText: "باشه"
            });
        }
    });
});

async function createTitle(selectedTitle) {
    const headlineInput = document.getElementById('news-title-input');

    const loadingAlert = Swal.fire({
        title: 'لطفاً صبر کنید...',
        html: '<div class="spinner"></div>',
        allowOutsideClick: false,
        showConfirmButton: false,
        willOpen: () => {
            Swal.showLoading();
        },
    });

    try {
        const editorInstance = CKEDITOR.instances.editor;
        const body = editorInstance.getData();

        if (body === "" || body == null) {
            Swal.fire({
                icon: 'warning',
                title: 'خطا',
                text: 'متن خبر خالی است. لطفاً متن خبر را وارد کنید.',
                confirmButtonText: 'باشه'
            });
            return;
        }

        const requestBody = {};
        requestBody[selectedTitle] = body;

        const response = await fetch('/api/gemini/modify', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(requestBody),
        });

        if (!response.ok) {
            throw new Error("جوابی گرفته نشد.");
        }

        const data = await response.json();

        if (data.content) {
            const htmlContent = data.content.replace(/```html|```/g, '').trim();
            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlContent, 'text/html');
            const generatedHeadline = doc.querySelector('#answer')?.innerHTML;

            if (generatedHeadline) {
                const strippedHeadline = generatedHeadline.replace(/<\/?[^>]+(>|$)/g, "").trim();
                headlineInput.value = "";
                headlineInput.value = strippedHeadline;
                Swal.fire({
                    icon: 'success',
                    title: 'تیتر ایجاد شد',
                    text: 'تیتر جدید با موفقیت ایجاد شد.',
                    confirmButtonText: 'باشه'
                });
            } else {
                console.warn("No content found in the 'answer' div.");
            }
        } else {
            console.error("No content field found in the response.");
        }
    } catch (error) {
        Swal.fire({
            icon: 'error',
            title: 'خطا',
            text: 'مشکلی پیش آمد. لطفاً دوباره تلاش کنید.',
            confirmButtonText: 'باشه'
        });
        console.error("Error:", error.message);
    } finally {
        loadingAlert.close();
    }
}

async function createHeadline(selectedTitle) {
    const headlineInput = document.getElementById('news-headline-input');

    const loadingAlert = Swal.fire({
        title: 'لطفاً صبر کنید...',
        html: '<div class="spinner"></div>',
        allowOutsideClick: false,
        showConfirmButton: false,
        willOpen: () => {
            Swal.showLoading();
        },
    });

    try {
        const editorInstance = CKEDITOR.instances.editor;
        const body = editorInstance.getData();

        if (body === "" || body == null) {
            Swal.fire({
                icon: 'warning',
                title: 'خطا',
                text: 'متن خبر خالی است. لطفاً متن خبر را وارد کنید.',
                confirmButtonText: 'باشه'
            });
            return;
        }

        const requestBody = {};
        requestBody[selectedTitle] = body;

        const response = await fetch('/api/gemini/modify', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(requestBody),
        });

        if (!response.ok) {
            throw new Error("جوابی گرفته نشد.");
        }

        const data = await response.json();

        if (data.content) {
            const htmlContent = data.content.replace(/```html|```/g, '').trim();
            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlContent, 'text/html');
            const generatedHeadline = doc.querySelector('#answer')?.innerHTML;

            if (generatedHeadline) {
                const strippedHeadline = generatedHeadline.replace(/<\/?[^>]+(>|$)/g, "").trim();
                headlineInput.value = "";
                headlineInput.value = strippedHeadline;
                Swal.fire({
                    icon: 'success',
                    title: 'رو تیتر ایجاد شد',
                    text: 'رو تیتر جدید با موفقیت ایجاد شد.',
                    confirmButtonText: 'باشه'
                });
            } else {
                console.warn("No content found in the 'answer' div.");
            }
        } else {
            console.error("No content field found in the response.");
        }
    } catch (error) {
        Swal.fire({
            icon: 'error',
            title: 'خطا',
            text: 'مشکلی پیش آمد. لطفاً دوباره تلاش کنید.',
            confirmButtonText: 'باشه'
        });
        console.error("Error:", error.message);
    } finally {
        loadingAlert.close();
    }
}

async function createSubHeadline(selectedTitle) {
    const subheadlineInput = document.getElementById('news-subheadline-input');

    const loadingAlert = Swal.fire({
        title: 'لطفاً صبر کنید...',
        html: '<div class="spinner"></div>',
        allowOutsideClick: false,
        showConfirmButton: false,
        willOpen: () => {
            Swal.showLoading();
        },
    });

    try {
        const editorInstance = CKEDITOR.instances.editor;
        const body = editorInstance.getData();

        if (body === "" || body == null) {
            Swal.fire({
                icon: 'warning',
                title: 'خطا',
                text: 'متن خبر خالی است. لطفاً متن خبر را وارد کنید.',
                confirmButtonText: 'باشه'
            });
            return;
        }
        const requestBody = {};
        requestBody[selectedTitle] = body;

        const response = await fetch('/api/gemini/modify', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(requestBody),
        });

        if (!response.ok) {
            throw new Error("جوابی گرفته نشد.");
        }

        const data = await response.json();

        if (data.content) {
            const htmlContent = data.content.replace(/```html|```/g, '').trim();
            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlContent, 'text/html');
            const generatedSubheadline = doc.querySelector('#answer')?.innerHTML;

            if (generatedSubheadline) {
                const strippedSubheadline = generatedSubheadline.replace(/<\/?[^>]+(>|$)/g, "").trim();
                subheadlineInput.value = "";
                subheadlineInput.value = strippedSubheadline;
                Swal.fire({
                    icon: 'success',
                    title: 'زیر تیتر ایجاد شد',
                    text: 'زیر تیتر جدید با موفقیت ایجاد شد.',
                    confirmButtonText: 'باشه'
                });
            } else {
                console.warn("No content found in the 'answer' div.");
            }
        } else {
            console.error("No content field found in the response.");
        }
    } catch (error) {
        Swal.fire({
            icon: 'error',
            title: 'خطا',
            text: 'مشکلی پیش آمد. لطفاً دوباره تلاش کنید.',
            confirmButtonText: 'باشه'
        });
        console.error("Error:", error.message);
    } finally {
        loadingAlert.close();
    }
}

async function createLead(selectedLead) {
    const leadInput = document.getElementById('news-lead-input');

    const loadingAlert = Swal.fire({
        title: 'لطفاً صبر کنید...',
        html: '<div class="spinner"></div>',
        allowOutsideClick: false,
        showConfirmButton: false,
        willOpen: () => {
            Swal.showLoading();
        },
    });

    try {
        const editorInstance = CKEDITOR.instances.editor;
        const body = editorInstance.getData();

        if (body === "" || body == null) {
            Swal.fire({
                icon: 'warning',
                title: 'خطا',
                text: 'متن خبر خالی است. لطفاً متن خبر را وارد کنید.',
                confirmButtonText: 'باشه'
            });
            return;
        }

        const requestBody = {};
        requestBody[selectedLead] = body;

        const response = await fetch('/api/gemini/modify', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(requestBody),
        });

        if (!response.ok) {
            throw new Error("جوابی گرفته نشد.");
        }

        const data = await response.json();

        if (data.content) {
            const htmlContent = data.content.replace(/```html|```/g, '').trim();
            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlContent, 'text/html');
            const generatedLead = doc.querySelector('#answer')?.innerHTML;

            if (generatedLead) {
                const strippedLead = generatedLead.replace(/<\/?[^>]+(>|$)/g, "").trim();
                leadInput.value = "";
                leadInput.value = strippedLead;
                Swal.fire({
                    icon: 'success',
                    title: 'سرنخ ایجاد شد',
                    text: 'سرنخ جدید با موفقیت ایجاد شد.',
                    confirmButtonText: 'باشه'
                });
            } else {
                console.warn("No content found in the 'answer' div.");
            }
        } else {
            console.error("No content field found in the response.");
        }
    } catch (error) {
        Swal.fire({
            icon: 'error',
            title: 'خطا',
            text: 'مشکلی پیش آمد. لطفاً دوباره تلاش کنید.',
            confirmButtonText: 'باشه'
        });
        console.error("Error:", error.message);
    } finally {
        loadingAlert.close();
    }
}

//Ckeditor Config
if (CKEDITOR.instances['editor']) {
    CKEDITOR.instances['editor'].destroy();
}

CKEDITOR.replace('editor', {
    language: 'fa',
    height: 600,
    extraPlugins: 'sendtoapi',
    toolbar: 'full',
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

//Filepond Config
FilePond.registerPlugin(
    FilePondPluginFileEncode,
    FilePondPluginFileValidateSize,
    FilePondPluginImageExifOrientation,
    FilePondPluginImagePreview
);
const firstInput = document.querySelector('input.filepond.first');
const pondFirst = FilePond.create(firstInput, {
    storeAsFile: true,
    labelIdle: '.فایل خود را در<span class="filepond--label-action">اینجا </span> آپلود کنید',
});

const secondInput = document.querySelector('input.filepond.second');
const pondSecond = FilePond.create(secondInput, {
    storeAsFile: true,
    labelIdle: '.فایل خود را در<span class="filepond--label-action">اینجا </span> آپلود کنید',
});

//VideoJs declaring
document.addEventListener('DOMContentLoaded', function () {
    videojs('news-video-player');
});

// Upload image
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("imageUploadForm");
    const modalElement = document.getElementById("addImage");
    const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
    const firstInput = document.querySelector('input.filepond.first');

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const formData = new FormData(form);
        const url = "/api/image/upload";

        const loadingAlert = Swal.fire({
            title: 'لطفاً صبر کنید...',
            html: '<div class="spinner"></div>',
            allowOutsideClick: false,
            showConfirmButton: false,
            willOpen: () => {
                Swal.showLoading();
            },
        });

        fetch(url, {
            method: "POST",
            body: formData,
        })
            .then((response) => response.json())
            .then((data) => {
                if (data.success) {
                    Swal.fire({
                        icon: "success",
                        title: "موفقیت",
                        text: data.success,
                        toast: true,
                        position: "top-end",
                        showConfirmButton: false,
                        timer: 3000,
                    });
                    form.reset();
                    pondFirst.removeFiles();
                } else {
                    Swal.fire({
                        icon: "error",
                        title: "خطا",
                        text: data.error || "مشکلی رخ داد.",
                        toast: true,
                        position: "top-end",
                        showConfirmButton: false,
                        timer: 3000,
                    });
                }
                modal.hide();
            })
            .catch((error) => {
                loadingAlert.close();
                Swal.fire({
                    icon: "error",
                    title: "خطا",
                    text: "خطایی رخ داد: " + error.message,
                    toast: true,
                    position: "top-end",
                    showConfirmButton: false,
                    timer: 3000,
                });
                modal.hide();
            });
    });
});

// Upload Video
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("videoUploadForm");
    const modalElement = document.getElementById("addVideo");
    const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const formData = new FormData(form);
        const url = "/api/video/upload";

        const loadingAlert = Swal.fire({
            title: 'لطفاً صبر کنید...',
            html: '<div class="spinner"></div>',
            allowOutsideClick: false,
            showConfirmButton: false,
            willOpen: () => {
                Swal.showLoading();
            },
        });

        fetch(url, {
            method: "POST",
            body: formData,
        })
            .then((response) => response.json())
            .then((data) => {
                if (data.success) {
                    Swal.fire({
                        icon: "success",
                        title: "موفقیت",
                        text: data.success,
                        toast: true,
                        position: "top-end",
                        showConfirmButton: false,
                        timer: 3000,
                    });
                    form.reset();
                    pondSecond.removeFiles();
                } else {
                    Swal.fire({
                        icon: "error",
                        title: "خطا",
                        text: data.error || "مشکلی رخ داد.",
                        toast: true,
                        position: "top-end",
                        showConfirmButton: false,
                        timer: 3000,
                    });
                }
                modal.hide();
            })
            .catch((error) => {
                loadingAlert.close();
                Swal.fire({
                    icon: "error",
                    title: "خطا",
                    text: "خطایی رخ داد: " + error.message,
                    toast: true,
                    position: "top-end",
                    showConfirmButton: false,
                    timer: 3000,
                });
                modal.hide();
            });
    });
});

// Image Manager Config
document.addEventListener("DOMContentLoaded", () => {
    const clearSelectionButton = document.getElementById("clearSelection");
    const previewContainer = document.getElementById("previewContainer");
    const previewImage = document.getElementById("previewImage");
    const mainImageInput = document.getElementById("mainImageInput");

    // Clear selection logic
    clearSelectionButton.addEventListener("click", () => {
        mainImageInput.value = ""; // Clear the hidden input
        previewImage.src = ""; // Clear the preview image
        previewContainer.style.display = "none"; // Hide the preview container
        clearSelectionButton.style.display = "none"; // Hide the clear button
    });
});

// Video Manager Config
document.addEventListener("DOMContentLoaded", () => {
    const clearVideoSelectionButton = document.getElementById("clearVideoSelection");
    const videoPreviewContainer = document.getElementById("videoPreviewContainer");
    const previewVideo = document.getElementById("previewVideo");
    const mainVideoInput = document.getElementById("mainVideoInput");

    // Clear video selection logic
    clearVideoSelectionButton.addEventListener("click", () => {
        mainVideoInput.value = ""; // Clear the hidden input
        previewVideo.src = ""; // Clear the preview video
        videoPreviewContainer.style.display = "none"; // Hide the preview container
        clearVideoSelectionButton.style.display = "none"; // Hide the clear button
    });
});

// Fetch file manager images
document.addEventListener("DOMContentLoaded", function () {
    const imagesContainer = document.getElementById("imagesContainer");
    const paginationContainer = document.getElementById("paginationContainer");
    const searchInput = document.getElementById("searchInput");
    const pageSize = 12; // Items per page
    let currentPage = 0;
    let allImages = []; // Store all images for filtering

    // Fetch images from the server
    function fetchImages(page) {
        fetch(`/api/image/fetch?page=${page}&size=${pageSize}`)
            .then((response) => response.json())
            .then((data) => {
                console.log("Fetched data:", data); // Log the entire response to see if everything is correct
                allImages = data.images; // Store all images
                renderImages(allImages); // Render images
                renderPagination(data.currentPage, data.totalPages);
            })
            .catch((error) => {
                console.error("Error fetching images:", error);
                imagesContainer.innerHTML = `
                    <div class="text-center">
                        <p class="text-danger">خطایی در بارگیری تصاویر رخ داد.</p>
                    </div>
                `;
            });
    }

    // Render images in the modal
    function renderImages(images) {
        imagesContainer.innerHTML = ""; // Clear previous images
        if (images.length === 0) {
            imagesContainer.innerHTML = `
                <div class="text-center">
                    <p class="text-muted">تصویری موجود نیست.</p>
                </div>
            `;
        } else {
            images.forEach((image) => {
                const imageCard = `
                    <div class="col-md-3 col-sm-4 col-6 mb-4">
                        <div class="card image-container shadow-sm">
                            <img src="${image.imageUrl}" data-image="${image.imageUrl}" class="card-img-top" 
                                 style="width: 150px; height: 100px;" alt="تصویر" onclick="selectImage(this)">
                        </div>
                    </div>
                `;
                imagesContainer.insertAdjacentHTML("beforeend", imageCard);
            });
        }
    }

    // Render pagination links
    function renderPagination(currentPage, totalPages) {
        console.log("Rendering pagination:", {currentPage, totalPages});

        paginationContainer.innerHTML = ""; // Clear existing pagination
        if (totalPages <= 1) return; // Skip pagination if only one page

        // Previous button
        const prevDisabled = currentPage === 0 ? "disabled" : "";
        paginationContainer.innerHTML += `
            <li class="page-item ${prevDisabled}">
                <a class="page-link" href="#" data-page="${currentPage - 1}" ${prevDisabled && 'tabindex="-1"'}>→ &nbsp; قبلی</a>
            </li>
        `;

        // Page numbers
        for (let i = 0; i < totalPages; i++) {
            const activeClass = currentPage === i ? "active" : "";
            paginationContainer.innerHTML += `
            <li class="page-item ${activeClass}">
                <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
            </li>
        `;
        }

        // Next button
        const nextDisabled = currentPage === totalPages - 1 ? "disabled" : "";
        paginationContainer.innerHTML += `
            <li class="page-item ${nextDisabled}">
                <a class="page-link" href="#" data-page="${currentPage + 1}" ${nextDisabled && 'tabindex="-1"'}>بعدی &nbsp;←</a>
            </li>
        `;

        // Add event listeners to pagination links
        document.querySelectorAll("#paginationContainer .page-link").forEach((link) => {
            link.addEventListener("click", function (event) {
                event.preventDefault();
                const page = parseInt(this.getAttribute("data-page"));
                if (!isNaN(page)) {
                    currentPage = page;
                    fetchImages(page);
                }
            });
        });
    }

    // Filter images based on search input
    function filterImages() {
        const searchTerm = searchInput.value.toLowerCase();
        const filteredImages = allImages.filter(image =>
            image.imageUrl.toLowerCase().includes(searchTerm)
        );
        renderImages(filteredImages);
    }

    // Event listener to fetch images when the modal is shown
    const galleryModal = document.getElementById("galleryModal");
    galleryModal.addEventListener("show.bs.modal", function () {
        fetchImages(currentPage); // Fetch the first page when the modal opens
    });

    // Event listener for search input
    searchInput.addEventListener("keyup", filterImages);
});

// Fetch file manager videos
document.addEventListener("DOMContentLoaded", function () {
    const videoManagerButton = document.getElementById("VideoFileManagerButton");
    const videoGallery = document.querySelector("#videoGalleryModal .modal-body .row");

    function fetchAndRenderVideos() {
        fetch("/api/video/fetch")
            .then((response) => response.json())
            .then((data) => {
                videoGallery.innerHTML = "";
                if (data.length === 0) {
                    videoGallery.innerHTML = `
                        <div class="text-center">
                            <p class="text-muted">ویدیویی موجود نیست.</p>
                        </div>
                    `;
                } else {
                    data.forEach((videoMap) => {
                        const videoUrl = videoMap.videoUrl;
                        const videoCard = `
                            <div class="col-md-3 col-sm-4 col-6 mb-4">
                                <div class="card video-container shadow-sm">
                                    <video class="card-img-top" style="width: 100%; height: auto;">
                                        <source src="${videoUrl}" data-video="${videoUrl}" type="video/mp4">
                                        مرورگر شما از پخش ویدیو پشتیبانی نمی‌کند.
                                    </video>
                                    <button class="btn btn-primary mt-2" src="${videoUrl}" data-video="${videoUrl}"onclick="selectVideo(this)">انتخاب ویدیو</button>
                                </div>
                            </div>
                        `;
                        videoGallery.insertAdjacentHTML("beforeend", videoCard);
                    });
                }
            })
            .catch((error) => {
                console.error("Error fetching videos:", error);
                videoGallery.innerHTML = `
                    <div class="text-center">
                        <p class="text-danger">خطایی در بارگیری ویدیوها رخ داد.</p>
                    </div>
                `;
            });
    }

    videoManagerButton.addEventListener("click", function () {
        fetchAndRenderVideos(); // Fetch videos before showing the modal
    });
});

// Fetch file manager images and render them
document.addEventListener("DOMContentLoaded", function () {
    const fileManagerButton = document.getElementById("fileManagerImagesButton");
    const galleryModalBody = document.querySelector("#galleryImagesModal .modal-body .row");
    const paginationImagesContainer = document.getElementById("paginationImagesContainer");
    const searchInput = document.getElementById("searchImagesInput"); // Search input
    const pageSize = 12;
    let currentPage = 0;
    let allImages = []; // Store all images for filtering

    // Fetch images from the server
    function fetchImages(page) {
        fetch(`/api/image/fetch?page=${page}&size=${pageSize}`)
            .then((response) => response.json())
            .then((data) => {
                console.log("Fetched data:", data); // Log the entire response to see if everything is correct
                allImages = data.images; // Store all images
                renderImages(allImages); // Render images
                renderPagination(data.currentPage, data.totalPages);
            })
            .catch((error) => {
                console.error("Error fetching images:", error);
                galleryModalBody.innerHTML = `
                    <div class="text-center">
                        <p class="text-danger">خطایی در بارگیری تصاویر رخ داد.</p>
                    </div>
                `;
            });
    }

    // Render images in the modal
    function renderImages(images) {
        galleryModalBody.innerHTML = ""; // Clear previous images
        if (images.length === 0) {
            galleryModalBody.innerHTML = `
                <div class="text-center">
                    <p class="text-muted">تصویری موجود نیست.</p>
                </div>
            `;
        } else {
            images.forEach((image) => {
                const imageCard = `
                    <div class="col-md-3 col-sm-4 col-6 mb-4">
                        <div class="card image-container shadow-sm">
                            <img src="${image.imageUrl}" data-image="${image.imageUrl}" class="card-img-top"
                                 onclick="selectImages(this)" style="width: 150px; height: 100px;" alt="تصویر">
                        </div>
                    </div>
                `;
                galleryModalBody.insertAdjacentHTML("beforeend", imageCard);
            });
        }
    }

    // Render pagination links
    function renderPagination(currentPage, totalPages) {
        paginationImagesContainer.innerHTML = "";
        if (totalPages <= 1) return;

        const prevDisabled = currentPage === 0 ? "disabled" : "";
        paginationImagesContainer.innerHTML += `
            <li class="page-item ${prevDisabled}">
                <a class="page-link" href="#" data-page="${currentPage - 1}" ${prevDisabled && 'tabindex="-1"'}>→ قبلی</a>
            </li>
        `;

        for (let i = 0; i < totalPages; i++) {
            const activeClass = currentPage === i ? "active" : "";
            paginationImagesContainer.innerHTML += `
                <li class="page-item ${activeClass}">
                    <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
                </li>
            `;
        }

        const nextDisabled = currentPage === totalPages - 1 ? "disabled" : "";
        paginationImagesContainer.innerHTML += `
            <li class="page-item ${nextDisabled}">
                <a class="page-link" href="#" data-page="${currentPage + 1}" ${nextDisabled && 'tabindex="-1"'}>بعدی ←</a>
            </li>
        `;

        // Add event listeners to pagination links
        document.querySelectorAll("#paginationImagesContainer .page-link").forEach((link) => {
            link.addEventListener("click", function (event) {
                event.preventDefault();
                const page = parseInt(this.getAttribute("data-page"));
                if (!isNaN(page)) {
                    currentPage = page;
                    fetchImages(page);
                }
            });
        });
    }

    // Filter images based on search input
    function filterImages() {
        const searchTerm = searchInput.value.toLowerCase();
        const filteredImages = allImages.filter(image =>
            image.imageUrl.toLowerCase().includes(searchTerm)
        );
        renderImages(filteredImages); // Render filtered images
    }

    // Event listener for search input
    searchInput.addEventListener("keyup", filterImages);

    // Event listener to fetch images when the modal is shown
    const galleryImagesModal = document.getElementById("galleryImagesModal");
    galleryImagesModal.addEventListener("show.bs.modal", function () {
        fetchImages(currentPage); // Fetch the first page when the modal opens
    });

    // Event listener for the file manager button to trigger fetching images
    fileManagerButton.addEventListener("click", function () {
        fetchImages(currentPage);
    });
});

// Select video from file manager
function selectVideo(videoElement) {
    const videoUrl = videoElement.getAttribute("data-video");
    const fileName = videoUrl.split("/").pop();

    // Set the value of the hidden input field
    const mainVideoInput = document.getElementById("mainVideoInput");
    mainVideoInput.value = fileName;

    // Update the video preview
    const previewVideo = document.getElementById("previewVideo");
    previewVideo.src = videoUrl;

    // Show the preview and clear button
    document.getElementById("videoPreviewContainer").style.display = "block";
    document.getElementById("clearVideoSelection").style.display = "block";

    // Close the modal
    const modalElement = document.getElementById("videoGalleryModal");
    const modalInstance = bootstrap.Modal.getOrCreateInstance(modalElement);
    modalInstance.hide();
}

// Select image from file manager
function selectImage(imageElement) {
    const imageUrl = imageElement.getAttribute("data-image");
    const fileName = imageUrl.split("/").pop();

    console.log(fileName);

    // Set the value of the hidden input field
    const mainImageInput = document.getElementById("mainImageInput");
    mainImageInput.value = fileName;

    // Update the preview image
    const previewImage = document.getElementById("previewImage");
    previewImage.src = `/news/photos/${fileName}`;

    // Show the preview and clear button
    document.getElementById("previewContainer").style.display = "block";
    document.getElementById("clearSelection").style.display = "block";

    // Close the modal
    const modalElement = document.getElementById("galleryModal");
    const modalInstance = bootstrap.Modal.getOrCreateInstance(modalElement);
    modalInstance.hide();
}

// Select images from the file manager (multiple selection)
function selectImages(imageElement) {
    // Ensure that the 'data-image' attribute is used to fetch the correct URL
    const imageUrl = imageElement.getAttribute("data-image");  // Retrieve the 'data-image' attribute
    console.log("Selected Image URL:", imageUrl);

    // If imageUrl is null, check for the correct setting of 'data-image'
    if (!imageUrl) {
        console.error("No image URL found in the 'data-image' attribute");
    }

    const mainImageInput = document.getElementById("mainImagesInput");
    let selectedImages = mainImageInput.value ? mainImageInput.value.split(",") : [];

    if (!selectedImages.includes(imageUrl)) {
        selectedImages.push(imageUrl);
    }

    mainImageInput.value = selectedImages.join(",");
    console.log("Updated Hidden Input Value:", mainImageInput.value);

    const previewContainer = document.getElementById("previewImagesContainer");
    const selectedImagesPreview = document.getElementById("selectedImagesPreview");
    selectedImagesPreview.innerHTML = "";
    selectedImages.forEach(url => {
        const imgElement = document.createElement("img");
        imgElement.src = url;
        imgElement.style.width = "100px";
        selectedImagesPreview.appendChild(imgElement);
    });

    previewContainer.style.display = "block";
    document.getElementById("clearMultipleSelection").style.display = "block";

    const modalElement = document.getElementById("galleryImagesModal");
    const modalInstance = bootstrap.Modal.getOrCreateInstance(modalElement);
    modalInstance.hide();
}

// Clear selected images when the delete button is clicked
document.getElementById("clearMultipleSelection").addEventListener("click", function() {
    const mainImageInput = document.getElementById("mainImagesInput");
    mainImageInput.value = "";

    const selectedImagesPreview = document.getElementById("selectedImagesPreview");
    selectedImagesPreview.innerHTML = "";

    document.getElementById("previewImagesContainer").style.display = "none";
    document.getElementById("clearMultipleSelection").style.display = "none";
});

// Generate alt text using AI
async function generateAltText() {
    const preloader = document.getElementById('alt-text-preloader');
    const button = document.getElementById('generate-alt-text-btn');
    const altTextInput = document.getElementById('imageAltText');
    const mainImageInput = document.getElementById('mainImageInput');
    
    try {
        // Show loading state
        preloader.style.display = 'block';
        button.style.opacity = '0.5';
        button.disabled = true;

        // Get current form data
        const newsTitle = document.getElementById('news-title-input').value;
        const newsContent = CKEDITOR.instances.editor.getData();
        const imageFileName = mainImageInput.value;

        // Validate inputs
        if (!imageFileName || imageFileName.trim() === '') {
            Swal.fire({
                icon: 'warning',
                title: 'خطا',
                text: 'ابتدا تصویری انتخاب کنید.',
                confirmButtonText: 'باشه'
            });
            return;
        }

        if (!newsTitle || newsTitle.trim() === '') {
            Swal.fire({
                icon: 'warning',
                title: 'خطا',
                text: 'ابتدا عنوان خبر را وارد کنید.',
                confirmButtonText: 'باشه'
            });
            return;
        }

        if (!newsContent || newsContent.trim() === '') {
            Swal.fire({
                icon: 'warning',
                title: 'خطا',
                text: 'ابتدا محتوای خبر را وارد کنید.',
                confirmButtonText: 'باشه'
            });
            return;
        }

        // Prepare image URL
        const imageUrl = `/news/photos/${imageFileName}`;

        // Call the API
        const response = await fetch('/api/gemini/generate-alt-text', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                imageUrl: imageUrl,
                newsTitle: newsTitle,
                newsContent: newsContent
            }),
        });

        if (!response.ok) {
            throw new Error('خطا در ارتباط با سرور');
        }

        const data = await response.json();

        if (data.error) {
            throw new Error(data.error);
        }

        if (data.altText) {
            // Update the alt text input field
            altTextInput.value = data.altText;
            
            // Also update the hidden field for form submission
            const mainImageAltText = document.getElementById('mainImageAltText');
            mainImageAltText.value = data.altText;

            // Show success message
            Swal.fire({
                icon: 'success',
                title: 'موفقیت',
                text: 'متن جایگزین تصویر با موفقیت تولید شد.',
                confirmButtonText: 'باشه'
            });
        } else {
            throw new Error('پاسخ نامعتبر از سرور');
        }

    } catch (error) {
        console.error('Error generating alt text:', error);
        Swal.fire({
            icon: 'error',
            title: 'خطا',
            text: error.message || 'خطایی در تولید متن جایگزین تصویر رخ داد.',
            confirmButtonText: 'باشه'
        });
    } finally {
        // Hide loading state
        preloader.style.display = 'none';
        button.style.opacity = '1';
        button.disabled = false;
    }
}

// Add event listener for the alt text generation button
document.addEventListener('DOMContentLoaded', function() {
    const generateAltTextBtn = document.getElementById('generate-alt-text-btn');
    if (generateAltTextBtn) {
        generateAltTextBtn.addEventListener('click', generateAltText);
    }

    // Update hidden alt text field when user types in the visible field
    const altTextInput = document.getElementById('imageAltText');
    const mainImageAltText = document.getElementById('mainImageAltText');
    if (altTextInput && mainImageAltText) {
        altTextInput.addEventListener('input', function() {
            mainImageAltText.value = this.value;
        });
    }
});

async function produceTag() {
    const preloader = document.getElementById('preloader-tag');
    const button = document.getElementById('tag-button');
    try {
        preloader.style.display = 'block';
        button.style.width = '80%';

        const editorInstance = CKEDITOR.instances.editor;
        const body = editorInstance.getData();

        if (body === "" || body == null) {
            Swal.fire({
                icon: 'warning',
                title: 'خطا',
                text: 'متن خبر خالی است.',
                confirmButtonText: 'باشه'
            });
            return;
        }

        const response = await fetch('/api/gemini/modify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ "tag": body }),
        });

        if (!response.ok) {
            throw new Error("جوابی گرفته نشد.");
        }

        const data = await response.json();

        if (data.content) {
            const htmlContent = data.content.replace(/```html|```/g, '').trim();
            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlContent, 'text/html');
            const innerHTML = doc.querySelector('#answer')?.innerHTML;



            if (innerHTML) {
                const tags = innerHTML.split(',').map(tag => tag.trim());
                if (choices) {
                    choices.removeActiveItems();
                    choices.clearStore();
                    choices.setValue(tags.map(tag => ({ value: tag, label: tag })));
                }

            } else {
                console.warn("No content found in the 'answer' div.");
            }
        } else {
            console.error("No content field found in the response.");
        }
    } catch (error) {
        console.error("Error:", error.message);
    } finally {
        preloader.style.display = 'none';
        button.style.width = '100%';
    }
}

async function produceMeta() {
    const preloader = document.getElementById('preloader-meta');
    const button = document.getElementById('meta-button');
    const metaTitle = document.getElementById('meta-title-input');
    const metaDescription = document.getElementById('meta-description-input');

    try {
        preloader.style.display = 'block';
        button.style.width = '80%';

        const editorInstance = CKEDITOR.instances.editor;
        const body = editorInstance.getData();

        if (body === "" || body == null) {
            Swal.fire({
                icon: 'warning',
                title: 'خطا',
                text: 'متن خبر خالی است.',
                confirmButtonText: 'باشه'
            });
            return;
        }

        const responseTitle = await fetch('/api/gemini/modify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ "metaTitle": body }),
        });

        if (!responseTitle.ok) {
            throw new Error("جوابی گرفته نشد.");
        }

        const dataTitle = await responseTitle.json();

        if (dataTitle.content) {
            const htmlContent = dataTitle.content.replace(/```html|```/g, '').trim();
            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlContent, 'text/html');
            const innerHTML = doc.querySelector('#answer')?.innerHTML;


            if (innerHTML) {
                metaTitle.value = "";
                metaTitle.value = innerHTML;
            } else {
                console.warn("No content found in the 'answer' div.");
            }
        } else {
            console.error("No content field found in the responseTitle.");
        }



        const responseKey = await fetch('/api/gemini/modify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ "metaTag": body }),
        });

        if (!responseKey.ok) {
            throw new Error("جوابی گرفته نشد.");
        }

        const dataKey = await responseKey.json();

        if (dataKey.content) {
            const htmlContent = dataKey.content.replace(/```html|```/g, '').trim();
            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlContent, 'text/html');
            const innerHTML = doc.querySelector('#answer')?.innerHTML;

            if (innerHTML) {
                const tags = innerHTML.split(',').map(tag => tag.trim());
                console.log(tags);
                metaInputField.choicesInstance = metaChoices;
                metaChoices.clearStore();
                metaChoices.setValue(tags);
            } else {
                console.warn("No content found in the 'answer' div.");
            }
        } else {
            console.error("No content field found in the responseKey.");
        }



        const responseBody = await fetch('/api/gemini/modify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ "metaDescription": body }),
        });

        if (!responseBody.ok) {
            throw new Error("جوابی گرفته نشد.");
        }

        const dataBody = await responseBody.json();

        if (dataBody.content) {
            const htmlContent = dataBody.content.replace(/```html|```/g, '').trim();
            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlContent, 'text/html');
            const innerHTML = doc.querySelector('#answer')?.innerHTML;

            if (innerHTML) {
                metaDescription.value = "";
                metaDescription.value = innerHTML;
            } else {
                console.warn("No content found in the 'answer' div.");
            }
        } else {
            console.error("No content field found in the responseBody.");
        }
    } catch (error) {
        console.error("Error:", error.message);
    } finally {
        preloader.style.display = 'none';
        button.style.width = '100%';
    }
}

document.addEventListener('DOMContentLoaded', function () {
    // Initialize Persian Date Picker with format conversion
    $('#persian-date-picker').persianDatepicker({
        initialValue: false,
        format: 'YYYY/MM/DD HH:mm',
        autoClose: true,
        timePicker: {
            enabled: true,
            meridiem: {
                enabled: false
            }
        },
        calendar: {
            persian: {
                locale: 'fa',
                showHint: true,
                leapYearMode: 'algorithmic'
            }
        },
        navigator: {
            enabled: true,
            scroll: {
                enabled: true
            },
            text: {
                btnNextText: "بعد",
                btnPrevText: "قبل"
            }
        },
        toolbox: {
            enabled: true,
            calendarSwitch: {
                enabled: true,
                format: 'MMMM'
            },
            todayButton: {
                enabled: true,
                text: {
                    fa: "امروز"
                }
            },
            submitButton: {
                enabled: true,
                text: {
                    fa: "تایید"
                }
            }
        },
        dayPicker: {
            enabled: true,
            titleFormat: 'YYYY MMMM'
        },
        responsive: true,
        inline: false,
        fontSize: 13,
        calendarType: 'persian',
        inputDelay: 800,
        observer: true,
        onSelect: function (unix) {
            // Convert Persian date to Gregorian format for backend
            convertAndSetDate(unix);
        }
    });

    // Function to convert Unix timestamp to Gregorian format
    function convertAndSetDate(unixTimestamp) {
        try {
            // Convert to JavaScript Date object
            const jsDate = new Date(unixTimestamp);

            // Format for backend: YYYY-MM-DD HH:mm:ss
            const gregorianFormatted = formatDateForBackend(jsDate);

            // Set the hidden input value that will be sent to backend
            document.getElementById('gregorian-date-input').value = gregorianFormatted;

            console.log('Date converted:', {
                persian: document.getElementById('persian-date-picker').value,
                gregorian: gregorianFormatted,
                unix: unixTimestamp
            });

        } catch (error) {
            console.error('Error converting date:', error);
        }
    }

    // Format date for backend
    function formatDateForBackend(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        const microseconds = String(date.getMilliseconds() * 1000).padStart(6, '0');

        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}.${microseconds}`;
    }


    // Function to open date picker when clicking calendar icon
    window.openDatePicker = function () {
        $('#persian-date-picker').focus();
    };

    // Set initial date if editing existing news
    function setInitialDate() {
        const gregorianInput = document.getElementById('gregorian-date-input');
        if (gregorianInput && gregorianInput.value) {
            try {
                const jsDate = new Date(gregorianInput.value);
                const persianDate = new persianDate(jsDate);
                const persianFormatted = persianDate.format('YYYY/MM/DD HH:mm');

                $('#persian-date-picker').val(persianFormatted);
                console.log('Initial date set:', {
                    gregorian: gregorianInput.value,
                    persian: persianFormatted
                });
            } catch (error) {
                console.error('Error setting initial date:', error);
            }
        }
    }

    // Set initial date after a short delay to ensure DOM is ready
    setTimeout(setInitialDate, 500);

    // Enhanced form validation (keeps your existing validation)
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function (e) {
            const dateInput = document.getElementById('persian-date-picker');
            const gregorianInput = document.getElementById('gregorian-date-input');

            // If Persian date is empty but we want to allow empty dates for auto-setting
            if (!dateInput.value.trim()) {
                gregorianInput.value = ''; // Ensure hidden field is also empty
                const confirmMsg = 'تاریخ انتشار خالی است. سیستم زمان فعلی را در نظر خواهد گرفت. ادامه می‌دهید؟';
                if (!confirm(confirmMsg)) {
                    e.preventDefault();
                    dateInput.focus();
                    return false;
                }
            }
        });
    }
});



