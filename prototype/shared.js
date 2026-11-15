/* ============================================================
   心飞IM · 原型共享脚本 v1.0
   全局交互与无障碍基线：
   1) 弹层 Esc 关闭（modal / drawer / 右键菜单 / 帮助 / 命令面板 / 引导）
   2) 全局搜索聚焦（/）；命令面板（Ctrl/Cmd+K）
   3) 使用帮助（?）；首用引导（localStorage 记忆）
   4) 防重复提交锁（data-lock）
   5) toast 播报区域标记（aria-live）
   各页面按需在 </body> 前引入，所有查询均为防御式。
   ============================================================ */
(function () {
  'use strict';

  function qs(sel) { return document.querySelector(sel); }

  /* ---------- 1. 关闭最上层弹层 ---------- */
  /* 各页弹层类名统一为 .show 控制显示；mockup 的 .modal-overlay 用 .hidden */
  var SHOW_OVERLAYS =
    '.cal-overlay.show, .approval-overlay.show, .approval-modal-overlay.show, ' +
    '.confirm-overlay.show, .ct-overlay.show, .search-modal-overlay.show, ' +
    '.settings-modal-overlay.show, .checkin-modal-overlay.show, .xf-confirm-overlay.show, ' +
    '.xf-palette-overlay.show, .xf-onboard-overlay.show';

  function closeTopOverlay() {
    var help = qs('.xf-help-overlay');
    if (help) { help.remove(); return true; }

    var modal = qs('.modal-overlay:not(.hidden)');
    if (modal) { modal.classList.add('hidden'); return true; }

    var shows = document.querySelectorAll(SHOW_OVERLAYS);
    var show = shows.length ? shows[shows.length - 1] : null;
    if (show) {
      if (show.classList.contains('xf-confirm-overlay')) {
        closeConfirm();
      } else if (show.classList.contains('checkin-modal-overlay') && typeof window.closeCheckinModal === 'function') {
        window.closeCheckinModal();
      } else {
        show.classList.remove('show');
      }
      return true;
    }

    var drawer = qs('.right-drawer-overlay');
    if (drawer && !drawer.classList.contains('hidden')) { drawer.classList.add('hidden'); return true; }

    var menu = qs('.context-menu.show');
    if (menu) { menu.classList.remove('show'); return true; }

    return false;
  }

  /* ---------- 2. 聚焦搜索 ---------- */
  function focusSearch() {
    var s = qs('.l2-search');
    if (s) {
      if (typeof s.focus === 'function') s.focus();
      if (typeof s.click === 'function') s.click();
      return;
    }
    var input = qs('input[type="text"], input:not([type])');
    if (input) input.focus();
  }

  /* ---------- 3. 使用帮助 ---------- */
  function showHelp() {
    if (qs('.xf-help-overlay')) return;
    var el = document.createElement('div');
    el.className = 'xf-help-overlay';
    el.setAttribute('role', 'dialog');
    el.setAttribute('aria-modal', 'true');
    el.setAttribute('aria-label', '使用帮助');
    el.innerHTML =
      '<div class="xf-help-panel">' +
        '<div class="xf-help-head"><span>心飞IM 使用帮助</span>' +
          '<button class="xf-help-close" aria-label="关闭帮助">×</button></div>' +
        '<div class="xf-help-body">' +
          '<div class="xf-help-sec"><div class="xf-help-title">全局快捷键</div>' +
            '<ul class="xf-help-list">' +
              '<li><kbd>/</kbd><span>聚焦搜索</span></li>' +
              '<li><kbd>Ctrl</kbd>+<kbd>K</kbd><span>打开命令面板（可跳转模块 / 执行动作）</span></li>' +
              '<li><kbd>Esc</kbd><span>关闭当前弹层</span></li>' +
              '<li><kbd>?</kbd><span>打开 / 关闭本帮助</span></li>' +
            '</ul></div>' +
          '<div class="xf-help-sec"><div class="xf-help-title">聊天窗口</div>' +
            '<ul class="xf-help-list">' +
              '<li><kbd>Enter</kbd><span>发送消息</span></li>' +
              '<li><kbd>Shift</kbd>+<kbd>Enter</kbd><span>换行</span></li>' +
              '<li><kbd>Esc</kbd><span>取消回复 / 编辑</span></li>' +
            '</ul></div>' +
          '<div class="xf-help-sec"><div class="xf-help-title">产品</div>' +
            '<p class="xf-help-note">心飞IM v8.2.0 · 心飞科技 · xinfei.com<br>本页为高保真原型，数据为演示用，非真实业务数据。</p></div>' +
        '</div>' +
      '</div>';
    el.addEventListener('click', function (e) {
      if (e.target === el || (e.target.closest && e.target.closest('.xf-help-close'))) el.remove();
    });
    document.body.appendChild(el);
    var btn = el.querySelector('.xf-help-close');
    if (btn) btn.focus();
  }

  function toggleHelp() {
    var help = qs('.xf-help-overlay');
    if (help) help.remove(); else showHelp();
  }

  /* ---------- 4. 全局确认弹层 ---------- */
  /* 用途：破坏性操作（删除会话/消息/邮件/文件）前的二次确认。
     用法：xfConfirm({title, body, hint, confirmText, cancelText, danger, onConfirm}) */
  function escapeHtml(s) {
    return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
  }

  function closeConfirm() {
    var el = qs('.xf-confirm-overlay');
    if (el) el.remove();
  }

  function xfConfirm(opt) {
    if (!opt) return;
    closeConfirm();
    var el = document.createElement('div');
    el.className = 'xf-confirm-overlay';
    el.setAttribute('role', 'dialog');
    el.setAttribute('aria-modal', 'true');
    el.setAttribute('aria-label', opt.title || '确认操作');
    el.innerHTML =
      '<div class="xf-confirm">' +
        '<div class="xf-confirm-title">' + escapeHtml(opt.title || '确认操作') + '</div>' +
        '<div class="xf-confirm-body">' + escapeHtml(opt.body || '') +
          (opt.hint ? '<span class="xf-confirm-hint">' + escapeHtml(opt.hint) + '</span>' : '') +
        '</div>' +
        '<div class="xf-confirm-actions">' +
          '<button type="button" class="xf-confirm-btn" data-xf="cancel">' + escapeHtml(opt.cancelText || '取消') + '</button>' +
          '<button type="button" class="xf-confirm-btn ' + (opt.danger ? 'danger' : 'primary') + '" data-xf="ok">' + escapeHtml(opt.confirmText || '确定') + '</button>' +
        '</div>' +
      '</div>';
    el.addEventListener('click', function (e) {
      var t = e.target;
      if (!t || !t.closest) return;
      if (t === el) { closeConfirm(); return; }
      var btn = t.closest('.xf-confirm-btn');
      if (!btn) return;
      var ok = btn.getAttribute('data-xf') === 'ok';
      closeConfirm();
      if (ok && typeof opt.onConfirm === 'function') opt.onConfirm();
    });
    document.body.appendChild(el);
    el.classList.add('show');
    var okBtn = el.querySelector('[data-xf="ok"]');
    if (okBtn) okBtn.focus();
  }

  window.xfConfirm = xfConfirm;
  window.xfCloseConfirm = closeConfirm;

  /* ---------- 5. 键盘 ---------- */
  document.addEventListener('keydown', function (e) {
    var t = e.target;
    var tag = (t && t.tagName) || '';
    var typing = tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT' || !!(t && t.isContentEditable);

    if (e.key === 'Escape') { closeTopOverlay(); return; }
    if (typing) return;

    if (e.key === '?') { e.preventDefault(); toggleHelp(); return; }
    if ((e.key === '/' || ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k'))) {
      e.preventDefault();
      if (e.key === '/') focusSearch(); else showPalette();
    }
  });

  /* ---------- 6. 命令面板 ---------- */
  var PALETTE_BASE = [
    { group:'动作', name:'搜索', hint:'/', run:function(){ closePalette(); focusSearch(); } },
    { group:'动作', name:'打开使用帮助', hint:'?', run:function(){ closePalette(); showHelp(); } },
    { group:'动作', name:'重新查看新手指引', run:function(){ closePalette(); showOnboard(); } },
    { group:'模块', name:'消息', href:'dingtalk-mockup.html' },
    { group:'模块', name:'通讯录', href:'dingtalk-contacts.html' },
    { group:'模块', name:'日历', href:'dingtalk-calendar.html' },
    { group:'模块', name:'文档', href:'dingtalk-docs.html' },
    { group:'模块', name:'工作台', href:'dingtalk-workbench.html' },
    { group:'模块', name:'审批', href:'dingtalk-approval.html' },
    { group:'模块', name:'邮件', href:'dingtalk-mail.html' },
    { group:'模块', name:'管理后台', href:'dingtalk-admin.html' }
  ];

  function paletteCommands() {
    var extra = Array.isArray(window.xfPaletteExtra) ? window.xfPaletteExtra : [];
    return PALETTE_BASE.concat(extra);
  }

  function closePalette() {
    var el = qs('.xf-palette-overlay');
    if (el) el.remove();
  }

  function runCommand(c) {
    closePalette();
    if (!c) return;
    if (c.href) { location.href = c.href; return; }
    if (typeof c.run === 'function') c.run();
  }

  function showPalette() {
    closePalette();
    var cmds = paletteCommands();
    var el = document.createElement('div');
    el.className = 'xf-palette-overlay';
    el.setAttribute('role', 'dialog');
    el.setAttribute('aria-modal', 'true');
    el.setAttribute('aria-label', '命令面板');
    el.innerHTML =
      '<div class="xf-palette">' +
        '<div class="xf-palette-input-wrap">' +
          '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>' +
          '<input class="xf-palette-input" type="text" placeholder="输入命令或关键词…" aria-label="命令搜索">' +
        '</div>' +
        '<div class="xf-palette-list" role="listbox"></div>' +
        '<div class="xf-palette-foot"><span>↑↓ 选择</span><span>Enter 执行</span><span>Esc 关闭</span></div>' +
      '</div>';
    var list = el.querySelector('.xf-palette-list');
    var input = el.querySelector('.xf-palette-input');
    var state = { filtered: [], active: 0 };

    function render() {
      var q = input.value.trim().toLowerCase();
      var items = cmds.filter(function (c) {
        if (!q) return true;
        return (c.name || '').toLowerCase().indexOf(q) > -1 || (c.group || '').toLowerCase().indexOf(q) > -1;
      });
      state.filtered = items;
      state.active = 0;
      if (!items.length) { list.innerHTML = '<div class="xf-palette-empty">没有匹配的命令</div>'; return; }
      var html = '', lastGroup = null;
      items.forEach(function (c, i) {
        if (c.group !== lastGroup) { html += '<div class="xf-palette-group">' + escapeHtml(c.group || '其他') + '</div>'; lastGroup = c.group; }
        html += '<div class="xf-palette-item' + (i === 0 ? ' active' : '') + '" role="option" aria-selected="' + (i === 0) + '" data-idx="' + i + '">' +
          '<span class="xf-palette-name">' + escapeHtml(c.name) + '</span>' +
          (c.hint ? '<kbd>' + escapeHtml(c.hint) + '</kbd>' : '') + '</div>';
      });
      list.innerHTML = html;
    }

    function move(delta) {
      if (!state.filtered.length) return;
      state.active = (state.active + delta + state.filtered.length) % state.filtered.length;
      var nodes = list.querySelectorAll('.xf-palette-item');
      for (var i = 0; i < nodes.length; i++) nodes[i].classList.toggle('active', i === state.active);
      var cur = list.querySelector('.xf-palette-item.active');
      if (cur && cur.scrollIntoView) cur.scrollIntoView({ block: 'nearest' });
    }

    el.addEventListener('click', function (e) {
      if (e.target === el) { closePalette(); return; }
      var item = e.target.closest ? e.target.closest('.xf-palette-item') : null;
      if (item) runCommand(state.filtered[parseInt(item.getAttribute('data-idx'), 10)]);
    });
    input.addEventListener('input', render);
    input.addEventListener('keydown', function (e) {
      if (e.key === 'ArrowDown') { e.preventDefault(); move(1); }
      else if (e.key === 'ArrowUp') { e.preventDefault(); move(-1); }
      else if (e.key === 'Enter') { e.preventDefault(); runCommand(state.filtered[state.active]); }
      else if (e.key === 'Escape') { e.preventDefault(); closePalette(); }
    });

    document.body.appendChild(el);
    el.classList.add('show');
    render();
    input.focus();
  }

  window.xfShowPalette = showPalette;
  window.xfClosePalette = closePalette;

  /* ---------- 7. 首用引导 ---------- */
  var ONBOARD_KEY = 'xf_onboard_done_v1';
  var ONBOARD_STEPS = [
    { title:'三栏工作台', body:'左侧深色栏切换模块，中间列表快速定位，右侧主区承载内容。窄屏（手机浏览器）会自动折叠为「列表 → 详情」两级。' },
    { title:'全局快捷键', body:'按 / 直接搜索；按 Ctrl/⌘ + K 打开命令面板，输入即可跳转模块或执行动作；按 ? 随时查看帮助。' },
    { title:'安全与反馈', body:'删除会话、消息、文件等破坏性操作都会二次确认；发送失败可重发，断网出现错误态与重试。' }
  ];

  function onboardDone() {
    try { return localStorage.getItem(ONBOARD_KEY) === '1'; } catch (e) { return false; }
  }

  function setOnboardDone() {
    try { localStorage.setItem(ONBOARD_KEY, '1'); } catch (e) { /* 隐私模式降级 */ }
  }

  function closeOnboard() {
    var el = qs('.xf-onboard-overlay');
    if (el) el.remove();
  }

  function showOnboard() {
    closeOnboard();
    var el = document.createElement('div');
    el.className = 'xf-onboard-overlay';
    el.setAttribute('role', 'dialog');
    el.setAttribute('aria-modal', 'true');
    el.setAttribute('aria-label', '新手指引');
    el.innerHTML = '<div class="xf-onboard">' +
      '<div class="xf-onboard-step"></div>' +
      '<div class="xf-onboard-title"></div>' +
      '<div class="xf-onboard-body"></div>' +
      '<div class="xf-onboard-dots"></div>' +
      '<div class="xf-onboard-actions">' +
        '<button type="button" class="xf-onboard-skip">跳过</button>' +
        '<button type="button" class="xf-onboard-next">下一步</button>' +
      '</div></div>';
    var idx = 0;

    function paint() {
      var s = ONBOARD_STEPS[idx];
      el.querySelector('.xf-onboard-step').textContent = '第 ' + (idx + 1) + ' 步 / 共 ' + ONBOARD_STEPS.length + ' 步';
      el.querySelector('.xf-onboard-title').textContent = s.title;
      el.querySelector('.xf-onboard-body').textContent = s.body;
      var dots = '';
      for (var i = 0; i < ONBOARD_STEPS.length; i++) dots += '<span class="xf-onboard-dot' + (i === idx ? ' active' : '') + '"></span>';
      el.querySelector('.xf-onboard-dots').innerHTML = dots;
      el.querySelector('.xf-onboard-next').textContent = idx === ONBOARD_STEPS.length - 1 ? '开始使用' : '下一步';
    }

    function finish() { setOnboardDone(); closeOnboard(); }

    el.querySelector('.xf-onboard-skip').addEventListener('click', finish);
    el.querySelector('.xf-onboard-next').addEventListener('click', function () {
      if (idx >= ONBOARD_STEPS.length - 1) finish(); else { idx++; paint(); }
    });
    el.addEventListener('click', function (e) { if (e.target === el) finish(); });

    document.body.appendChild(el);
    el.classList.add('show');
    paint();
  }

  window.xfShowOnboard = showOnboard;

  /* ---------- 8. 防重复提交锁 ---------- */
  /* 用法：给提交类按钮加 data-lock="毫秒"，锁定期间重复点击会被丢弃 */
  document.addEventListener('click', function (e) {
    var el = e.target && e.target.closest ? e.target.closest('[data-lock]') : null;
    if (!el) return;
    if (el.__xfLock) {
      e.preventDefault();
      e.stopImmediatePropagation();
      return;
    }
    var ms = parseInt(el.getAttribute('data-lock'), 10);
    if (!ms || ms <= 0) ms = 1200;
    el.__xfLock = true;
    if (typeof el.disabled === 'boolean') el.disabled = true;
    el.classList.add('xf-locking');
    setTimeout(function () {
      el.__xfLock = false;
      if (typeof el.disabled === 'boolean') el.disabled = false;
      el.classList.remove('xf-locking');
    }, ms);
  }, true);

  /* ---------- 9. toast 播报标记 ---------- */
  function isToast(n) {
    if (!n || n.nodeType !== 1) return false;
    var id = (n.id || '').toLowerCase();
    var cls = (typeof n.className === 'string' ? n.className : '').toLowerCase();
    return id.indexOf('toast') > -1 || cls.indexOf('toast') > -1;
  }

  function markNode(n) {
    n.setAttribute('role', 'status');
    n.setAttribute('aria-live', 'polite');
  }

  function markToasts(root) {
    var nodes = (root || document).querySelectorAll('[id$="toast" i], [class$="toast" i]');
    for (var i = 0; i < nodes.length; i++) markNode(nodes[i]);
  }

  function initToasts() {
    markToasts();
    if (window.MutationObserver) {
      var mo = new MutationObserver(function (mutations) {
        for (var i = 0; i < mutations.length; i++) {
          var added = mutations[i].addedNodes;
          for (var j = 0; j < added.length; j++) {
            var n = added[j];
            if (isToast(n)) markNode(n);
            if (n.querySelectorAll) markToasts(n);
          }
        }
      });
      mo.observe(document.body, { childList: true, subtree: true });
    }
  }

  /* ---------- 9. 移动端 / H5 降级 ---------- */
  /* L1 → 底部 Tab；L2/L3(/L4) → 「列表 → 详情」逐级切换。
     结构标记 .xf-l1/.xf-l2/.xf-l3/.xf-l4 自动注入，无需逐页改 HTML。 */
  var LAYOUT_BLOCKS = [];
  var MOBILE_MQ = null;

  function isOverlayCls(cls) {
    return /overlay|modal|toast|context-menu|palette|onboard|help|xf-/.test(cls || '');
  }

  function detectItems(block) {
    var counts = {};
    var all = block.querySelectorAll('*');
    for (var i = 0; i < all.length; i++) {
      var cls = (typeof all[i].className === 'string') ? all[i].className : '';
      var parts = cls.split(' ');
      for (var j = 0; j < parts.length; j++) {
        var p = parts[j];
        if (!p || !/item|node|row|cell/.test(p)) continue;
        if (/^(l2-|sk-|t-arrow|xf-)/.test(p)) continue;
        counts[p] = (counts[p] || 0) + 1;
      }
    }
    var best = null, bestN = 0;
    for (var k in counts) { if (counts[k] > bestN) { bestN = counts[k]; best = k; } }
    block.__xfItems = best;
  }

  function markLayout() {
    var kids = Array.prototype.slice.call(document.body.children);
    var l1 = null;
    var blocks = [];
    for (var i = 0; i < kids.length; i++) {
      var n = kids[i];
      var tag = n.tagName;
      if (tag === 'SCRIPT' || tag === 'SVG' || tag === 'STYLE' || tag === 'LINK' || tag === 'TITLE' || tag === 'META') continue;
      var cls = (typeof n.className === 'string') ? n.className : '';
      if (isOverlayCls(cls)) continue;
      if (tag === 'NAV') { l1 = n; continue; }
      if (!l1) continue;
      blocks.push(n);
    }
    if (l1) l1.classList.add('xf-l1');
    for (var b = 0; b < blocks.length && b < 3; b++) {
      blocks[b].classList.add('xf-l' + (b + 2));
      detectItems(blocks[b]);
    }
    LAYOUT_BLOCKS = blocks;
  }

  function currentDepth() {
    var m = /xf-depth-(\d+)/.exec(document.body.className || '');
    return m ? parseInt(m[1], 10) : 0;
  }

  function setDepth(d) {
    var body = document.body;
    body.classList.remove('xf-depth-0', 'xf-depth-1', 'xf-depth-2');
    body.classList.add('xf-depth-' + d);
    if (d > 0) ensureBackBtn();
  }

  function ensureBackBtn() {
    var btn = qs('.xf-back');
    if (btn) return;
    btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'xf-back';
    btn.setAttribute('aria-label', '返回上一级');
    btn.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"></polyline></svg><span>返回</span>';
    btn.addEventListener('click', function () { setDepth(currentDepth() - 1); });
    document.body.appendChild(btn);
  }

  function onLayoutClick(e) {
    var body = document.body;
    if (!body.classList.contains('xf-mobile')) return;
    var d = currentDepth();
    if (d >= LAYOUT_BLOCKS.length - 1) return;
    var block = LAYOUT_BLOCKS[d];
    if (!block) return;
    var sel = block.__xfItems;
    if (!sel) return;
    var t = e.target;
    if (!t || !t.closest) return;
    var item = t.closest('.' + sel);
    if (!item || !block.contains(item)) return;
    setDepth(d + 1);
  }

  function applyMode() {
    var mobile = !!(MOBILE_MQ && MOBILE_MQ.matches);
    var body = document.body;
    if (mobile) body.classList.add('xf-mobile'); else body.classList.remove('xf-mobile');
    if (!mobile) {
      body.classList.remove('xf-depth-1', 'xf-depth-2');
      body.classList.add('xf-depth-0');
    }
  }

  function initMobileLayout() {
    markLayout();
    document.body.classList.add('xf-depth-0');
    document.addEventListener('click', onLayoutClick);
    if (window.matchMedia) {
      MOBILE_MQ = window.matchMedia('(max-width:768px)');
      applyMode();
      if (MOBILE_MQ.addEventListener) MOBILE_MQ.addEventListener('change', applyMode);
      else if (MOBILE_MQ.addListener) MOBILE_MQ.addListener(applyMode);
    }
  }

  function boot() {
    initToasts();
    initMobileLayout();
    if (!onboardDone()) showOnboard();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
