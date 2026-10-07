/* خبرگزاری زلال: small behaviors. No dependencies. */
(() => {
  'use strict';
  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];
  const reduceMQ = matchMedia('(prefers-reduced-motion: reduce)');

  /* ---- live dates: Persian, Hijri, Gregorian ---- */
  function parts(locale, opts, d) {
    const o = {};
    try {
      new Intl.DateTimeFormat(locale, opts).formatToParts(d).forEach(p => { o[p.type] = p.value; });
    } catch (e) { return null; }
    return o;
  }
  function paintDates() {
    const d = new Date();
    const fa = parts('fa-IR-u-ca-persian', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }, d);
    const hj = parts('fa-IR-u-ca-islamic-umalqura', { day: 'numeric', month: 'long', year: 'numeric' }, d);
    const gr = parts('fa-IR-u-ca-gregory', { day: 'numeric', month: 'long', year: 'numeric' }, d);
    if (fa) $('[data-date="fa"]').textContent = `${fa.weekday} ${fa.day} ${fa.month} ${fa.year}`;
    if (hj) $('[data-date="hijri"]').textContent = `${hj.day} ${hj.month} ${hj.year}`;
    if (gr) $('[data-date="greg"]').textContent = `${gr.day} ${gr.month} ${gr.year}`;
  }
  paintDates();

  /* ---- breaking ticker: one headline at a time, pausable ---- */
  const ticker = $('.ticker');
  if (ticker) {
    const items = $$('.ticker__item', ticker);
    const pauseBtn = $('[data-ticker="pause"]', ticker);
    let i = 0, timer = null, userPaused = false, hover = false;

    const show = n => {
      items[i].classList.remove('is-on');
      items[i].setAttribute('aria-hidden', 'true');
      i = (n + items.length) % items.length;
      items[i].classList.add('is-on');
      items[i].removeAttribute('aria-hidden');
    };
    const running = () => !userPaused && !hover && !reduceMQ.matches && !document.hidden;
    const arm = () => {
      clearInterval(timer); timer = null;
      if (running()) timer = setInterval(() => show(i + 1), 6000);
    };
    const setPauseUI = () => {
      const paused = userPaused || reduceMQ.matches;
      pauseBtn.setAttribute('aria-pressed', String(paused));
      pauseBtn.setAttribute('aria-label', paused ? 'پخش خبرهای فوری' : 'توقف خبرهای فوری');
      $('.i-pause', pauseBtn).style.display = paused ? 'none' : '';
      $('.i-play', pauseBtn).style.display = paused ? '' : 'none';
    };

    $('[data-ticker="next"]', ticker).addEventListener('click', () => { show(i + 1); arm(); });
    $('[data-ticker="prev"]', ticker).addEventListener('click', () => { show(i - 1); arm(); });
    pauseBtn.addEventListener('click', () => { userPaused = !userPaused; setPauseUI(); arm(); });
    ticker.addEventListener('mouseenter', () => { hover = true; arm(); });
    ticker.addEventListener('mouseleave', () => { hover = false; arm(); });
    ticker.addEventListener('focusin', () => { hover = true; arm(); });
    ticker.addEventListener('focusout', () => { hover = false; arm(); });
    reduceMQ.addEventListener('change', () => { setPauseUI(); arm(); });
    document.addEventListener('visibilitychange', arm);
    items.forEach((el, n) => { if (n) el.setAttribute('aria-hidden', 'true'); });
    setPauseUI(); arm();
  }

  /* ---- tabs: latest / most read ---- */
  $$('[data-tabs]').forEach(root => {
    const tabs = $$('[role="tab"]', root);
    const select = (tab, focus) => {
      tabs.forEach(t => {
        const on = t === tab;
        t.setAttribute('aria-selected', String(on));
        t.tabIndex = on ? 0 : -1;
        document.getElementById(t.getAttribute('aria-controls')).hidden = !on;
      });
      if (focus) tab.focus();
    };
    tabs.forEach((t, n) => {
      t.addEventListener('click', () => select(t));
      t.addEventListener('keydown', e => {
        // RTL: ArrowLeft moves to the next tab, ArrowRight to the previous one
        let k = null;
        if (e.key === 'ArrowLeft') k = n + 1;
        if (e.key === 'ArrowRight') k = n - 1;
        if (e.key === 'Home') k = 0;
        if (e.key === 'End') k = tabs.length - 1;
        if (k === null) return;
        e.preventDefault();
        select(tabs[(k + tabs.length) % tabs.length], true);
      });
    });
  });

  /* ---- mobile drawer ---- */
  const drawer = $('#drawer');
  const openBtn = $('[data-drawer="open"]');
  if (drawer && openBtn) {
    const panel = $('.drawer__panel', drawer);
    const focusables = () => $$('a[href], button, input', panel).filter(el => !el.disabled);
    const open = () => {
      drawer.classList.add('open');
      drawer.removeAttribute('inert');
      openBtn.setAttribute('aria-expanded', 'true');
      document.body.style.overflow = 'hidden';
      setTimeout(() => focusables()[0]?.focus(), 30);
    };
    const close = () => {
      drawer.classList.remove('open');
      drawer.setAttribute('inert', '');
      openBtn.setAttribute('aria-expanded', 'false');
      document.body.style.overflow = '';
      openBtn.focus();
    };
    openBtn.addEventListener('click', open);
    $$('[data-drawer="close"]', drawer).forEach(el => el.addEventListener('click', close));
    $$('nav a', drawer).forEach(a => a.addEventListener('click', close));
    drawer.addEventListener('keydown', e => {
      if (e.key === 'Escape') { close(); return; }
      if (e.key !== 'Tab') return;
      const f = focusables(), first = f[0], last = f[f.length - 1];
      if (e.shiftKey && document.activeElement === first) { e.preventDefault(); last.focus(); }
      else if (!e.shiftKey && document.activeElement === last) { e.preventDefault(); first.focus(); }
    });
    matchMedia('(min-width: 720px)').addEventListener('change', e => { if (e.matches && drawer.classList.contains('open')) close(); });
  }

  /* ---- header search: empty searches stay on the page ---- */
  $$('form.search').forEach(f => f.addEventListener('submit', e => {
    const input = f.querySelector('input[name="q"]');
    if (input && !input.value.trim()) { e.preventDefault(); input.focus(); }
  }));

  /* ---- newsletter form: honest demo success state ---- */
  const form = $('#letter-form');
  if (form) {
    const input = $('input[type="email"]', form);
    const msg = $('.form__msg', form);
    form.addEventListener('submit', e => {
      e.preventDefault();
      const ok = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(input.value.trim());
      if (!ok) {
        input.setAttribute('aria-invalid', 'true');
        msg.className = 'form__msg err';
        msg.textContent = 'لطفاً یک نشانی ایمیل درست وارد کنید.';
        input.focus();
        return;
      }
      input.removeAttribute('aria-invalid');
      msg.className = 'form__msg ok';
      msg.textContent = 'عضویت ثبت شد. این نسخهٔ نمایشی است و ایمیلی ارسال نمی‌شود.';
      form.reset();
    });
    input.addEventListener('input', () => {
      if (input.getAttribute('aria-invalid')) { input.removeAttribute('aria-invalid'); msg.textContent = ''; }
    });
  }

  /* ---- pause CSS animations on hidden tabs ---- */
  document.addEventListener('visibilitychange', () => document.body.classList.toggle('paused', document.hidden));
})();

