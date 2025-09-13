(function(window) {
    'use strict';

    let selectedSuggestionIndex = null;
    let currentSuggestions = [];
    let currentField = null;

    // Make the function global
    window.updateMagicButtonState = function(button, state) {
        const icon = button.querySelector('i');

        // Remove all states
        button.classList.remove('loading', 'success');
        icon.classList.remove('ri-magic-line', 'ri-loader-4-line', 'ri-check-line');

        switch(state) {
            case 'loading':
                button.classList.add('loading');
                icon.classList.add('ri-loader-4-line');
                break;
            case 'success':
                button.classList.add('success');
                icon.classList.add('ri-check-line');
                setTimeout(() => {
                    icon.classList.remove('ri-check-line');
                    icon.classList.add('ri-magic-line');
                    button.classList.remove('success');
                }, 1500);
                break;
            default:
                icon.classList.add('ri-magic-line');
        }
    }

    // HELPER function for converting numbers to Persian
    function toPersianNumber(num) {
        const persianNumbers = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];
        return num.toString().split('').map(c => persianNumbers[c] || c).join('');
    }

    // Button ke magic wand click mishe
    window.getAISuggestion = async function(element) {
        const field = element.getAttribute('data-field');
        if (!field) {
            console.error('No field specified for AI suggestion');
            return;
        }

        currentField = field;
        try {
            // Show loading spinner and hide suggestions
            document.getElementById('loadingSpinner').style.display = 'block';
            document.getElementById('suggestionsContainer').classList.add('d-none');
            updateMagicButtonState(element, 'loading');

            // Get the content based on field type
            const content = getFieldContent(field);

            if (!content || content.trim() === '') {
                throw new Error(`لطفا ${getFieldLabel(field)} را وارد کنید`);
            }


            const mappedField = getFieldMapping(field);

            // Call Gemini API with mapped field (ya ba gpt)
            const response = await getGeminiSuggestions(content, mappedField);

            // Parse and display suggestions
            const { suggestions } = parseAISuggestions(response, field);

            // Future GPT USAGE (UNCOMMENT THIS WHEN YOU WANT TO USE IT)
            // const response = await getGPTSuggestions(content, mappedField);
            // const { suggestions } = parseGPTSuggestions(response, field);

            currentSuggestions = suggestions;

            const modal = new bootstrap.Modal(document.getElementById('suggestionModal'));
            modal.show();

            updateMagicButtonState(element, 'success');

        } catch (error) {
            updateMagicButtonState(element, 'default');
            alert('خطا در دریافت پیشنهادات: ' + error.message);
        } finally {
            document.getElementById('loadingSpinner').style.display = 'none';
        }
    };

    // Function to parse AI suggestions (Supports diff types of gemini answers) +  ex. <p> <span> ya masalan <div><p>...
    function parseAISuggestions(response, field) {
        if (!response || typeof response !== 'string') {
            throw new Error('پیشنهادی یافت نشد. لطفا دوباره تلاش کنید');
        }

        try {
            const suggestions = [];
            const tempDiv = document.createElement('div');
            const cleanResponse = response.replace(/^.*?```html\n?/, '').replace(/```.*$/s, '');
            tempDiv.innerHTML = cleanResponse;

            if (['title', 'headline', 'subheading', 'lead'].includes(field)) {
                // First try to get all divs
                const elements = tempDiv.querySelectorAll('div');
                elements.forEach(element => {
                    const text = element.textContent.trim();
                    if (text && !suggestions.includes(text)) {
                        suggestions.push(text);
                    }
                });

                // Fallback to headings if no divs found
                if (suggestions.length === 0) {
                    const headings = tempDiv.querySelectorAll('h1, h2, h3, h4, h5, h6');
                    headings.forEach(heading => {
                        const text = heading.textContent.trim();
                        if (text && !suggestions.includes(text)) {
                            suggestions.push(text);
                        }
                    });
                }
            }
            //BODY PARSE
            else if (['body'].includes(field)) {
                // First try to find article elements
                const articles = tempDiv.querySelectorAll('article');

                if (articles.length > 0) {
                    articles.forEach(article => {
                        // Keep the entire article content including the article tag
                        const content = article.outerHTML.trim();
                        if (content && !suggestions.includes(content)) {
                            suggestions.push(content);
                        }
                    });
                } else {
                    // Fallback to div elements if no articles found
                    const blocks = tempDiv.children;
                    Array.from(blocks).forEach(block => {
                        if (block.tagName === 'DIV' && block.className) {
                            const content = block.innerHTML.trim();
                            if (content && !suggestions.includes(content)) {
                                suggestions.push(content);
                            }
                        }
                    });
                }
            }

            if (suggestions.length === 0) {
                throw new Error('پیشنهادی یافت نشد. لطفا دوباره تلاش کنید');
            }

            const suggestionsContainer = document.getElementById('suggestionsContainer');
            if (!suggestionsContainer) {
                throw new Error('خطا در نمایش پیشنهادات');
            }
            suggestionsContainer.classList.remove('d-none');

            const container = document.querySelector('.suggestions-grid');
            container.innerHTML = '';

            suggestions.forEach((suggestion, index) => {
                let previewText, fullText;

                if (field === 'body') {
                    const tempDiv = document.createElement('div');
                    tempDiv.innerHTML = suggestion;

                    // For preview: title + first paragraph
                    const title = tempDiv.querySelector('h1')?.textContent?.trim() || '';
                    const firstP = tempDiv.querySelector('p')?.textContent?.trim() || '';
                    previewText = title ? title + '\n\n' + firstP.substring(0, 100) + '...' : firstP.substring(0, 100) + '...';

                    // For full view: collect all text content with proper spacing
                    const fullContent = [];

                    // Add title
                    const h1 = tempDiv.querySelector('h1');
                    if (h1) fullContent.push(h1.textContent.trim());

                    // Add subtitles and paragraphs
                    tempDiv.querySelectorAll('h2, h3, p').forEach(element => {
                        const text = element.textContent.trim();
                        if (text) {
                            if (element.tagName === 'H2' || element.tagName === 'H3') {
                                fullContent.push('\n' + text);
                            } else {
                                fullContent.push(text);
                            }
                        }
                    });

                    fullText = fullContent.join('\n\n');
                } else {
                    previewText = suggestion.length > 150 ?
                        suggestion.substring(0, 150) + '...' :
                        suggestion;
                    fullText = suggestion;
                }

                const card = document.createElement('div');
                card.className = 'suggestion-card mb-3';
                card.innerHTML = `
                    <div class="suggestion-number">${toPersianNumber(index + 1)}</div>
                    <div class="suggestion-preview" id="preview-${index}" style="white-space: pre-line;">${previewText}</div>
                    <div class="full-content d-none" id="full-${index}" style="white-space: pre-line;">${fullText}</div>
                    <div class="suggestion-actions">
                        <button class="btn btn-primary btn-sm" onclick="selectSuggestion(${index}, event)">انتخاب</button>
                        ${(field === 'body' || suggestion.length > 150) ?
                    `<button class="btn btn-secondary btn-sm" onclick="togglePreview(${index}, event)">مشاهده کامل</button>` :
                    ''}
                    </div>
                `;
                container.appendChild(card);
            });

            if (!document.getElementById('suggestion-card-styles')) {
                const style = document.createElement('style');
                style.id = 'suggestion-card-styles';
                style.textContent = `
                    .suggestions-grid {
                        display: grid;
                        gap: 1rem;
                        padding: 1rem;
                    }
                    .suggestion-card {
                        position: relative;
                        padding: 1.5rem 1rem 1rem !important;
                        border: 1px solid rgba(0,0,0,0.1);
                        border-radius: 8px;
                        background: white;
                    }
                    .suggestion-number {
                        position: absolute;
                        top: 0;
                        right: 0;
                        background: #6366f1;
                        color: white;
                        padding: 2px 8px;
                        border-radius: 0 8px 0 8px;
                        font-size: 0.875rem;
                        font-family: inherit;
                    }
                    .suggestion-preview, .full-content {
                        margin-bottom: 1rem;
                        line-height: 1.6;
                    }
                    .suggestion-actions {
                        display: flex;
                        gap: 0.5rem;
                        justify-content: flex-end;
                    }
                `;
                document.head.appendChild(style);
            }

            return {
                suggestions,
                container: suggestionsContainer
            };
        } catch (error) {
            throw error;
        }
    }

    // GPT IMPLEMENTATION uncomment this when you want to use it
    // window.getGPTSuggestions = async function(content, field) {
    //     // TODO: Implement
    //     try {
    //         const response = await Promise.race([
    //             fetch('/api/gpt/modify', {
    //                 method: 'POST',
    //                 headers: {
    //                     'Content-Type': 'application/json',
    //                     'Accept': 'application/json'
    //                 },
    //                 body: JSON.stringify({ [field]: content })
    //             }),
    //             new Promise((_, reject) =>
    //                 setTimeout(() => reject(new Error('درخواست با تاخیر مواجه شد')), 15000)
    //             )
    //         ]);

    //         if (!response.ok) {
    //             throw new Error(`خطای سرور: ${response.status} ${response.statusText}`);
    //         }

    //         const data = await response.json();
    //         if (!data || !data.content) {
    //             throw new Error('پاسخ نامعتبر از سرور');
    //         }

    //         return data.content;

    //     } catch (error) {
    //         throw error;
    //     }
    // }

    function parseGPTSuggestions(response, field) {
        if (!response || typeof response !== 'string') {
            throw new Error('پیشنهادی یافت نشد. لطفا دوباره تلاش کنید');
        }

        try {
            const suggestions = [];
            const tempDiv = document.createElement('div');
            const cleanResponse = response.replace(/^.*?```html\n?/, '').replace(/```.*$/s, '');
            tempDiv.innerHTML = cleanResponse;

            if (['title', 'headline', 'subTitle', 'lead'].includes(field)) {
                const headings = tempDiv.querySelectorAll('h1, h2, h3, h4, h5, h6');
                headings.forEach(heading => {
                    const text = heading.textContent.trim();
                    if (text && !suggestions.includes(text)) {
                        suggestions.push(text);
                    }
                });

                if (suggestions.length === 0) {
                    const divs = tempDiv.getElementsByTagName('div');
                    for (let div of divs) {
                        const text = div.textContent.trim();
                        if (text && !suggestions.includes(text)) {
                            suggestions.push(text);
                        }
                    }
                }
            }
            else if (['body'].includes(field)) {
                const divs = tempDiv.getElementsByTagName('div');
                for (let div of divs) {
                    const text = div.textContent.trim();
                    if (text && !suggestions.includes(text)) {
                        suggestions.push(text);
                    }
                }
            }

            if (suggestions.length === 0) {
                throw new Error('پیشنهادی یافت نشد. لطفا دوباره تلاش کنید');
            }

            const suggestionsContainer = document.getElementById('suggestionsContainer');
            if (!suggestionsContainer) {
                throw new Error('خطا در نمایش پیشنهادات');
            }
            suggestionsContainer.classList.remove('d-none');

            const container = document.querySelector('.suggestions-grid');
            container.innerHTML = '';

            suggestions.forEach((suggestion, index) => {
                const previewText = suggestion.length > 150 ?
                    suggestion.substring(0, 150) + '...' :
                    suggestion;

                const card = document.createElement('div');
                card.className = 'suggestion-card mb-3';
                card.innerHTML = `
                    <div class="suggestion-number">${toPersianNumber(index + 1)}</div>
                    <div class="suggestion-preview" id="preview-${index}">${previewText}</div>
                    <div class="full-content d-none" id="full-${index}">${suggestion}</div>
                    <div class="suggestion-actions">
                        <button class="btn btn-primary btn-sm" onclick="selectSuggestion(${index}, event)">انتخاب</button>
                        ${suggestion.length > 150 ?
                    `<button class="btn btn-secondary btn-sm" onclick="togglePreview(${index}, event)">مشاهده کامل</button>` :
                    ''}
                    </div>
                `;
                container.appendChild(card);
            });

            if (!document.getElementById('suggestion-card-styles')) {
                const style = document.createElement('style');
                style.id = 'suggestion-card-styles';
                style.textContent = `
                    .suggestions-grid {
                        display: grid;
                        gap: 1rem;
                        padding: 1rem;
                    }
                    .suggestion-card {
                        position: relative;
                        padding: 1.5rem 1rem 1rem !important;
                        border: 1px solid rgba(0,0,0,0.1);
                        border-radius: 8px;
                        background: white;
                    }
                    .suggestion-number {
                        position: absolute;
                        top: 0;
                        right: 0;
                        background: #6366f1;
                        color: white;
                        padding: 2px 8px;
                        border-radius: 0 8px 0 8px;
                        font-size: 0.875rem;
                        font-family: inherit;
                    }
                    .suggestion-preview, .full-content {
                        margin-bottom: 1rem;
                        line-height: 1.6;
                    }
                    .suggestion-actions {
                        display: flex;
                        gap: 0.5rem;
                        justify-content: flex-end;
                    }
                `;
                document.head.appendChild(style);
            }

            return {
                suggestions,
                container: suggestionsContainer
            };
        } catch (error) {
            throw error;
        }
    }
    // get gemini suggestions (khode response)
    window.getGeminiSuggestions = async function(content, field) {
        try {
            const requestBody = {
                [field]: content
            };


            const response = await Promise.race([
                fetch('/api/gemini/modify', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(requestBody)
                }),
                new Promise((_, reject) =>
                    setTimeout(() => reject(new Error('درخواست با تاخیر مواجه شد')), 120000)
                )
            ]);

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Gemini API Response:', {
                    status: response.status,
                    statusText: response.statusText,
                    error: errorText
                });
                throw new Error(`خطای سرور: ${response.status} ${response.statusText}`);
            }

            const data = await response.json();
            if (!data || !data.content) {
                throw new Error('پاسخ نامعتبر از سرور');
            }

            return data.content;

        } catch (error) {
            throw error;
        }
    };

    // Make generateAIPrompt global
    // PLZ DONT REMOVE THIS (in payino felan)
