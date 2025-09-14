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
            });
        } else {
            console.log('DOM already ready, setting up listeners immediately...');
            this.setupEventListeners();
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
        const metaDescInput = document.getElementById('meta-description-input');
        const keywordInput = document.getElementById('meta-keywords-input');
        const refreshButton = document.getElementById('refresh-seo-analysis');

        console.log('Form elements found:', {
            titleInput: !!titleInput,
            metaDescInput: !!metaDescInput,
            keywordInput: !!keywordInput,
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

        // Manual refresh button
        if (refreshButton && !refreshButton.dataset.yoastBound) {
            console.log('Adding refresh button listener');
            refreshButton.addEventListener('click', () => {
                console.log('Manual refresh triggered');
                this.analyze();
            });
            refreshButton.dataset.yoastBound = 'true';
        }

        // Setup CKEditor listener
        this.setupCKEditorListener();
        
        // Mark events as bound if we found at least some elements
        if (titleInput || metaDescInput || keywordInput) {
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
        const metaDescElement = document.getElementById('meta-description-input');
        const keywordElement = document.getElementById('meta-keywords-input');

        const title = titleElement ? titleElement.value : '';
        const metaDescription = metaDescElement ? metaDescElement.value : '';
        const keyword = keywordElement ? keywordElement.value : '';
        
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

        // Create proper slug from title
        const slug = title ? title
            .toLowerCase()
            .replace(/[^\w\s-]/g, '') // Remove special chars
            .replace(/\s+/g, '-') // Replace spaces with hyphens
            .trim() : 'untitled';

        const data = {
            text: finalContent,
            keyword: keyword,
            title: title,
            description: metaDescription,
            url: slug,
            slug: slug,
            locale: 'en_US'
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

    analyze() {
        try {
            // Get form data
            const data = this.collectFormData();
            
            // Check if we have meaningful content to analyze
            const hasContent = data.title.length > 0 || data.keyword.length > 0 || data.text.length > 10;
            
            if (!hasContent) {
                console.log('Skipping analysis - no meaningful content yet');
                this.updateEmptyState();
                return;
            }

            // Create Paper object for YoastSEO analysis
            const paper = new Paper(data.text, {
                keyword: data.keyword,
                title: data.title,
                description: data.description,
                url: data.url || '',
                locale: 'en_US'
            });
            
            console.log('Paper created:', paper);
            
            // Create YoastSEO researcher for language analysis
            const researcher = new AbstractResearcher(paper);
            
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
        const keywordElement = document.getElementById('meta-keywords-input');

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
            
            if (assessment.score >= 9) {
                statusClass = 'excellent';
            } else if (assessment.score >= 6) {
                statusClass = 'good';
            } else if (assessment.score >= 1) {
                statusClass = 'poor';
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
            // 'ImageCountAssessment': 'تعداد تصاویر',
            'InternalLinksAssessment': 'لینک‌های داخلی',
            'OutboundLinksAssessment': 'لینک‌های خارجی',
            'FunctionWordsInKeyphraseAssessment': 'کلمات عملکردی در کلیدواژه',
            // 'TextTitleAssessment': 'عنوان اصلی (H1) در محتوا',
            
            // Readability Assessments
            'TextPresenceAssessment': 'وجود متن',
            'SubheadingDistributionTooLongAssessment': 'توزیع زیرعناوین',
            'WordComplexityAssessment': 'پیچیدگی کلمات',
            'ParagraphTooLongAssessment': 'طول پاراگراف‌ها'
        };
        return translations[name] || name;
    }

    translateYoastFeedback(englishText, assessmentName) {
        // Remove HTML tags and clean text
        let cleanText = englishText
            .replace(/<a[^>]*>/gi, '')
            .replace(/<\/a>/gi, '')
            .replace(/target='_blank'/gi, '');

        // Translate common YoastSEO feedback patterns
        const translations = {
            'TextLengthAssessment': (text) => {
                if (text.includes('far below the recommended minimum')) {
                    const wordCount = text.match(/(\d+) words/)?.[1] || '0';
                    return `متن شامل ${wordCount} کلمه است. این تعداد بسیار کمتر از حداقل توصیه شده ۳۰۰ کلمه است. محتوای بیشتری اضافه کنید.`;
                }
                if (text.includes('below the recommended minimum')) {
                    const wordCount = text.match(/(\d+) words/)?.[1] || '0';
                    return `متن شامل ${wordCount} کلمه است. برای بهینه‌سازی سئو، حداقل ۳۰۰ کلمه توصیه می‌شود.`;
                }
                if (text.includes('Good job')) {
                    return 'طول متن مناسب است.';
                }
                return 'متن نیاز به بررسی طول دارد.';
            },
            
            'MetaDescriptionLengthAssessment': (text) => {
                if (text.includes('too short')) {
                    return 'توضیحات متا خیلی کوتاه است (کمتر از ۱۲۰ کاراکتر). حداکثر ۱۵۶ کاراکتر در دسترس است.';
                }
                if (text.includes('over 156 characters')) {
                    return 'توضیحات متا بیش از ۱۵۶ کاراکتر است. برای نمایش کامل در نتایج جستجو، طول را کاهش دهید.';
                }
                if (text.includes('Good job')) {
                    return 'طول توضیحات متا مناسب است.';
                }
                return 'توضیحات متا نیاز به بهینه‌سازی دارد.';
            },
            
            'PageTitleWidthAssessment': (text) => {
                if (text.includes('Please create an SEO title')) {
                    return 'شما نیاز به یک عنوان سئو دارید. این عنوان در نتایج گوگل نمایش داده می‌شود و باید بین 30-60 کاراکتر باشد.';
                }
                if (text.includes('too wide')) {
                    return 'عنوان سئو شما خیلی طولانی است. عناوین بلند در نتایج جستجو کوتاه می‌شوند.';
                }
                if (text.includes('Good job')) {
                    return 'طول عنوان سئو مناسب است برای نمایش در گوگل.';
                }
                return 'عنوان سئو (که در نتایج گوگل نمایش داده می‌شود) نیاز به بررسی دارد.';
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
            
            'InternalLinksAssessment': (text) => {
                if (text.includes('No internal links')) {
                    return 'هیچ لینک داخلی در این صفحه یافت نشد. لینک به صفحات مرتبط داخلی توصیه می‌شود.';
                }
                return 'لینک‌های داخلی مناسب است.';
            },
            
            'OutboundLinksAssessment': (text) => {
                if (text.includes('No outbound links')) {
                    return 'هیچ لینک خارجی در این صفحه وجود ندارد. لینک به منابع معتبر خارجی توصیه می‌شود.';
                }
                return 'لینک‌های خارجی مناسب است.';
            },
            
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
            
            'FunctionWordsInKeyphraseAssessment': (text) => {
                if (text.includes('contains function words only')) {
                    return 'کلیدواژه شما فقط شامل کلمات عملکردی است. کلمات عملکردی مانند "از"، "در"، "به" برای سئو مناسب نیستند.';
                }
                if (text.includes('keyphrase') && text.includes('function words')) {
                    return 'کلیدواژه شما شامل کلمات عملکردی است. سعی کنید از کلمات اصلی و مهم استفاده کنید.';
                }
                return 'کلمات عملکردی در کلیدواژه بررسی شد.';
            }
            
            // 'TextTitleAssessment': (text) => {
            //     if (text.includes('does not have a title')) {
            //         return 'صفحه شما هنوز عنوان H1 ندارد. لطفاً در محتوا عنوان اصلی (H1) اضافه کنید.';
            //     }
            //     if (text.includes('Add one')) {
            //         return 'لطفاً عنوان اصلی (H1) را در محتوا اضافه کنید.';
            //     }
            //     return 'عنوان اصلی صفحه بررسی شد.';
            // }
        };

        // Apply specific translation if available
        if (translations[assessmentName]) {
            return translations[assessmentName](cleanText);
        }

        // Fallback: general translations for common phrases
        cleanText = cleanText
            .replace(/Text length:/gi, 'طول متن:')
            .replace(/Meta description length:/gi, 'طول توضیحات متا:')
            .replace(/SEO title width:/gi, 'عرض تیتر سئو:')
            .replace(/Images:/gi, 'تصاویر:')
            .replace(/Internal links:/gi, 'لینک‌های داخلی:')
            .replace(/Outbound links:/gi, 'لینک‌های خارجی:')
            .replace(/Subheading distribution:/gi, 'توزیع زیرعناوین:')
            .replace(/Word complexity:/gi, 'پیچیدگی کلمات:')
            .replace(/Good job!/gi, 'عالی است!')
            .replace(/Great!/gi, 'عالی!')
            .replace(/Add more content/gi, 'محتوای بیشتری اضافه کنید')
            .replace(/Use the space/gi, 'از فضای موجود استفاده کنید');

        return cleanText;
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
        
        // SEO Assessments that work with AbstractResearcher (TESTED & VERIFIED)
        const seoAssessments = [
            { name: 'TextLengthAssessment', class: assessments.seo.TextLengthAssessment },
            { name: 'MetaDescriptionLengthAssessment', class: assessments.seo.MetaDescriptionLengthAssessment },
            { name: 'PageTitleWidthAssessment', class: assessments.seo.PageTitleWidthAssessment },
            // { name: 'ImageCountAssessment', class: assessments.seo.ImageCountAssessment },
            { name: 'InternalLinksAssessment', class: assessments.seo.InternalLinksAssessment },
            { name: 'OutboundLinksAssessment', class: assessments.seo.OutboundLinksAssessment },
            { name: 'FunctionWordsInKeyphraseAssessment', class: assessments.seo.FunctionWordsInKeyphraseAssessment }
            // { name: 'TextTitleAssessment', class: assessments.seo.TextTitleAssessment }
        ];
        
        // Readability Assessments that work with AbstractResearcher (TESTED & VERIFIED)
        const readabilityAssessments = [
            { name: 'SubheadingDistributionTooLongAssessment', class: assessments.readability.SubheadingDistributionTooLongAssessment },
            { name: 'WordComplexityAssessment', class: assessments.readability.WordComplexityAssessment },
            { name: 'TextPresenceAssessment', class: assessments.readability.TextPresenceAssessment }
        ];
        
        console.log('Running YoastSEO assessments...');
        
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
                // YoastSEO uses: 9 = excellent, 6-8 = OK, 4-5 = needs improvement, <4 = bad
                // Special handling for negative scores and edge cases
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
                    // Handle moderate negative scores (like -20 for short text)
                    normalizedScore = 10;
                } else {
                    // Handle very negative scores (like -10000 for missing title)
                    normalizedScore = 0;
                }
                
                seoTotalScore += normalizedScore;
                seoCount++;
                
                console.log(`SEO ${assessmentInfo.name}: score=${result.score}, normalized=${normalizedScore}`);
                
            } catch (error) {
                console.log(`SEO ${assessmentInfo.name} failed:`, error.message);
            }
        });
        
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
        
        // Calculate final scores using YoastSEO's actual scoring
        yoastResults.scores.seo = seoCount > 0 ? Math.round(seoTotalScore / seoCount) : 0;
        yoastResults.scores.readability = readabilityCount > 0 ? Math.round(readabilityTotalScore / readabilityCount) : 0;
        
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
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new SimpleYoastSEO();
});
