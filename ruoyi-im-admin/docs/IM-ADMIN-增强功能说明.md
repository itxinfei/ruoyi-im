# IM Admin 后台管理系统 - 增强功能说明

## 概述

本文档说明 ruoyi-im-admin 后台管理系统的增强功能和使用方法。

---

## 一、CSS 增强样式

### 1.1 全局过渡动画

所有元素自动添加平滑过渡效果，提升视觉体验：
- 背景色过渡
- 边框色过渡
- 阴影过渡
- 变换过渡

### 1.2 统计卡片增强

**新增效果：**
- Hover 时上浮动画（translateY -4px）
- Hover 时阴影增强
- 图标旋转缩放效果
- 点击波纹效果
- 数字缩放动画

**使用方式：**
```html
<div class="stat-box stat-box-primary" onclick="filterByStatus('')">
    <div class="stat-number" id="totalCount">0</div>
    <div class="stat-label">用户总数</div>
    <i class="fa fa-users stat-icon"></i>
</div>
```

**可用的颜色类：**
- `stat-box-primary` - 蓝绿色
- `stat-box-success` - 蓝色
- `stat-box-danger` - 红色
- `stat-box-warning` - 橙色

### 1.3 搜索区域折叠功能

当搜索条件超过 4 个时，自动显示折叠按钮。

**使用方式（JS 初始化）：**
```javascript
ImCommon.initSearchCollapse('.search-collapse', {
    collapsed: false,           // 默认是否折叠
    collapseCount: 4,            // 超过多少个搜索条件时显示折叠按钮
    collapseText: '收起搜索',   // 折叠按钮文本
    expandText: '展开搜索'      // 展开按钮文本
});
```

**手动切换：**
```javascript
ImCommon.toggleSearchCollapse('.search-collapse');
```

### 1.4 批量操作进度条

**显示进度：**
```javascript
ImCommon.BatchProgress.show('#toolbar', {
    total: 100,
    current: 0,
    text: '处理中...'
});
```

**更新进度：**
```javascript
ImCommon.BatchProgress.update('#toolbar', current, total, '处理中...');
```

**完成进度：**
```javascript
ImCommon.BatchProgress.complete('#toolbar', '处理完成');
```

**隐藏进度：**
```javascript
ImCommon.BatchProgress.hide('#toolbar');
```

### 1.5 选中计数提示

自动显示"已选中 X 项"提示条，启用/禁用批量按钮。

**使用方式：**
```javascript
ImCommon.updateSelectedInfo('#bootstrap-table', {
    targetId: '.select-table',
    infoId: '#selectedInfo',
    multipleClass: '.multiple',
    singleClass: '.single'
});
```

### 1.6 Toast 消息提示

**显示 Toast：**
```javascript
ImCommon.toast('操作成功', 'success', 3000);
ImCommon.toast('操作失败', 'error', 3000);
ImCommon.toast('注意', 'warning', 3000);
ImCommon.toast('提示', 'info', 3000);
```

### 1.7 快捷键提示样式

在按钮旁显示快捷键提示：
```html
<a class="btn btn-primary" onclick="doSearch()">
    搜索 <span class="keyboard-hint">Ctrl+F</span>
</a>
```

---

## 二、JavaScript 增强功能

### 2.1 页面初始化

一键初始化所有增强功能：
```javascript
$(function() {
    ImCommon.initPage({
        searchCollapse: true,        // 启用搜索折叠
        keyboardShortcuts: true,     // 启用快捷键
        tableId: '#bootstrap-table'  // 表格ID
    });
});
```

### 2.2 快捷键系统

**默认快捷键：**
| 快捷键 | 功能 |
|--------|------|
| F1 | 显示快捷键帮助 |
| Ctrl+F | 聚焦搜索框 |
| Ctrl+R | 刷新表格 |
| Ctrl+A | 全选当前页 |
| ESC | 取消选择/关闭帮助 |

**注册自定义快捷键：**
```javascript
ImCommon.KeyboardShortcuts.register('ctrl+s', function() {
    // 保存操作
}, '保存');
```

**显示帮助面板：**
```javascript
ImCommon.KeyboardShortcuts.showHelp();
```

### 2.3 数字滚动动画

**单个数字动画：**
```javascript
ImCommon.animateNumber('#totalCount', 100, 1000);
```

**批量数字动画：**
```javascript
ImCommon.animateStatistics({
    '#totalCount': 100,
    '#onlineCount': 50,
    '#offlineCount': 50
}, 800);
```

### 2.4 统计数据缓存加载

