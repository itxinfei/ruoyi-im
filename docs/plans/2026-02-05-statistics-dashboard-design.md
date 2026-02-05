# RuoYi-IM 数据统计看板设计文档

> **版本**: v1.0  
> **日期**: 2026-02-05  
> **作者**: AI 辅助设计  
> **状态**: 待实施

---

## 一、概述

### 1.1 目标
为 RuoYi-IM 管理后台添加综合数据统计看板，支持实时数据展示、历史趋势分析、数据钻取和导出功能。

### 1.2 核心功能
- 实时概览：在线用户、今日消息、用户总数、群组总数
- 趋势分析：用户增长、消息量变化、群组发展趋势
- 分布统计：消息类型分布、群组规模分布、活跃时段分析
- 数据钻取：点击指标可查看详细数据
- 数据导出：支持 Excel 格式导出

### 1.3 时间范围
- 实时数据：当前实时状态
- 预设时段：今日、近7天、近30天
- 自定义区间：管理员可任意选择日期范围

---

## 二、架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                   前端展示层                            │
│  统计卡片 │ 图表组件 │ 时间选择器 │ 钻取弹窗 │ 导出     │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │ WebSocket / REST API
┌─────────────────────────────────────────────────────────┐
│                   服务层                                │
│  StatisticsService:                                     │
│  - 实时统计 (Redis 计数器)                              │
│  - 历史统计 (定时任务聚合)                              │
│  - 钻取详情 (查询原始数据)                              │
└─────────────────────────────────────────────────────────┘
                          ▲
┌─────────────────────────────────────────────────────────┐
│                   数据层                                │
│  Redis (实时缓存) │ 统计表 (im_stats_*) │ 业务表       │
└─────────────────────────────────────────────────────────┘
```

### 2.2 核心设计原则
- 实时数据走 Redis，不查主库，避免影响业务性能
- 历史趋势数据通过定时任务预聚合，存入统计表
- 前端采用轮询（30秒）+ 手动刷新混合模式

---

## 三、数据库设计

### 3.1 新增统计表

```sql
-- 每日统计表（主表）
CREATE TABLE im_statistics_daily (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date       DATE NOT NULL COMMENT '统计日期',
    user_total      INT DEFAULT 0 COMMENT '用户总数',
    user_new        INT DEFAULT 0 COMMENT '新增用户',
    user_active     INT DEFAULT 0 COMMENT '日活用户',
    message_total   BIGINT DEFAULT 0 COMMENT '消息总数',
    message_day     BIGINT DEFAULT 0 COMMENT '当日消息',
    group_total     INT DEFAULT 0 COMMENT '群组总数',
    group_new       INT DEFAULT 0 COMMENT '新增群组',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date (stat_date)
) COMMENT '每日统计数据';

-- 消息类型分布表
CREATE TABLE im_statistics_message_type (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date       DATE NOT NULL,
    message_type    TINYINT NOT NULL COMMENT '消息类型 1:文本 2:图片 3:文件',
    count           BIGINT DEFAULT 0,
    UNIQUE KEY uk_date_type (stat_date, message_type)
) COMMENT '消息类型统计';

-- 时段分布表（用于高峰时段分析）
CREATE TABLE im_statistics_hourly (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date       DATE NOT NULL,
    stat_hour       TINYINT NOT NULL,
    message_count   BIGINT DEFAULT 0,
    active_user_count INT DEFAULT 0,
    UNIQUE KEY uk_date_hour (stat_date, stat_hour)
) COMMENT '小时级统计';
```

### 3.2 Redis 实时计数器

```
im:stat:online              -> 在线用户数
im:stat:message:today       -> 今日消息数
im:stat:user:new:today      -> 今日新增用户
im:stat:trend:{type}:{start}:{end}  -> 趋势数据缓存（5分钟）
```

---

## 四、后端 API 设计

### 4.1 Controller 接口

```java
@Api(tags = "数据统计管理")
@RestController
@RequestMapping("/api/admin/statistics")
public class ImStatisticsController {
    
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/overview")
    public AjaxResult getOverview();
    
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/trend")
    public AjaxResult getTrend(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam TrendType type
    );
    
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/distribution")
    public AjaxResult getDistribution(@RequestParam String category);
    
