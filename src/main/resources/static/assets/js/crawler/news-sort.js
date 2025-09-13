let maxSelectableNews = 15;
let currentSection = '';

// Configuration for different sections
const sectionConfig = {
    breakingNews: {
        name: 'خبر فوری',
        maxNews: 15,
        apiEndpoint: 'breakingNews'
    },
    newsHeadline: {
        name: 'سرخط خبرها',
        maxNews: 15,
        apiEndpoint: 'newsHeadline'
    },
    showMostViewed: {
        name: 'پربیننده ترین ها',
        maxNews: 15,
        apiEndpoint: 'showMostViewed'
    },
    slider: {
        name: 'اسلایدر',
        maxNews: 5,
        apiEndpoint: 'slider'
    }
};

document.addEventListener("DOMContentLoaded", function () {
    var nestedSortables = [].slice.call(document.querySelectorAll('.nested-sortable'));

    if (nestedSortables.length > 0) {
        nestedSortables.forEach(function (nestedSort) {
            new Sortable(nestedSort, {
                group: {
                    name: 'nested',
                    put: false
                },
                animation: 150,
                fallbackOnBody: true,
                swapThreshold: 0.65
            });
        });
    }
});

function toggleModalButton(show) {
    const addButtonDiv = document.getElementById("addButton").parentElement;
    if (show) {
        addButtonDiv.removeAttribute("hidden");
    } else {
        addButtonDiv.setAttribute("hidden", "true");
    }
}

async function parseNestList() {
    const selectInput = document.getElementById('select-section');
    const nestName = document.getElementById('nestName');
    const listGroup = document.getElementById('list-group');
    const saveOrderButton = document.getElementById('saveOrderButton');
    const selectedValue = selectInput.value;

    // Clear previous state if no section selected
    if (!selectedValue) {
        resetToEmptyState();
        return;
    }

    currentSection = selectedValue;
    const config = sectionConfig[selectedValue];

    if (!config) {
        console.error("Invalid section selected");
        return;
    }

    // Show loading state
    showLoadingState();

    try {
        const response = await fetch('/api/news/find', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ "section": selectedValue }),
        });

        if (!response.ok) {
            throw new Error("جوابی گرفته نشد.");
        }

        const text = await response.text();
        const data = JSON.parse(text);

        // Update UI elements
        updateSectionInfo(config, data);
        updateModalTitle(config.name);

        listGroup.innerHTML = "";
        hideEmptyState();

        let currentNewsCount = 0;
        const sortKey = `${selectedValue}Sort`;
        const listKey = `${selectedValue}List`;

        if (!data[sortKey] || data[sortKey].length === 0) {
            toggleModalButton(true);
            modalData(data[listKey] || []);
            showEmptyNewsState(config.name);
        } else {
            data[sortKey].forEach(newsId => {
                currentNewsCount = data[sortKey].length;
                const matchingNews = data[listKey].find(news => news.id === newsId);
                if (matchingNews) {
                    const newsItem = createNewsItem(matchingNews);
                    listGroup.appendChild(newsItem);
                }
            });

            maxSelectableNews = config.maxNews - currentNewsCount;

            if (maxSelectableNews > 0) {
                toggleModalButton(true);
                modalData(data[listKey] || []);
            } else {
                toggleModalButton(false);
            }
        }

        // Update current count display
        updateCurrentCount(currentNewsCount, config.maxNews);

        setTimeout(initializeSortable, 100);

        saveOrderButton.style.display = (data[sortKey] && data[sortKey].length > 0) ? "block" : "none";

    } catch (error) {
        console.error("Error fetching news:", error);
        showToast("خطا در دریافت اخبار!", "error");
        hideLoadingState();
    }
}