```javascript
ImCommon.loadStatisticsWithCache(
    '/api/statistics',           // 请求URL
    function(data) {             // 更新UI回调
        $('#count').text(data.count);
    },
    'cache_key',                 // 缓存键名
    5                            // 缓存时长（分钟）
);
```

### 2.5 表格列配置管理

**保存配置：**
```javascript
ImCommon.saveColumnConfig('table_id', columns, { pageSize: 20 });
```

**获取配置：**
```javascript
var config = ImCommon.getColumnConfig('table_id');
```

### 2.6 表格高级筛选

```javascript
// 添加筛选条件
ImCommon.TableFilter.add('table_id', 'status', '=', '1');

// 获取筛选条件
var filters = ImCommon.TableFilter.get('table_id');

// 清除筛选
ImCommon.TableFilter.clear('table_id');

// 构建查询参数
var params = ImCommon.TableFilter.buildParams('table_id');
```

### 2.7 增强的数据导出

```javascript
ImCommon.exportData({
    url: '/api/export',
    tableId: '#bootstrap-table',
    fileName: '用户数据',
    fileType: 'excel',
    selectedOnly: true,
    showProgress: true,
    onComplete: function(success) {
        console.log('导出完成:', success);
    }
});
```

---

## 三、示例页面

### 3.1 用户管理页面

**文件位置：** `templates/im/user/user.html`

**增强功能：**
- 统计卡片点击筛选
- 数字滚动动画
- 快捷键支持
- 批量操作进度条
- Toast 消息提示

**关键代码：**
```javascript
$(function() {
    // 初始化页面增强功能
    ImCommon.initPage({
        searchCollapse: true,
        keyboardShortcuts: true,
        tableId: '#bootstrap-table'
    });

    loadStatistics();
});

// 加载统计数据（带动画）
function loadStatistics() {
    ImCommon.loadStatisticsWithCache(
        prefix + "/statistics",
        function(data) {
            ImCommon.animateStatistics({
                '#totalCount': data.totalCount || 0,
                '#onlineCount': data.onlineCount || 0,
                '#offlineCount': data.offlineCount || 0,
                '#disabledCount': data.disabledCount || 0
            }, 800);
        },
        'user_statistics',
        5
    );
}

// 统计卡片点击筛选
function filterByStatus(status) {
    $('select[name="status"]').val(status);
    doSearch();
}

// 批量操作（带进度）
function executeBatchUpdate(rows, newStatus, statusText) {
    ImCommon.BatchProgress.show('#toolbar', {
        total: rows.length,
        current: 0,
        text: '正在批量' + statusText + '...'
    });

    $.ajax({
        // ... AJAX 请求
        success: function(result) {
            if (result.code == 0) {
                ImCommon.BatchProgress.complete('#toolbar', '批量' + statusText + '成功');
                ImCommon.toast("批量" + statusText + "成功，共 " + rows.length + " 个用户", 'success');
                $.table.refresh();
                loadStatistics();
            }
        }
    });
}
```

### 3.2 审计日志页面

**文件位置：** `templates/im/auditLog/auditLog.html`

**增强功能：**
- 统计卡片点击筛选（成功/失败/今日）
- 搜索条件折叠
- 清理操作进度提示

---

## 四、应用到其他页面

### 4.1 最小配置

在页面 `<script>` 标签内添加：

```javascript
$(function() {
    // 初始化页面增强功能
    ImCommon.initPage();
});
```

### 4.2 完整配置

