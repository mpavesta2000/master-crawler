var socket = new SockJS('/ws');
// for server
// var socket = new SockJS(window.location.protocol + '//' + window.location.host + '/ws');
var stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/logs', function (message) {
        var log = message.body;
        var logsContainer = document.getElementById('logsContainer');
        var logElement = document.createElement('p');
        logElement.textContent = log;

        if (log.includes('Error') || log.includes('Exception')) {
            logElement.classList.add('error');
        }

        logsContainer.appendChild(logElement);
        logsContainer.scrollTop = logsContainer.scrollHeight;
    });
});

document.getElementById('bornaRSSToggle').addEventListener('change', function () {
    var enabled = this.checked;
    fetch('/api/borna-rss/toggle', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ enabled: enabled })
    })
        .then(response => {
            if (response.ok) {
                Swal.fire({
                    icon: 'success',
                    title: 'عملیات موفقیت‌آمیز',
                    text: enabled ? 'RSS خبرگزاری برنا فعال شد' : 'RSS خبرگزاری برنا غیرفعال شد',
                    confirmButtonText: 'تایید'
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: 'خطا',
                    text: 'مشکلی در تغییر وضعیت RSS رخ داده است',
                    confirmButtonText: 'تایید'
                });
            }
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                title: 'خطای ارتباطی',
                text: 'مشکل در برقراری ارتباط با سرور',
                confirmButtonText: 'تایید'
            });
        });
});


document.getElementById('setMaxNewsBtn').addEventListener('click', function() {
    var maxNews = document.getElementById('maxNewsInput').value;

    fetch('/api/borna-rss/max-news', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ maxNews: parseInt(maxNews) })
    })
        .then(response => {
            if (response.ok) {
                Swal.fire({
                    icon: 'success',
                    title: 'تنظیمات موفقیت‌آمیز',
                    text: 'حداکثر تعداد اخبار به ' + maxNews + ' عدد تنظیم شد',
                    confirmButtonText: 'تأیید'
                });
            } else {
                response.json().then(errorData => {
                    Swal.fire({
                        icon: 'error',
                        title: errorData.title || 'خطا',
                        text: errorData.message || 'مشکلی در تنظیم حداکثر اخبار رخ داده است',
                        confirmButtonText: 'تایید'
                    });
                }).catch(() => {
                    Swal.fire({
                        icon: 'error',
                        title: 'خطا',
                        text: 'مشکلی در تنظیم حداکثر اخبار رخ داده است',
                        confirmButtonText: 'تایید'
                    });
                });
            }
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                title: 'خطای ارتباطی',
                text: 'مشکل در برقراری ارتباط با سرور',
                confirmButtonText: 'تایید'
            });
        });
});


document.getElementById('fararuRSSToggle').addEventListener('change', function () {
    var enabled = this.checked;
    fetch('/api/fararu-rss/toggle', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ enabled: enabled })
    })
        .then(response => {
            if (response.ok) {
                Swal.fire({
                    icon: 'success',
                    title: 'عملیات موفقیت‌آمیز',
                    text: enabled ? 'RSS خبرگزاری فرارو فعال شد' : 'RSS خبرگزاری فرارو غیرفعال شد',
                    confirmButtonText: 'تایید'
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: 'خطا',
                    text: 'مشکلی در تغییر وضعیت RSS رخ داده است',
                    confirmButtonText: 'تایید'
                });
            }
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                title: 'خطای ارتباطی',
                text: 'مشکل در برقراری ارتباط با سرور',
                confirmButtonText: 'تایید'
            });
        });
});


document.getElementById('setFararuMaxNewsBtn').addEventListener('click', function() {
    var maxNews = document.getElementById('maxNewsInput').value;

    fetch('/api/fararu-rss/max-news', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ maxNews: parseInt(maxNews) })
    })
        .then(response => {
            if (response.ok) {
                Swal.fire({
                    icon: 'success',
                    title: 'تنظیمات موفقیت‌آمیز',
                    text: 'حداکثر تعداد اخبار به ' + maxNews + ' عدد تنظیم شد',
                    confirmButtonText: 'تأیید'
                });
            } else {
                response.json().then(errorData => {
                    Swal.fire({
                        icon: 'error',
                        title: errorData.title || 'خطا',
                        text: errorData.message || 'مشکلی در تنظیم حداکثر اخبار رخ داده است',
                        confirmButtonText: 'تایید'
                    });
                }).catch(() => {
                    Swal.fire({
                        icon: 'error',
                        title: 'خطا',
                        text: 'مشکلی در تنظیم حداکثر اخبار رخ داده است',
                        confirmButtonText: 'تایید'
                    });
                });
            }
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                title: 'خطای ارتباطی',
                text: 'مشکل در برقراری ارتباط با سرور',
                confirmButtonText: 'تایید'
            });
        });
});

