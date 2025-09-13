CKEDITOR.plugins.add('sendtoapi', {
    icons: 'sendtoapi',
    init: function (editor) {
        editor.addCommand('sendToApiCommand', {
            exec: function (editor) {
                const editorData = editor.getData();
                if (editorData === "" || editorData == null) {
                    Swal.fire({
                        icon: 'warning',
                        title: 'متن خبر خالی است.',
                        text: 'لطفاً متن را وارد کنید.',
                    });
                    return;
                }

                const apiUrl = '/api/gemini/modify';

                Swal.fire({
                    title: 'لطفاً صبر کنید...',
                    html: '<div class="spinner"></div>',
                    allowOutsideClick: false,
                    showConfirmButton: false,
                    willOpen: () => {
                        Swal.showLoading();
                    },
                });

                fetch(apiUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ bodySeo: editorData }),
                })
                    .then((response) => {
                        if (!response.ok) {
                            throw new Error(`HTTP error! status: ${response.status}`);
                        }
                        return response.json();
                    })
                    .then((data) => {
                        Swal.close();
                        if (data.content) {
                            const htmlContent = data.content.replace(/```html|```/g, '').trim();
                            const parser = new DOMParser();
                            const doc = parser.parseFromString(htmlContent, 'text/html');
                            const innerHTML = doc.querySelector('#answer')?.innerHTML;

                            if (innerHTML) {
                                editor.setData(innerHTML);
                                Swal.fire({
                                    icon: 'success',
                                    title: 'عملیات موفقیت‌آمیز',
                                    text: 'متن خبر شما با موفقیت اصلاح شد!',
                                    confirmButtonText: "تایید",
                                });
                            } else {
                                console.warn("No content found in the 'answer' div.");
                                editor.setData(editorData);
                            }
                        } else {
                            console.error("هیچ متنی یافت نشد. متن خبر شما تغییری نخواهد کرد");
                            Swal.fire({
                                icon: 'error',
                                title: 'خطا',
                                text: 'هیچ متنی یافت نشد. متن خبر شما تغییری نخواهد کرد.',
                                confirmButtonText: "تایید",
                            });
                            editor.setData(editorData);
                        }
                    })
                    .catch((error) => {
                        Swal.close();
                        console.error('Error:', error);
                        Swal.fire({
                            icon: 'error',
                            title: 'مشکل در ارسال درخواست',
                            text: 'مشکلی در فرستادن درخواست به GEMINI پیش آمده است.',
                            confirmButtonText: "تایید",
                        });
                    });
            },
        });

        editor.ui.addButton('SendToApi', {
            label: 'Send to API',
            command: 'sendToApiCommand',
            toolbar: 'insert',
            icon: '/assets/images/magic-wand-wizard-svgrepo-com.svg'
        });
    },
});