function createNewsItem(news) {
    const newsItem = document.createElement("div");
    newsItem.className = "list-group-item nested-2 news-item-enhanced mb-2";
    newsItem.setAttribute("data-id", news.id);

    newsItem.innerHTML = `
        <div class="d-flex justify-content-between align-items-center">
            <div class="news-content flex-grow-1">
                <div class="d-flex align-items-center">
                    <i class="ri-drag-move-line me-2 text-muted"></i>
                    <div>
                        <span class="news-id">#${news.id}</span>
                        <h6 class="news-title">${news.title}</h6>
                    </div>
                </div>
            </div>
            <button class="btn btn-outline-danger btn-sm" onclick="deleteNews(${news.id})" title="حذف خبر">
                <i class="ri-delete-bin-line"></i>
            </button>
        </div>
    `;

    return newsItem;
}

function updateSectionInfo(config, data) {
    const sectionInfoRow = document.getElementById('section-info-row');
    const sectionDisplayName = document.getElementById('section-display-name');
    const sectionDescription = document.getElementById('section-description');

    sectionDisplayName.textContent = config.name;
    sectionDescription.textContent = `حداکثر ${config.maxNews} خبر قابل انتخاب است`;
    sectionInfoRow.style.display = 'block';
}

function updateCurrentCount(current, max) {
    const currentCountElement = document.getElementById('current-count');
    if (currentCountElement) {
        currentCountElement.textContent = `${current} از ${max} خبر`;

        // Change color based on usage
        currentCountElement.className = 'badge fs-6 ';
        if (current === max) {
            currentCountElement.className += 'bg-danger text-white';
        } else if (current > max * 0.8) {
            currentCountElement.className += 'bg-warning text-dark';
        } else {
            currentCountElement.className += 'bg-success text-white';
        }
    }
}

function showLoadingState() {
    const nestName = document.getElementById('nestName');
    nestName.innerHTML = '<i class="ri-loader-4-line"></i> در حال بارگذاری...';
}

function hideLoadingState() {
    // Loading state will be replaced by actual content or empty state
}

function resetToEmptyState() {
    const sectionInfoRow = document.getElementById('section-info-row');
    const listGroup = document.getElementById('list-group');
    const saveOrderButton = document.getElementById('saveOrderButton');

    sectionInfoRow.style.display = 'none';
    listGroup.innerHTML = '';
    saveOrderButton.style.display = 'none';
    toggleModalButton(false);
    showEmptyState();
    currentSection = '';
}

function showEmptyState() {
    const emptyState = document.getElementById('empty-state');
    const nestName = document.getElementById('nestName');

    emptyState.style.display = 'block';
    nestName.textContent = 'ناحیه مدنظر خود را انتخاب کنید';
}

function hideEmptyState() {
    const emptyState = document.getElementById('empty-state');
    emptyState.style.display = 'none';
}

function showEmptyNewsState(sectionName) {
    const emptyState = document.getElementById('empty-state');
    const nestName = document.getElementById('nestName');

    emptyState.style.display = 'block';
    nestName.innerHTML = `<i class="ri-information-line me-2"></i>هیچ خبری در بخش "${sectionName}" وجود ندارد`;
}

function updateModalTitle(sectionName) {
    const modalTitle = document.getElementById('myExtraLargeModalLabel');
    if (modalTitle) {
        modalTitle.textContent = `انتخاب خبر برای ناحیه ${sectionName}`;
    }
}

