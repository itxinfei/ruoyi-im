# Dashboard API 测试脚本

## 环境准备

- 服务器地址：`http://localhost:8081`
- 基础路径：`/im/dashboard`
- 认证方式：Shiro Session（需先登录）

## 测试步骤

### 1. 登录系统（获取Session）

```bash
curl -X POST "http://localhost:8081/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123" \
  -c cookies.txt
```

### 2. 测试基础统计接口

```bash
curl -X GET "http://localhost:8081/im/dashboard/basic" \
  -b cookies.txt
```

**预期响应**：
```json
{
  "code": 0,
  "msg": "查询成功",
  "data": {
    "total_users": 100,
    "online_users": 50,
    "total_groups": 20,
    "total_messages": 1000,
    "total_conversations": 150
  }
}
```

### 3. 测试趋势数据接口

#### 3.1 用户注册趋势（7天）

```bash
curl -X GET "http://localhost:8081/im/dashboard/trend?type=user&days=7" \
  -b cookies.txt
```

#### 3.2 消息发送趋势（30天）

```bash
curl -X GET "http://localhost:8081/im/dashboard/trend?type=message&days=30" \
  -b cookies.txt
```

#### 3.3 群组创建趋势（90天）

```bash
curl -X GET "http://localhost:8081/im/dashboard/trend?type=group&days=90" \
  -b cookies.txt
```

#### 3.4 每小时活跃度趋势（7天）

```bash
curl -X GET "http://localhost:8081/im/dashboard/trend?type=hourly&days=7" \
  -b cookies.txt
```

#### 3.5 每日活跃用户（30天）

```bash
curl -X GET "http://localhost:8081/im/dashboard/trend?type=daily&days=30" \
  -b cookies.txt
```

**预期响应**：
```json
{
  "code": 0,
  "msg": "查询成功",
  "data": [
    {"date_label": "2025-01-11", "count": 10},
    {"date_label": "2025-01-12", "count": 15},
    {"date_label": "2025-01-13", "count": 20}
  ]
}
```

### 4. 测试分布数据接口

#### 4.1 消息类型分布

```bash
curl -X GET "http://localhost:8081/im/dashboard/distribution?type=msgType" \
  -b cookies.txt
```

#### 4.2 文件类型分布

```bash
curl -X GET "http://localhost:8081/im/dashboard/distribution?type=fileType" \
  -b cookies.txt
```

#### 4.3 用户活跃度分布

```bash
curl -X GET "http://localhost:8081/im/dashboard/distribution?type=userActivity&days=30" \
  -b cookies.txt
```

#### 4.4 会话类型分布

```bash
curl -X GET "http://localhost:8081/im/dashboard/distribution?type=conversationType" \
  -b cookies.txt
```

**预期响应**：
```json
{
  "code": 0,
  "msg": "查询成功",
  "data": [
    {"type_label": "TEXT", "count": 500},
    {"type_label": "IMAGE", "count": 300},
    {"type_label": "FILE", "count": 200}
  ]
}
```

### 5. 测试对比数据接口

#### 5.1 周对比（本周vs上周）

```bash
curl -X GET "http://localhost:8081/im/dashboard/comparison?type=weekly" \
  -b cookies.txt
```

#### 5.2 月对比（本月vs上月）

```bash
curl -X GET "http://localhost:8081/im/dashboard/comparison?type=monthly" \
  -b cookies.txt
```

#### 5.3 每小时对比（今天vs昨天）

```bash
curl -X GET "http://localhost:8081/im/dashboard/comparison?type=hourly" \
  -b cookies.txt
```

**预期响应**：
```json
{
  "code": 0,
  "msg": "查询成功",
  "data": {
    "本周": 5000,
    "上周": 4500
  }
}
```

### 6. 测试排行榜接口

#### 6.1 最活跃用户排行

```bash
curl -X GET "http://localhost:8081/im/dashboard/ranking?type=users&days=7&limit=10" \
  -b cookies.txt
```

#### 6.2 最活跃群组排行

```bash
curl -X GET "http://localhost:8081/im/dashboard/ranking?type=groups&days=7&limit=10" \
  -b cookies.txt
```

#### 6.3 消息发送排行

```bash
curl -X GET "http://localhost:8081/im/dashboard/ranking?type=senders&days=7&limit=10" \
  -b cookies.txt
```