/* ---------- inner pages ---------- */
(() => {
  'use strict';
  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];

  /* article tools: text size, copy link, print, share */
  const prose = $('[data-prose]');
  const say = (root, text) => {
    const m = $('.tools__msg', root);
    if (!m) return;
    m.textContent = text;
    clearTimeout(m._t);
    m._t = setTimeout(() => { m.textContent = ''; }, 2600);
  };
  $$('.tools').forEach(bar => {
    const up = $('[data-font="up"]', bar), down = $('[data-font="down"]', bar);
    const setSize = n => {
      if (!prose) return;
      n = Math.max(-1, Math.min(2, n));
      prose.dataset.size = String(n);
      if (up) up.disabled = n >= 2;
      if (down) down.disabled = n <= -1;
      try { localStorage.setItem('zolal-text', String(n)); } catch (e) {}
    };
    if (prose && (up || down)) {
      let saved = 0;
      try { saved = Number(localStorage.getItem('zolal-text')) || 0; } catch (e) {}
      setSize(saved);
      up?.addEventListener('click', () => setSize(Number(prose.dataset.size) + 1));
      down?.addEventListener('click', () => setSize(Number(prose.dataset.size) - 1));
    }
    $('[data-copy]', bar)?.addEventListener('click', async () => {
      try { await navigator.clipboard.writeText(location.href.split('#')[0]); say(bar, 'پیوند کپی شد.'); }
      catch (e) { say(bar, 'کپی انجام نشد؛ نشانی را از نوار مرورگر بردارید.'); }
    });
    $('[data-print]', bar)?.addEventListener('click', () => window.print());
  });
  const url = encodeURIComponent(location.href.split('#')[0]);
  const title = encodeURIComponent(document.querySelector('h1')?.textContent.trim() || document.title);
  $$('[data-share]').forEach(a => {
    const kind = a.dataset.share;
    if (kind === 'telegram') a.href = `https://t.me/share/url?url=${url}&text=${title}`;
    if (kind === 'x') a.href = `https://twitter.com/intent/tweet?url=${url}&text=${title}`;
    if (kind === 'whatsapp') a.href = `https://wa.me/?text=${title}%20${url}`;
    a.target = '_blank';
    a.rel = 'noopener noreferrer';
  });

  /* listing filters */
  $$('[data-filter-group]').forEach(group => {
    const list = document.getElementById(group.dataset.filterGroup);
    const status = document.getElementById(group.dataset.status);
    const empty = list && list.parentElement.querySelector('.empty');
    const buttons = $$('button[data-filter]', group);
    buttons.forEach(btn => btn.addEventListener('click', () => {
      const f = btn.dataset.filter;
      buttons.forEach(b => b.setAttribute('aria-pressed', String(b === btn)));
      let shown = 0;
      $$('[data-cat]', list).forEach(item => {
        const ok = f === 'all' || item.dataset.cat.split(' ').includes(f);
        item.hidden = !ok;
        if (ok) shown++;
      });
      if (empty) empty.hidden = shown !== 0;
      if (status) status.textContent = f === 'all' ? '' : `${shown.toLocaleString('fa-IR')} مورد در دستهٔ «${btn.textContent.trim()}»`;
    }));
  });

  /* video player: plays data-src when a real file is set, otherwise says so */
  const player = $('[data-player]');
  if (player) {
    const msg = $('.player__msg', player);
    const playBtn = $('.player__play', player);
    let video = null;
    const noFile = () => {
      msg.hidden = false;
      clearTimeout(msg._t);
      msg._t = setTimeout(() => { msg.hidden = true; }, 5000);
    };
    const ensureVideo = () => {
      if (video) return video;
      const src = player.dataset.src;
      if (!src) return null;
      video = document.createElement('video');
      video.src = src;
      video.controls = true;
      video.playsInline = true;
      video.preload = 'metadata';
      video.poster = $('img', player)?.src || '';
      player.appendChild(video);
      playBtn.remove();
      $('.dur', player)?.remove();
      return video;
    };
    playBtn.addEventListener('click', () => {
      const v = ensureVideo();
      if (!v) return noFile();
      v.play().catch(() => {});
      v.focus();
    });
    $$('[data-seek]').forEach(b => b.addEventListener('click', () => {
      const v = ensureVideo();
      if (!v) { noFile(); player.scrollIntoView({ block: 'nearest' }); return; }
      v.currentTime = Number(b.dataset.seek) || 0;
      v.play().catch(() => {});
    }));
    $$('[data-download]').forEach(b => b.addEventListener('click', () => {
      if (!player.dataset.src) { noFile(); return; }
      const a = document.createElement('a');
      a.href = player.dataset.src; a.download = '';
      document.body.appendChild(a); a.click(); a.remove();
    }));
  }

  /* photo viewer */
  const viewer = $('[data-viewer]');
  if (viewer) {
    const thumbs = $$('.thumbs button');
    const img = $('.viewer__stage img', viewer);
    const count = $('.viewer__count', viewer);
    const cap = $('.viewer__info h2', viewer);
    const text = $('.viewer__info p', viewer);
    const by = $('.viewer__by', viewer);
    const dl = $('[data-photo-download]', viewer);
    const full = $('[data-photo-full]', viewer);
    const live = $('.viewer__live', viewer);
    const total = thumbs.length;
    let cur = 0;
    const fa = n => n.toLocaleString('fa-IR');

    const show = (n, opts = {}) => {
      cur = (n + total) % total;
      const t = thumbs[cur].dataset;
      img.classList.add('is-loading');
      const next = new Image();
      next.onload = next.onerror = () => {
        img.src = t.large; img.alt = t.alt;
        img.classList.remove('is-loading');
      };
      next.src = t.large;
      count.textContent = `${fa(cur + 1)} از ${fa(total)}`;
      cap.textContent = t.title;
      text.textContent = t.text;
      by.textContent = `عکس: ${t.by}`;
      dl.href = t.large;
      full.href = t.large;
      thumbs.forEach((b, i) => b.setAttribute('aria-current', String(i === cur)));
      if (live) live.textContent = `عکس ${fa(cur + 1)} از ${fa(total)}: ${t.title}`;
      if (!opts.keepHash) history.replaceState(null, '', `#p${cur + 1}`);
      // warm the neighbours
      [cur + 1, cur - 1].forEach(k => { const d = thumbs[(k + total) % total].dataset; const im = new Image(); im.src = d.large; });
    };

    thumbs.forEach((b, i) => b.addEventListener('click', () => show(i)));
    // RTL: the "next" arrow sits on the left
    $('.viewer__nav--next', viewer).addEventListener('click', () => show(cur + 1));
    $('.viewer__nav--prev', viewer).addEventListener('click', () => show(cur - 1));
    viewer.addEventListener('keydown', e => {
      if (e.target.closest('input, textarea')) return;
      if (e.key === 'ArrowLeft') { e.preventDefault(); show(cur + 1); }
      if (e.key === 'ArrowRight') { e.preventDefault(); show(cur - 1); }
    });
    // touch swipe; in RTL the next photo sits to the left, so a rightward swipe moves forward
    const stage = $('.viewer__stage', viewer);
    let sx = null, sy = 0;
    stage.addEventListener('pointerdown', e => { if (e.pointerType !== 'mouse') { sx = e.clientX; sy = e.clientY; } });
    stage.addEventListener('pointerup', e => {
      if (sx === null) return;
      const dx = e.clientX - sx, dy = e.clientY - sy;
      sx = null;
      if (Math.abs(dx) > 40 && Math.abs(dx) > Math.abs(dy)) show(cur + (dx > 0 ? 1 : -1));
    });
    stage.addEventListener('pointercancel', () => { sx = null; });
    const fromHash = () => {
      const m = /^#p(\d+)$/.exec(location.hash);
      return m ? Math.min(total, Math.max(1, Number(m[1]))) - 1 : 0;
    };
    show(fromHash(), { keepHash: true });
    if (location.hash) viewer.scrollIntoView({ block: 'start' });
    window.addEventListener('hashchange', () => show(fromHash(), { keepHash: true }));
  }
})();

