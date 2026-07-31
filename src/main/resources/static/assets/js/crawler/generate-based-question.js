document.addEventListener('DOMContentLoaded', function () {
    const openNewsGeneratorBtn = document.getElementById('openNewsGeneratorBtn');
    const newsGeneratorModal = document.getElementById('newsGeneratorModal');
    const backBtn = document.getElementById('backBtn');
    const nextBtn = document.getElementById('nextBtn');
    const loadingOverlay = document.getElementById('loadingOverlay');
    const copyNewsBtn = document.getElementById('copyNewsBtn');

    const step1 = document.getElementById('step1');
    const step2 = document.getElementById('step2');
    const step3 = document.getElementById('step3');
    const step4 = document.getElementById('step4');

    const step1Indicator = document.getElementById('step1Indicator');
    const step2Indicator = document.getElementById('step2Indicator');
    const step3Indicator = document.getElementById('step3Indicator');
    const step4Indicator = document.getElementById('step4Indicator');

    const newsModelsContainer = document.getElementById('newsModelsContainer');
    const questionsContainer = document.getElementById('questionsContainer');
    const answersReviewContainer = document.getElementById('answersReviewContainer');
    const generatedNewsContainer = document.getElementById('generatedNewsContainer');

    const selectedModelName = document.getElementById('selectedModelName');
    const confirmModelName = document.getElementById('confirmModelName');
    const questionValidationError = document.getElementById('questionValidationError');

    const modal = new bootstrap.Modal(newsGeneratorModal);

    let currentStep = 1;
    let selectedModel = null;
    let questions = [];
    let answers = {};

    const newsModels = [
        {id: 'news-quote', name: 'خبر نقلی', icon: 'quote-right', color: 'primary'},
        {id: 'news-event', name: 'خبر رویدادی', icon: 'calendar-day', color: 'success'},
        {id: 'news-analysis', name: 'خبر تحلیلی', icon: 'chart-line', color: 'danger'}
    ];

    function setupModelSelection() {
        const modelCards = document.querySelectorAll('.news-card');
        modelCards.forEach(card => {
            card.addEventListener('click', function () {
                const modelId = this.getAttribute('data-model-id');
                selectedModel = newsModels.find(model => model.id === modelId);

                modelCards.forEach(c => c.classList.remove('border-primary', 'bg-light'));
                this.classList.add('border-primary', 'bg-light');
            });
        });
    }

    function goToStep(step) {
        // Hide all steps
        step1.style.display = 'none';
        step2.style.display = 'none';
        step3.style.display = 'none';
        step4.style.display = 'none';

        // Reset indicators
        step1Indicator.className = 'step';
        step2Indicator.className = 'step';
        step3Indicator.className = 'step';
        step4Indicator.className = 'step';

        switch (step) {
            case 1:
                step1.style.display = 'block';
                step1Indicator.className = 'step active';
                backBtn.style.display = 'none';
                nextBtn.textContent = 'ادامه';
                nextBtn.innerHTML = 'ادامه<i class="ri-arrow-left-line ms-1"></i>';
                break;
            case 2:
                step2.style.display = 'block';
                step1Indicator.className = 'step completed';
                step2Indicator.className = 'step active';
                backBtn.style.display = 'block';
                nextBtn.textContent = 'ادامه';
                nextBtn.innerHTML = 'ادامه<i class="ri-arrow-left-line ms-1"></i>';
                break;
            case 3:
                step3.style.display = 'block';
                step1Indicator.className = 'step completed';
                step2Indicator.className = 'step completed';
                step3Indicator.className = 'step active';
                backBtn.style.display = 'block';
                nextBtn.textContent = 'تولید خبر';
                nextBtn.innerHTML = 'تولید خبر<i class="ri-robot-line ms-1"></i>';
                break;
            case 4:
                step4.style.display = 'block';
                step1Indicator.className = 'step completed';
                step2Indicator.className = 'step completed';
                step3Indicator.className = 'step completed';
                step4Indicator.className = 'step active';
                backBtn.style.display = 'block';
                break;
        }

        currentStep = step;
    }

    function renderAnswersReview() {
        answersReviewContainer.innerHTML = '';
        confirmModelName.textContent = selectedModel.name;

        questions.forEach(question => {
            const row = document.createElement('div');
            row.className = 'mb-3';

            const questionDiv = document.createElement('div');
            questionDiv.className = 'fw-bold mb-1';
            questionDiv.textContent = question.text;

            const answerDiv = document.createElement('div');
            answerDiv.className = 'bg-light p-2 rounded';
            answerDiv.textContent = answers[question.id];

            row.appendChild(questionDiv);
            row.appendChild(answerDiv);

            answersReviewContainer.appendChild(row);
        });
    }

    function resetForm() {
        selectedModel = null;
        questions = [];
        answers = {};

        const modelCards = document.querySelectorAll('.news-card');
        modelCards.forEach(card => card.classList.remove('border-primary', 'bg-light'));

        questionsContainer.innerHTML = '';
        answersReviewContainer.innerHTML = '';
        generatedNewsContainer.innerHTML = '';

        goToStep(1);
    }

    function showLoading() {
        loadingOverlay.style.display = 'flex';
    }

    function hideLoading() {
        loadingOverlay.style.display = 'none';
    }

    async function fetchQuestionsForModel(modelId) {
        try {
            const response = await fetch(`/api/news-generator/questions?modelId=${modelId}`, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch questions');
            }

            return await response.json();
        } catch (error) {
            console.error('Error fetching questions:', error);
            Swal.fire({
                icon: 'error',
                title: 'خطا',
                text: 'خطا در دریافت سوالات. لطفاً دوباره تلاش کنید.'
            });
            return [];
        }
    }

    async function generateNewsFromBackend() {
        try {
            const response = await fetch('/api/news-generator/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    modelId: selectedModel.id,
                    answers: answers
                })
            });

            if (!response.ok) {
                throw new Error('Failed to generate news');
            }

            const result = await response.json();
            return result.generatedNews;
        } catch (error) {
            console.error('Error generating news:', error);
            Swal.fire({
                icon: 'error',
                title: 'خطا',
                text: 'خطا در تولید خبر. لطفاً دوباره تلاش کنید.'
            });
            return null;
        }
    }

    openNewsGeneratorBtn.addEventListener('click', function () {
        resetForm();
        modal.show();
    });

    nextBtn.addEventListener('click', async function () {
        switch (currentStep) {
            case 1:
                if (selectedModel) {
                    await loadQuestions(selectedModel.id);
                } else {
                    Swal.fire({
                        icon: 'warning',
                        title: 'هشدار',
                        text: 'لطفاً یک مدل خبری انتخاب کنید'
                    });
                }
                break;
            case 2:
                if (validateQuestions()) {
                    goToStep(3);
                    renderAnswersReview();
                } else {
                    questionValidationError.style.display = 'block';
                }
                break;
            case 3:
                await generateNews();
                break;
            case 4:
                resetForm();
                goToStep(1);
                break;
        }
    });

    backBtn.addEventListener('click', function () {
        if (currentStep > 1) {
            goToStep(currentStep - 1);
        }
    });

    copyNewsBtn.addEventListener('click', function () {
        const newsText = generatedNewsContainer.innerText;
        navigator.clipboard.writeText(newsText).then(function () {
            const originalText = copyNewsBtn.innerHTML;
            copyNewsBtn.innerHTML = '<i class="ri-check-line me-1"></i>کپی شد';
            setTimeout(() => {
                copyNewsBtn.innerHTML = originalText;
            }, 2000);
        });
    });

    async function loadQuestions(modelId) {
        showLoading();

        try {
            questions = await fetchQuestionsForModel(modelId);

            renderQuestions(questions);

            selectedModelName.textContent = selectedModel.name;

            goToStep(2);
        } catch (error) {
            console.error('Error loading questions:', error);
        } finally {
            hideLoading();
        }
    }

    // the model answers in markdown, so **bold** was showing up literally.
    // Converts **bold** / *italic* and turns blank lines into paragraphs.
    function renderGeneratedNews(text) {
        if (!text) return '';
        return text
            .split(/\n\s*\n/)
            .map(function (block) {
                return '<p>' + block
                    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
                    .replace(/(^|[^*])\*([^*\n]+)\*(?!\*)/g, '$1<em>$2</em>')
                    .replace(/\n/g, '<br>') + '</p>';
            })
            .join('');
    }

    async function generateNews() {
        showLoading();

        try {
            const newsContent = await generateNewsFromBackend();

            if (newsContent) {
                generatedNewsContainer.innerHTML = renderGeneratedNews(newsContent);

                goToStep(4);

                nextBtn.innerHTML = 'تولید خبر جدید<i class="ri-add-line ms-1"></i>';
            }
        } catch (error) {
            console.error('Error in news generation:', error);
        } finally {
            hideLoading();
        }
    }

    function renderQuestions(questions) {
        questionsContainer.innerHTML = '';

        if (!questions || questions.length === 0) {
            const errorMsg = document.createElement('div');
            errorMsg.className = 'alert alert-danger';
            errorMsg.textContent = 'سوالی برای این مدل یافت نشد.';
            questionsContainer.appendChild(errorMsg);
            return;
        }

        questions.forEach(question => {
            const formGroup = document.createElement('div');
            formGroup.className = 'form-group mb-3';

            const label = document.createElement('label');
            label.className = 'form-label';
            label.textContent = question.text;

            formGroup.appendChild(label);

            if (question.type === 'select') {
                const select = document.createElement('select');
                select.className = 'form-select';
                select.id = `question_${question.id}`;
                select.required = true;

                const defaultOption = document.createElement('option');
                defaultOption.value = '';
                defaultOption.textContent = 'انتخاب کنید';
                select.appendChild(defaultOption);

                (question.options || []).forEach(option => {
                    const optionEl = document.createElement('option');
                    optionEl.value = option;
                    optionEl.textContent = option;
                    select.appendChild(optionEl);
                });

                select.addEventListener('change', function () {
                    answers[question.id] = this.value;
                });

                formGroup.appendChild(select);
            } else {
                const textarea = document.createElement('textarea');
                textarea.className = 'form-control';
                textarea.rows = 3;
                textarea.id = `question_${question.id}`;
                textarea.required = true;

                textarea.addEventListener('input', function () {
                    answers[question.id] = this.value;
                });

                formGroup.appendChild(textarea);
            }

            questionsContainer.appendChild(formGroup);
        });
    }

    function validateQuestions() {
        questionValidationError.style.display = 'none';
        return questions.every(question => answers[question.id] && answers[question.id].trim() !== '');
    }

    setupModelSelection();
});