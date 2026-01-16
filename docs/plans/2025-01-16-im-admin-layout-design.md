# IM管理后台布局优化设计

## 设计目标

优化IM管理后台页面的视觉层次，提升用户操作体验。

## 页面结构

### 三区域布局

```
┌─────────────────────────────────────────────┐
│  📊 统计卡片（数据概览）                    │
└─────────────────────────────────────────────┘
                    ↓ 10px
┌─────────────────────────────────────────────┐
│  🔍 搜索区（筛选条件）                      │
└─────────────────────────────────────────────┘
                    ↓ 10px
┌─────────────────────────────────────────────┐
│  📋 数据管理区                              │
│  [+ 新增] [✏️ 修改] [🗑️ 删除]               │
│  ─────────────────────────────────────────  │
│  │ 数据表格...                             │
└─────────────────────────────────────────────┘
```

### 视觉层次

| 区域 | 作用 | 视觉特征 |
|------|------|----------|
| 统计卡片 | 数据概览 | 渐变彩色背景 |
| 搜索区 | 筛选条件 | 白色卡片，若依原生样式 |
| 数据管理区 | 数据操作+展示 | 白色卡片，工具栏浅灰背景 |

## 样式规范

### 统计卡片区域

```css
.statistics-section {
    margin-bottom: 10px;
}
```

### 搜索区域

使用若依原生 `.search-collapse` 样式，添加底部间距：

```css
.search-collapse {
    margin-bottom: 10px;
}
```

### 数据管理区

```css
/* 数据管理区卡片容器 */
.data-management-card {
    background: #fff;
    border-radius: 6px;
    box-shadow: 1px 1px 3px rgba(0,0,0,.2);
    overflow: hidden;
}

/* 工具栏 - 浅灰背景 */
.data-toolbar {
    padding: 12px 15px;
    background: #fafafa;
    border-bottom: 1px solid #e5e5e5;
}

/* 表格区域 */
.data-table {
    padding: 0;
}
```

### 统计卡片样式（保持现有）

```css
.statistics-card {
    padding: 15px;
    border-radius: 4px;
    color: #fff;
    position: relative;
    overflow: hidden;
}
.stat-number {
    font-size: 24px;
    font-weight: bold;
}
.stat-label {
    font-size: 12px;
    opacity: 0.9;
}
.stat-icon {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 40px;
    opacity: 0.2;
}
```

## HTML结构

```html
<div class="container-div">
    <div class="row">
        <!-- 1. 统计卡片区域 -->
        <div class="col-sm-12 statistics-section">
            <div class="row">
                <div class="col-sm-3">
                    <div class="statistics-card stat-total">
                        <div class="stat-number" id="totalCount">0</div>
                        <div class="stat-label">总数</div>
                        <i class="fa fa-users stat-icon"></i>
                    </div>
                </div>
                <!-- 其他3个卡片 -->
            </div>
        </div>

        <!-- 2. 搜索区域 -->
        <div class="col-sm-12 search-collapse">
            <form id="xxx-form">
                <div class="select-list">
                    <ul>
                        <li>搜索条件：<input type="text" name="xxx"/></li>
                        <li>
                            <a class="btn btn-primary btn-rounded btn-sm" onclick="$.table.search()">
                                <i class="fa fa-search"></i>&nbsp;搜索
                            </a>
                            <a class="btn btn-warning btn-rounded btn-sm" onclick="$.form.reset()">
                                <i class="fa fa-refresh"></i>&nbsp;重置
                            </a>
                        </li>
                    </ul>
                </div>
            </form>
        </div>

        <!-- 3. 数据管理区 -->
        <div class="col-sm-12 data-management-card">
            <div class="data-toolbar">
                <div class="btn-group-sm" id="toolbar" role="group">
                    <a class="btn btn-success" onclick="$.operate.add()">
                        <i class="fa fa-plus"></i> 新增
                    </a>
                    <a class="btn btn-primary single disabled" onclick="$.operate.edit()">
                        <i class="fa fa-edit"></i> 修改
                    </a>
                    <a class="btn btn-danger multiple disabled" onclick="$.operate.removeAll()">
                        <i class="fa fa-remove"></i> 删除
                    </a>
                </div>
            </div>
            <div class="data-table">
                <table id="bootstrap-table" data-mobile-responsive="true"></table>
            </div>
        </div>
    </div>
</div>
```

## 响应式处理

| 屏幕尺寸 | 统计卡片 | 搜索区 | 数据管理区 |
|----------|----------|--------|------------|
| ≥768px | 4个一行 | 正常显示 | 正常显示 |
| <768px | 4个一行 | 隐藏 | 正常显示 |

## 涉及页面

- user/user.html - 用户管理
- group/group.html - 群组管理
- friend/friend.html - 好友管理
- session/session.html - 会话管理
- message/message.html - 消息管理
- file/file.html - 文件管理
- member/member.html - 群组成员管理
- conversation/conversation.html - 会话列表
- announcement/announcement.html - 公告管理
- approval/approval.html - 审批管理
- application/application.html - 应用管理
- email/email.html - 邮箱管理
- ding/ding.html - 钉钉管理

## 实施步骤

1. 在每个页面的 `<style>` 标签中添加新的 CSS 样式
2. 调整 HTML 结构，将工具栏和表格包装在 `data-management-card` 中
3. 验证视觉效果和交互功能