async function deleteNews(newsId) {
    if (!currentSection) {
        showToast("ابتدا یک ناحیه انتخاب کنید!", "warning");
        return;
    }

    const config = sectionConfig[currentSection];

    Swal.fire({
        title: 'آیا مطمئن هستید؟',
        text: 'این عمل قابل بازگشت نیست!',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'بله، حذف کن',
        cancelButtonText: 'لغو',
        reverseButtons: true
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                const response = await fetch(`/api/news/delete/${currentSection}/${newsId}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' }
                });

                if (!response.ok) {
                    throw new Error("خطا در حذف خبر.");
                }

                Swal.fire({
                    icon: 'success',
                    title: 'موفقیت!',
                    text: 'خبر با موفقیت حذف شد.',
                    confirmButtonText: 'باشه'
                }).then(() => {
                    parseNestList();
                });

            } catch (error) {
                Swal.fire({
                    icon: 'error',
                    title: 'خطا!',
                    text: 'مشکلی در حذف خبر رخ داد.',
                    confirmButtonText: 'متوجه شدم'
                });
            }
        }
    });
}

function modalData(newsList) {
    const modalTableBody = document.querySelector(".modal tbody");
    const modalLoading = document.getElementById("modal-loading");

    // Show loading state
    modalLoading.style.display = "block";
    modalTableBody.innerHTML = "";

    setTimeout(() => {
        modalLoading.style.display = "none";

        if (!newsList || newsList.length === 0) {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td colspan="3" class="text-center py-4">
                    <i class="ri-inbox-line fs-1 text-muted"></i>
                    <p class="text-muted mt-2 mb-0">هیچ خبری برای نمایش وجود ندارد</p>
                </td>
            `;
            modalTableBody.appendChild(row);
            return;
        }

        newsList.forEach(news => {
            const row = document.createElement("tr");

            row.innerHTML = `
                <td>
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" value="${news.id}" id="newsCheck${news.id}">
                        <label class="form-check-label" for="newsCheck${news.id}"></label>
                    </div>
                </td>
                <td>
                    <span class="badge bg-primary-subtle text-primary">#${news.id}</span>
                </td>
                <td>
                    <div class="d-flex align-items-center">
                        <i class="ri-news-line me-2 text-muted"></i>
                        <span>${news.title}</span>
                    </div>
                </td>
            `;

            modalTableBody.appendChild(row);
        });

        // Reset select all checkbox
        const selectAllCheckbox = document.getElementById('selectAll');
        if (selectAllCheckbox) {
            selectAllCheckbox.checked = false;
        }
    }, 300); // Small delay for better UX
}

// Enhanced checkbox handling with better UX
document.addEventListener("change", function (event) {
    if (event.target.matches(".modal tbody input[type='checkbox']")) {
        const checkedCheckboxes = document.querySelectorAll(".modal tbody input[type='checkbox']:checked");
        const currentCount = checkedCheckboxes.length;

        if (currentCount > maxSelectableNews) {
            event.target.checked = false;

            // Show more informative toast
            const remainingSlots = maxSelectableNews - (currentCount - 1);
            const sectionName = sectionConfig[currentSection]?.name || 'این بخش';

            if (maxSelectableNews === 0) {
                showToast(`ظرفیت ${sectionName} پر شده است. برای افزودن خبر جدید، ابتدا یک خبر را حذف کنید.`, "error");
            } else {
                showToast(`حداکثر ${maxSelectableNews} خبر می‌توانید به ${sectionName} اضافه کنید. ${currentCount - 1} خبر انتخاب شده است.`, "warning");
            }

            // Visual feedback - highlight the limit
            highlightSelectionLimit();
        }

        // Update selection counter
        updateSelectionCounter();
    }
});

// Add visual feedback for selection limit
function highlightSelectionLimit() {
    const modal = document.querySelector('.bs-example-modal-xl');
    if (modal) {
        modal.classList.add('shake-animation');
        setTimeout(() => {
            modal.classList.remove('shake-animation');
        }, 600);
    }
}