/* ---------- footer pages ---------- */
(() => {
  'use strict';
  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];
  const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

  /* contact and ad forms: check fields, then an honest demo success line */
  $$('form[data-demo-form]').forEach(form => {
    const done = $('.form-done', form);
    const fields = $$('input, select, textarea', form).filter(el => el.id);
    const check = el => {
      const v = el.value.trim();
      let msg = '';
      if (el.required && !v) msg = el.dataset.req || 'این بخش را پر کنید.';
      else if (el.type === 'email' && v && !EMAIL.test(v)) msg = 'لطفاً یک نشانی ایمیل درست وارد کنید.';
      const err = document.getElementById(el.id + '-err');
      if (err) err.textContent = msg;
      if (msg) el.setAttribute('aria-invalid', 'true'); else el.removeAttribute('aria-invalid');
      return !msg;
    };
    fields.forEach(el => {
      el.addEventListener('blur', () => { if (el.value.trim() || el.getAttribute('aria-invalid')) check(el); });
      el.addEventListener('input', () => { if (el.getAttribute('aria-invalid')) check(el); });
    });
    form.addEventListener('submit', e => {
      e.preventDefault();
      const bad = fields.filter(el => !check(el));
      if (bad.length) { done.hidden = true; bad[0].focus(); return; }
      done.textContent = form.dataset.done;
      done.hidden = false;
      form.reset();
    });
  });

  /* preselect the subject from ?subject=… (used by the corrections page) */
  const subject = new URLSearchParams(location.search).get('subject');
  const subjectSel = $('#c-subject');
  if (subject && subjectSel && [...subjectSel.options].some(o => o.value === subject)) subjectSel.value = subject;

  /* archive filters */
  const af = $('#archive-form');
  if (af) {
    const status = $('#arch-status');
    const empty = $('#arch-empty');
    const run = () => {
      const sec = af.elements.sec.value, day = af.elements.day.value, q = af.elements.q.value.trim();
      let n = 0;
      $$('.day').forEach(d => {
        let dn = 0;
        const dayOk = day === 'all' || d.dataset.day === day;
        $$('li[data-sec]', d).forEach(li => {
          const ok = dayOk && (sec === 'all' || li.dataset.sec === sec) && (!q || li.textContent.includes(q));
          li.hidden = !ok;
          if (ok) { dn++; n++; }
        });
        d.hidden = dn === 0;
      });
      empty.hidden = n !== 0;
      status.textContent = `${n.toLocaleString('fa-IR')} خبر`;
    };
    af.addEventListener('submit', e => { e.preventDefault(); run(); });
    af.addEventListener('change', run);
    af.elements.q.addEventListener('input', run);
  }
})();