```html
<!DOCTYPE html>
<html lang="zh" xmlns:th="http://www.thymeleaf.org">
<head>
    <th:block th:include="include :: header('页面标题')" />
    <link th:href="@{/css/im-admin.css}" rel="stylesheet"/>
</head>
<body class="gray-bg">
    <div class="container-div">
        <!-- 统计卡片（可选） -->
        <div class="col-sm-12">
            <div class="row">
                <div class="col-sm-3">
                    <div class="stat-box stat-box-primary">
                        <div class="stat-number" id="totalCount">0</div>
                        <div class="stat-label">总数</div>
                        <i class="fa fa-list stat-icon"></i>
                    </div>
                </div>
                <!-- 更多卡片... -->
            </div>
        </div>

        <!-- 搜索区域 -->
        <div class="col-sm-12 search-collapse">
            <form id="form-id">
                <div class="select-list">
                    <ul>
                        <li>
                            <label>字段名：</label>
                            <input type="text" name="fieldName" placeholder="请输入"/>
                        </li>
                        <!-- 更多搜索条件... -->
                        <li>
                            <a class="btn btn-primary btn-rounded btn-sm" onclick="doSearch()">
                                <i class="fa fa-search"></i>&nbsp;搜索 <span class="keyboard-hint">Ctrl+F</span>
                            </a>
                            <a class="btn btn-warning btn-rounded btn-sm" onclick="clearSearch()">
                                <i class="fa fa-refresh"></i>&nbsp;重置
                            </a>
                        </li>
                    </ul>
                </div>
            </form>
        </div>

        <!-- 数据表格 -->
        <div class="col-sm-12 select-table table-striped">
            <div class="btn-group-sm" id="toolbar">
                <a class="btn btn-success" onclick="$.operate.add()">
                    <i class="fa fa-plus"></i> 新增
                </a>
                <!-- 更多按钮... -->
            </div>
            <table id="bootstrap-table"></table>
        </div>
    </div>

    <th:block th:include="include :: footer" />
    <script th:src="@{/js/im-common.js}"></script>
    <script th:inline="javascript">
        var prefix = ctx + "im/module";

        $(function() {
            // 初始化页面增强功能
            ImCommon.initPage({
                searchCollapse: true,
                keyboardShortcuts: true,
                tableId: '#bootstrap-table'
            });

            var options = {
                url: prefix + "/list",
                // ... 表格配置
            };
            $.table.init(options);
        });

        function doSearch() {
            ImCommon.showSearchConditions('#form-id', '#searchStatus', 'clearSearch()');
            $.table.search();
        }

        function clearSearch() {
            $.form.reset();
            $('#searchStatus').hide();
            $.table.search();
        }
    </script>
</body>
</html>
```

---

## 五、注意事项

### 5.1 浏览器兼容性

- Chrome/Edge 90+
- Firefox 88+
- Safari 14+

### 5.2 性能优化

1. **全局过渡动画**：表格内部元素已禁用过渡，不影响大数据量表格性能
2. **统计缓存**：统计数据自动缓存 5 分钟，减少服务器请求
3. **动画时长**：默认动画时长为 800ms，可根据需要调整

### 5.3 自定义样式

如需覆盖默认样式，请在页面级 `<style>` 标签中添加：

```css
/* 覆盖统计卡片颜色 */
.stat-box.stat-box-custom {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 覆盖按钮 hover 效果 */
#toolbar .btn:hover {
    transform: none;
    box-shadow: none;
}
```

---

## 六、API 参考

### ImCommon.* 方法列表

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `initPage(options)` | 配置对象 | void | 初始化页面增强功能 |
| `toast(message, type, duration)` | 消息, 类型, 时长 | void | 显示 Toast 消息 |
| `animateNumber(element, target, duration)` | 元素, 目标值, 时长 | void | 数字滚动动画 |
| `animateStatistics(data, duration)` | 数据对象, 时长 | void | 批量数字动画 |
| `initSearchCollapse(container, options)` | 容器, 配置 | void | 初始化搜索折叠 |
| `toggleSearchCollapse(container)` | 容器 | void | 切换搜索折叠状态 |
| `updateSelectedInfo(tableId, options)` | 表格ID, 配置 | void | 更新选中信息 |
| `loadStatisticsWithCache(url, callback, key, duration)` | URL, 回调, 键名, 时长 | void | 缓存加载统计 |

### ImCommon.BatchProgress.* 方法列表

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `show(target, options)` | 目标, 配置 | void | 显示进度条 |
| `update(target, current, total, text)` | 目标, 当前进度, 总数, 文本 | void | 更新进度 |
| `hide(target)` | 目标 | void | 隐藏进度条 |
| `complete(target, text)` | 目标, 完成文本 | void | 完成进度 |

### ImCommon.KeyboardShortcuts.* 方法列表

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `register(key, handler, description)` | 快捷键, 处理函数, 描述 | void | 注册快捷键 |
| `init()` | 无 | void | 初始化快捷键监听 |
| `showHelp()` | 无 | void | 显示帮助面板 |
| `hideHelp()` | 无 | void | 隐藏帮助面板 |
| `enable()` | 无 | void | 启用快捷键 |
| `disable()` | 无 | void | 禁用快捷键 |

---

## 更新日志

### v1.0.0 (2024-01-21)

**新增功能：**
- 全局过渡动画系统
- 统计卡片 hover 动画和点击交互
- 搜索区域折叠/展开功能
- 批量操作进度条
- 选中计数提示
- Toast 消息提示
- 快捷键系统（F1 帮助、Ctrl+F 搜索、Ctrl+R 刷新等）
- 数字滚动动画
- 表格列配置管理增强
- 高级筛选功能
- 增强的数据导出

**优化：**
- 统计数据缓存机制
- 空状态显示优化
- 表格行 hover 效果
- 下拉菜单动画效果
- 响应式布局优化

---

## 技术支持

如有问题或建议，请联系开发团队。
