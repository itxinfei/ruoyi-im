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
    '.checkin-modal-overlay.show, .xf-confirm-overlay.show, ' +
    '.xf-settings-overlay.show, .xf-palette-overlay.show, .xf-onboard-overlay.show';

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
      } else if (show.classList.contains('xf-settings-overlay')) {
        closeSettings();
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

  /* ---------- 4b. 全局设置弹层（L1 底部「设置」入口） ---------- */
  /* 用途：打开「个人设置」，含消息通知 / 隐私与安全 / 通用 / 快捷键 / 关于。
     与 xfConfirm 一致：动态注入、点遮罩关闭、Esc 关闭、自动聚焦。 */
  var XF_SETTINGS_KEY = 'xf_settings_tab_v1';
  var XF_THEME_KEY = 'xf_theme_color_v1';
  var XF_SETTINGS_THEME = [
    { name:'品牌蓝', color:'#277EFB', light:'#E6F2FF', hover:'#1169E0', active:'#0656C6' },
    { name:'活力橙', color:'#FF7D00', light:'#FFF7E6', hover:'#E06E00', active:'#C25F00' },
    { name:'安心绿', color:'#00B42A', light:'#F6FFED', hover:'#009E24', active:'#00861F' },
    { name:'绛红', color:'#F53F3F', light:'#FFF1F0', hover:'#D93838', active:'#BF3131' },
    { name:'烟雨紫', color:'#6C5CE7', light:'#F5F3FF', hover:'#5A4BD1', active:'#4B3EB8' }
  ];
  var XF_SETTINGS_NAV = [
    { key:'profile', name:'个人设置' },
    { key:'notify', name:'消息通知' },
    { key:'privacy', name:'隐私与安全' },
    { key:'general', name:'通用' },
    { key:'shortcut', name:'快捷键' },
    { key:'about', name:'关于' }
  ];

  function xfSettingsTabName(key) {
    for (var i = 0; i < XF_SETTINGS_NAV.length; i++) {
      if (XF_SETTINGS_NAV[i].key === key) return XF_SETTINGS_NAV[i].name;
    }
    return '设置';
  }

  function xfSettingsRow(title, desc, opt) {
    opt = opt || {};
    var right = '';
    if (opt.kind === 'switch') {
      right = '<button type="button" class="xf-settings-switch' + (opt.on ? ' on' : '') + '" role="switch" aria-checked="' + (opt.on ? 'true' : 'false') + '" aria-label="' + escapeHtml(title) + '"></button>';
    } else if (opt.kind === 'text') {
      right = '<span class="xf-settings-value">' + escapeHtml(opt.value || '') + '</span>';
    } else if (opt.kind === 'action') {
      right = '<span class="xf-settings-more">›</span>';
    }
    return '<div class="xf-settings-row"><div><div class="xf-settings-row-title">' + escapeHtml(title) + '</div>' +
      (desc ? '<div class="xf-settings-row-desc">' + escapeHtml(desc) + '</div>' : '') +
      '</div>' + right + '</div>';
  }

  function xfSettingsThemeDots() {
    var cur = '';
    try { cur = (localStorage.getItem(XF_THEME_KEY) || '').split('|')[0]; } catch (err) { cur = ''; }
    if (!cur) cur = XF_SETTINGS_THEME[0].color;
    var html = '';
    XF_SETTINGS_THEME.forEach(function (t) {
      var on = t.color === cur;
      html += '<button type="button" class="xf-settings-theme-dot' + (on ? ' active' : '') + '"' +
        ' data-color="' + t.color + '" data-light="' + t.light + '"' +
        ' data-hover="' + t.hover + '" data-active="' + t.active + '"' +
        ' style="background:' + t.color + (on ? ';border-color:' + t.active : '') + '" aria-label="' + escapeHtml(t.name) + '" title="' + escapeHtml(t.name) + '"></button>';
    });
    return html;
  }

  function xfSettingsPane(key) {
    if (key === 'notify') {
      return xfSettingsRow('新消息通知', '开启后收到系统通知横幅和声音', { kind:'switch', on:true }) +
        xfSettingsRow('通知显示消息详情', '在通知横幅中显示消息内容预览', { kind:'switch', on:true }) +
        xfSettingsRow('声音', '收到消息时播放提示音', { kind:'switch', on:false }) +
        xfSettingsRow('免打扰时段', '时段内仅静音，未读角标仍然保留', { kind:'text', value:'22:00 - 08:00' });
    }
    if (key === 'privacy') {
      return xfSettingsRow('展示在线状态', '关闭后他人看不到你的在线状态', { kind:'switch', on:true }) +
        xfSettingsRow('接收已读回执', '开启后对方可看到你已读消息', { kind:'switch', on:true }) +
        xfSettingsRow('禁止陌生人私信', '开启后仅通讯录联系人可发起会话', { kind:'switch', on:false }) +
        xfSettingsRow('屏蔽列表', '已屏蔽的账号不会再收到其消息', { kind:'text', value:'0 人' });
    }
    if (key === 'general') {
      return '<div class="xf-settings-sec-title">主题色</div>' +
        '<div class="xf-settings-theme-dots">' + xfSettingsThemeDots() + '</div>' +
        '<div class="xf-settings-gap"></div>' +
        xfSettingsRow('多语言', '设置界面与消息翻译的默认语言', { kind:'text', value:'简体中文' }) +
        xfSettingsRow('会话列表密度', '调整会话项的显示密度', { kind:'text', value:'标准' });
    }
    if (key === 'shortcut') {
      return '<ul class="xf-help-list">' +
        '<li><span>聚焦搜索</span><kbd>/</kbd></li>' +
        '<li><span>打开命令面板</span><span><kbd>Ctrl</kbd> + <kbd>K</kbd></span></li>' +
        '<li><span>打开使用帮助</span><kbd>?</kbd></li>' +
        '<li><span>发送消息</span><kbd>Enter</kbd></li>' +
        '<li><span>换行</span><span><kbd>Ctrl</kbd> + <kbd>Enter</kbd></span></li>' +
        '<li><span>关闭当前弹层</span><kbd>Esc</kbd></li>' +
        '</ul>';
    }
    if (key === 'about') {
      return xfSettingsRow('产品版本', '当前为高保真原型版本', { kind:'text', value:'v8.2.0' }) +
        xfSettingsRow('开发商', '', { kind:'text', value:'心飞科技' }) +
        xfSettingsRow('官方网站', '', { kind:'text', value:'xinfei.com' }) +
        xfSettingsRow('用户协议与隐私政策', '', { kind:'action' }) +
        '<p class="xf-settings-note">心飞IM 高保真原型 · 数据为演示用，非真实业务数据。</p>';
    }
    /* 个人设置（默认） */
    return '<div class="xf-settings-profile">' +
        '<div class="xf-settings-avatar">飞</div>' +
        '<div><div class="xf-settings-name">架构师</div>' +
        '<div class="xf-settings-sub">产品设计部 · architect@xinfei.com</div></div>' +
      '</div>' +
      '<div class="xf-settings-sec-title">账号信息</div>' +
      xfSettingsRow('昵称', '', { kind:'text', value:'架构师' }) +
      xfSettingsRow('工号', '', { kind:'text', value:'XF10086' }) +
      xfSettingsRow('部门', '', { kind:'text', value:'产品设计部' }) +
      xfSettingsRow('邮箱', '', { kind:'text', value:'architect@xinfei.com' }) +
      '<div class="xf-settings-gap"></div>' +
      '<button type="button" class="xf-settings-logout">退出登录</button>';
  }

  function xfApplyTheme(color, light, hover, active) {
    if (!color) return;
    var root = document.documentElement.style;
    root.setProperty('--dt-brand', color);
    if (light) root.setProperty('--dt-brand-light', light);
    if (hover) root.setProperty('--dt-brand-hover', hover);
    if (active) root.setProperty('--dt-brand-active', active);
    try { localStorage.setItem(XF_THEME_KEY, [color, light || '', hover || '', active || ''].join('|')); } catch (err) {}
  }

  function xfRestoreTheme() {
    try {
      var v = localStorage.getItem(XF_THEME_KEY);
      if (!v) return;
      var p = v.split('|');
      if (!p[0]) return;
      xfApplyTheme(p[0], p[1], p[2], p[3]);
    } catch (err) {}
  }

  function closeSettings() {
    var el = qs('.xf-settings-overlay');
    if (el) el.remove();
  }

  /* 轻量 toast：优先复用页面已定义的 window.showToast，否则用内置浮层 */
  function xfToast(msg) {
    if (typeof window.showToast === 'function') { window.showToast(msg); return; }
    var old = document.getElementById('xfToast');
    if (old && old.parentNode) old.parentNode.removeChild(old);
    var t = document.createElement('div');
    t.id = 'xfToast';
    t.setAttribute('role', 'status');
    t.textContent = msg;
    t.style.cssText = 'position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,0.82);color:#fff;padding:8px 20px;border-radius:6px;font-size:13px;line-height:1.4;z-index:99999;opacity:0;transition:opacity .2s;pointer-events:none;font-family:inherit';
    document.body.appendChild(t);
    requestAnimationFrame(function () { t.style.opacity = '1'; });
    setTimeout(function () {
      t.style.opacity = '0';
      setTimeout(function () { if (t.parentNode) t.parentNode.removeChild(t); }, 200);
    }, 1500);
  }

  /* 退出登录（演示）：供各页「退出登录」按钮复用 */
  function xfSettingsLogout() {
    closeSettings();
    xfToast('已退出登录（演示）');
  }

  function xfSettings(tab) {
    closeSettings();
    var key = '';
    try { key = localStorage.getItem(XF_SETTINGS_KEY) || ''; } catch (err) { key = ''; }
    if (tab && XF_SETTINGS_NAV.some(function (n) { return n.key === tab; })) {
      key = tab;
    } else if (!XF_SETTINGS_NAV.some(function (n) { return n.key === key; })) {
      key = 'profile';
    }
    var tab = key;

    var el = document.createElement('div');
    el.className = 'xf-settings-overlay';
    el.setAttribute('role', 'dialog');
    el.setAttribute('aria-modal', 'true');
    el.setAttribute('aria-label', '设置');

    var navHtml = '';
    XF_SETTINGS_NAV.forEach(function (n) {
      navHtml += '<button type="button" class="xf-settings-nav-item' + (n.key === tab ? ' active' : '') + '"' +
        ' role="tab" aria-selected="' + (n.key === tab ? 'true' : 'false') + '" data-xf-tab="' + n.key + '">' + escapeHtml(n.name) + '</button>';
    });

    el.innerHTML =
      '<div class="xf-settings">' +
        '<div class="xf-settings-aside">' +
          '<div class="xf-settings-aside-title">设置</div>' +
          '<div class="xf-settings-nav" role="tablist">' + navHtml + '</div>' +
        '</div>' +
        '<div class="xf-settings-content">' +
          '<div class="xf-settings-content-head">' +
            '<span class="xf-settings-title">' + escapeHtml(xfSettingsTabName(tab)) + '</span>' +
            '<button type="button" class="xf-settings-close" aria-label="关闭设置">×</button>' +
          '</div>' +
          '<div class="xf-settings-body"></div>' +
        '</div>' +
      '</div>';

    function render(key) {
      var body = el.querySelector('.xf-settings-body');
      var title = el.querySelector('.xf-settings-title');
      body.innerHTML = xfSettingsPane(key);
      if (title) title.textContent = xfSettingsTabName(key);
      Array.prototype.forEach.call(el.querySelectorAll('.xf-settings-nav-item'), function (item) {
        var on = item.getAttribute('data-xf-tab') === key;
        item.classList.toggle('active', on);
        item.setAttribute('aria-selected', on ? 'true' : 'false');
      });
      body.scrollTop = 0;
    }

    el.addEventListener('click', function (e) {
      var t = e.target;
      if (!t || !t.closest) return;
      if (t === el) { closeSettings(); return; }
      if (t.closest('.xf-settings-close')) { closeSettings(); return; }
      var nav = t.closest('.xf-settings-nav-item');
      if (nav) {
        var key = nav.getAttribute('data-xf-tab');
        try { localStorage.setItem(XF_SETTINGS_KEY, key); } catch (err) {}
        render(key);
        return;
      }
      var sw = t.closest('.xf-settings-switch');
      if (sw) {
        sw.setAttribute('aria-checked', sw.classList.toggle('on') ? 'true' : 'false');
        return;
      }
      var dot = t.closest('.xf-settings-theme-dot');
      if (dot) {
        Array.prototype.forEach.call(el.querySelectorAll('.xf-settings-theme-dot'), function (d) { d.classList.remove('active'); });
        dot.classList.add('active');
        xfApplyTheme(dot.getAttribute('data-color'), dot.getAttribute('data-light'), dot.getAttribute('data-hover'), dot.getAttribute('data-active'));
        return;
      }
      if (t.closest('.xf-settings-logout')) { xfSettingsLogout(); }
    });

    document.body.appendChild(el);
    el.classList.add('show');
    render(tab);
    var closeBtn = el.querySelector('.xf-settings-close');
    if (closeBtn) closeBtn.focus();
  }

  window.xfSettings = xfSettings;
  window.xfCloseSettings = closeSettings;
  window.xfSettingsLogout = xfSettingsLogout;
  window.xfToast = xfToast;

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
    { group:'动作', name:'个人设置', run:function(){ closePalette(); xfSettings(); } },
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
    { title:'三栏工作台', body:'左侧品牌蓝栏切换模块，中间列表快速定位，右侧主区承载内容。窄屏（手机浏览器）会自动折叠为「列表 → 详情」两级。' },
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
    return /overlay|modal|toast|context-menu|palette|onboard|help|mask|xf-/.test(cls || '');
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
        /* mg-cell 是日历月视图日期格（数量最多），自身有 selectDate 行为，不可作为下钻项 */
        if (/^(l2-|sk-|t-arrow|xf-|mg-cell)/.test(p)) continue;
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
    /* 命中自身带交互的元素（onclick/链接/表单控件）时，优先执行其自身行为，不下钻 */
    if (t.closest('[onclick],a[href],button,input,select,textarea,label')) return;
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
    /* 捕获阶段监听：目标元素的自带 onclick（如日历 selectDate）会重渲染列表、
       把被点击节点移出 DOM，冒泡阶段再校验 block.contains(item) 会失败。 */
    document.addEventListener('click', onLayoutClick, true);
    if (window.matchMedia) {
      MOBILE_MQ = window.matchMedia('(max-width:768px)');
      applyMode();
      if (MOBILE_MQ.addEventListener) MOBILE_MQ.addEventListener('change', applyMode);
      else if (MOBILE_MQ.addListener) MOBILE_MQ.addListener(applyMode);
    }
  }

  /* ============================================================
     可点击元素键盘可达（doc12 §13 无障碍基线）
     原型大量交互用 div + onclick 实现，需补齐 role / tabindex，
     并支持 Enter / Space 触发，保证键盘可完整走查原型。
     ============================================================ */
  var A11Y_CLICKABLE =
    '.l1-item,.l1-avatar,.nav-item,.nav-btn,.tab-item,.sess-item,.file-row,' +
    '.ctx-item,.mm-item,.more-menu-item,.l2-action-item,.toggle-switch,.mg-cell,' +
    '.a-node,.t-node,.g-node,.h5-plus-item,.rp-member,.search-modal-card,' +
    '.search-modal-file,.search-modal-msg,.ct-card,.msg-avatar,.crumb,.sortable,' +
    '.am-file-upload,' +
    /* 无类名的行内可点击块（如通讯录快捷入口、后台统计卡片） */
    'div[onclick]:not([class])';

  function a11yRoleFor(el) {
    var cls = (typeof el.className === 'string' ? el.className : '').trim();
    if (/^toggle-switch\b/.test(cls)) return 'switch';
    if (/^tab-item\b/.test(cls)) return 'tab';
    if (/^(a-node|t-node|g-node)\b/.test(cls)) return 'treeitem';
    return 'button';
  }

  function xfEnhanceClickable(el) {
    if (!el || el.hasAttribute('role') || el.hasAttribute('tabindex')) return;
    var role = a11yRoleFor(el);
    el.setAttribute('role', role);
    el.setAttribute('tabindex', '0');
    if (role === 'switch') {
      el.setAttribute('aria-checked', /(^|\s)on(\s|$)/.test(el.className) ? 'true' : 'false');
    } else if (role === 'tab') {
      el.setAttribute('aria-selected', /(^|\s)active(\s|$)/.test(el.className) ? 'true' : 'false');
    }
    if (!el.hasAttribute('aria-label') && el.hasAttribute('title')) {
      el.setAttribute('aria-label', el.getAttribute('title'));
    }
    el.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' || e.key === ' ' || e.key === 'Spacebar') {
        e.preventDefault();
        this.click();
      }
    });
  }

  function initA11y() {
    var nodes = document.querySelectorAll(A11Y_CLICKABLE);
    for (var i = 0; i < nodes.length; i++) xfEnhanceClickable(nodes[i]);
    /* 弹层内动态插入的可点击项同样需要增强 */
    if (window.MutationObserver) {
      new MutationObserver(function (muts) {
        for (var i = 0; i < muts.length; i++) {
          var added = muts[i].addedNodes;
          for (var j = 0; j < added.length; j++) {
            var n = added[j];
            if (!n || n.nodeType !== 1) continue;
            if (n.matches && n.matches(A11Y_CLICKABLE)) xfEnhanceClickable(n);
            if (n.querySelectorAll) {
              var inner = n.querySelectorAll(A11Y_CLICKABLE);
              for (var k = 0; k < inner.length; k++) xfEnhanceClickable(inner[k]);
            }
          }
        }
      }).observe(document.body, { childList: true, subtree: true });
    }
  }

  function boot() {
    xfRestoreTheme();
    initToasts();
    initMobileLayout();
    initA11y();
    if (!onboardDone()) showOnboard();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