// BBC Arabic RSS Events
document.getElementById('bbcArabicRSSToggle').addEventListener('change', function () {
    var enabled = this.checked;
    fetch('/api/bbcarabic-rss/toggle', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ enabled: enabled })
    })
        .then(response => {
            if (response.ok) {
                Swal.fire({
                    icon: 'success',
                    title: 'عملیات موفقیت‌آمیز',
                    text: enabled ? 'RSS بی بی سی عربی فعال شد' : 'RSS بی بی سی عربی غیرفعال شد',
                    confirmButtonText: 'تایید'
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: 'خطا',
                    text: 'مشکلی در تغییر وضعیت RSS رخ داده است',
                    confirmButtonText: 'تایید'
                });
            }
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                title: 'خطا',
                text: 'مشکلی در تغییر وضعیت RSS رخ داده است',
                confirmButtonText: 'تایید'
            });
            this.checked = !enabled;
        });
});

document.getElementById('setBBCArabicMaxNewsBtn').addEventListener('click', function () {
    var maxNews = document.getElementById('maxBBCArabicNewsInput').value;

    fetch('/api/bbcarabic-rss/max-news', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ maxNews: parseInt(maxNews) })
    })
        .then(response => {
            if (response.ok) {
                Swal.fire({
                    icon: 'success',
                    title: 'تنظیمات موفقیت‌آمیز',
                    text: 'حداکثر تعداد اخبار بی بی سی عربی به ' + maxNews + ' عدد تنظیم شد',
                    confirmButtonText: 'تأیید'
                });
            } else {
                response.json().then(errorData => {
                    Swal.fire({
                        icon: 'error',
                        title: errorData.title || 'خطا',
                        text: errorData.message || 'مشکلی در تنظیم حداکثر اخبار رخ داده است',
                        confirmButtonText: 'تایید'
                    });
                }).catch(() => {
                    Swal.fire({
                        icon: 'error',
                        title: 'خطا',
                        text: 'مشکلی در تنظیم حداکثر اخبار رخ داده است',
                        confirmButtonText: 'تایید'
                    });
                });
            }
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                title: 'خطای ارتباطی',
                text: 'مشکل در برقراری ارتباط با سرور',
                confirmButtonText: 'تایید'
            });
        });
});

// Al Watan RSS Toggle
document.getElementById('alWatanRSSToggle').addEventListener('change', function() {
    var enabled = this.checked;

    fetch('/api/alwatan-rss/toggle', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ enabled: enabled })
    })
        .then(response => {
            if (response.ok) {
                Swal.fire({
                    icon: 'success',
                    title: 'عملیات موفقیت‌آمیز',
                    text: enabled ? 'RSS الوطن فعال شد' : 'RSS الوطن غیرفعال شد',
                    confirmButtonText: 'تایید'
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: 'خطا',
                    text: 'مشکلی در تغییر وضعیت RSS رخ داده است',
                    confirmButtonText: 'تایید'
                });
            }
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                title: 'خطای ارتباطی',
                text: 'مشکل در برقراری ارتباط با سرور',
                confirmButtonText: 'تایید'
            });
        });
});

// Al Watan RSS Set Max News
document.getElementById('setAlWatanMaxNewsBtn').addEventListener('click', function() {
    var maxNews = document.getElementById('maxAlWatanNewsInput').value;

    fetch('/api/alwatan-rss/max-news', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ maxNews: parseInt(maxNews) })
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(errorData => {
                    throw new Error(JSON.stringify(errorData));
                });
            }
        })
        .then(data => {
            Swal.fire({
                icon: 'success',
                title: data.title || 'تنظیمات موفقیت‌آمیز',
                text: data.message || 'حداکثر تعداد اخبار الوطن به ' + maxNews + ' عدد تنظیم شد',
                confirmButtonText: 'تأیید'
            });
        })
        .catch(error => {
            try {
                const errorData = JSON.parse(error.message);
                Swal.fire({
                    icon: 'error',
                    title: errorData.title || 'خطا',
                    text: errorData.message || 'مشکلی در تنظیم حداکثر اخبار رخ داده است',
                    confirmButtonText: 'تایید'
                });
            } catch {
                Swal.fire({
                    icon: 'error',
                    title: 'خطا',
                    text: 'مشکلی در تنظیم حداکثر اخبار رخ داده است',
                    confirmButtonText: 'تایید'
                });
            }
        });
});