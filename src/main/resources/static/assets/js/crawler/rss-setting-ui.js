/*
 * RSS settings — presentation layer only.
 *
 * This file never talks to the backend. It only reflects the state of the
 * existing controls in the new UI (status pills, active counter, log console
 * search / clear / counter / connection indicator). All the API calls stay in
 * rss-setting.js, which loads before this file.
 */
(function () {
    'use strict';

    /* ------------------------------------------------------------------ */
    /* source cards: status pill + active counter                          */
    /* ------------------------------------------------------------------ */

    var sources = Array.prototype.slice.call(document.querySelectorAll('[data-rss-source]'));

    function paintSource(card) {
        var toggle = card.querySelector('[data-rss-toggle]');
        if (!toggle) return;

        var on = toggle.checked;
        card.classList.toggle('is-active', on);

        var text = card.querySelector('[data-rss-state-text]');
        if (text) text.textContent = on ? 'فعال' : 'غیرفعال';
    }

    function paintCounter() {
        var active = sources.filter(function (card) {
            var toggle = card.querySelector('[data-rss-toggle]');
            return toggle && toggle.checked;
        }).length;

        var activeEl = document.getElementById('rssActiveCount');
        var totalEl = document.getElementById('rssTotalCount');
        if (activeEl) activeEl.textContent = active.toLocaleString('fa-IR');
        if (totalEl) totalEl.textContent = sources.length.toLocaleString('fa-IR');
    }

    sources.forEach(function (card) {
        var toggle = card.querySelector('[data-rss-toggle]');
        if (toggle) {
            toggle.addEventListener('change', function () {
                paintSource(card);
                paintCounter();
            });
        }
        paintSource(card);
    });
    paintCounter();

    /* ------------------------------------------------------------------ */
    /* log console                                                         */
    /* ------------------------------------------------------------------ */

    var logs = document.getElementById('logsContainer');
    var empty = document.getElementById('rssConsoleEmpty');
    var countEl = document.getElementById('rssLogCount');
    var search = document.getElementById('rssLogSearch');
    var clearBtn = document.getElementById('rssLogClear');

    function currentFilter() {
        return search ? search.value.trim().toLowerCase() : '';
    }

    function applyFilter(line) {
        var term = currentFilter();
        if (!term) {
            line.classList.remove('rss-log-hidden');
            return;
        }
        var hit = (line.textContent || '').toLowerCase().indexOf(term) !== -1;
        line.classList.toggle('rss-log-hidden', !hit);
    }

    function refreshConsole() {
        if (!logs) return;

        var lines = logs.querySelectorAll('p');
        if (countEl) countEl.textContent = lines.length.toLocaleString('fa-IR');
        if (empty) empty.classList.toggle('d-none', lines.length > 0);
    }

    if (logs) {
        // new lines are appended by rss-setting.js over the STOMP socket
        new MutationObserver(function (mutations) {
            mutations.forEach(function (mutation) {
                Array.prototype.forEach.call(mutation.addedNodes, function (node) {
                    if (node.nodeType === 1 && node.tagName === 'P') applyFilter(node);
                });
            });
            refreshConsole();
        }).observe(logs, { childList: true });

        refreshConsole();
    }

    if (search) {
        search.addEventListener('input', function () {
            if (!logs) return;
            Array.prototype.forEach.call(logs.querySelectorAll('p'), applyFilter);
        });
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', function () {
            if (!logs) return;
            logs.innerHTML = '';
            refreshConsole();
        });
    }

    /* ------------------------------------------------------------------ */
    /* socket connection indicator                                         */
    /* ------------------------------------------------------------------ */

    var live = document.getElementById('rssLiveStatus');
    var liveText = document.getElementById('rssLiveStatusText');

    if (live && liveText) {
        var connectedOnce = false;

        setInterval(function () {
            var connected = typeof stompClient !== 'undefined' && stompClient && stompClient.connected;

            if (connected) {
                connectedOnce = true;
                live.classList.add('is-online');
                live.classList.remove('is-offline');
                liveText.textContent = 'متصل';
            } else {
                live.classList.remove('is-online');
                live.classList.add('is-offline');
                liveText.textContent = connectedOnce ? 'قطع شد' : 'در حال اتصال…';
            }
        }, 1000);
    }
})();
