// Social Publishing JavaScript for News Add Page

let currentNewsId = null;

// Initialize tooltips and setup
document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Set current news ID if editing existing news
    const newsIdField = document.querySelector('[name="id"]');
    if (newsIdField && newsIdField.value) {
        currentNewsId = newsIdField.value;
    }
});

// Quick post to X
function postToX() {
    if (!validateNewsForm()) return;
    
    const newsData = collectNewsData();
    publishToSpecificPlatform(newsData, ['X']);
}

// Quick post to Bale
function postToBale() {
    if (!validateNewsForm()) return;
    
    const newsData = collectNewsData();
    publishToSpecificPlatform(newsData, ['BALE']);
}

// Open social publishing modal
function openSocialModal() {
    if (!validateNewsForm()) return;
    
    const modal = new bootstrap.Modal(document.getElementById('socialPublishModal'));
    modal.show();
}

// Publish to selected platforms from modal
function publishToSocials() {
    const selectedPlatforms = [];
    document.querySelectorAll('#socialPublishForm input[type="checkbox"]:checked').forEach(checkbox => {
        if (checkbox.value && ['X', 'TELEGRAM', 'EITAA', 'BALE'].includes(checkbox.value)) {
            selectedPlatforms.push(checkbox.value);
        }
    });

    if (selectedPlatforms.length === 0) {
        Swal.fire({
            title: 'خطا',
            text: 'لطفاً حداقل یک پلتفرم را انتخاب کنید.',
            icon: 'error'
        });
        return;
    }

    const newsData = collectNewsData();
    
    // Add custom settings
    newsData.settings = {
        includeImage: document.getElementById('includeImage').checked,
        includeUrl: document.getElementById('includeUrl').checked,
        customMessage: document.getElementById('customMessage').value || null,
        maxLength: 280 // Default for X
    };

    const scheduleTime = document.getElementById('scheduleTime').value;
    if (scheduleTime) {
        newsData.scheduledAt = new Date(scheduleTime).toISOString();
    }

    publishToSpecificPlatform(newsData, selectedPlatforms);
    
    // Close modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('socialPublishModal'));
    modal.hide();
}

// Validate that required news fields are filled
function validateNewsForm() {
    const title = document.querySelector('[name="title"]').value;
    const lead = document.querySelector('[name="lead"]').value;
    
    if (!title || !lead) {
        Swal.fire({
            title: 'خطا',
            text: 'لطفاً ابتدا عنوان و سرنخ خبر را تکمیل کنید.',
            icon: 'error'
        });
        return false;
    }
    return true;
}

// Collect news data from form
function collectNewsData() {
    // Get CKEditor content
    let content = '';
    try {
        if (typeof CKEDITOR !== 'undefined' && CKEDITOR.instances.ckeditor5) {
            content = CKEDITOR.instances.ckeditor5.getData();
        } else {
            // Fallback to textarea content
            const textarea = document.querySelector('[name="body"]');
            content = textarea ? textarea.value : '';
        }
    } catch (e) {
        console.error('Error getting CKEditor content:', e);
        const textarea = document.querySelector('[name="body"]');
        content = textarea ? textarea.value : '';
    }

    // Get main image URL
    let imageUrl = '';
    const mainImageInput = document.querySelector('[name="mainImage"]');
    if (mainImageInput && mainImageInput.value) {
        imageUrl = mainImageInput.value;
    }

    return {
        newsId: currentNewsId,
        title: document.querySelector('[name="title"]').value || '',
        lead: document.querySelector('[name="lead"]').value || '',
        content: content,
        metaTitle: document.querySelector('[name="metaTitle"]').value || '',
        metaDescription: document.querySelector('[name="metaDescription"]').value || '',
        metaKeywords: document.querySelector('[name="metaKeywords"]').value || '',
        imageUrl: imageUrl,
        newsUrl: window.location.origin + '/news/' + (currentNewsId || 'new'),
        tags: getSelectedTags()
    };
}

// Get selected tags
function getSelectedTags() {
    const tags = [];
    try {
        const tagElements = document.querySelectorAll('[name="tags"] option:checked');
        tagElements.forEach(option => {
            if (option.textContent) {
                tags.push(option.textContent);
            }
        });
    } catch (e) {
        console.error('Error getting tags:', e);
    }
    return tags;
}

