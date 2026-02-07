---
name: ruoyi-im-development
description: 专门用于 RuoYi-IM 项目开发的综合技能，包含前后端开发规范、架构设计最佳实践和代码生成模板
version: 1.0.0
---

# RuoYi-IM 开发技能

## 技能概述

这是专门为 RuoYi-IM 即时通讯系统项目定制的开发技能，提供完整的项目开发指导、代码模板和最佳实践。

## 适用场景

- 新功能开发（用户、消息、群组、文件等）
- 代码重构和优化
- 性能调优
- 问题排查和解决
- 项目架构设计和改进

## 核心知识库

### 1. 项目架构知识

**技术栈组合**:
- 后端: Spring Boot 2.7 + WebSocket + MyBatis Plus + Redis
- 前端: Vue 3 + Element Plus + Vuex + WebSocket
- 数据库: MySQL 8.0 + Redis 7.0
- 通信: WebSocket/Netty 实时通信

**分层架构**:
```
前端 (Vue 3)
    ↓ HTTP/WebSocket
后端 (Spring Boot)
    ├── Controller (API 接口层)
    ├── Service (业务逻辑层)
    ├── Mapper (数据访问层)
    └── WebSocket (实时通信层)
    ↓
数据库 (MySQL + Redis)
```

### 2. 开发规范知识

**Java 开发规范**:
- 使用 JDK 1.8 语法（禁止 var、record 等）
- 分层调用：Controller → Service → Mapper
- 使用 DTO/VO 传递数据，不用 Entity 直接暴露
- MyBatis Plus 必须使用 Lambda 查询
- 使用 @Transactional 管理事务
- 使用 log 记录日志，禁止 System.out.println

**Vue 开发规范**:
- 使用 `<script setup>` 语法
- 文件名 PascalCase
- CSS 必须加 scoped
- v-for 必须指定 key
- API 封装在 api/ 目录

### 3. 实时通信知识

**WebSocket 连接管理**:
- 连接建立和认证
- 心跳检测机制
- 断线重连策略
- 连接池管理

**消息处理流程**:
1. 客户端发送消息
2. 服务端接收和验证
3. 消息持久化
4. 实时推送给接收方
5. 更新消息状态

### 4. 数据库设计知识

**核心表结构**:
- 用户表 (im_user)
- 好友关系表 (im_friend)
- 群组表 (im_group)
- 群组成员表 (im_group_member)
- 消息表 (im_message)
- 会话表 (im_conversation)
- 文件表 (im_file)

**设计原则**:
- 使用 utf8mb4 字符集支持表情
- 合理设置索引优化查询
- 使用软删除保留数据
- 添加创建时间和更新时间

## 代码模板

### 1. Controller 模板

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
    
    @GetMapping("/list")
    public Result<List<XxxVO>> list(@Valid XxxQueryDTO dto) {
        log.info("查询XXX列表, dto={}", dto);
        List<XxxVO> list = xxxService.list(dto);
        return Result.success(list);
    }
}
```

### 2. Service 模板

```java
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ImXxxServiceImpl implements ImXxxService {
    
    @Autowired
    private ImXxxMapper xxxMapper;
    
    @Override
    public XxxVO create(XxxCreateDTO dto) {
        // 参数校验
        if (dto == null) {
            throw new ServiceException("参数不能为空");
        }
        
        // 业务逻辑处理
        ImXxx entity = new ImXxx();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        
        // 保存数据
        xxxMapper.insert(entity);
        
        // 返回结果
        XxxVO vo = new XxxVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
```

### 3. Vue 组件模板

```vue
<template>
  <div class="xxx-container">
    <el-card>
      <div class="header">
        <h3>XXX管理</h3>
        <el-button type="primary" @click="handleCreate">
          <i class="material-icons-outlined">add</i>
          新增
        </el-button>
      </div>
      
      <div class="content">
        <el-table :data="list" v-loading="loading">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="createTime" label="创建时间" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" @click="handleEdit(scope.row)">
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getXxxList, createXxx, updateXxx } from '@/api/xxx'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])

const state = reactive({
  formData: {
    name: '',
    description: ''
  }
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getXxxList()
    list.value = res.data
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  // 创建逻辑
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.xxx-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.content {
  min-height: 400px;
}
</style>
```

## 常见问题解决方案

### 1. WebSocket 连接问题

**问题**: WebSocket 连接频繁断开
**解决方案**:
```java
// 心跳检测
@Scheduled(fixedRate = 30000)
public void heartbeat() {
    sessions.forEach((sessionId, session) -> {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage("heartbeat"));
            } else {
                sessions.remove(sessionId);
            }
        } catch (Exception e) {
            log.error("心跳检测失败, sessionId={}", sessionId, e);
        }
    });
}
```

### 2. 消息重复问题

**问题**: 接收到重复消息
**解决方案**:
```java
// 使用消息ID去重
private final Set<String> processedMessageIds = new ConcurrentHashMap<>();

public void processMessage(MessageDTO message) {
    String messageId = message.getId();
    if (processedMessageIds.contains(messageId)) {
        return;
    }
    processedMessageIds.add(messageId);
    // 处理消息
}
```

### 3. 大量消息渲染问题

**问题**: 聊天记录过多导致页面卡顿
**解决方案**:
```vue
<!-- 虚拟滚动 -->
<el-virtual-list
  :data="messageList"
  :height="400"
  :item-size="80"
>
  <template #default="{ item }">
    <message-item :message="item" />
  </template>
</el-virtual-list>
```

## 性能优化建议

### 1. 数据库优化
- 合理使用索引
- 避免全表扫描
- 使用批量操作
- 实现读写分离

### 2. 缓存优化
- 热点数据缓存
- 查询结果缓存
- 分布式缓存
- 缓存预热

### 3. 前端优化
- 组件懒加载
- 图片懒加载
- 虚拟滚动
- 代码分割

## 安全注意事项

1. **输入验证**: 所有用户输入必须验证
2. **权限控制**: 严格的权限验证
3. **SQL注入防护**: 使用参数化查询
4. **XSS防护**: 输出转义
5. **敏感数据加密**: 密码、密钥等加密存储

## 测试策略

1. **单元测试**: Service 层业务逻辑测试
2. **集成测试**: API 接口测试
3. **性能测试**: 并发和压力测试
4. **端到端测试**: 完整业务流程测试

记住：这个技能包含了 RuoYi-IM 项目的所有核心知识，遇到问题时首先查阅这里！