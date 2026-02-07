# RuoYi-IM OpenCode 插件

专为 RuoYi-IM 即时通讯系统定制的 OpenCode 插件，提供完整的开发辅助功能。

## 🚀 功能特性

### 📋 快速启动
- `/start-ruoyi-im`: 一键启动完整开发环境
- `/init-db`: 快速初始化数据库

### 🛠️ 代码生成
- `/ruoyi-im-init`: 生成标准功能模块代码
- 自动生成 Controller、Service、Mapper、Vue 组件
- 符合项目开发规范

### 🧪 测试工具
- `/ruoyi-im-test`: 运行完整测试套件
- 单元测试、集成测试、性能测试
- 测试覆盖率报告

### 👨‍💻 专家代理
- `ruoyi-im-frontend-expert`: Vue 3 前端开发专家
- `ruoyi-im-backend-expert`: Spring Boot 后端专家

### 📚 开发技能
- `ruoyi-im-development`: 项目综合开发知识库
- 代码模板和最佳实践
- 常见问题解决方案

## 📦 安装配置

### 1. 插件结构
```
.claude-plugin/
├── plugin.json          # 插件配置
├── commands/            # 命令定义
├── agents/              # 专家代理
├── skills/              # 开发技能
└── hooks/               # 钩子配置
```

### 2. 启用插件
在 Claude Code 中启用 `ruoyi-im-opencode` 插件即可使用所有功能。

## 🎯 使用指南

### 快速开始
```bash
# 启动开发环境
/start-ruoyi-im

# 初始化数据库
/init-db

# 创建新功能模块
/ruoyi-im-init notice im_notice

# 运行测试
/ruoyi-im-test
```

### 专家代理使用
```
请调用前端专家来优化聊天界面性能
请调用后端专家来解决 WebSocket 连接问题
```

### 开发技能应用
```
使用 ruoyi-im-development 技能来生成消息模块代码
使用 ruoyi-im-development 技能来解决性能问题
```

## 📋 开发规范

### Java 后端规范
- ✅ 使用 JDK 1.8 语法（禁止 var、record）
- ✅ 分层调用：Controller → Service → Mapper
- ✅ 使用 DTO/VO 传递数据
- ✅ MyBatis Plus 必须使用 Lambda 查询
- ✅ 使用 @Transactional 管理事务
- ✅ 使用 log 记录日志

### Vue 前端规范
- ✅ 使用 `<script setup>` 语法
- ✅ 文件名 PascalCase
- ✅ CSS 必须加 scoped
- ✅ v-for 必须指定 key
- ✅ API 封装在 api/ 目录

## 🔧 钩子功能

### PreToolUse 钩子
- 代码规范检查
- 安全性验证
- 性能提示

### PostToolUse 钩子
- 测试提醒
- 代码审查提示
- 提交检查清单

### SessionStart 钩子
- 开发环境提醒
- 规范重申
- 安全注意事项

## 📊 项目架构

### 技术栈
```
前端: Vue 3 + Element Plus + Vuex + WebSocket
后端: Spring Boot 2.7 + MyBatis Plus + Redis
数据库: MySQL 8.0 + Redis 7.0
通信: WebSocket/Netty
```

### 目录结构
```
ruoyi-im-api/          # 后端服务
├── src/main/java/com/im/
│   ├── controller/    # 控制层
│   ├── service/       # 业务层
│   ├── mapper/        # 数据层
│   ├── dto/          # 数据传输对象
│   ├── vo/           # 视图对象
│   └── entity/       # 实体类

ruoyi-im-web/          # 前端应用
├── src/
│   ├── api/          # API 接口
│   ├── components/   # 组件
│   ├── views/        # 页面
│   ├── store/        # 状态管理
│   └── utils/        # 工具函数
```

## 🎨 代码模板

### Controller 模板
```java
@RestController
@RequestMapping("/api/im/xxx")
@PreAuthorize("hasRole('USER')")
@Slf4j
public class ImXxxController {
    
    @Autowired
    private ImXxxService xxxService;
    
    @PostMapping("/create")
    public Result<XxxVO> create(@RequestBody @Valid XxxCreateDTO dto) {
        log.info("创建XXX, dto={}", dto);
        XxxVO vo = xxxService.create(dto);
        return Result.success(vo);
    }
}
```

### Vue 组件模板
```vue
<template>
  <div class="xxx-container">
    <el-card>
      <el-table :data="list" v-loading="loading">
        <!-- 表格内容 -->
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getXxxList } from '@/api/xxx'

const loading = ref(false)
const list = ref([])

onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.xxx-container {
  padding: 20px;
}
</style>
```

## 🐛 常见问题

### Q: WebSocket 连接断开？
A: 检查心跳机制和重连策略，使用后端专家诊断。

### Q: 前端页面卡顿？
A: 使用虚拟滚动和懒加载，调用前端专家优化。

### Q: 数据库查询慢？
A: 检查索引和查询语句，使用后端专家优化。

## 📈 性能指标

### 目标指标
- 并发用户数：≥ 500
- 消息延迟：≤ 300ms
- 页面加载时间：≤ 2s
- 测试覆盖率：≥ 80%

## 🔒 安全注意事项

1. 所有用户输入必须验证
2. 敏感数据加密存储
3. 使用参数化查询防止 SQL 注入
4. 实现严格的权限控制
5. 定期更新依赖包

## 📞 技术支持

如遇到问题，可以：
1. 使用相应的专家代理
2. 查看开发技能文档
3. 运行测试套件诊断
4. 查看项目日志

---

**版本**: 1.0.0  
**更新时间**: 2026-02-07  
**适用版本**: RuoYi-IM v5.1+