// Publish to specific platforms
function publishToSpecificPlatform(newsData, platforms) {
    // If news is not saved yet, save it first
    if (!newsData.newsId) {
        Swal.fire({
            title: 'ذخیره خبر',
            text: 'ابتدا خبر ذخیره می‌شود، سپس در شبکه‌های اجتماعی منتشر خواهد شد.',
            icon: 'info',
            showCancelButton: true,
            confirmButtonText: 'ادامه',
            cancelButtonText: 'انصراف'
        }).then((result) => {
            if (result.isConfirmed) {
                // Save the news first, then publish to social
                saveNewsAndPublishToSocial(platforms, newsData);
            }
        });
    } else {
        // News already exists, publish directly
        publishToSocialPlatforms(newsData, platforms);
    }
}

// Save news and then publish to social
function saveNewsAndPublishToSocial(platforms, socialData) {
    // Submit the main form
    const form = document.querySelector('form');
    const formData = new FormData(form);
    
    fetch(form.action, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.ok) {
            // Extract news ID from response if possible
            // For now, we'll show success and ask user to publish manually
            Swal.fire({
                title: 'موفقیت',
                text: 'خبر ذخیره شد. لطفاً دوباره روی دکمه انتشار کلیک کنید.',
                icon: 'success'
            });
        } else {
            throw new Error('خطا در ذخیره خبر');
        }
    })
    .catch(error => {
        console.error('Error saving news:', error);
        Swal.fire({
            title: 'خطا',
            text: 'خطا در ذخیره خبر. لطفاً دوباره تلاش کنید.',
            icon: 'error'
        });
    });
}

// Publish to social platforms
function publishToSocialPlatforms(newsData, platforms) {
    const publishData = {
        ...newsData,
        platforms: platforms
    };

    Swal.fire({
        title: 'در حال انتشار...',
        text: 'لطفاً صبر کنید',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    fetch('/api/socials/publish', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(publishData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            Swal.fire({
                title: 'موفقیت',
                text: data.message,
                icon: 'success'
            });
            
            // Show social publishing status section
            showSocialStatus(data.jobIds);
            
        } else {
            Swal.fire({
                title: 'خطا',
                text: data.message || 'خطا در انتشار',
                icon: 'error'
            });
        }
    })
    .catch(error => {
        console.error('Error publishing to social:', error);
        Swal.fire({
            title: 'خطا',
            text: 'خطا در ارتباط با سرور',
            icon: 'error'
        });
    });
}

// Show social status section
function showSocialStatus(jobIds) {
    if (!jobIds || jobIds.length === 0) return;
    
    const statusSection = document.getElementById('socialStatusSection');
    const statusContainer = document.getElementById('socialJobStatuses');
    
    statusSection.style.display = 'block';
    
    // Clear existing statuses
    statusContainer.innerHTML = '';
    
    // Add status for each job
    jobIds.forEach(jobId => {
        const statusDiv = document.createElement('div');
        statusDiv.className = 'col-md-6 mb-2';
        statusDiv.innerHTML = `
            <div class="d-flex align-items-center">
                <div class="flex-grow-1">
                    <span id="job-status-${jobId}">در انتظار...</span>
                </div>
                <div class="spinner-border spinner-border-sm text-primary" id="job-spinner-${jobId}"></div>
            </div>
        `;
        statusContainer.appendChild(statusDiv);
        
        // Start polling for this job status
        pollJobStatus(jobId);
    });
}

// Poll job status
function pollJobStatus(jobId) {
    const pollInterval = setInterval(() => {
        fetch(`/api/socials/status/${jobId}`)
            .then(response => response.json())
            .then(data => {
                const statusElement = document.getElementById(`job-status-${jobId}`);
                const spinnerElement = document.getElementById(`job-spinner-${jobId}`);
                
                if (statusElement) {
                    let statusText = `${data.platform}: `;
                    let statusClass = '';
                    
                    switch (data.status) {
                        case 'SUCCESS':
                            statusText += 'منتشر شد ✅';
                            statusClass = 'text-success';
                            spinnerElement.style.display = 'none';
                            clearInterval(pollInterval);
                            break;
                        case 'FAILED':
                            statusText += `خطا: ${data.errorMessage || 'نامشخص'}`;
                            statusClass = 'text-danger';
                            spinnerElement.style.display = 'none';
                            clearInterval(pollInterval);
                            break;
                        case 'PROCESSING':
                            statusText += 'در حال انتشار...';
                            statusClass = 'text-warning';
                            break;
                        default:
                            statusText += 'در انتظار...';
                            statusClass = 'text-muted';
                    }
                    
                    statusElement.textContent = statusText;
                    statusElement.className = statusClass;
                }
            })
            .catch(error => {
                console.error('Error polling job status:', error);
                clearInterval(pollInterval);
            });
    }, 2000); // Poll every 2 seconds
    
    // Stop polling after 2 minutes
    setTimeout(() => clearInterval(pollInterval), 120000);
}