// Add a selection counter to the modal
function updateSelectionCounter() {
    const checkedCount = document.querySelectorAll(".modal tbody input[type='checkbox']:checked").length;
    let counterElement = document.getElementById('selection-counter');

    if (!counterElement) {
        // Create counter element if it doesn't exist
        const modalBody = document.querySelector('.modal-body');
        if (modalBody) {
            const counterDiv = document.createElement('div');
            counterDiv.id = 'selection-counter-container';
            counterDiv.className = 'alert alert-info d-flex justify-content-between align-items-center mt-3';
            counterDiv.innerHTML = `
                <span>
                    <i class="ri-checkbox-multiple-line me-2"></i>
                    تعداد انتخاب شده: <strong id="selection-counter">0</strong> از <strong id="max-selectable">${maxSelectableNews}</strong>
                </span>
                <span id="selection-status" class="badge bg-success">آماده</span>
            `;

            // Insert after the info alert
            const infoAlert = modalBody.querySelector('.alert-info');
            if (infoAlert && infoAlert.nextSibling) {
                modalBody.insertBefore(counterDiv, infoAlert.nextSibling);
            } else {
                modalBody.appendChild(counterDiv);
            }
        }
        counterElement = document.getElementById('selection-counter');
    }

    // Update counter values
    if (counterElement) {
        counterElement.textContent = checkedCount;

        const maxElement = document.getElementById('max-selectable');
        if (maxElement) {
            maxElement.textContent = maxSelectableNews;
        }

        // Update status badge
        const statusElement = document.getElementById('selection-status');
        if (statusElement) {
            if (checkedCount === maxSelectableNews) {
                statusElement.className = 'badge bg-warning';
                statusElement.textContent = 'حداکثر ظرفیت';
            } else if (checkedCount > maxSelectableNews * 0.8) {
                statusElement.className = 'badge bg-info';
                statusElement.textContent = 'نزدیک به حداکثر';
            } else {
                statusElement.className = 'badge bg-success';
                statusElement.textContent = 'آماده';
            }
        }
    }
}

// Enhanced select all functionality with limit checking
document.addEventListener('DOMContentLoaded', function() {
    const selectAllCheckbox = document.getElementById('selectAll');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('.modal tbody input[type="checkbox"]');
            const maxSelectable = maxSelectableNews || 0;

            if (this.checked) {
                let selected = 0;
                checkboxes.forEach(checkbox => {
                    if (selected < maxSelectable) {
                        checkbox.checked = true;
                        selected++;
                    } else {
                        checkbox.checked = false;
                    }
                });

                if (checkboxes.length > maxSelectable && maxSelectable > 0) {
                    const sectionName = sectionConfig[currentSection]?.name || 'این بخش';
                    showToast(`فقط ${maxSelectable} خبر انتخاب شد (حداکثر مجاز برای ${sectionName})`, "info");
                }
            } else {
                checkboxes.forEach(checkbox => {
                    checkbox.checked = false;
                });
            }

            // Update counter after select all action
            updateSelectionCounter();
        });
    }
});

// Reset counter when modal is opened
document.addEventListener('shown.bs.modal', function (event) {
    if (event.target.classList.contains('bs-example-modal-xl')) {
        updateSelectionCounter();

        // Remove any existing counter to ensure fresh state
        const existingCounter = document.getElementById('selection-counter-container');
        if (existingCounter) {
            existingCounter.remove();
        }
    }
});

// Enhanced modalData function to include counter initialization
const originalModalData = modalData;
modalData = function(newsList) {
    originalModalData(newsList);

    // Initialize counter after modal data is loaded
    setTimeout(() => {
        updateSelectionCounter();
    }, 400);
};

// Add CSS for shake animation
if (!document.getElementById('checkbox-limit-styles')) {
    const style = document.createElement('style');
    style.id = 'checkbox-limit-styles';
    style.textContent = `
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
            20%, 40%, 60%, 80% { transform: translateX(5px); }
        }
        
        .shake-animation {
            animation: shake 0.6s;
        }
        
        #selection-counter-container {
            margin-top: 1rem;
            margin-bottom: 1rem;
        }
        
        .modal tbody tr.disabled-row {
            opacity: 0.5;
            pointer-events: none;
        }
    `;
    document.head.appendChild(style);
}