**预期响应**：
```json
{
  "code": 0,
  "msg": "查询成功",
  "data": [
    {"id": 1, "username": "admin", "nickname": "管理员", "message_count": 500},
    {"id": 2, "username": "user1", "nickname": "用户1", "message_count": 300}
  ]
}
```

### 7. 测试实时监控接口

#### 7.1 实时在线用户数

```bash
curl -X GET "http://localhost:8081/im/dashboard/realtime?type=online" \
  -b cookies.txt
```

#### 7.2 实时消息流量（最近30分钟）

```bash
curl -X GET "http://localhost:8081/im/dashboard/realtime?type=messageFlow&minutes=30" \
  -b cookies.txt
```

**预期响应**：
```json
{
  "code": 0,
  "msg": "查询成功",
  "data": 50
}
```

### 8. 测试缓存刷新接口

```bash
curl -X POST "http://localhost:8081/im/dashboard/refresh?cacheType=all" \
  -b cookies.txt
```

**预期响应**：
```json
{
  "code": 0,
  "msg": "数据已刷新（当前未启用缓存）"
}
```

## Postman 测试集合

可以使用以下JSON导入到Postman：

```json
{
  "info": {
    "name": "RuoYi-IM Dashboard API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Dashboard API",
      "item": [
        {
          "name": "基础统计",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8081/im/dashboard/basic",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["im", "dashboard", "basic"]
            }
          }
        },
        {
          "name": "用户趋势",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8081/im/dashboard/trend?type=user&days=7",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["im", "dashboard", "trend"],
              "query": [
                {"key": "type", "value": "user"},
                {"key": "days", "value": "7"}
              ]
            }
          }
        }
      ]
    }
  ]
}
```

## 性能测试

### 使用 Apache Bench (ab)

测试基础统计接口（100并发，1000请求）：

```bash
ab -n 1000 -c 100 -C "JSESSIONID=xxx" \
  http://localhost:8081/im/dashboard/basic
```

### 使用 wrk

```bash
wrk -t4 -c100 -d30s --latency \
  -H "Cookie: JSESSIONID=xxx" \
  http://localhost:8081/im/dashboard/basic
```

## 验收标准

### 功能测试
- ✅ 所有API返回状态码200
- ✅ 数据格式正确（JSON）
- ✅ 数据内容合理（非负数、日期正确等）

### 性能测试
- ✅ 基础统计接口响应时间 < 500ms
- ✅ 趋势数据接口响应时间 < 1s
- ✅ 排行榜接口响应时间 < 1s

### 并发测试
- ✅ 100并发用户访问无错误
- ✅ 1000次请求成功率 > 99%

## 常见问题

### 1. 认证失败

**原因**：Session过期或未登录

**解决**：重新执行登录步骤

### 2. 返回数据为空

**原因**：数据库中没有数据

**解决**：先生成一些测试数据

### 3. SQL错误

**原因**：表结构不匹配

**解决**：检查数据库表结构，确保字段名正确

## 测试数据生成

可以参考以下SQL生成测试数据：

```sql
-- 生成测试用户
INSERT INTO im_user (username, nickname, status, create_time)
SELECT
  CONCAT('test_user_', n),
  CONCAT('测试用户', n),
  IF(n % 10 = 0, 'online', 'offline'),
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY)
FROM (
  SELECT @n := @n + 1 AS n
  FROM (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) t1,
       (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) t2,
       (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) t3,
       (SELECT @n:=0) init
  LIMIT 100
) numbers;

-- 生成测试消息
INSERT INTO im_message (conversation_id, sender_id, message_type, content, create_time)
SELECT
  (SELECT id FROM im_conversation ORDER BY RAND() LIMIT 1),
  (SELECT id FROM im_user WHERE id > 1 ORDER BY RAND() LIMIT 1),
  ELT(FLOOR(RAND() * 3) + 1, 'TEXT', 'IMAGE', 'FILE'),
  CONCAT('测试消息', n),
  DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY)
FROM (
  SELECT @n := @n + 1 AS n
  FROM (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) t1,
       (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) t2,
       (SELECT 0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4) t3,
       (SELECT @n:=0) init
  LIMIT 1000
) numbers;
```