//     window.generateAIPrompt = function(field, content) {
//         const basePrompts = {
//             title: `لطفا ۵ نمونه تیتر خبری سئو شده برای این محتوا ارائه دهید:
// ${content}
//
// لطفا پاسخ را در قالب HTML با کلاس‌های مشخص به صورت زیر برگردانید:
// <div class="ai-suggestions">
//     <h2 class="headline1">تیتر اول</h2>
//     <h2 class="headline2">تیتر دوم</h2>
//     <h2 class="headline3">تیتر سوم</h2>
//     <h2 class="headline4">تیتر چهارم</h2>
//     <h2 class="headline5">تیتر پنجم</h2>
// </div>`,
//             // ... rest of the prompts remain the same ...
//         };
//
//         return basePrompts[field] || '';
//     };

    // HELPER function to get field labels in Persian
    function getFieldLabel(field) {
        const labels = {
            'title': 'عنوان',
            'lead': 'لید',
            'body': 'متن',
            'subheading': 'روتیتر',
            'headline': 'تیتر'
        };
        return labels[field] || field;
    }

    // HELPER function to get content based on field type
    function getFieldContent(field) {
        const MIN_LENGTH = 5;

        if (field === 'body') {
            let content;
            if (typeof CKEDITOR !== 'undefined' && CKEDITOR.instances['news-body-textarea']) {
                content = CKEDITOR.instances['news-body-textarea'].getData();
            } else {
                content = document.getElementById('news-body-textarea').value;
            }

            // Length Check (Validation)
            const textContent = content.replace(/<[^>]*>/g, '').trim();
            if (textContent.length < MIN_LENGTH) {
                throw new Error(`متن خبر نمی‌تواند کمتر از ${MIN_LENGTH} کاراکتر باشد`);
            }
            return content;

        } else if (field === 'subheading') {
            const subheadingContent = document.getElementById('news-subheading-input')?.value?.trim() || '';

            // If subheading is empty and we have title/lead, create a prompt...
            if (!subheadingContent) {
                const title = document.getElementById('news-title-input')?.value?.trim();
                const lead = document.getElementById('news-lead-input')?.value?.trim();

                if (!title && !lead) {
                    throw new Error('برای پیشنهاد روتیتر، لطفا ابتدا عنوان یا لید خبر را وارد کنید');
                }

                if (title && title.length < MIN_LENGTH) {
                    throw new Error(`عنوان خبر نمی‌تواند کمتر از ${MIN_LENGTH} کاراکتر باشد`);
                }

                if (lead && lead.length < MIN_LENGTH) {
                    throw new Error(`لید خبر نمی‌تواند کمتر از ${MIN_LENGTH} کاراکتر باشد`);
                }

                return `لطفا برای این خبر ۵ روتیتر مناسب پیشنهاد دهید:

${title ? `عنوان خبر:
${title}

` : ''}${lead ? `لید خبر:
${lead}` : ''}

روتیتر باید کوتاه‌تر از تیتر اصلی باشد و اطلاعات تکمیلی به خواننده بدهد.`;
            }

            if (subheadingContent.length < MIN_LENGTH) {
                throw new Error(`روتیتر نمی‌تواند کمتر از ${MIN_LENGTH} کاراکتر باشد`);
            }
            return subheadingContent;

        } else {
            const input = document.getElementById(`news-${field}-input`);
            const content = input ? input.value.trim() : '';

            if (content.length < MIN_LENGTH) {
                const fieldLabel = {
                    'title': 'عنوان',
                    'lead': 'لید',
                    'headline': 'تیتر'
                }[field] || field;

                throw new Error(`${fieldLabel} خبر نمی‌تواند کمتر از ${MIN_LENGTH} کاراکتر باشد`);
            }
            return content;
        }
    }

    window.selectSuggestion = function(index, event) {
        const buttons = document.querySelectorAll('.suggestion-card .btn-select');
        buttons.forEach(btn => btn.classList.remove('active'));
        event.target.classList.add('active');

        const cards = document.querySelectorAll('.suggestion-card');
        cards.forEach(card => card.classList.remove('selected'));
        event.target.closest('.suggestion-card').classList.add('selected');

        document.getElementById('applyButton').disabled = false;
        selectedSuggestionIndex = index;
    };

    window.previewSuggestion = function(index, event) {
        const suggestion = currentSuggestions[index];
        alert(suggestion);
    };

    window.applySuggestion = function() {
        if (selectedSuggestionIndex === null || !currentSuggestions[selectedSuggestionIndex]) {
            return;
        }

        const selectedText = currentSuggestions[selectedSuggestionIndex];

        if (currentField === 'body') {
            if (CKEDITOR && CKEDITOR.instances['news-body-textarea']) {
                CKEDITOR.instances['news-body-textarea'].setData(selectedText);
            }
        } else {
            const input = document.getElementById(`news-${currentField}-input`);
            if (input) {
                input.value = selectedText;
            }
        }

        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('suggestionModal'));
        if (modal) {
            modal.hide();
        }
    };

    window.togglePreview = function(index, event) {
        event.preventDefault();
        const previewElement = document.getElementById(`preview-${index}`);
        const fullElement = document.getElementById(`full-${index}`);
        const button = event.target;

        if (fullElement.classList.contains('d-none')) {
            // Show full content
            previewElement.classList.add('d-none');
            fullElement.classList.remove('d-none');
            button.textContent = 'نمایش خلاصه';
        } else {
            // Show preview
            previewElement.classList.remove('d-none');
            fullElement.classList.add('d-none');
            button.textContent = 'مشاهده کامل';
        }
    };

    // Update field mapping if needed (Avesta) (moshkel ro titr)
    function getFieldMapping(field) {
        const mappings = {
            'title': 'title',
            'lead': 'lead',
            'body': 'body',
            'subheading': 'title',
            'headline': 'headline'
        };
        return mappings[field] || field;
    }

    // async function getAISuggestions(field) {
    //     try {
    //         const content = getFieldContent(field);
    //
    //         // Special handling for subheading - don't validate content if field is empty
    //         if (!content && field !== 'subheading') {
    //             throw new Error(`لطفا ${getFieldLabel(field)} خبر را وارد کنید`);
    //         }
    //
    //         // Show loading state
    //         const button = document.querySelector(`button[data-field="${field}"]`);
    //         if (button) {
    //             button.disabled = true;
    //             button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
    //         }
    //
    //         const response = await fetch('/api/v1/crawler/ai/suggest', {
    //             method: 'POST',
    //             headers: {
    //                 'Content-Type': 'application/json',
    //                 'Accept': 'application/json'
    //             },
    //             body: JSON.stringify({
    //                 content: content,
    //                 field: getFieldMapping(field)
    //             })
    //         });
    //
    //         if (!response.ok) {
    //             throw new Error('خطا در دریافت پیشنهادات');
    //         }
    //
    //         const data = await response.json();
    //         if (!data.success) {
    //             throw new Error(data.message || 'خطا در دریافت پیشنهادات');
    //         }
    //
    //         const result = parseAISuggestions(data.response, field);
    //
    //         // Reset button state
    //         if (button) {
    //             button.disabled = false;
    //             button.innerHTML = '<i class="bi bi-magic"></i>';
    //         }
    //
    //         return result;
    //     } catch (error) {
    //         // Reset button state
    //         const button = document.querySelector(`button[data-field="${field}"]`);
    //         if (button) {
    //             button.disabled = false;
    //             button.innerHTML = '<i class="bi bi-magic"></i>';
    //         }
    //
    //         // Show error message
    //         const errorMessage = error.message || 'خطا در دریافت پیشنهادات';
    //         alert(`خطا در دریافت پیشنهادات: ${errorMessage}`);
    //         throw error;
    //     }
    // }

})(window);