    @PreAuthorize("hasAuthority('im:statistics:export')")
    @GetMapping("/export")
    public void exportStatistics(
        HttpServletResponse response,
        @RequestParam String metric,
        @RequestParam LocalDate date
    );
    
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/drill-down")
    public AjaxResult drillDown(
        @RequestParam String metric,
        @RequestParam LocalDate date,
        @RequestParam Integer page,
        @RequestParam Integer size
    );
}
```

### 4.2 返回数据结构

| 接口 | 返回 VO |
|------|---------|
| /overview | StatisticsOverviewVO - 用户总数/在线数、今日消息、群组数 |
| /trend | List\<StatisticsTrendVO\> - 日期 + 数值列表 |
| /distribution | Map\<String, Long\> - 分类和数量 |
| /drill-down | TableDataInfo\<StatisticsDetailVO\> - 分页详情 |

### 4.3 定时任务

```java
@Scheduled(cron = "0 0 1 * * ?")  // 每天凌晨1点
public void aggregateDailyStatistics();

@Scheduled(cron = "0 0 * * * ?")  // 每小时
public void aggregateHourlyStatistics();
```

---

## 五、前端设计

### 5.1 目录结构

```
src/views/admin/
└── StatisticsDashboard.vue          # 主页面
    ├── components/
    │   ├── StatCard.vue             # 统计卡片
    │   ├── TrendChart.vue           # 趋势图表（ECharts）
    │   ├── PieChart.vue             # 饼图（分布）
    │   ├── BarChart.vue             # 柱状图（分布）
    │   └── DrillDownDialog.vue      # 钻取详情弹窗
```

### 5.2 全屏布局

```vue
<template>
  <div class="statistics-dashboard">
    <!-- 顶部：统计卡片 - 横向铺满 -->
    <div class="stat-cards-row">
      <StatCard v-for="item in cardItems" :key="item.key" />
    </div>
    
    <!-- 中部：左右分栏图表 -->
    <div class="charts-container">
      <div class="chart-row">
        <div class="chart-card half-width"><TrendChart type="message" /></div>
        <div class="chart-card half-width"><TrendChart type="user" /></div>
      </div>
      <div class="chart-row">
        <div class="chart-card third-width"><PieChart type="messageType" /></div>
        <div class="chart-card third-width"><BarChart type="groupSize" /></div>
        <div class="chart-card third-width"><BarChart type="activeTime" /></div>
      </div>
    </div>
  </div>
</template>
```

### 5.3 组件交互流程

1. 页面加载 → 默认展示"今日概览"
2. 用户切换时间范围 → 请求 /trend 接口 → 图表更新
3. 用户点击卡片 → 打开 DrillDownDialog → 请求 /drill-down
4. 每 30 秒自动刷新概览数据
5. 用户点击"导出"按钮 → 下载 Excel 文件

---

## 六、技术实现要点

### 6.1 实时数据更新

在业务操作时同步更新 Redis 计数器：

```java
// 发送消息时
redisTemplate.opsForValue().increment("im:stat:message:today");

// 用户上线/离线
redisTemplate.opsForSet().add("im:stat:online:set", userId);
```

### 6.2 缓存策略

| 数据类型 | 缓存时长 | 存储位置 |
|---------|---------|---------|
| 概览数据 | 30 秒 | Redis |
| 趋势数据 | 5 分钟 | Redis |
| 分布数据 | 10 分钟 | Redis |

### 6.3 权限控制

- 访问统计看板：`ROLE_ADMIN` 或 `ROLE_OPERATOR`
- 导出数据：`im:statistics:export` 权限

### 6.4 数据脱敏

钻取详情中：
- 用户信息：显示昵称，不显示手机号
- 消息内容：超过 50 字符截断

---

## 七、实施步骤

1. **数据库**：创建统计表
2. **后端基础**：创建 Controller、Service、Mapper、VO
3. **定时任务**：实现数据聚合逻辑
4. **实时计数**：在业务代码中添加 Redis 计数器更新
5. **前端页面**：创建主页面和组件
6. **图表集成**：配置 ECharts
7. **钻取功能**：实现弹窗和表格
8. **导出功能**：集成 EasyExcel
9. **权限配置**：添加角色和权限
10. **测试验证**：功能测试和性能测试

---

## 八、依赖清单

### 后端新增依赖

```xml
<!-- EasyExcel 导出 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>easyexcel</artifactId>
    <version>3.3.2</version>
</dependency>
```

### 前端新增依赖

```json
{
  "echarts": "^5.4.3"
}
```

---

*文档结束*