function showToast(message, type = "info") {
    const toastContainer = document.getElementById("toast-container");

    if (!toastContainer) {
        const newToastContainer = document.createElement("div");
        newToastContainer.id = "toast-container";
        newToastContainer.style.position = "fixed";
        newToastContainer.style.top = "20px";
        newToastContainer.style.right = "20px";
        newToastContainer.style.zIndex = "2000";
        document.body.appendChild(newToastContainer);
    }

    const toast = document.createElement("div");
    toast.className = `toast align-items-center text-white bg-${type} border-0`;
    toast.role = "alert";
    toast.style.minWidth = "250px";
    toast.style.padding = "10px";
    toast.style.marginBottom = "10px";
    toast.style.borderRadius = "5px";
    toast.style.zIndex = "2000";
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;

    document.getElementById("toast-container").appendChild(toast);

    const toastBootstrap = new bootstrap.Toast(toast);
    toastBootstrap.show();

    setTimeout(() => {
        toast.remove();
    }, 3000);
}

function getCurrentOrder() {
    const listGroup = document.getElementById('list-group');
    return Array.from(listGroup.children)
        .map(item => parseInt(item.getAttribute("data-id")))
        .filter(id => !isNaN(id));
}

function saveOrder() {
    if (!currentSection) {
        showToast("ابتدا یک ناحیه انتخاب کنید!", "warning");
        return;
    }

    const newOrder = getCurrentOrder();

    if (newOrder.length === 0) {
        showToast("هیچ خبری برای ذخیره وجود ندارد!", "warning");
        return;
    }

    const config = sectionConfig[currentSection];
    console.log(`✅ New Order Saved for ${config.name}:`, newOrder);

    fetch(`/api/news/${config.apiEndpoint}/order/save`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ order: newOrder }),
    })
        .then(response => response.json())
        .then(data => {
            console.log("Order successfully saved:", data);
            showToast(`ترتیب ${config.name} با موفقیت ذخیره شد!`, "success");
        })
        .catch(error => {
            console.error("Error saving order:", error);
            showToast("خطا در ذخیره ترتیب اخبار!", "error");
        });
}

function submitSelectedNews() {
    if (!currentSection) {
        showToast("ابتدا یک ناحیه انتخاب کنید!", "warning");
        return;
    }

    const selectedNewsIds = [];

    document.querySelectorAll(".modal tbody input[type='checkbox']:checked").forEach(checkbox => {
        selectedNewsIds.push(parseInt(checkbox.value));
    });

    if (selectedNewsIds.length === 0) {
        Swal.fire({
            icon: 'warning',
            title: 'هیچ خبری انتخاب نشده است!',
            text: 'لطفاً حداقل یک خبر را انتخاب کنید.',
            confirmButtonText: 'باشه'
        });
        return;
    }

    if (selectedNewsIds.length > maxSelectableNews) {
        showToast(`شما فقط می‌توانید ${maxSelectableNews} خبر دیگر انتخاب کنید!`, "warning");
        return;
    }

    const config = sectionConfig[currentSection];
    const requestBody = {};
    requestBody[`${currentSection}Ids`] = selectedNewsIds;

    fetch(`/api/news/${config.apiEndpoint}/save`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw new Error(err.message || `خطا در ذخیره‌سازی ${config.name}.`);
                });
            }
            return response.json();
        })
        .then(data => {
            console.log("Response from server:", data);

            Swal.fire({
                icon: 'success',
                title: 'موفقیت!',
                text: data.message,
                confirmButtonText: 'باشه'
            }).then(() => {
                const modalElement = document.querySelector(".bs-example-modal-xl");
                const modalInstance = bootstrap.Modal.getInstance(modalElement);
                if (modalInstance) {
                    modalInstance.hide();
                }
                parseNestList();
            });
        })
        .catch(error => {
            Swal.fire({
                icon: 'error',
                title: 'خطا!',
                text: error.message || 'مشکلی پیش آمده است.',
                confirmButtonText: 'تایید'
            });
        });
}

function initializeSortable() {
    new Sortable(document.getElementById('list-group'), {
        animation: 150,
    });
}