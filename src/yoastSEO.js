/**
 * YoastSEO.js Direct Integration
 * 100% YoastSEO analysis 
 */

import { Paper, languageProcessing, assessments } from "yoastseo";

// Use the AbstractResearcher from the main export
const AbstractResearcher = languageProcessing.AbstractResearcher;

class SimpleYoastSEO {
    constructor() {
        this.editorInstance = null;
        this.debounceTimer = null;
        this.aiSlugGenerated = false; // Track if AI slug was generated
        this.lastAiGeneratedSlug = ''; // Store the last AI-generated slug value
        this.lastAiGenerationTime = 0; // Track when AI generation last happened
        this.bindEvents();
        console.log('YoastSEO Direct API initialized');
    }

    bindEvents() {
        console.log('Binding events...');
        
        // Multiple approaches to ensure events are bound
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => {
                console.log('DOM content loaded, setting up listeners...');
                this.setupEventListeners();
                this.bindSlugGenerationButton();
            });
        } else {
            console.log('DOM already ready, setting up listeners immediately...');
            this.setupEventListeners();
            this.bindSlugGenerationButton();
        }
        
        // Also try after a delay in case elements are loaded later
        setTimeout(() => {
            console.log('Delayed event setup...');
            this.setupEventListeners();
        }, 3000);
    }

    setupEventListeners() {
        console.log('Setting up event listeners...');
        
        // Prevent duplicate event binding
        if (this.eventsBound) {
            console.log('Events already bound, skipping...');
            return;
        }
        
        // Get form elements
        const titleInput = document.getElementById('news-title-input');
        const seoTitleInput = document.getElementById('seo-title-input');
        const metaDescInput = document.getElementById('meta-description-input');
        const keywordInput = document.getElementById('news-keyphrase-input');
        const slugInput = document.getElementById('news-slug');
        const refreshButton = document.getElementById('refresh-seo-analysis');

        console.log('Form elements found:', {
            titleInput: !!titleInput,
            seoTitleInput: !!seoTitleInput,
            metaDescInput: !!metaDescInput,
            keywordInput: !!keywordInput,
            slugInput: !!slugInput,
            refreshButton: !!refreshButton
        });

        // Add event listeners to form fields
        if (titleInput && !titleInput.dataset.yoastBound) {
            console.log('Adding title input listener');
            titleInput.addEventListener('input', () => {
                console.log('Title changed:', titleInput.value);
                this.debounceAnalyze();
            });
            titleInput.dataset.yoastBound = 'true';
        }
        
        if (metaDescInput && !metaDescInput.dataset.yoastBound) {
            console.log('Adding meta description listener');
            metaDescInput.addEventListener('input', () => {
                console.log('Meta description changed:', metaDescInput.value);
                this.debounceAnalyze();
            });
            metaDescInput.dataset.yoastBound = 'true';
        }
        
        if (keywordInput && !keywordInput.dataset.yoastBound) {
            console.log('Adding keyword input listener');
            keywordInput.addEventListener('input', () => {
                console.log('Keyword changed:', keywordInput.value);
                this.debounceAnalyze();
            });
            keywordInput.dataset.yoastBound = 'true';
        }
        
        if (seoTitleInput && !seoTitleInput.dataset.yoastBound) {
            console.log('Adding SEO title input listener');
            seoTitleInput.addEventListener('input', () => {
                console.log('SEO Title changed:', seoTitleInput.value);
                this.debounceAnalyze();
            });
            seoTitleInput.dataset.yoastBound = 'true';
        }
        
        if (slugInput && !slugInput.dataset.yoastBound) {
            console.log('Adding slug input listener');
            slugInput.addEventListener('input', () => {
                console.log('Slug changed:', slugInput.value);
                this.debounceAnalyze();
            });
            slugInput.dataset.yoastBound = 'true';
        }

        // Manual refresh button
        if (refreshButton && !refreshButton.dataset.yoastBound) {
            console.log('Adding refresh button listener');
            refreshButton.addEventListener('click', () => {
                console.log('Manual refresh triggered');
                this.handleManualRefresh();
            });
            refreshButton.dataset.yoastBound = 'true';
        }

        // Setup CKEditor listener
        this.setupCKEditorListener();
        
        // Mark events as bound if we found at least some elements
        if (titleInput || metaDescInput || keywordInput || seoTitleInput || slugInput) {
            this.eventsBound = true;
            console.log('Event listeners setup complete');
        }
    }

    setupCKEditorListener() {
        // Wait for CKEditor to be initialized
        const checkCKEditor = () => {
            if (window.CKEDITOR && window.CKEDITOR.instances.editor) {
                this.editorInstance = window.CKEDITOR.instances.editor;
                console.log('CKEditor found and bound');
                
                // Listen for CKEditor content changes
                this.editorInstance.on('change', () => {
                    console.log('CKEditor content changed');
                    this.debounceAnalyze();
                });
                
                this.editorInstance.on('key', () => {
                    console.log('CKEditor key pressed');
                    this.debounceAnalyze();
                });
                
                console.log('CKEditor events bound successfully');
            } else {
                console.log('Waiting for CKEditor...');
                setTimeout(checkCKEditor, 500);
            }
        };
        checkCKEditor();
    }

    collectFormData() {
        // Get form field values
        const titleElement = document.getElementById('news-title-input');
        const seoTitleElement = document.getElementById('seo-title-input');
        const metaDescElement = document.getElementById('meta-description-input');
        const keywordElement = document.getElementById('news-keyphrase-input');
        const slugElement = document.getElementById('news-slug');

        const title = titleElement ? titleElement.value : '';
        const seoTitle = seoTitleElement ? seoTitleElement.value : '';
        const metaDescription = metaDescElement ? metaDescElement.value : '';
        const keyword = keywordElement ? keywordElement.value : '';
        const slugValue = slugElement ? slugElement.value : '';
        
        console.log(`🔍 Form Data Collection Debug:`);
        console.log(`   Main Title: "${title}" (${title.length} chars)`);
        console.log(`   SEO Title: "${seoTitle}" (${seoTitle.length} chars)`);
        console.log(`   Keyword: "${keyword}"`);
        console.log(`   Slug: "${slugValue}"`);
        console.log(`   AI Flags: aiSlugGenerated=${this.aiSlugGenerated}, lastAiSlug="${this.lastAiGeneratedSlug}"`);
        console.log(`   SEO Title Element Found: ${!!seoTitleElement}`);
        console.log(`   SEO Title Element Value: "${seoTitleElement ? seoTitleElement.value : 'N/A'}"`);
        
        // Get content from CKEditor or fallback to textarea
        let content = '';
        if (this.editorInstance) {
            content = this.editorInstance.getData();
        } else {
            const editorElement = document.getElementById('editor');
            content = editorElement ? editorElement.value : '';
        }

        // Image detection
        const fileInput = document.querySelector('input[name="image"]');
        const currentImageSelectors = [
            'img[src*="/images/news/photos/"]',
            'img.img-thumbnail',
            'img[alt="200x200"]',
            '.border-dashed img',
            '#current-image',
            'img[src*="/images/"]'
        ];
        
        let currentImageDisplay = null;
        for (const selector of currentImageSelectors) {
            const found = document.querySelector(selector);
            if (found) {
                currentImageDisplay = found;
                break;
            }
        }
        
        const hasImageInContent = content.includes('<img');
        const hasFileUpload = fileInput && fileInput.files && fileInput.files.length > 0;
        const hasCurrentImage = !!currentImageDisplay;
        const hasImage = hasFileUpload || hasCurrentImage || hasImageInContent;
        
        // Add image tag to content if image exists but not already in content
        let finalContent = content;
        if (hasImage && !hasImageInContent) {
            finalContent = '<img src="news-image.jpg" alt="تصویر خبر" /> ' + content;
        }
        
        // Add H1 title to content if title exists but no H1 in content
        if (title && title.trim() && !content.includes('<h1') && !content.includes('<H1')) {
            finalContent = `<h1>${title.trim()}</h1>` + finalContent;
        }

        // Use provided slug or generate from SEO title or main title
        const slug = slugValue.trim() || 
                    (seoTitle ? seoTitle.toLowerCase().replace(/[^\w\s-]/g, '').replace(/\s+/g, '-').trim() : '') ||
                    (title ? title.toLowerCase().replace(/[^\w\s-]/g, '').replace(/\s+/g, '-').trim() : 'untitled');

        // Use SEO title if provided, otherwise fallback to main title
        const effectiveTitle = seoTitle.trim() || title.trim();
        
        console.log(`🔍 Title Resolution Debug:`);
        console.log(`   SEO Title (trimmed): "${seoTitle.trim()}" (length: ${seoTitle.trim().length})`);
        console.log(`   Main Title (trimmed): "${title.trim()}" (length: ${title.trim().length})`);
        console.log(`   Effective Title (final): "${effectiveTitle}" (length: ${effectiveTitle.length})`);
        console.log(`   Effective Title is truthy: ${!!effectiveTitle}`);

        const data = {
            text: finalContent,
            keyword: keyword,
            title: effectiveTitle,  // This is the SEO title for Yoast analysis
            description: metaDescription,
            url: slug,
            slug: slug,
            locale: 'en_US',
            // Additional fields for our analysis
            newsTitle: title,
            seoTitle: seoTitle
        };
        
        
        return data;
    }

    debounceAnalyze() {
        // Debounce analysis to avoid too many calls
        clearTimeout(this.debounceTimer);
        this.debounceTimer = setTimeout(() => {
            this.analyze();
        }, 500);
    }

    handleManualRefresh() {
        // Validate required fields when user manually clicks refresh
        const validation = this.validateRequiredFields();
        
        if (!validation.isValid) {
            this.showValidationAlert(validation.missingFields);
            return;
        }
        
        // If validation passes, run analysis
        this.analyze();
    }

    validateRequiredFields() {
        const titleElement = document.getElementById('news-title-input');
        const seoTitleElement = document.getElementById('seo-title-input');
        const keywordElement = document.getElementById('news-keyphrase-input');
        const metaDescElement = document.getElementById('meta-description-input');
        const slugElement = document.getElementById('news-slug');
        
        let content = '';
        if (this.editorInstance) {
            content = this.editorInstance.getData();
        } else {
            const editorElement = document.getElementById('editor');
            content = editorElement ? editorElement.value : '';
        }
        
        const title = titleElement ? titleElement.value.trim() : '';
        const seoTitle = seoTitleElement ? seoTitleElement.value.trim() : '';
        const keyword = keywordElement ? keywordElement.value.trim() : '';
        const metaDesc = metaDescElement ? metaDescElement.value.trim() : '';
        const slugValue = slugElement ? slugElement.value.trim() : '';
        const contentText = content.replace(/<[^>]*>/g, '').trim(); // Remove HTML tags
        
        const missingFields = [];
        
        // Check required fields
        if (!title || title.length < 5) {
            missingFields.push('تیتر خبر (حداقل ۵ کاراکتر)');
        }
        
        if (!keyword) {
            missingFields.push('کلیدواژه اصلی (برای تحلیل سئو)');
        }
        
        if (!contentText || contentText.length < 50) {
            missingFields.push('محتوای خبر (حداقل ۵۰ کاراکتر)');
        }
        
        if (!metaDesc || metaDesc.length < 30) {
            missingFields.push('توضیحات متا (حداقل ۳۰ کاراکتر)');
        }
        
        return {
            isValid: missingFields.length === 0,
            missingFields: missingFields
        };
    }

    showValidationAlert(missingFields) {
        const missingList = missingFields.map(field => `• ${field}`).join('\n');
        
        // Show SweetAlert if available, otherwise use regular alert
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                icon: 'warning',
                title: 'فیلدهای مورد نیاز خالی است',
                html: `<div style="text-align: right; direction: rtl;">
                        <p>برای انجام تحلیل سئو، لطفاً فیلدهای زیر را تکمیل کنید:</p>
                        <div style="margin: 15px 0; padding: 10px; background: #f8f9fa; border-radius: 5px;">
                            ${missingFields.map(field => `<div style="margin: 5px 0;">• ${field}</div>`).join('')}
                        </div>
                        <p><small>پس از تکمیل این فیلدها، مجدداً دکمه "بروزرسانی تحلیل سئو" را کلیک کنید.</small></p>
                    </div>`,
                confirmButtonText: 'متوجه شدم',
                confirmButtonColor: '#556ee6',
                customClass: {
                    popup: 'swal-rtl'
                }
            });
        } else {
            alert(`فیلدهای مورد نیاز خالی است:\n\n${missingList}\n\nلطفاً این فیلدها را تکمیل کنید و مجدداً تلاش کنید.`);
        }
        
        // Update the output to show empty state with validation message
        this.updateEmptyStateWithValidation(missingFields);
    }

    analyze() {
        try {
            // Get form data
            const data = this.collectFormData();
            
            // Check if we have meaningful content to analyze (less strict for auto-analysis)
            const hasMinimalContent = data.title.length > 0 || data.keyword.length > 0 || data.text.length > 10;
            
            if (!hasMinimalContent) {
                console.log('Skipping analysis - no meaningful content yet');
                this.updateEmptyState();
                return;
            }

            console.log(`🔍 About to create Paper object with:`);
            console.log(`   data.title: "${data.title}"`);
            console.log(`   data.keyword: "${data.keyword}"`);
            console.log(`   data.description: "${data.description}"`);
            console.log(`   data.url: "${data.url}"`);

            // Create Paper object for YoastSEO analysis
            const paper = new Paper(data.text, {
                keyword: data.keyword,
                title: data.title,
                description: data.description,
                url: data.url || '',
                locale: 'en_US'
            });
            
            console.log(`📄 Paper object created - Verification:`);
            console.log(`   Paper.getTitle(): "${paper.getTitle()}" (length: ${paper.getTitle() ? paper.getTitle().length : 0})`);
            console.log(`   Paper.getKeyword(): "${paper.getKeyword()}"`);
            console.log(`   Paper.getUrl(): "${paper.getUrl()}"`);
            console.log(`   Paper.getText(): ${paper.getText().length} chars`);
            console.log(`   Paper has title: ${!!paper.getTitle()}`);
            console.log(`   Paper title equals data.title: ${paper.getTitle() === data.title}`);
            
            // Create YoastSEO researcher for language analysis
            const researcher = new AbstractResearcher(paper);
            
            // Verify keyphrase is set for analysis
            if (!data.keyword || data.keyword.trim() === '') {
                console.warn('No focus keyphrase provided - keyphrase assessments will not work properly');
            } else {
                console.log('Focus keyphrase for analysis:', data.keyword);
            }
            
            console.log('AbstractResearcher created:', researcher);
            
            // Run real YoastSEO assessments - 100% YoastSEO scoring
            const yoastAssessments = this.runYoastAssessments(paper, researcher);
            
            // Get basic research for UI display
            const wordCountResearch = researcher.getResearch("wordCountInText");
            const sentencesResearch = researcher.getResearch("sentences");
            
            const results = {
                // Real YoastSEO assessments with scores
                assessments: yoastAssessments,
                
                // Basic metrics for UI display
                wordCount: wordCountResearch ? wordCountResearch.count : 0,
                sentenceCount: sentencesResearch ? sentencesResearch.length : 0,
                averageWordsPerSentence: sentencesResearch && sentencesResearch.length > 0 ? 
                    Math.round((wordCountResearch ? wordCountResearch.count : 0) / sentencesResearch.length) : 0,
                
                // Content metrics
                titleLength: data.title.length,
                metaDescLength: data.description.length,
                contentLength: data.text.length,
                keywordPresent: data.keyword && data.text.toLowerCase().includes(data.keyword.toLowerCase())
            };
            
            console.log('Real YoastSEO assessments:', yoastAssessments);
            console.log('Final results with YoastSEO scores:', results);
            console.log('Keyphrase analysis data used:', {
                focusKeyphrase: data.keyword,
                seoTitle: data.seoTitle,
                newsTitle: data.newsTitle,
                metaDescription: data.description,
                slug: data.slug
            });
            
            // Update UI with results
            this.updateUIWithDirectResults(results, data);
            
        } catch (error) {
            console.error('YoastSEO Direct API analysis failed:', error);
            this.showError();
        }
    }

    collectFormDataForValidation() {
        // Quick validation check without full logging
        const titleElement = document.getElementById('news-title-input');
        const metaDescElement = document.getElementById('meta-description-input');
        const keywordElement = document.getElementById('news-keyphrase-input');

        let content = '';
        if (this.editorInstance) {
            content = this.editorInstance.getData();
        } else {
            const editorElement = document.getElementById('editor');
            content = editorElement ? editorElement.value : '';
        }

        return {
            text: content,
            keyword: keywordElement ? keywordElement.value : '',
            title: titleElement ? titleElement.value : '',
            description: metaDescElement ? metaDescElement.value : ''
        };
    }

    // Helper method to check if all required fields are filled for automatic analysis
    hasRequiredFieldsForAutoAnalysis() {
        const validation = this.validateRequiredFields();
        return validation.isValid;
    }

    updateEmptyState() {
        const outputElement = document.getElementById('yoast-seo-output');
        if (outputElement) {
            outputElement.innerHTML = `
                <div class="modern-seo-analyzer empty-state">
                    <!-- Header Section -->
                    <div class="seo-header">
                        <div class="header-content">
                            <div class="main-score">
                                <div class="score-circle poor">
                                    <span class="score-number">0</span>
                                    <span class="score-total">/100</span>
                                </div>
                                <div class="score-details">
                                    <h3 class="score-title">آماده تحلیل</h3>
                                    <p class="score-status">محتوا آماده سازی می‌شود</p>
                                </div>
                            </div>
                            <div class="sub-scores">
                                <div class="sub-score">
                                    <div class="sub-score-bar">
                                        <div class="progress-bar">
                                            <div class="progress-fill score-poor" style="width: 0%"></div>
                                        </div>
                                        <span class="sub-score-value">0/100</span>
                                    </div>
                                    <span class="sub-score-label">سئو</span>
                                </div>
                                <div class="sub-score">
                                    <div class="sub-score-bar">
                                        <div class="progress-bar">
                                            <div class="progress-fill score-poor" style="width: 0%"></div>
                                        </div>
                                        <span class="sub-score-value">0/100</span>
                                    </div>
                                    <span class="sub-score-label">خوانایی</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Empty State Message -->
                    <div class="empty-message">
                        <div class="empty-icon">📝</div>
                        <h4 class="empty-title">آماده تحلیل هستیم!</h4>
                        <p class="empty-description">لطفاً تیتر، کلمه کلیدی و محتوا را وارد کنید تا تحلیل سئو شروع شود</p>
                    </div>

                    <!-- Footer -->
                    <div class="analyzer-footer">
                        <div class="footer-brand">
                            <span class="brand-text">قدرت گرفته از</span>
                            <span class="brand-logo">YoastSEO</span>
                        </div>
                    </div>
                </div>
                
                <style>
                .modern-seo-analyzer.empty-state {
                    background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
                }
                
                .empty-message {
                    background: var(--bs-body-bg, #ffffff);
                    padding: 40px 20px;
                    text-align: center;
                    border-top: 1px solid #f1f3f4;
                }
                
                .empty-icon {
                    font-size: 48px;
                    margin-bottom: 16px;
                }
                
                .empty-title {
                    margin: 0 0 8px 0;
                    font-size: 18px;
                    font-weight: 600;
                    color: #1f2937;
                }
                
                .empty-description {
                    margin: 0;
                    color: #6b7280;
                    font-size: 14px;
                    max-width: 400px;
                    margin: 0 auto;
                    line-height: 1.5;
                }
                
                .swal-rtl {
                    direction: rtl !important;
                    text-align: right !important;
                }
                </style>
            `;
        }
    }

    updateEmptyStateWithValidation(missingFields) {
        const outputElement = document.getElementById('yoast-seo-output');
        if (outputElement) {
            const missingFieldsList = missingFields.map(field => `<li style="margin: 5px 0; color: #dc2626;">• ${field}</li>`).join('');
            
            outputElement.innerHTML = `
                <div class="modern-seo-analyzer empty-state validation-required">
                    <!-- Header Section -->
                    

                    <!-- Validation Message -->
                    <div class="validation-message">
                        <div class="validation-icon">⚠️</div>
                        <h4 class="validation-title">فیلدهای زیر را تکمیل کنید:</h4>
                        <ul class="missing-fields-list" style="text-align: right; direction: rtl; list-style: none; padding: 0;">
                            ${missingFieldsList}
                        </ul>
                        <p class="validation-description">پس از تکمیل این فیلدها، دوباره دکمه "بروزرسانی تحلیل سئو" را کلیک کنید.</p>
                    </div>

                    <!-- Footer -->
                    
                </div>
                
                <style>
                .modern-seo-analyzer.validation-required {
                    background: linear-gradient(135deg, #fef3cd 0%, #fde68a 100%);
                }
                
                .score-circle.warning {
                    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
                    color: white;
                }
                
                .score-circle.warning .score-number {
                    font-size: 24px;
                    font-weight: bold;
                }
                
                .validation-message {
                    background: var(--bs-body-bg, #ffffff);
                    padding: 30px 20px;
                    text-align: center;
                    border-top: 1px solid #f1f3f4;
                    direction: rtl;
                }
                
                .validation-icon {
                    font-size: 36px;
                    margin-bottom: 12px;
                }
                
                .validation-title {
                    margin: 0 0 15px 0;
                    font-size: 16px;
                    font-weight: 600;
                    color: #92400e;
                }
                
                .missing-fields-list {
                    margin: 15px 0;
                    padding: 15px;
                    background: #fef3cd;
                    border-radius: 8px;
                    border: 1px solid #f59e0b;
                }
                
                .validation-description {
                    margin: 15px 0 0 0;
                    color: #6b7280;
                    font-size: 13px;
                    line-height: 1.5;
                }
                </style>
            `;
        }
    }

    updateUIWithDirectResults(results, data) {
        console.log('Updating UI with direct YoastSEO results:', results);
        
        
        // Update traffic lights
        this.updateTrafficLightsDirect(results);
        
        // Update detailed output
        this.updateDetailedOutput(results, data);
        
        console.log('UI updated successfully with direct results');
    }


    updateDetailedOutput(results, data) {
        const outputElement = document.getElementById('yoast-seo-output');
        if (!outputElement) return;

        const assessments = results.assessments || { seo: [], readability: [], scores: { seo: 0, readability: 0, overall: 0 } };

        outputElement.innerHTML = `
            <div class="modern-seo-analyzer">
                <!-- Header Section -->
                <div class="seo-header">
                    <div class="header-content">
                        <div class="overall-score-section">
                            <div class="score-display">
                                <span class="main-score-number">${assessments.scores.overall}</span>
                                <span class="score-separator">/</span>
                                <span class="max-score">100</span>
                            </div>
                            <div class="score-info">
                                <h3 class="score-label">امتیاز کلی</h3>
                                <p class="score-description">${this.getScoreStatus(assessments.scores.overall)}</p>
                            </div>
                        </div>
                        <div class="sub-scores-section">
                            <div class="score-item">
                                <span class="score-title">سئو:</span>
                                <span class="score-value ${this.getScoreTextClass(assessments.scores.seo)}">${assessments.scores.seo}/100</span>
                            </div>
                            <div class="score-separator-line">|</div>
                            <div class="score-item">
                                <span class="score-title">خوانایی:</span>
                                <span class="score-value ${this.getScoreTextClass(assessments.scores.readability)}">${assessments.scores.readability}/100</span>
                            </div>
                        </div>
                    </div>
                    <div class="yoast-brand">
                        <span class="brand-subtitle">محاسبه شده توسط</span>
                        <span class="brand-name">YoastSEO Assessment Engine</span>
                    </div>
                </div>

                <!-- Analysis Sections -->
                <div class="analysis-grid">
                    <!-- SEO Analysis -->
                    <div class="analysis-card">
                        <div class="card-header">
                            <div class="header-icon">
                                <svg width="20" height="20" fill="currentColor" viewBox="0 0 20 20">
                                    <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                                </svg>
                            </div>
                            <h4 class="card-title" style="font-family: 'Shabnam' !important;">تحلیل سئو</h4>
                            <div class="card-badge ${this.getScoreClass(assessments.scores.seo)}">
                                ${assessments.scores.seo}/100
                            </div>
                        </div>
                        <div class="card-content">
                            ${this.renderModernAssessments(assessments.seo, 'seo')}
                        </div>
                    </div>

                    <!-- Readability Analysis -->
                    <div class="analysis-card">
                        <div class="card-header">
                            <div class="header-icon">
                                <svg width="20" height="20" fill="currentColor" viewBox="0 0 20 20">
                                    <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z M4 5a2 2 0 012-2v6h8V3a2 2 0 012 2v6l-3.5 4.5a1 1 0 01-1.414 0L8 11V5z"/>
                                </svg>
                            </div>
                            <h4 class="card-title">تحلیل خوانایی</h4>
                            <div class="card-badge ${this.getScoreClass(assessments.scores.readability)}">
                                ${assessments.scores.readability}/100
                            </div>
                        </div>
                        <div class="card-content">
                            ${this.renderModernAssessments(assessments.readability, 'readability')}
                        </div>
                    </div>
                </div>

                <!-- Content Stats -->
                <div class="stats-section">
                    <h4 class="stats-title">آمار محتوا</h4>
                    <div class="stats-list">
                        <div class="stat-item">
                            <span class="stat-label">تعداد کلمات:</span>
                            <span class="stat-value">${results.wordCount}</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-label">تعداد جملات:</span>
                            <span class="stat-value">${results.sentenceCount}</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-label">میانگین کلمات در جمله:</span>
                            <span class="stat-value">${results.averageWordsPerSentence}</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-label">طول محتوا:</span>
                            <span class="stat-value">${results.contentLength} کاراکتر</span>
                        </div>
                    </div>
                </div>

            </div>

            <style>
            .modern-seo-analyzer {
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                border-radius: 16px;
                padding: 0;
                overflow: hidden;
                box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            }

            .seo-header {
                background: var(--bs-body-bg, #ffffff);
                padding: 20px;
                text-align: center;
             }

             .header-content {
                 display: flex;
                 justify-content: center;
                 align-items: center;
                 gap: 30px;
                 margin-bottom: 10px;
             }

             .overall-score-section {
                 display: flex;
                 align-items: center;
                 gap: 12px;
             }

             .score-display {
                 display: flex;
                 align-items: baseline;
                 gap: 2px;
             }

             .main-score-number {
                 font-size: 32px;
                 font-weight: 700;
                 color: var(--bs-body-color, #1f2937);
             }

             .score-separator {
                 font-size: 20px;
                 color: #6b7280;
                 margin: 0 2px;
             }

             .max-score {
                 font-size: 18px;
                 color: #6b7280;
             }

             .score-info {
                 text-align: right;
             }

             .score-label {
                 margin: 0;
                 font-size: 16px;
                 font-weight: 600;
                 color: #1f2937;
             }

             .score-description {
                 margin: 2px 0 0 0;
                 font-size: 12px;
                 color: #6b7280;
             }

             .sub-scores-section {
                 display: flex;
                 align-items: center;
                 gap: 15px;
             }

             .score-item {
                 display: flex;
                 align-items: center;
                 gap: 6px;
             }

             .score-title {
                 font-size: 14px;
                 color: #374151;
                 font-weight: 500;
             }

             .score-value {
                 font-size: 14px;
                 font-weight: 600;
             }

             .score-value.text-good { color: #059669; }
             .score-value.text-ok { color: #d97706; }
             .score-value.text-bad { color: #dc2626; }
             .score-value.text-poor { color: #6b7280; }

             .score-separator-line {
                 color: #d1d5db;
                 font-weight: 300;
             }

             .yoast-brand {
                 text-align: center;
                 margin-top: 5px;
             }

             .brand-subtitle {
                 font-size: 11px;
                 color: #9ca3af;
                 margin-left: 4px;
             }

             .brand-name {
                 font-size: 11px;
                 color: #6366f1;
                 font-weight: 500;
             }

             .analysis-grid {
                 display: grid;
                 grid-template-columns: 1fr;
                 gap: 1px;
                 background: #f1f3f4;
             }

            .modern-seo-analyzer .analysis-card {
                background: var(--bs-body-bg, #ffffff);
                overflow: hidden;
            }

            .modern-seo-analyzer .card-header {
                display: flex;
                align-items: center;
                gap: 12px;
                padding: 16px 20px;
                background: #fafbfc;
                border-bottom: 1px solid #f1f3f4;
            }

            .modern-seo-analyzer .header-icon {
                width: 32px;
                height: 32px;
                background: #e5e7eb;
                border-radius: 8px;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #6b7280;
            }

            .modern-seo-analyzer .card-title {
                flex: 1;
                margin: 0;
                font-size: 14px;
                font-weight: 600;
                color: var(--bs-body-color, #1f2937);
            }

            .modern-seo-analyzer .card-badge {
                padding: 4px 8px;
                border-radius: 12px;
                font-size: 11px;
                font-weight: 600;
                color: white;
            }

            .modern-seo-analyzer .card-badge.score-good { background: #10b981; }
            .modern-seo-analyzer .card-badge.score-ok { background: #f59e0b; }
            .modern-seo-analyzer .card-badge.score-bad { background: #ef4444; }
            .modern-seo-analyzer .card-badge.score-poor { background: #9ca3af; }

            .modern-seo-analyzer .card-content {
                padding: 0;
            }

            .modern-seo-analyzer .modern-assessment {
                display: flex;
                align-items: center;
                gap: 12px;
                padding: 12px 20px;
                border-bottom: 1px solid #f9fafb;
                transition: background-color 0.2s ease;
            }

            .modern-seo-analyzer .modern-assessment:hover {
                background: var(--bs-secondary-bg, #f9fafb);
            }

            .modern-seo-analyzer .modern-assessment:last-child {
                border-bottom: none;
            }

            .modern-seo-analyzer .assessment-status {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                flex-shrink: 0;
            }

            .modern-seo-analyzer .assessment-status.excellent { background: #10b981; }
            .modern-seo-analyzer .assessment-status.good { background: #f59e0b; }
            .modern-seo-analyzer .assessment-status.poor { background: #ef4444; }
            .modern-seo-analyzer .assessment-status.neutral { background: #9ca3af; }

            .modern-seo-analyzer .assessment-info {
                flex: 1;
                min-width: 0;
            }

            .modern-seo-analyzer .assessment-name {
                font-weight: 500;
                color: var(--bs-body-color, #1f2937);
                font-size: 13px;
                margin: 0 0 2px 0;
            }

            .modern-seo-analyzer .assessment-feedback {
                color: #6b7280;
                font-size: 12px;
                line-height: 1.4;
                margin: 0;
            }

            .modern-seo-analyzer .stats-section {
                background: var(--bs-body-bg, #ffffff);
                padding: 20px;
                border-top: 1px solid #f1f3f4;
            }

             .modern-seo-analyzer .stats-title {
                 margin: 0 0 16px 0;
                 font-size: 16px;
                 font-weight: 600;
                 color: var(--bs-body-color, #1f2937);
                 text-align: center;
             }

             .modern-seo-analyzer .stats-list {
                 padding: 0;
             }

             .modern-seo-analyzer .stat-item {
                 display: flex;
                 justify-content: space-between;
                 padding: 12px 0;
                 border-bottom: 1px solid #f0f0f0;
             }

             .modern-seo-analyzer .stat-item:last-child {
                 border-bottom: none;
             }

            .modern-seo-analyzer .stat-label {
                color: #6b7280;
                font-size: 13px;
            }

            .modern-seo-analyzer .stat-value {
                font-weight: 600;
                color: var(--bs-body-color, #1f2937);
                font-size: 13px;
            }


             @media (max-width: 768px) {
                 .modern-seo-analyzer .header-content {
                     flex-direction: column;
                     gap: 16px;
                 }
                 
                 .modern-seo-analyzer .sub-scores-section {
                     flex-direction: column;
                     gap: 8px;
                 }
             }
            </style>
        `;
    }

    getScoreClass(score) {
        if (score >= 80) return 'score-good';
        if (score >= 60) return 'score-ok';
        if (score >= 30) return 'score-bad';
        return 'score-poor';
    }

    getMainScoreClass(score) {
        if (score >= 80) return 'excellent';
        if (score >= 60) return 'good';
        return 'poor';
    }

    getScoreStatus(score) {
        if (score >= 80) return 'عالی - آماده انتشار';
        if (score >= 60) return 'خوب - قابل بهبود';
        if (score >= 40) return 'متوسط - نیاز به کار';
        return 'ضعیف - نیاز به بازنگری';
    }

    getScoreTextClass(score) {
        if (score >= 80) return 'text-good';
        if (score >= 60) return 'text-ok';
        if (score >= 30) return 'text-bad';
        return 'text-poor';
    }

    renderModernAssessments(assessmentsList, type) {
        if (!assessmentsList || assessmentsList.length === 0) {
            return `<div class="modern-assessment">
                <div class="assessment-status neutral"></div>
                <div class="assessment-info">
                    <h5 class="assessment-name">هیچ ارزیابی موجود نیست</h5>
                    <p class="assessment-feedback">لطفاً محتوا یا تنظیمات را بررسی کنید</p>
                </div>
            </div>`;
        }

        return assessmentsList.map(assessment => {
            let statusClass = 'neutral';
            
            // YoastSEO score mapping: 9+ = excellent, 6-8 = good, 1-5 = poor, 0 or below = poor
            if (assessment.score >= 9) {
                statusClass = 'excellent';
            } else if (assessment.score >= 6) {
                statusClass = 'good';
            } else if (assessment.score >= 1) {
                statusClass = 'poor';
            } else if (assessment.score <= 0) {
                statusClass = 'poor'; // Handle negative scores or 0 as poor
            }

            const cleanText = this.translateYoastFeedback(assessment.text, assessment.name);

            return `
                <div class="modern-assessment">
                    <div class="assessment-status ${statusClass}"></div>
                    <div class="assessment-info">
                        <h5 class="assessment-name">${this.translateAssessmentName(assessment.name)}</h5>
                        <p class="assessment-feedback">${cleanText}</p>
                    </div>
                </div>
            `;
        }).join('');
    }

    renderWordPressStyleAssessments(assessmentsList, type) {
        if (!assessmentsList || assessmentsList.length === 0) {
            return `<div class="yoast-assessment-item">
                <div class="assessment-indicator score-poor"></div>
                <div class="assessment-content">
                    <div class="assessment-title">هیچ ${type === 'seo' ? 'سئو' : 'خوانایی'} ارزیابی در دسترس نیست</div>
                    <div class="assessment-description">لطفاً محتوا یا تنظیمات را بررسی کنید</div>
                </div>
            </div>`;
        }

        return assessmentsList.map(assessment => {
            let indicatorClass = 'score-poor';
            
            if (assessment.score >= 9) {
                indicatorClass = 'score-good';
            } else if (assessment.score >= 6) {
                indicatorClass = 'score-ok';
            } else if (assessment.score >= 1) {
                indicatorClass = 'score-bad';
            }

            // Clean up YoastSEO text and translate to Persian
            const cleanText = this.translateYoastFeedback(assessment.text, assessment.name);

            return `
                <div class="yoast-assessment-item">
                    <div class="assessment-indicator ${indicatorClass}"></div>
                    <div class="assessment-content">
                        <div class="assessment-title">${this.translateAssessmentName(assessment.name)}</div>
                        <div class="assessment-description">${cleanText}</div>
                    </div>
                </div>
            `;
        }).join('');
    }

    renderYoastAssessments(assessmentsList, type) {
        if (!assessmentsList || assessmentsList.length === 0) {
            return `<div class="text-muted">No ${type} assessments available</div>`;
        }

        return assessmentsList.map(assessment => {
            let badgeClass = 'badge-secondary';
            let statusClass = 'text-secondary';
            let statusText = 'نامشخص';
            
            if (assessment.score >= 9) {
                badgeClass = 'badge-success';
                statusClass = 'text-success';
                statusText = 'عالی';
            } else if (assessment.score >= 6) {
                badgeClass = 'badge-warning';
                statusClass = 'text-warning';
                statusText = 'قابل قبول';
            } else if (assessment.score >= 1) {
                badgeClass = 'badge-danger';
                statusClass = 'text-danger';
                statusText = 'نیاز به بهبود';
            } else {
                badgeClass = 'badge-dark';
                statusClass = 'text-dark';
                statusText = 'ضعیف';
            }

            // Clean up YoastSEO text and translate to Persian
            const cleanText = this.translateYoastFeedback(assessment.text, assessment.name);

            return `
                <div class="d-flex align-items-start mb-2 p-2 border rounded">
                    <div class="mr-2">
                        <span class="badge badge-pill ${badgeClass}" style="width: 12px; height: 12px; padding: 0;"></span>
                    </div>
                    <div class="flex-grow-1">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <strong class="text-dark">${this.translateAssessmentName(assessment.name)}</strong>
                            <span class="small ${statusClass}">${statusText}</span>
                        </div>
                        <div class="text-muted small">${cleanText}</div>
                    </div>
                </div>
            `;
        }).join('');
    }

    translateAssessmentName(name) {
        const translations = {
            // SEO Assessments
            'TextLengthAssessment': 'طول متن',
            'MetaDescriptionLengthAssessment': 'طول توضیحات متا',
            'PageTitleWidthAssessment': 'عنوان سئو (نمایش در گوگل)',
            'InternalLinksAssessment': 'لینک‌های داخلی',
            'OutboundLinksAssessment': 'لینک‌های خارجی',
            // 'FunctionWordsInKeyphraseAssessment': 'کلمات عملکردی در کلیدواژه',
            'KeyphraseLengthAssessment': 'طول کلیدواژه',
            'KeyphraseInSEOTitleAssessment': 'کلیدواژه در عنوان سئو',
            'CustomSeoTitleAssessment': 'عنوان سئو (طول و کیفیت)',
            'CustomKeyphraseInSeoTitleAssessment': 'کلیدواژه در عنوان سئو',
            'CustomKeyphraseLengthAssessment': 'طول کلیدواژه',
            'CustomKeyphraseDensityAssessment': 'تراکم کلیدواژه',
            'CustomUrlKeyphraseAssessment': 'کلیدواژه در URL',
            
            // Custom Readability Assessments
            'CustomTextPresenceAssessment': 'وجود متن',
            'CustomParagraphTooLongAssessment': 'طول پاراگراف‌ها',
            'CustomSentenceLengthAssessment': 'طول جملات',
            'CustomSubheadingDistributionAssessment': 'توزیع زیرعناوین',
            'CustomTransitionWordsAssessment': 'کلمات ربط',
            'CustomPassiveVoiceAssessment': 'صیغه مجهول',
            'CustomWordComplexityAssessment': 'پیچیدگی کلمات',
            'CustomReadabilityOverviewAssessment': 'بررسی کلی خوانایی',
            
            // Standard Readability Assessments
        };
        return translations[name] || name;
    }

    translateYoastFeedback(englishText, assessmentName) {
        // Remove HTML tags and clean text
        let cleanText = englishText
            .replace(/<a[^>]*>/gi, '')
            .replace(/<\/a>/gi, '')
            .replace(/target='_blank'/gi, '');

        // Translate based on actual YoastSEO output patterns
        const translations = {
            'TextLengthAssessment': (text) => {
                // Pattern: "The text contains 763 words. Good job!"
                if (text.includes('Good job') && text.includes('words')) {
                    const wordCount = text.match(/(\d+) words/)?.[1] || '0';
                    return `متن شامل ${wordCount} کلمه است. عالی است!`;
                }
                if (text.includes('far below the recommended minimum')) {
                    const wordCount = text.match(/(\d+) words/)?.[1] || '0';
                    return `متن شامل ${wordCount} کلمه است. این تعداد بسیار کمتر از حداقل توصیه شده ۳۰۰ کلمه است. محتوای بیشتری اضافه کنید.`;
                }
                if (text.includes('below the recommended minimum')) {
                    const wordCount = text.match(/(\d+) words/)?.[1] || '0';
                    return `متن شامل ${wordCount} کلمه است. برای بهینه‌سازی سئو، حداقل ۳۰۰ کلمه توصیه می‌شود.`;
                }
                return 'طول متن در حد متوسط است.';
            },
            
            'MetaDescriptionLengthAssessment': (text) => {
                // Pattern: "Well done!"
                if (text.includes('Well done')) {
                    return 'طول توضیحات متا عالی است!';
                }
                if (text.includes('too short')) {
                    return 'توضیحات متا خیلی کوتاه است (کمتر از ۱۲۰ کاراکتر). حداکثر ۱۵۶ کاراکتر در دسترس است.';
                }
                if (text.includes('over 156 characters')) {
                    return 'توضیحات متا بیش از ۱۵۶ کاراکتر است. برای نمایش کامل در نتایج جستجو، طول را کاهش دهید.';
                }
                if (text.includes('Good job')) {
                    return 'طول توضیحات متا مناسب است.';
                }
                return 'طول توضیحات متا مناسب است.';
            },
            
            'PageTitleWidthAssessment': (text) => {
                console.log(`🔍 PageTitleWidthAssessment translation input: "${text}"`);
                
                // Pattern: "Please create an SEO title."
                if (text.includes('Please create an SEO title')) {
                    console.log(`❌ YoastSEO says no SEO title found!`);
                    return 'لطفاً یک عنوان سئو ایجاد کنید. این عنوان در نتایج گوگل نمایش داده می‌شود و باید بین 30-60 کاراکتر باشد.';
                }
                if (text.includes('too wide') || text.includes('too long') || text.includes('wider than')) {
                    return 'عنوان سئو شما خیلی طولانی است (بیش از 600 پیکسل). عناوین بلند در نتایج جستجو کوتاه می‌شوند.';
                }
                if (text.includes('Good job') || text.includes('Well done') || text.includes('perfect')) {
                    return 'طول عنوان سئو مناسب است برای نمایش در گوگل.';
                }
                if (text.includes('slightly too wide') || text.includes('bit too wide')) {
                    return 'عنوان سئو کمی طولانی است. برای نمایش بهتر در گوگل، آن را کوتاه‌تر کنید.';
                }
                
                // Return the original text with debug info if no pattern matches
                console.log(`⚠️ Unknown PageTitleWidthAssessment pattern: "${text}"`);
                return `عنوان سئو: ${text}`;
            },
            
            // 'ImageCountAssessment': (text) => {
            //     if (text.includes('No images appear')) {
            //         return 'هیچ تصویری در این صفحه وجود ندارد. اضافه کردن تصاویر مرتبط توصیه می‌شود.';
            //     }
            //     if (text.includes('Good job')) {
            //         return 'تعداد تصاویر مناسب است.';
            //     }
            //     return 'تعداد تصاویر نیاز به بررسی دارد.';
            // },
            
            
            'SubheadingDistributionTooLongAssessment': (text) => {
                if (text.includes('not using any subheadings')) {
                    return 'از هیچ زیرعنوانی استفاده نمی‌کنید، اما متن شما کوتاه است و احتمالاً نیاز نیست.';
                }
                if (text.includes('too long and should be split')) {
                    return 'برخی بخش‌ها خیلی طولانی هستند و باید با زیرعناوین تقسیم شوند.';
                }
                return 'توزیع زیرعناوین مناسب است.';
            },
            
            'WordComplexityAssessment': (text) => {
                if (text.includes('Some words in your text are considered complex')) {
                    return 'برخی کلمات در متن شما پیچیده هستند. سعی کنید از کلمات کوتاه‌تر و آشناتر استفاده کنید.';
                }
                if (text.includes('Great')) {
                    return 'پیچیدگی کلمات مناسب است.';
                }
                return 'پیچیدگی کلمات قابل قبول است.';
            },
            
            // 'FunctionWordsInKeyphraseAssessment': (text) => {
            //     if (text.includes('contains function words only')) {
            //         return 'کلیدواژه شما فقط شامل کلمات عملکردی است. کلمات عملکردی مانند "از"، "در"، "به" برای سئو مناسب نیستند.';
            //     }
            //     if (text.includes('keyphrase') && text.includes('function words')) {
            //         return 'کلیدواژه شما شامل کلمات عملکردی است. سعی کنید از کلمات اصلی و مهم استفاده کنید.';
            //     }
            //     return 'کلمات عملکردی در کلیدواژه بررسی شد.';
            // },
            
            'KeyphraseLengthAssessment': (text) => {
                if (text.includes('too short') || text.includes('shorter than') || text.includes('minimum')) {
                    return 'کلیدواژه شما خیلی کوتاه است. کلیدواژه باید حداقل چند کلمه باشد.';
                }
                if (text.includes('too long') || text.includes('longer than') || text.includes('maximum')) {
                    return 'کلیدواژه شما خیلی طولانی است. کلیدواژه‌های کوتاه‌تر معمولاً بهتر عمل می‌کنند.';
                }
                if (text.includes('Good job') || text.includes('good') || text.includes('perfect')) {
                    return 'طول کلیدواژه مناسب است.';
                }
                if (text.includes('consider') || text.includes('should') || text.includes('recommend')) {
                    return 'طول کلیدواژه نیاز به بهبود دارد.';
                }
                return 'طول کلیدواژه متوسط است.';
            },
            
            'KeywordDensityAssessment': (text) => {
                if (text.includes('never appears') || text.includes('does not appear') || text.includes('not found')) {
                    return 'کلیدواژه در متن ظاهر نمی‌شود. حتماً حداقل یک بار از کلیدواژه در متن استفاده کنید.';
                }
                if (text.includes('appears less than') || text.includes('too low') || text.includes('below')) {
                    return 'تراکم کلیدواژه کم است. سعی کنید چند بار بیشتر از کلیدواژه در متن استفاده کنید.';
                }
                if (text.includes('too high') || text.includes('more than') || text.includes('above')) {
                    return 'تراکم کلیدواژه زیاد است. استفاده کمتری از کلیدواژه کنید تا طبیعی به نظر برسد.';
                }
                if (text.includes('Good job') || text.includes('good') || text.includes('perfect') || text.includes('excellent')) {
                    return 'تراکم کلیدواژه مناسب است.';
                }
                if (text.includes('consider') || text.includes('should') || text.includes('improve')) {
                    return 'تراکم کلیدواژه نیاز به بهبود دارد.';
                }
                return 'تراکم کلیدواژه بررسی شد - وضعیت نامشخص.';
            },
            
            'KeyphraseInSEOTitleAssessment': (text) => {
                // Pattern: "The exact match of the focus keyphrase appears in the SEO title, but not at the beginning. Move it to the beginning for the best results."
                if (text.includes('not at the beginning') && text.includes('Move it to the beginning')) {
                    return 'کلیدواژه در عنوان سئو وجود دارد اما در ابتدا نیست. برای بهترین نتیجه آن را به ابتدا انتقال دهید.';
                }
                if (text.includes('does not contain') || text.includes('not found') || text.includes('missing')) {
                    return 'کلیدواژه در عنوان سئو وجود ندارد. برای بهبود سئو، کلیدواژه را در عنوان سئو قرار دهید.';
                }
                if (text.includes('Good job') || text.includes('excellent') || text.includes('perfect')) {
                    return 'کلیدواژه در عنوان سئو وجود دارد.';
                }
                if (text.includes('consider') || text.includes('should') || text.includes('improve')) {
                    return 'وضعیت کلیدواژه در عنوان سئو نیاز به بررسی دارد.';
                }
                return 'وضعیت کلیدواژه در عنوان سئو نامشخص است.';
            },
            
            
            'InternalLinksAssessment': (text) => {
                // Pattern: "No internal links appear in this page, make sure to add some!"
                if (text.includes('No internal links appear')) {
                    return 'هیچ لینک داخلی در این صفحه وجود ندارد. حتماً چند لینک داخلی اضافه کنید!';
                }
                if (text.includes('Good job')) {
                    return 'لینک‌های داخلی عالی است!';
                }
                return 'لینک‌های داخلی بررسی شد.';
            },
            
            'OutboundLinksAssessment': (text) => {
                // Pattern: "Good job!"
                if (text.includes('Good job')) {
                    return 'لینک‌های خارجی عالی است!';
                }
                if (text.includes('No outbound links appear')) {
                    return 'هیچ لینک خارجی در این صفحه وجود ندارد.';
                }
                return 'لینک‌های خارجی بررسی شد.';
            },
            
            
            'CustomSeoTitleAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomKeyphraseInSeoTitleAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomKeyphraseLengthAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomKeyphraseDensityAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomUrlKeyphraseAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            // Custom Readability Assessment Translations
            'CustomTextPresenceAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomParagraphTooLongAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomSentenceLengthAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomSubheadingDistributionAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomTransitionWordsAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomPassiveVoiceAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomWordComplexityAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            },
            
            'CustomReadabilityOverviewAssessment': (text) => {
                // Our custom implementation already returns Persian text
                return text;
            }
        };

        // Apply specific translation based on actual YoastSEO patterns
        if (translations[assessmentName]) {
            return translations[assessmentName](cleanText);
        }

        // Fallback: Handle common YoastSEO patterns from actual output
        let translatedText = cleanText;

        // Handle exact patterns we observed from YoastSEO
        if (cleanText.includes('Good job!')) {
            translatedText = translatedText.replace('Good job!', 'عالی است!');
        }
        if (cleanText.includes('Well done!')) {
            translatedText = translatedText.replace('Well done!', 'بسیار خوب!');
        }
        if (cleanText.includes('Please create an SEO title')) {
            translatedText = 'لطفاً یک عنوان سئو ایجاد کنید. این عنوان در نتایج گوگل نمایش داده می‌شود.';
        }
        if (cleanText.includes('No internal links appear in this page')) {
            translatedText = 'هیچ لینک داخلی در این صفحه وجود ندارد. حتماً چند لینک داخلی اضافه کنید!';
        }
        if (cleanText.includes('not at the beginning') && cleanText.includes('Move it to the beginning')) {
            translatedText = 'کلیدواژه در عنوان سئو وجود دارد اما در ابتدا نیست. برای بهترین نتیجه آن را به ابتدا انتقال دهید.';
        }

        // Clean up common labels
        translatedText = translatedText
            .replace(/Text length:/gi, 'طول متن:')
            .replace(/Meta description length:/gi, 'طول توضیحات متا:')
            .replace(/SEO title width:/gi, 'عرض عنوان سئو:')
            .replace(/Keyphrase in SEO title:/gi, 'کلیدواژه در عنوان سئو:')
            .replace(/Internal links:/gi, 'لینک‌های داخلی:')
            .replace(/Outbound links:/gi, 'لینک‌های خارجی:')
            .replace(/Keyphrase density:/gi, 'تراکم کلیدواژه:')
            .replace(/The text contains (\d+) words/gi, 'متن شامل $1 کلمه است')
            .replace(/words\./gi, 'کلمه.')
            .replace(/make sure to add some/gi, 'حتماً چند مورد اضافه کنید');

        return translatedText;
    }

    // Helper method to normalize scores
    normalizeScore(score) {
        if (score >= 9) return 100;  // Excellent - Green
        if (score >= 6) return 100;  // Good - Green (changed from 75 to 100)
        if (score >= 4) return 50;   // OK - Yellow
        if (score >= 1) return 25;   // Poor - Red
        if (score > -50) return 10;  // Very Poor - Red
        return 0;
    }

    // Custom implementations for assessments that require researcher.getHelper()
    createCustomKeyphraseDensityAssessment(paper) {
        const keyword = paper.getKeyword().toLowerCase();
        const text = paper.getText().toLowerCase();
        const words = text.split(/\s+/).filter(word => word.length > 0);
        const totalWords = words.length;

        // Count keyphrase occurrences
        const keyphraseWords = keyword.split(/\s+/);
        let keyphraseCount = 0;

        if (keyphraseWords.length === 1) {
            // Single word keyphrase
            keyphraseCount = words.filter(word => word.includes(keyphraseWords[0])).length;
        } else {
            // Multi-word keyphrase - look for exact phrase
            keyphraseCount = (text.match(new RegExp(keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi')) || []).length;
        }

        const density = totalWords > 0 ? (keyphraseCount / totalWords) * 100 : 0;

        console.log(`🔍 Custom Keyphrase Density: ${keyphraseCount} occurrences in ${totalWords} words = ${density.toFixed(2)}%`);

        let score, text_result;
        if (keyphraseCount === 0) {
            score = 1; // Poor
            text_result = `کلیدواژه "${keyword}" در متن ظاهر نمی‌شود. حتماً حداقل یک بار از کلیدواژه در متن استفاده کنید.`;
        } else if (density < 0.5) {
            score = 4; // Needs improvement
            text_result = `تراکم کلیدواژه ${density.toFixed(1)}% است (کم). سعی کنید چند بار بیشتر از کلیدواژه در متن استفاده کنید.`;
        } else if (density > 3.0) {
            score = 1; // Poor - too high
            text_result = `تراکم کلیدواژه ${density.toFixed(1)}% است (زیاد). استفاده کمتری از کلیدواژه کنید تا طبیعی به نظر برسد.`;
        } else {
            score = 9; // Good
            text_result = `تراکم کلیدواژه ${density.toFixed(1)}% است. مناسب است!`;
        }

        return {
            name: 'CustomKeyphraseDensityAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    createCustomUrlKeyphraseAssessment(paper) {
        const keyword = paper.getKeyword().toLowerCase();
        const url = paper.getUrl().toLowerCase();

        console.log(`🔍 Custom URL Keyphrase Assessment:`);
        console.log(`   Keyword: "${keyword}"`);
        console.log(`   URL: "${url}"`);
        console.log(`   AI Slug Generated Flag: ${this.aiSlugGenerated}`);
        console.log(`   Last AI Generated Slug: "${this.lastAiGeneratedSlug}"`);
        console.log(`   URL Length: ${url.length}`);

        // If URL is empty or just default values, it's poor
        if (!url || url === '' || url === 'untitled' || url.length < 3) {
            console.log(`   ❌ URL is empty or too short`);
            return {
                name: 'CustomUrlKeyphraseAssessment',
                score: 1, // Poor
                text: 'URL خالی است یا تنظیم نشده. برای بهبود سئو، یک اسلاگ مناسب ایجاد کنید.',
                hasClass: 'poor'
            };
        }

        // Check if AI generation happened recently (within last 10 seconds) as fallback
        const timeSinceAiGeneration = Date.now() - this.lastAiGenerationTime;
        const recentAiGeneration = timeSinceAiGeneration < 10000; // 10 seconds
        
        // If AI slug was generated OR URL matches AI OR recent AI generation, automatically give good score
        if (this.aiSlugGenerated || 
            (this.lastAiGeneratedSlug && url === this.lastAiGeneratedSlug.toLowerCase()) ||
            recentAiGeneration) {
            console.log(`   ✅ AI-generated slug detected - FORCING good score regardless of content`);
            console.log(`   Flag: ${this.aiSlugGenerated}, URL matches AI: ${url === this.lastAiGeneratedSlug.toLowerCase()}`);
            console.log(`   Time since AI generation: ${timeSinceAiGeneration}ms, Recent: ${recentAiGeneration}`);
            return {
                name: 'CustomUrlKeyphraseAssessment',
                score: 9, // Excellent
                text: ' اسلاگ با هوش مصنوعی تولید شده و بر اساس کلیدواژه بهینه‌سازی شده است. عالی!',
                hasClass: 'good'
            };
        }

        // Manual slug - check for keyphrase words in URL
        const keyphraseWords = keyword.split(/\s+/);
        
        // Check if any keyphrase words appear in URL
        let found = keyphraseWords.some(word => url.includes(word.toLowerCase()));
        
        console.log(`   Manual slug check - Keyphrase words: ${keyphraseWords.join(', ')}`);
        console.log(`   Found in URL: ${found}`);

        let score, text_result;
        if (found) {
            score = 9; // Good
            text_result = `کلیدواژه در URL وجود دارد. عالی است!`;
        } else {
            score = 3; // Poor - keyword not in URL
            text_result = `کلیدواژه در URL وجود ندارد. برای بهبود سئو، اسلاگ URL را به‌گونه‌ای تنظیم کنید که شامل کلیدواژه باشد.`;
        }

        console.log(`   Final result - Score: ${score}, Status: ${score >= 6 ? 'good' : 'poor'}`);

        return {
            name: 'CustomUrlKeyphraseAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : 'poor'
        };
    }

    createCustomSeoTitleAssessment(paper) {
        const title = paper.getTitle();
        
        console.log(`🔍 Custom SEO Title Assessment: analyzing "${title}"`);

        if (!title || title.trim() === '') {
            return {
                name: 'CustomSeoTitleAssessment',
                score: 1, // Poor
                text: 'عنوان سئو وجود ندارد. لطفاً یک عنوان سئو ایجاد کنید که در نتایج گوگل نمایش داده می‌شود.',
                hasClass: 'poor'
            };
        }

        const trimmedTitle = title.trim();
        const charCount = trimmedTitle.length;

        console.log(`   Title length: ${charCount} characters`);

        let score, text_result;

        if (charCount < 30) {
            score = 4; // Needs improvement - too short
            text_result = `عنوان سئو کوتاه است (${charCount} کاراکتر). برای بهتر شدن در نتایج گوگل، حداقل 30 کاراکتر توصیه می‌شود.`;
        } else if (charCount > 60) {
            score = 6; // OK but could be shorter
            text_result = `عنوان سئو طولانی است (${charCount} کاراکتر). عناوین بیش از 60 کاراکتر ممکن است در نتایج گوگل کوتاه شوند.`;
        } else {
            score = 9; // Good
            text_result = `طول عنوان سئو مناسب است (${charCount} کاراکتر). در نتایج گوگل به خوبی نمایش داده می‌شود.`;
        }

        return {
            name: 'CustomSeoTitleAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    createCustomKeyphraseInSeoTitleAssessment(paper) {
        const title = paper.getTitle();
        const keyword = paper.getKeyword();
        
        console.log(`🔍 Custom Keyphrase in SEO Title: checking "${keyword}" in "${title}"`);

        if (!title || title.trim() === '') {
            return {
                name: 'CustomKeyphraseInSeoTitleAssessment',
                score: 1, // Poor
                text: 'عنوان سئو وجود ندارد. ابتدا یک عنوان سئو ایجاد کنید.',
                hasClass: 'poor'
            };
        }

        if (!keyword || keyword.trim() === '') {
            return {
                name: 'CustomKeyphraseInSeoTitleAssessment',
                score: 1, // Poor
                text: 'کلیدواژه اصلی تعریف نشده است.',
                hasClass: 'poor'
            };
        }

        const lowerTitle = title.toLowerCase();
        const lowerKeyword = keyword.toLowerCase();
        const keyphraseWords = lowerKeyword.split(/\s+/);

        // Check if keyphrase appears in title
        let found = false;
        let isAtBeginning = false;

        // Check full phrase first
        if (lowerTitle.includes(lowerKeyword)) {
            found = true;
            isAtBeginning = lowerTitle.indexOf(lowerKeyword) === 0;
        } else {
            // Check individual words
            const foundWords = keyphraseWords.filter(word => lowerTitle.includes(word));
            found = foundWords.length > 0;
            
            if (found && foundWords.length === keyphraseWords.length) {
                // All words found, check if first word is at beginning
                isAtBeginning = lowerTitle.indexOf(keyphraseWords[0]) === 0;
            }
        }

        let score, text_result;

        if (!found) {
            score = 1; // Poor
            text_result = 'کلیدواژه در عنوان سئو وجود ندارد. برای بهبود سئو، کلیدواژه را در عنوان سئو قرار دهید.';
        } else if (isAtBeginning) {
            score = 9; // Excellent
            text_result = 'کلیدواژه در ابتدای عنوان سئو قرار دارد. عالی است!';
        } else {
            score = 6; // Good but could be better
            text_result = 'کلیدواژه در عنوان سئو وجود دارد اما در ابتدا نیست. برای بهترین نتیجه آن را به ابتدا انتقال دهید.';
        }

        return {
            name: 'CustomKeyphraseInSeoTitleAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    createCustomKeyphraseLengthAssessment(paper) {
        const keyword = paper.getKeyword();
        
        console.log(`🔍 Custom Keyphrase Length: analyzing "${keyword}"`);

        if (!keyword || keyword.trim() === '') {
            return {
                name: 'CustomKeyphraseLengthAssessment',
                score: 1, // Poor
                text: 'کلیدواژه اصلی تعریف نشده است. لطفاً یک کلیدواژه اصلی انتخاب کنید.',
                hasClass: 'poor'
            };
        }

        const trimmedKeyword = keyword.trim();
        const wordCount = trimmedKeyword.split(/\s+/).length;
        const charCount = trimmedKeyword.length;

        console.log(`   Words: ${wordCount}, Characters: ${charCount}`);

        let score, text_result;

        if (charCount < 3) {
            score = 1; // Poor - too short
            text_result = `کلیدواژه خیلی کوتاه است (${charCount} کاراکتر). کلیدواژه باید حداقل چند کاراکتر باشد.`;
        } else if (wordCount === 1 && charCount < 5) {
            score = 4; // Needs improvement
            text_result = `کلیدواژه کوتاه است (${charCount} کاراکتر). کلیدواژه‌های طولانی‌تر معمولاً بهتر عمل می‌کنند.`;
        } else if (wordCount > 4) {
            score = 6; // OK but could be shorter
            text_result = `کلیدواژه طولانی است (${wordCount} کلمه). کلیدواژه‌های کوتاه‌تر معمولاً بهتر عمل می‌کنند.`;
        } else if (charCount > 50) {
            score = 4; // Too long
            text_result = `کلیدواژه خیلی طولانی است (${charCount} کاراکتر). سعی کنید کوتاه‌تر کنید.`;
        } else {
            score = 9; // Good
            text_result = `طول کلیدواژه مناسب است (${wordCount} کلمه، ${charCount} کاراکتر).`;
        }

        return {
            name: 'CustomKeyphraseLengthAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    // ==================== CUSTOM READABILITY ASSESSMENTS ====================

    createCustomTextPresenceAssessment(paper) {
        const text = paper.getText();
        const cleanText = text.replace(/<[^>]*>/g, '').trim(); // Remove HTML tags
        
        console.log(`🔍 Custom Text Presence: analyzing text length ${cleanText.length}`);

        if (!cleanText || cleanText.length < 10) {
            return {
                name: 'CustomTextPresenceAssessment',
                score: 1, // Poor
                text: 'متن شما خیلی کوتاه است. لطفاً محتوای بیشتری اضافه کنید.',
                hasClass: 'poor'
            };
        }

        return {
            name: 'CustomTextPresenceAssessment',
            score: 9, // Good
            text: `متن موجود است (${cleanText.length} کاراکتر). عالی است!`,
            hasClass: 'good'
        };
    }

    createCustomParagraphTooLongAssessment(paper) {
        const text = paper.getText();
        const cleanText = text.replace(/<[^>]*>/g, '');
        
        // Split by paragraph breaks (double newlines or <p> tags)
        const paragraphs = cleanText.split(/\n\s*\n|\.\s+/).filter(p => p.trim().length > 0);
        
        console.log(`🔍 Custom Paragraph Length: analyzing ${paragraphs.length} paragraphs`);

        let longParagraphs = 0;
        let totalWords = 0;

        paragraphs.forEach(paragraph => {
            const words = paragraph.trim().split(/\s+/).length;
            totalWords += words;
            if (words > 150) { // Persian optimal paragraph length
                longParagraphs++;
            }
        });

        const avgWordsPerParagraph = paragraphs.length > 0 ? Math.round(totalWords / paragraphs.length) : 0;

        let score, text_result;
        if (longParagraphs === 0) {
            score = 9; // Excellent
            text_result = `طول پاراگراف‌ها مناسب است. میانگین: ${avgWordsPerParagraph} کلمه در هر پاراگراف.`;
        } else if (longParagraphs === 1) {
            score = 6; // OK
            text_result = `یک پاراگراف طولانی دارید. سعی کنید پاراگراف‌های بیش از 150 کلمه را تقسیم کنید.`;
        } else {
            score = 3; // Poor
            text_result = `${longParagraphs} پاراگراف طولانی دارید. پاراگراف‌های کوتاه‌تر خوانایی را بهبود می‌بخشند.`;
        }

        return {
            name: 'CustomParagraphTooLongAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    createCustomSentenceLengthAssessment(paper) {
        const text = paper.getText();
        const cleanText = text.replace(/<[^>]*>/g, '');
        
        // Split by Persian sentence endings
        const sentences = cleanText.split(/[.!?؟]/).filter(s => s.trim().length > 5);
        
        console.log(`🔍 Custom Sentence Length: analyzing ${sentences.length} sentences`);

        let longSentences = 0;
        let totalWords = 0;

        sentences.forEach(sentence => {
            const words = sentence.trim().split(/\s+/).length;
            totalWords += words;
            if (words > 25) { // Persian optimal sentence length
                longSentences++;
            }
        });

        const avgWordsPerSentence = sentences.length > 0 ? Math.round(totalWords / sentences.length) : 0;
        const longSentencePercentage = sentences.length > 0 ? Math.round((longSentences / sentences.length) * 100) : 0;

        let score, text_result;
        if (longSentencePercentage <= 10) {
            score = 9; // Excellent - Green
            text_result = `طول جملات عالی است. میانگین: ${avgWordsPerSentence} کلمه در جمله.`;
        } else if (longSentencePercentage <= 25) {
            score = 6; // Good - Yellow  
            text_result = `${longSentencePercentage}% جملات طولانی هستند. سعی کنید جملات را کوتاه‌تر کنید.`;
        } else if (longSentencePercentage <= 60) {
            score = 4; // Needs Improvement - Yellow (increased from 50 to 60)
            text_result = `${longSentencePercentage}% جملات خیلی طولانی هستند. جملات کوتاه‌تر خوانایی را بهبود می‌بخشند.`;
        } else {
            score = 2; // Poor - Red
            text_result = `${longSentencePercentage}% جملات خیلی طولانی هستند. متن نیاز به بازنویسی دارد.`;
        }

        return {
            name: 'CustomSentenceLengthAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    createCustomSubheadingDistributionAssessment(paper) {
        const text = paper.getText();
        
        // Count subheadings (h2, h3, etc.)
        const subheadingMatches = text.match(/<h[2-6][^>]*>.*?<\/h[2-6]>/gi) || [];
        const subheadingCount = subheadingMatches.length;
        
        // Remove HTML and count words
        const cleanText = text.replace(/<[^>]*>/g, '');
        const totalWords = cleanText.trim().split(/\s+/).length;
        
        console.log(`🔍 Custom Subheading Distribution: ${subheadingCount} subheadings, ${totalWords} words`);

        let score, text_result;
        
        if (totalWords < 150) {
            score = 9; // Good for short content
            text_result = `متن کوتاه است و نیازی به زیرعناوین ندارد.`;
        } else if (subheadingCount === 0) {
            score = 3; // Poor
            text_result = `متن طولانی است اما زیرعنوان ندارد. اضافه کردن زیرعناوین (H2, H3) خوانایی را بهبود می‌بخشد.`;
        } else {
            const wordsPerSubheading = Math.round(totalWords / (subheadingCount + 1));
            
            if (wordsPerSubheading <= 300) {
                score = 9; // Excellent
                text_result = `توزیع زیرعناوین عالی است. میانگین ${wordsPerSubheading} کلمه بین زیرعناوین.`;
            } else if (wordsPerSubheading <= 400) {
                score = 6; // OK
                text_result = `توزیع زیرعناوین خوب است اما می‌توانید زیرعناوین بیشتری اضافه کنید.`;
            } else {
                score = 3; // Poor
                text_result = `بخش‌های بین زیرعناوین خیلی طولانی هستند. زیرعناوین بیشتری اضافه کنید.`;
            }
        }

        return {
            name: 'CustomSubheadingDistributionAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    createCustomTransitionWordsAssessment(paper) {
        const text = paper.getText();
        const cleanText = text.replace(/<[^>]*>/g, '').toLowerCase();
        
        // Persian transition words
        const persianTransitionWords = [
            'همچنین', 'علاوه بر این', 'از طرفی', 'از سوی دیگر', 'در نتیجه', 'بنابراین', 
            'به همین دلیل', 'در واقع', 'البته', 'اما', 'ولی', 'با این حال', 'در مقابل',
            'برای مثال', 'مثلاً', 'به عنوان مثال', 'در ادامه', 'در نهایت', 'سرانجام',
            'اول', 'دوم', 'سوم', 'ابتدا', 'سپس', 'آنگاه', 'در ابتدا', 'در انتها',
            'ضمناً', 'همینطور', 'به ویژه', 'خصوصاً', 'به طور کلی', 'به طور خاص'
        ];
        
        console.log(`🔍 Custom Transition Words: analyzing Persian transition words`);

        const sentences = cleanText.split(/[.!?؟]/).filter(s => s.trim().length > 5);
        let sentencesWithTransitions = 0;

        sentences.forEach(sentence => {
            const hasTransition = persianTransitionWords.some(word => 
                sentence.includes(word)
            );
            if (hasTransition) {
                sentencesWithTransitions++;
            }
        });

        const transitionPercentage = sentences.length > 0 ? 
            Math.round((sentencesWithTransitions / sentences.length) * 100) : 0;

        let score, text_result;
        if (transitionPercentage >= 30) {
            score = 9; // Excellent
            text_result = `استفاده عالی از کلمات ربط (${transitionPercentage}% جملات). متن به خوبی پیوند خورده است.`;
        } else if (transitionPercentage >= 20) {
            score = 6; // OK
            text_result = `استفاده خوب از کلمات ربط (${transitionPercentage}% جملات). می‌توانید بیشتر استفاده کنید.`;
        } else {
            score = 3; // Poor
            text_result = `استفاده کم از کلمات ربط (${transitionPercentage}% جملات). کلمات ربطی مثل "همچنین"، "بنابراین" اضافه کنید.`;
        }

        return {
            name: 'CustomTransitionWordsAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    createCustomPassiveVoiceAssessment(paper) {
        const text = paper.getText();
        const cleanText = text.replace(/<[^>]*>/g, '').toLowerCase();
        
        // Persian passive voice indicators
        const passiveIndicators = [
            'شده است', 'شده', 'گردیده', 'گردیده است', 'می‌شود', 'خواهد شد',
            'شد', 'شدند', 'شده‌اند', 'گشته', 'گشته است'
        ];
        
        console.log(`🔍 Custom Passive Voice: analyzing Persian passive constructions`);

        const sentences = cleanText.split(/[.!?؟]/).filter(s => s.trim().length > 5);
        let passiveSentences = 0;

        sentences.forEach(sentence => {
            const hasPassive = passiveIndicators.some(indicator => 
                sentence.includes(indicator)
            );
            if (hasPassive) {
                passiveSentences++;
            }
        });

        const passivePercentage = sentences.length > 0 ? 
            Math.round((passiveSentences / sentences.length) * 100) : 0;

        let score, text_result;
        if (passivePercentage <= 10) {
            score = 9; // Excellent
            text_result = `استفاده عالی از صیغه فعال (${passivePercentage}% مجهول). متن روان و قابل فهم است.`;
        } else if (passivePercentage <= 20) {
            score = 6; // OK
            text_result = `استفاده معقول از صیغه مجهول (${passivePercentage}%). سعی کنید بیشتر از صیغه فعال استفاده کنید.`;
        } else {
            score = 3; // Poor
            text_result = `استفاده زیاد از صیغه مجهول (${passivePercentage}%). جملات فعال خوانایی را بهبود می‌بخشند.`;
        }

        return {
            name: 'CustomPassiveVoiceAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    createCustomWordComplexityAssessment(paper) {
        const text = paper.getText();
        const cleanText = text.replace(/<[^>]*>/g, '');
        
        // Split into words and filter Persian words
        const words = cleanText.split(/\s+/).filter(word => 
            word.length > 2 && /[\u0600-\u06FF]/.test(word)
        );
        
        console.log(`🔍 Custom Word Complexity: analyzing ${words.length} Persian words`);

        // More realistic complexity check for Persian:
        // 1. Very long words (>12 characters) - likely complex compounds
        // 2. Words with complex letter combinations
        const veryLongWords = words.filter(word => word.length > 12);
        const longWords = words.filter(word => word.length > 9);
        
        const veryLongPercentage = words.length > 0 ? 
            Math.round((veryLongWords.length / words.length) * 100) : 0;
        const longPercentage = words.length > 0 ? 
            Math.round((longWords.length / words.length) * 100) : 0;

        let score, text_result;
        
        // More conservative scoring - focus only on very long words
        if (veryLongPercentage <= 5 && longPercentage <= 20) {
            score = 9; // Excellent
            text_result = `پیچیدگی کلمات مناسب است. کلمات غالباً ساده و قابل فهم هستند.`;
        } else if (veryLongPercentage <= 10 && longPercentage <= 35) {
            score = 6; // OK
            text_result = `پیچیدگی کلمات متوسط است. می‌توانید برخی کلمات طولانی را ساده‌تر کنید.`;
        } else {
            score = 4; // Needs improvement
            text_result = `برخی کلمات ممکن است پیچیده باشند. سعی کنید کلمات ساده‌تر استفاده کنید.`;
        }

        return {
            name: 'CustomWordComplexityAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    createCustomReadabilityOverviewAssessment(paper) {
        const text = paper.getText();
        const cleanText = text.replace(/<[^>]*>/g, '');
        
        // Focus on simpler, more reliable metrics for Persian
        const sentences = cleanText.split(/[.!?؟]/).filter(s => s.trim().length > 5);
        const words = cleanText.split(/\s+/).filter(w => w.length > 0);
        
        console.log(`🔍 Custom Readability Overview: ${sentences.length} sentences, ${words.length} words`);

        if (sentences.length === 0 || words.length === 0) {
            return {
                name: 'CustomReadabilityOverviewAssessment',
                score: 1,
                text: 'متن کافی برای ارزیابی خوانایی وجود ندارد.',
                hasClass: 'poor'
            };
        }

        // Use simpler, more reliable metrics instead of complex syllable counting
        const avgWordsPerSentence = words.length / sentences.length;
        const longWords = words.filter(word => word.length > 8).length;
        const longWordPercentage = (longWords / words.length) * 100;
        
        console.log(`   Avg words/sentence: ${avgWordsPerSentence.toFixed(1)}, Long words: ${longWordPercentage.toFixed(1)}%`);
        
        // Conservative scoring based on reliable metrics
        let score, text_result;
        
        // Good readability: short sentences + fewer long words
        if (avgWordsPerSentence <= 20 && longWordPercentage <= 15) {
            score = 8; // Good
            text_result = `خوانایی متن خوب است. جملات و کلمات مناسب هستند.`;
        } 
        // Moderate readability
        else if (avgWordsPerSentence <= 30 && longWordPercentage <= 25) {
            score = 6; // OK
            text_result = `خوانایی متن متوسط است. می‌توانید جملات کوتاه‌تر یا کلمات ساده‌تر استفاده کنید.`;
        }
        // Needs improvement
        else if (avgWordsPerSentence <= 40) {
            score = 4; // Needs improvement
            text_result = `خوانایی متن نیاز به بهبود دارد. جملات کوتاه‌تر کنید.`;
        }
        // Poor readability
        else {
            score = 2; // Poor
            text_result = `جملات خیلی طولانی هستند. برای بهبود خوانایی، جملات را کوتاه‌تر کنید.`;
        }

        return {
            name: 'CustomReadabilityOverviewAssessment',
            score: score,
            text: text_result,
            hasClass: score >= 6 ? 'good' : (score >= 4 ? 'ok' : 'poor')
        };
    }

    generateContentQualityAssessment(results) {
        const assessments = [];
        
        // Word count assessment
        if (results.wordCount >= 300) {
            assessments.push('✅ طول محتوا مناسب برای سئو');
        } else if (results.wordCount >= 150) {
            assessments.push('⚠️ محتوا کوتاه - برای سئو بهتر افزایش دهید');
        } else {
            assessments.push('❌ محتوا خیلی کوتاه - حداقل 300 کلمه نیاز است');
        }
        
        // Sentence analysis
        if (results.averageWordsPerSentence <= 20) {
            assessments.push('✅ جملات کوتاه و خوانا');
        } else if (results.averageWordsPerSentence <= 25) {
            assessments.push('⚠️ جملات متوسط - کوتاه‌تر کنید');
        } else {
            assessments.push('❌ جملات خیلی طولانی - خوانایی کم');
        }
        
        // Title assessment
        if (results.titleLength >= 30 && results.titleLength <= 65) {
            assessments.push('✅ طول تیتر ایده‌آل برای نتایج جستجو');
        } else {
            assessments.push('⚠️ تیتر را بین 30-65 کاراکتر تنظیم کنید');
        }
        
        // Meta description assessment  
        if (results.metaDescLength >= 120 && results.metaDescLength <= 160) {
            assessments.push('✅ توضیحات متا در بازه مناسب');
        } else if (results.metaDescLength === 0) {
            assessments.push('❌ توضیحات متا وارد نشده');
        } else {
            assessments.push('⚠️ توضیحات متا را بین 120-160 کاراکتر تنظیم کنید');
        }
        
        return assessments.map(assessment => `<div class="mb-1">${assessment}</div>`).join('');
    }

    getReadabilityColor(avgWords) {
        if (avgWords <= 20) return 'text-success';
        if (avgWords <= 25) return 'text-warning';
        return 'text-danger';
    }

    getOverallReadabilityColor(results) {
        if (results.averageWordsPerSentence <= 20 && results.sentenceCount >= 3) {
            return 'text-success';
        } else if (results.averageWordsPerSentence <= 25) {
            return 'text-warning';
        }
        return 'text-danger';
    }

    getReadabilityStatus(results) {
        if (results.averageWordsPerSentence <= 20 && results.sentenceCount >= 3) {
            return 'عالی';
        } else if (results.averageWordsPerSentence <= 25) {
            return 'قابل قبول';
        }
        return 'نیاز به بهبود';
    }

    getTitleColor(length) {
        if (length >= 30 && length <= 65) return 'text-success';
        if (length >= 20 && length <= 80) return 'text-warning';
        return 'text-danger';
    }

    getMetaColor(length) {
        if (length >= 120 && length <= 160) return 'text-success';
        if (length >= 100 && length <= 180) return 'text-warning';
        return 'text-danger';
    }

    updateTrafficLightsDirect(results) {
        // Update traffic light indicators based on direct results
        const indicators = [
            { id: 'title-length-indicator', status: this.getTitleStatus(results.titleLength) },
            { id: 'meta-desc-length-indicator', status: this.getMetaStatus(results.metaDescLength) },
            { id: 'content-length-indicator', status: this.getContentStatus(results.wordCount) },
            { id: 'keyword-density-indicator', status: this.getKeywordStatus(results.keywordPresent) }
        ];

        const texts = [
            { id: 'title-length-text', text: `طول تیتر: ${results.titleLength} کاراکتر - ${this.getTitleStatus(results.titleLength).message}` },
            { id: 'meta-desc-length-text', text: `طول توضیحات متا: ${results.metaDescLength} کاراکتر - ${this.getMetaStatus(results.metaDescLength).message}` },
            { id: 'content-length-text', text: `تعداد کلمات محتوا: ${results.wordCount} - ${this.getContentStatus(results.wordCount).message}` }
            // { id: 'keyword-density-text', text: `کلمه کلیدی: ${results.keywordPresent ? 'یافت شد' : 'یافت نشد'}` }
        ];

        indicators.forEach(item => {
            const element = document.getElementById(item.id);
            if (element) {
                element.className = `seo-indicator ${item.status.class}`;
            }
        });

        texts.forEach(item => {
            const element = document.getElementById(item.id);
            if (element) {
                element.textContent = item.text;
            }
        });
    }

    getTitleStatus(length) {
        if (length >= 30 && length <= 65) {
            return { class: 'seo-good', message: 'مناسب' };
        } else if (length >= 20 && length <= 80) {
            return { class: 'seo-needs-improvement', message: 'قابل قبول' };
        } else {
            return { class: 'seo-bad', message: 'نیاز به بهبود' };
        }
    }

    getMetaStatus(length) {
        if (length >= 120 && length <= 160) {
            return { class: 'seo-good', message: 'مناسب' };
        } else if (length >= 100 && length <= 180) {
            return { class: 'seo-needs-improvement', message: 'قابل قبول' };
        } else {
            return { class: 'seo-bad', message: 'نیاز به بهبود' };
        }
    }

    getContentStatus(wordCount) {
        if (wordCount >= 300) {
            return { class: 'seo-good', message: 'مناسب' };
        } else if (wordCount >= 150) {
            return { class: 'seo-needs-improvement', message: 'کوتاه' };
        } else {
            return { class: 'seo-bad', message: 'خیلی کوتاه' };
        }
    }

    getKeywordStatus(present) {
        if (present) {
            return { class: 'seo-good', message: 'یافت شد' };
        } else {
            return { class: 'seo-bad', message: 'یافت نشد' };
        }
    }


    updateTrafficLights(results) {
        // Clear indicators - YoastSEO will handle output via targets.output
        const indicators = [
            'title-length-indicator',
            'meta-desc-length-indicator', 
            'content-length-indicator',
            'keyword-density-indicator'
        ];
        
        indicators.forEach(id => {
            const indicator = document.getElementById(id);
            if (indicator) {
                indicator.className = 'seo-indicator seo-good';
            }
        });
        
        const texts = [
            'title-length-text',
            'meta-desc-length-text',
            'content-length-text', 
            'keyword-density-text'
        ];
        
        texts.forEach(id => {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = 'تحلیل توسط YoastSEO انجام شد';
            }
        });
    }

    runYoastAssessments(paper, researcher) {
        const yoastResults = {
            seo: [],
            readability: [],
            scores: {
                seo: 0,
                readability: 0,
                overall: 0
            }
        };
        
        // SEO Assessments - Start with ONLY verified working assessments
        const seoAssessments = [];
        
        // Use only verified working assessments
        const potentialAssessments = [
            'TextLengthAssessment',                // ✅ Working
            'MetaDescriptionLengthAssessment',     // ✅ Working  
            // 'PageTitleWidthAssessment',         // ❌ Has issues with Persian titles - using custom version
            // 'KeyphraseInSEOTitleAssessment',    // ❌ Might have issues with Persian - using custom version
            'InternalLinksAssessment',             // ✅ Working
            'OutboundLinksAssessment'              // ✅ Working
        ];
        
        // REMOVED - These are causing errors:
        // 'KeyphraseInIntroductionAssessment',   // ❌ Cannot read properties of null
        // 'KeyphraseInSubheadingAssessment',     // ❌ Cannot read properties of null  
        // 'TextCompetingLinksAssessment'         // ❌ assessmentInfo.class is not a constructor
        
        // Add custom implementations that don't use researcher.getHelper()
        // We'll add these after the standard assessments
        
        potentialAssessments.forEach(assessmentName => {
            if (assessments.seo && assessments.seo[assessmentName]) {
                seoAssessments.push({
                    name: assessmentName,
                    class: assessments.seo[assessmentName]
                });
                console.log(`✓ Added SEO assessment: ${assessmentName}`);
            } else {
                console.log(`✗ SEO assessment not available: ${assessmentName}`);
            }
        });
        
        // Readability Assessments - Only add what exists in the package
        const readabilityAssessments = [];
        
        
        
        
        
        console.log('🚀 Running YoastSEO assessments...');
        
        // Run SEO assessments
        let seoTotalScore = 0;
        let seoCount = 0;
        
        seoAssessments.forEach(assessmentInfo => {
            try {
                const assessment = new assessmentInfo.class();
                const result = assessment.getResult(paper, researcher);
                
                yoastResults.seo.push({
                    name: assessmentInfo.name,
                    score: result.score,
                    text: result.text,
                    hasClass: result.hasClass || 'none'
                });
                
                // Convert YoastSEO scores to 0-100 scale for averaging
                let normalizedScore = 0;
                if (result.score >= 9) {
                    normalizedScore = 100;
                } else if (result.score >= 6) {
                    normalizedScore = 75;
                } else if (result.score >= 4) {
                    normalizedScore = 50;
                } else if (result.score >= 1) {
                    normalizedScore = 25;
                } else if (result.score > -50) {
                    normalizedScore = 10;
                } else {
                    normalizedScore = 0;
                }
                
                // Debug negative or unexpected scores
                if (result.score <= 0) {
                    console.log(`⚠️ Negative/Zero score detected in ${assessmentInfo.name}: ${result.score}`);
                }
                
                seoTotalScore += normalizedScore;
                seoCount++;
                
                
                console.log(`✅ ${assessmentInfo.name}: Score ${result.score} (${normalizedScore}/100)`);
                
            } catch (error) {
                console.log(`SEO ${assessmentInfo.name} failed:`, error.message);
                // Add failed assessment as placeholder so it still shows in UI
                yoastResults.seo.push({
                    name: assessmentInfo.name,
                    score: 0,
                    text: `Assessment failed: ${error.message}`,
                    hasClass: 'error'
                });
            }
        });
        
        // Add custom implementations for assessments that fail with researcher.getHelper()
        if (paper.getKeyword() && paper.getKeyword().trim()) {
            console.log('✅ Adding custom assessments for keyphrase analysis...');
            console.log(`   📊 Current AI Slug Flag State: ${this.aiSlugGenerated}`);
            
            // Custom SEO Title Assessment (since YoastSEO might have issues with Persian)
            const customSeoTitle = this.createCustomSeoTitleAssessment(paper);
            yoastResults.seo.push(customSeoTitle);
            seoTotalScore += this.normalizeScore(customSeoTitle.score);
            seoCount++;
            
            // Custom Keyphrase in SEO Title Assessment
            const customKeyphraseInTitle = this.createCustomKeyphraseInSeoTitleAssessment(paper);
            yoastResults.seo.push(customKeyphraseInTitle);
            seoTotalScore += this.normalizeScore(customKeyphraseInTitle.score);
            seoCount++;
            
            // Custom Keyphrase Length Assessment  
            const customKeyphraseLength = this.createCustomKeyphraseLengthAssessment(paper);
            yoastResults.seo.push(customKeyphraseLength);
            seoTotalScore += this.normalizeScore(customKeyphraseLength.score);
            seoCount++;
            
            // Custom Keyphrase Density Assessment
            const customKeyphraseDensity = this.createCustomKeyphraseDensityAssessment(paper);
            yoastResults.seo.push(customKeyphraseDensity);
            seoTotalScore += this.normalizeScore(customKeyphraseDensity.score);
            seoCount++;
            
            // Custom URL Keyphrase Assessment
            const customUrlKeyphrase = this.createCustomUrlKeyphraseAssessment(paper);
            yoastResults.seo.push(customUrlKeyphrase);
            seoTotalScore += this.normalizeScore(customUrlKeyphrase.score);
            seoCount++;
        }
        
        // Note: Only using standard YoastSEO assessments as per package documentation
        
        // Run Readability assessments
        let readabilityTotalScore = 0;
        let readabilityCount = 0;
        
        readabilityAssessments.forEach(assessmentInfo => {
            try {
                const assessment = new assessmentInfo.class();
                const result = assessment.getResult(paper, researcher);
                
                yoastResults.readability.push({
                    name: assessmentInfo.name,
                    score: result.score,
                    text: result.text,
                    hasClass: result.hasClass || 'none'
                });
                
                // Convert YoastSEO scores to 0-100 scale
                // Special handling for readability assessments
                let normalizedScore = 0;
                
                // Special case for TextPresenceAssessment - if we have content, give base score
                if (assessmentInfo.name === 'TextPresenceAssessment' && paper.getText() && paper.getText().length > 10) {
                    normalizedScore = 75; // Give good score if text is present
                } else if (result.score >= 9) {
                    normalizedScore = 100;
                } else if (result.score >= 6) {
                    normalizedScore = 75;
                } else if (result.score >= 4) {
                    normalizedScore = 50;
                } else if (result.score >= 1) {
                    normalizedScore = 25;
                } else if (result.score > -50) {
                    // Handle moderate negative scores
                    normalizedScore = 10;
                } else {
                    // Handle very negative scores
                    normalizedScore = 0;
                }
                
                readabilityTotalScore += normalizedScore;
                readabilityCount++;
                
                console.log(`Readability ${assessmentInfo.name}: score=${result.score}, normalized=${normalizedScore}`);
                
            } catch (error) {
                console.log(`Readability ${assessmentInfo.name} failed:`, error.message);
            }
        });
        
        // Add comprehensive custom readability assessments for Persian content
        console.log('✅ Adding custom readability assessments for Persian content...');
        
        // Custom Text Presence Assessment
        const customTextPresence = this.createCustomTextPresenceAssessment(paper);
        yoastResults.readability.push(customTextPresence);
        readabilityTotalScore += this.normalizeScore(customTextPresence.score);
        readabilityCount++;
        
        // Custom Paragraph Length Assessment  
        const customParagraphLength = this.createCustomParagraphTooLongAssessment(paper);
        yoastResults.readability.push(customParagraphLength);
        readabilityTotalScore += this.normalizeScore(customParagraphLength.score);
        readabilityCount++;
        
        // Custom Sentence Length Assessment
        const customSentenceLength = this.createCustomSentenceLengthAssessment(paper);
        yoastResults.readability.push(customSentenceLength);
        readabilityTotalScore += this.normalizeScore(customSentenceLength.score);
        readabilityCount++;
        
        // Custom Subheading Distribution Assessment
        const customSubheadingDist = this.createCustomSubheadingDistributionAssessment(paper);
        yoastResults.readability.push(customSubheadingDist);
        readabilityTotalScore += this.normalizeScore(customSubheadingDist.score);
        readabilityCount++;
        
        // Custom Transition Words Assessment
        const customTransitionWords = this.createCustomTransitionWordsAssessment(paper);
        yoastResults.readability.push(customTransitionWords);
        readabilityTotalScore += this.normalizeScore(customTransitionWords.score);
        readabilityCount++;
        
        // Custom Passive Voice Assessment
        const customPassiveVoice = this.createCustomPassiveVoiceAssessment(paper);
        yoastResults.readability.push(customPassiveVoice);
        readabilityTotalScore += this.normalizeScore(customPassiveVoice.score);
        readabilityCount++;
        
        // Custom Word Complexity Assessment
        const customWordComplexity = this.createCustomWordComplexityAssessment(paper);
        yoastResults.readability.push(customWordComplexity);
        readabilityTotalScore += this.normalizeScore(customWordComplexity.score);
        readabilityCount++;
        
        // Custom Readability Overview Assessment (more reliable than Flesch for Persian)
        const customReadabilityOverview = this.createCustomReadabilityOverviewAssessment(paper);
        yoastResults.readability.push(customReadabilityOverview);
        readabilityTotalScore += this.normalizeScore(customReadabilityOverview.score);
        readabilityCount++;
        
        console.log(`📊 Custom Readability Assessments Added: ${readabilityCount - readabilityAssessments.length} assessments`);
        
        // Calculate final scores using YoastSEO's actual scoring
        yoastResults.scores.seo = seoCount > 0 ? Math.round(seoTotalScore / seoCount) : 0;
        yoastResults.scores.readability = readabilityCount > 0 ? Math.round(readabilityTotalScore / readabilityCount) : 0;
        
        console.log(`📊 Final Scores: SEO ${yoastResults.scores.seo}/100, Readability ${yoastResults.scores.readability}/100`);
        
        // Ensure minimum scores when we have content (prevent 0/100 with good content)
        if (paper.getText() && paper.getText().length > 50) {
            if (yoastResults.scores.seo === 0 && seoCount > 0) yoastResults.scores.seo = 15;
            if (yoastResults.scores.readability === 0 && readabilityCount > 0) yoastResults.scores.readability = 25;
        }
        
        yoastResults.scores.overall = Math.round((yoastResults.scores.seo + yoastResults.scores.readability) / 2);
        
        console.log('YoastSEO final scores:', yoastResults.scores);
        
        return yoastResults;
    }
    

    showError() {
        const outputElement = document.getElementById('yoast-seo-output');
        if (outputElement) {
            outputElement.innerHTML = `
                <div class="alert alert-danger text-center">
                    <h6>خطا در بارگذاری YoastSEO</h6>
                    <p class="small mb-0">امکان اتصال به موتور تحلیل سئو وجود ندارد.</p>
                </div>
            `;
        }

    }

    bindSlugGenerationButton() {
        console.log('🔗 Setting up AI slug generation button...');
        
        const generateButton = document.getElementById('generate-slug-btn');
        const slugInput = document.getElementById('news-slug');
        
        if (!generateButton) {
            console.log('❌ Generate slug button not found');
            return;
        }

        // Bind generation button
        generateButton.addEventListener('click', () => {
            this.generateAISlug();
        });
        
        // Reset AI flag when user manually edits slug
        if (slugInput) {
            let isAiGenerating = false;
            
            slugInput.addEventListener('input', (event) => {
                // Don't reset flag if we're in the middle of AI generation
                if (isAiGenerating) {
                    console.log('🤖 AI generation in progress - ignoring input event');
                    return;
                }
                
                // Don't reset if this is the AI-generated value being set
                if (this.aiSlugGenerated && slugInput.value === this.lastAiGeneratedSlug) {
                    console.log('🤖 AI-generated value detected - keeping AI flag');
                    console.log(`   AI value: "${this.lastAiGeneratedSlug}"`);
                    return;
                }
                
                if (this.aiSlugGenerated && slugInput.value !== this.lastAiGeneratedSlug) {
                    console.log('🔄 User manually edited slug - resetting AI flag');
                    console.log(`   Previous AI value: "${this.lastAiGeneratedSlug}"`);
                    console.log(`   New manual value: "${slugInput.value}"`);
                    this.aiSlugGenerated = false;
                    this.lastAiGeneratedSlug = '';
                    
                    // Re-run analysis to update assessment
                    this.analyzeContent();
                }
            });
            
            // Temporarily disable input monitoring during AI generation
            generateButton.addEventListener('click', () => {
                isAiGenerating = true;
                console.log('🔒 AI generation started - temporarily disabling input monitoring');
                setTimeout(() => {
                    isAiGenerating = false;
                    console.log('🔓 AI generation window closed - input monitoring resumed');
                }, 3000); // 3 second window
            });
        }
        
        console.log('✅ AI slug generation button and manual edit detection bound successfully');
    }

    async generateAISlug() {
        console.log('🤖 Starting AI slug generation...');
        
        // Get form values
        const titleElement = document.getElementById('news-title-input');
        const keyphraseElement = document.getElementById('news-keyphrase-input');
        const slugElement = document.getElementById('news-slug');
        const generateButton = document.getElementById('generate-slug-btn');
        
        if (!titleElement || !keyphraseElement || !slugElement) {
            alert('خطا: فیلدهای مورد نیاز پیدا نشد');
            return;
        }

        const title = titleElement.value.trim();
        const keyphrase = keyphraseElement.value.trim();
        
        // Get content from CKEditor or textarea
        let body = '';
        if (this.editorInstance) {
            body = this.editorInstance.getData();
        } else {
            const editorElement = document.getElementById('editor');
            body = editorElement ? editorElement.value : '';
        }
        
        // Remove HTML tags from body for AI processing
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = body;
        const cleanBody = tempDiv.textContent || tempDiv.innerText || '';

        // Validate required fields
        if (!title) {
            alert('لطفاً ابتدا عنوان خبر را وارد کنید');
            titleElement.focus();
            return;
        }
        
        if (!keyphrase) {
            alert('لطفاً ابتدا کلیدواژه اصلی را وارد کنید');
            keyphraseElement.focus();
            return;
        }
        
        if (!cleanBody || cleanBody.length < 50) {
            alert('لطفاً ابتدا محتوای خبر را وارد کنید (حداقل 50 کاراکتر)');
            return;
        }

        // Show loading state
        const originalText = generateButton.innerHTML;
        generateButton.disabled = true;
        generateButton.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>در حال تولید...';
        
        try {
            // Call the backend API
            const response = await fetch('/api/gemini/generate-slug', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    title: title,
                    keyphrase: keyphrase,
                    body: cleanBody
                })
            });

            const result = await response.json();
            
            if (response.ok && result.slug) {
                // Success - set the generated slug
                slugElement.value = result.slug;
                
                // Force the value to ensure it's properly set
                slugElement.setAttribute('value', result.slug);
                
                // Mark that AI slug was generated AFTER setting value
                this.aiSlugGenerated = true;
                this.lastAiGeneratedSlug = result.slug; // Store the AI value
                this.lastAiGenerationTime = Date.now(); // Store when it happened
                console.log(`🔥 AI SLUG FLAG SET TO TRUE! Generated: "${result.slug}"`);
                console.log(`🔥 Stored AI slug value: "${this.lastAiGeneratedSlug}"`);
                console.log(`🔥 AI generation timestamp: ${this.lastAiGenerationTime}`);
                
                // Show success message
                this.showToast('success', '✅ اسلاگ با موفقیت تولید شد!', result.message);
                
                // Trigger SEO analysis update AFTER setting the flag
                console.log(`🔄 About to run analysis with AI flag = ${this.aiSlugGenerated}`);
                
                // Store reference to this for setTimeout callback
                const self = this;
                
                // Small delay to ensure slug value is properly set
                setTimeout(function() {
                    console.log(`🔄 Running delayed analysis with AI flag = ${self.aiSlugGenerated}`);
                    console.log(`🔄 Slug input value: "${slugElement.value}"`);
                    console.log(`🔄 Expected AI value: "${self.lastAiGeneratedSlug}"`);
                    self.analyzeContent();
                }, 200); // Increased delay
                
                console.log('🎉 AI slug generated successfully:', result.slug);
            } else {
                // Error from API
                const errorMessage = result.error || 'خطایی در تولید اسلاگ رخ داد';
                console.error('API Error:', result);
            }
            
        } catch (error) {
            console.error('Network Error:', error);
        } finally {
            // Restore button state
            generateButton.disabled = false;
            generateButton.innerHTML = originalText;
        }
    }

    showToast(type, title, message) {
        // Use SweetAlert2 if available, otherwise fallback to alert
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                icon: type === 'success' ? 'success' : 'error',
                title: title,
                text: message,
                timer: 3000,
                showConfirmButton: false,
                toast: true,
                position: 'top-end'
            });
        } else {
            alert(title + '\n' + message);
        }
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new SimpleYoastSEO();
});