/* ---------- search page ---------- */
(() => {
  'use strict';
  const app = document.getElementById('search-app');
  if (!app) return;
  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];
  const DB = JSON.parse(document.getElementById('search-data').textContent);
  const ITEMS = DB.items, TYPES = DB.types, SECS = DB.secs;
  const PER_PAGE = 8;
  const fa = n => n.toLocaleString('fa-IR');
  const DATES = { all: 'همهٔ زمان‌ها', day: '۲۴ ساعت گذشته', week: 'هفتهٔ گذشته', month: 'ماه گذشته' };

  // one-to-one character map, so positions in the normalized text match the original
  const MAP = { 'ي': 'ی', 'ى': 'ی', 'ئ': 'ی', 'ك': 'ک', 'ة': 'ه', 'ۀ': 'ه', 'أ': 'ا', 'إ': 'ا', 'ٱ': 'ا', '‌': ' ', '‏': ' ', ' ': ' ' };
  const LAT = '0123456789', ARB = '٠١٢٣٤٥٦٧٨٩', PER = '۰۱۲۳۴۵۶۷۸۹';
  const norm = str => {
    let o = '';
    for (let i = 0; i < str.length; i++) {
      let c = str[i];
      if (MAP[c] !== undefined) c = MAP[c];
      let k = LAT.indexOf(c); if (k >= 0) c = PER[k];
      k = ARB.indexOf(c); if (k >= 0) c = PER[k];
      const lc = c.toLowerCase();
      o += lc.length === 1 ? lc : c;
    }
    return o;
  };
  ITEMS.forEach(it => {
    it._t = norm(it.t); it._s = norm(it.s);
    it._all = norm([it.t, it.s, TYPES[it.type], SECS[it.sec], it.p || '', it.k || ''].join(' '));
  });

  const esc = s => s.replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const highlight = (orig, normed, terms) => {
    if (!terms.length) return esc(orig);
    const ranges = [];
    terms.forEach(t => { let i = normed.indexOf(t); while (i >= 0) { ranges.push([i, i + t.length]); i = normed.indexOf(t, i + t.length); } });
    if (!ranges.length) return esc(orig);
    ranges.sort((a, b) => a[0] - b[0]);
    const merged = [ranges[0]];
    ranges.slice(1).forEach(r => { const last = merged[merged.length - 1]; if (r[0] <= last[1]) last[1] = Math.max(last[1], r[1]); else merged.push(r); });
    let out = '', pos = 0;
    merged.forEach(([a, b]) => { out += esc(orig.slice(pos, a)) + '<mark>' + esc(orig.slice(a, b)) + '</mark>'; pos = b; });
    return out + esc(orig.slice(pos));
  };
  const count = (hay, t) => { let n = 0, i = hay.indexOf(t); while (i >= 0) { n++; i = hay.indexOf(t, i + t.length); } return n; };

  /* ---- state, read from and written to the address ---- */
  const P = new URLSearchParams(location.search);
  const list = v => (v ? v.split(',').filter(Boolean) : []);
  const state = {
    q: (P.get('q') || '').trim(),
    type: list(P.get('type')).filter(v => TYPES[v]),
    sec: list(P.get('sec')).filter(v => SECS[v]),
    date: DATES[P.get('date')] ? P.get('date') : 'all',
    prov: P.get('prov') || 'all',
    sort: ['relevance', 'new', 'views'].includes(P.get('sort')) ? P.get('sort') : '',
    page: Math.max(1, parseInt(P.get('page'), 10) || 1),
  };
  let sortTouched = !!state.sort;

  const form = $('#search-form'), input = $('#sq');
  const sortSel = $('#ssort'), provSel = $('#sprov');
  const listEl = $('#results'), countEl = $('#res-count'), chipsEl = $('#active-chips');
  const emptyEl = $('#search-empty'), pagerEl = $('#spager');
  const panel = $('.filters-panel'), toggle = $('.filters-toggle'), badge = $('.filters-toggle .badge');

  // split on real spaces first, so a half-space word like «کم‌آبی» stays one phrase
  const terms = () => state.q.split(/\s+/).map(t => norm(t).trim()).filter(t => t.length >= 2 || /[۰-۹]/.test(t));
  const dateOk = it => state.date === 'all' || (state.date === 'day' ? it.d === 0 : state.date === 'week' ? it.d <= 7 : it.d <= 30);
  const pass = (it, T, except) =>
    (except === 'type' || !state.type.length || state.type.includes(it.type)) &&
    (except === 'sec' || !state.sec.length || state.sec.includes(it.sec)) &&
    (except === 'date' || dateOk(it)) &&
    (except === 'prov' || state.prov === 'all' || it.p === state.prov) &&
    T.every(t => it._all.includes(t));
  const score = (it, T) => T.reduce((sum, t) => sum + count(it._t, t) * 3 + count(it._s, t), 0) - it.d * 0.01;

  function writeUrl() {
    const u = new URLSearchParams();
    if (state.q) u.set('q', state.q);
    if (state.type.length) u.set('type', state.type.join(','));
    if (state.sec.length) u.set('sec', state.sec.join(','));
    if (state.date !== 'all') u.set('date', state.date);
    if (state.prov !== 'all') u.set('prov', state.prov);
    if (sortTouched) u.set('sort', state.sort);
    if (state.page > 1) u.set('page', state.page);
    const qs = u.toString();
    history.replaceState(null, '', location.pathname + (qs ? '?' + qs : ''));
  }

  function syncControls() {
    input.value = state.q;
    $$('form.search input[name="q"]').forEach(i => { i.value = state.q; });
    $$('input[name="type"]').forEach(c => { c.checked = state.type.includes(c.value); });
    $$('input[name="sec"]').forEach(c => { c.checked = state.sec.includes(c.value); });
    $$('input[name="date"]').forEach(r => { r.checked = r.value === state.date; });
    provSel.value = [...provSel.options].some(o => o.value === state.prov) ? state.prov : 'all';
    const relOpt = sortSel.querySelector('option[value="relevance"]');
    relOpt.disabled = !state.q;
    if (!sortTouched || (!state.q && state.sort === 'relevance')) state.sort = state.q ? 'relevance' : 'new';
    sortSel.value = state.sort;
  }

  function resultHTML(it, T) {
    const type = TYPES[it.type], sec = SECS[it.sec];
    let thumb = '';
    if (it.img) {
      let extra = '';
      if (it.type === 'video') extra = '<span class="play play--sm" aria-hidden="true"><span><svg><use href="#i-play"/></svg></span></span><span class="dur">' + esc(it.x) + '</span>';
      if (it.type === 'photo') extra = '<span class="count"><svg aria-hidden="true"><use href="#i-camera"/></svg>' + esc(it.x) + '</span>';
      thumb = '<div class="frame ritem__thumb"><img src="/assets/zolal/img/' + it.img + '-800.jpg" width="800" height="500" loading="lazy" alt="">' + extra + '</div>';
    }
    const foot = [];
    if (it.v) foot.push(fa(it.v) + ' بازدید');
    if (it.p) foot.push('استان ' + esc(it.p));
    if (it.type === 'video' && !it.img && it.x) foot.push('مدت ' + esc(it.x));
    return '<li class="ritem"><a class="ritem__link' + (it.img ? '' : ' ritem__link--text') + '" href="' + esc(it.href) + '">' + thumb +
      '<div class="ritem__body"><div class="ritem__meta"><span class="tbadge tbadge--' + it.type + '">' + type + '</span><span class="tagline">' + sec + '</span><time>' + esc(it.dl) + '، ' + esc(it.tm) + '</time></div>' +
      '<h3>' + highlight(it.t, it._t, T) + '</h3><p>' + highlight(it.s, it._s, T) + '</p>' +
      (foot.length ? '<div class="ritem__foot">' + foot.map(f => '<span>' + f + '</span>').join('') + '</div>' : '') +
      '</div></a></li>';
  }

  function chip(label, onRemove) {
    const b = document.createElement('button');
    b.type = 'button'; b.className = 'achip';
    b.setAttribute('aria-label', 'حذف فیلتر ' + label);
    b.append(label);
    const x = document.createElement('span'); x.setAttribute('aria-hidden', 'true'); x.textContent = '×';
    b.append(x);
    b.addEventListener('click', () => { onRemove(); state.page = 1; render(); });
    return b;
  }

  function renderChips() {
    chipsEl.replaceChildren();
    if (state.q) chipsEl.append(chip('عبارت: ' + state.q, () => { state.q = ''; }));
    state.type.forEach(v => chipsEl.append(chip(TYPES[v], () => { state.type = state.type.filter(x => x !== v); })));
    state.sec.forEach(v => chipsEl.append(chip(SECS[v], () => { state.sec = state.sec.filter(x => x !== v); })));
    if (state.date !== 'all') chipsEl.append(chip(DATES[state.date], () => { state.date = 'all'; }));
    if (state.prov !== 'all') chipsEl.append(chip('استان ' + state.prov, () => { state.prov = 'all'; }));
    if (chipsEl.children.length > 1) {
      const c = document.createElement('button');
      c.type = 'button'; c.className = 'achip achip--clear'; c.textContent = 'پاک کردن همه';
      c.addEventListener('click', clearAll);
      chipsEl.append(c);
    }
    const active = state.type.length + state.sec.length + (state.date !== 'all') + (state.prov !== 'all');
    badge.textContent = active ? fa(active) : '';
  }

  function renderCounts(T) {
    $$('input[name="type"]').forEach(c => { c.closest('.check').querySelector('.fcount').textContent = fa(ITEMS.filter(it => it.type === c.value && pass(it, T, 'type')).length); });
    $$('input[name="sec"]').forEach(c => { c.closest('.check').querySelector('.fcount').textContent = fa(ITEMS.filter(it => it.sec === c.value && pass(it, T, 'sec')).length); });
    $$('input[name="date"]').forEach(r => {
      const saved = state.date; state.date = r.value;
      const n = ITEMS.filter(it => pass(it, T)).length;
      state.date = saved;
      r.closest('.check').querySelector('.fcount').textContent = fa(n);
    });
  }

  function renderPager(total) {
    pagerEl.replaceChildren();
    const pages = Math.ceil(total / PER_PAGE);
    pagerEl.hidden = pages <= 1;
    if (pages <= 1) return;
    const btn = (label, page, opts = {}) => {
      const b = document.createElement('button');
      b.type = 'button'; b.textContent = label;
      if (opts.current) b.setAttribute('aria-current', 'page');
      if (opts.disabled) b.disabled = true;
      if (opts.aria) b.setAttribute('aria-label', opts.aria);
      b.addEventListener('click', () => {
        state.page = page; render();
        const top = countEl.getBoundingClientRect().top;
        if (top < 0) countEl.scrollIntoView({ block: 'start' });
        countEl.focus({ preventScroll: true });
      });
      return b;
    };
    pagerEl.append(btn('قبلی', state.page - 1, { disabled: state.page === 1, aria: 'صفحهٔ قبلی' }));
    for (let i = 1; i <= pages; i++) pagerEl.append(btn(fa(i), i, { current: i === state.page, aria: 'صفحهٔ ' + fa(i) }));
    pagerEl.append(btn('بعدی', state.page + 1, { disabled: state.page === pages, aria: 'صفحهٔ بعدی' }));
  }

  function render() {
    syncControls();
    const T = terms();
    let res = ITEMS.filter(it => pass(it, T));
    if (state.sort === 'relevance' && T.length) res.sort((a, b) => score(b, T) - score(a, T) || a.d - b.d || b.m - a.m);
    else if (state.sort === 'views') res.sort((a, b) => b.v - a.v);
    else res.sort((a, b) => a.d - b.d || b.m - a.m);
    const pages = Math.max(1, Math.ceil(res.length / PER_PAGE));
    if (state.page > pages) state.page = pages;
    const slice = res.slice((state.page - 1) * PER_PAGE, state.page * PER_PAGE);
    listEl.innerHTML = slice.map(it => resultHTML(it, T)).join('');
    emptyEl.hidden = res.length !== 0;
    countEl.textContent = state.q
      ? (res.length ? fa(res.length) + ' نتیجه برای «' + state.q + '»' : 'نتیجه‌ای برای «' + state.q + '» پیدا نشد')
      : fa(res.length) + ' مطلب' + (res.length && (state.type.length || state.sec.length || state.date !== 'all' || state.prov !== 'all') ? ' با فیلترهای انتخاب‌شده' : ' در زلال');
    renderCounts(T);
    renderChips();
    renderPager(res.length);
    writeUrl();
    document.title = (state.q ? 'جستجو: ' + state.q : 'جستجو') + ' | خبرگزاری زلال';
  }

  function clearAll() {
    state.type = []; state.sec = []; state.date = 'all'; state.prov = 'all'; state.page = 1;
    render();
  }

  form.addEventListener('submit', e => {
    e.preventDefault();
    state.q = input.value.trim(); state.page = 1;
    render();
  });
  $$('form.search').forEach(f => f.addEventListener('submit', e => {
    e.preventDefault();
    state.q = f.querySelector('input[name="q"]').value.trim(); state.page = 1;
    render();
    input.focus();
  }));
  $('.filters-body').addEventListener('change', () => {
    state.type = $$('input[name="type"]:checked').map(c => c.value);
    state.sec = $$('input[name="sec"]:checked').map(c => c.value);
    state.date = ($('input[name="date"]:checked') || {}).value || 'all';
    state.prov = provSel.value;
    state.page = 1;
    render();
  });
  sortSel.addEventListener('change', () => { state.sort = sortSel.value; sortTouched = true; state.page = 1; render(); });
  $$('[data-clear]').forEach(b => b.addEventListener('click', () => { state.q = ''; clearAll(); input.focus(); }));
  $$('[data-q]').forEach(b => b.addEventListener('click', () => { state.q = b.dataset.q; state.page = 1; render(); }));
  toggle.addEventListener('click', () => {
    const open = !panel.classList.contains('open');
    panel.classList.toggle('open', open);
    toggle.setAttribute('aria-expanded', String(open));
  });

  render();
})();
