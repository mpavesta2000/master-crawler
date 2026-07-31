/*
 * ChapChin page — presentation layer only.
 *
 * filterSites() in chapchin.js does the actual show/hide work (it is wired
 * through the inline onkeyup attribute, so it always runs first). This file
 * only reacts to the result: it flips the "no match" message and keeps the
 * visible-site counter in sync. No backend calls, no filtering logic.
 */
(function () {
    'use strict';

    var search = document.getElementById('searchInput');
    var noResult = document.getElementById('noSiteResult');
    var counter = document.getElementById('visibleSiteCount');
    if (!search) return;

    function refresh() {
        var items = document.getElementsByClassName('site-item');
        var visible = 0;

        for (var i = 0; i < items.length; i++) {
            if (items[i].style.display !== 'none') visible++;
        }

        if (counter) counter.textContent = visible.toLocaleString('fa-IR');
        if (noResult) noResult.classList.toggle('is-visible', visible === 0);
    }

    // keyup (not input): the inline onkeyup handler is registered first, so by
    // the time this fires the rows have already been filtered
    search.addEventListener('keyup', refresh);
    search.addEventListener('search', refresh);

    refresh();
})();
