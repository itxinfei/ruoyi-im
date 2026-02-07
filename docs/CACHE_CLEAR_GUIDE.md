# Redis 缓存清理配置说明

## 功能概述

在开发/测试环境启动时自动清理 Redis 缓存，避免缓存与数据库数据不一致。

## 配置项

### application.yml

```yaml
app:
  env: dev          # 环境标识：dev/test/prod
  clear-cache-on-startup: true   # 是否在启动时清理缓存
```

## 触发条件

缓存清理需要**同时满足**以下两个条件：

1. `env` 为 `dev` 或 `test`
2. `clear-cache-on-startup` 为 `true`

## 清理的缓存范围

| 键模式 | 说明 |
|--------|------|
| `im:*` | IM 系统数据 |
| `contact:list:*` | 好友列表缓存 |
| `conversation:*` | 会话缓存 |
| `user:*` | 用户缓存 |

## 环境配置示例

### 开发环境（需要清理缓存）

```yaml
app:
  env: dev
  clear-cache-on-startup: true
```

### 测试环境（通常不需要清理）

```yaml
app:
  env: test
  clear-cache-on-startup: false
```

### 生产环境（必须禁止）

```yaml
app:
  env: prod
  clear-cache-on-startup: false  # 即使设为true也不会执行
```

## 日志输出

### 清理缓存时

```
=== IM系统启动验证开始 ===
运行环境: dev
✓ IM系统API接口已成功启动
✓ 所有IM服务组件已就绪
###############################################
#     ⚠️  警告：正在清理 Redis 缓存！     #
#     所有 IM 相关缓存将被清空            #
###############################################
环境: dev | 配置: clear-cache-on-startup=true
即将清理以下缓存键前缀:
  - im:* (IM数据)
  - contact:list:* (好友列表)
  - conversation:* (会话)
  - user:* (用户)
✓ Redis 缓存清理完成 (耗时: 45ms)
###############################################
=== IM系统启动验证完成 ===
```

### 跳过清理时

```
=== IM系统启动验证开始 ===
运行环境: test
✓ IM系统API接口已成功启动
✓ 所有IM服务组件已就绪
跳过缓存清理 (env=test, clear-cache-on-startup=false)
=== IM系统启动验证完成 ===
```

## 注意事项

1. **多实例部署**：如果有多个服务实例，只有一个实例清理缓存即可
2. **缓存预热**：清理缓存后首次请求会较慢，建议添加缓存预热逻辑
3. **生产环境**：生产环境设置 `env=prod` 后，无论 `clear-cache-on-startup` 设置为何值都不会清理缓存

## 手动清理缓存

如果需要手动清理缓存，可以：

1. **调用 API**
   ```
   POST /api/im/contact/cache/clear
   ```

2. **Redis CLI**
   ```bash
   redis-cli KEYS "im:*" | xargs redis-cli DEL
   redis-cli KEYS "contact:list:*" | xargs redis-cli DEL
   ```
