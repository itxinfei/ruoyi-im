---
name: ruoyi-im-test
description: 运行 RuoYi-IM 项目的测试套件（单元测试、集成测试、性能测试）
---

# RuoYi-IM 测试命令

运行 RuoYi-IM 项目的完整测试套件，确保代码质量和系统稳定性。

## 测试类型

### 1. 单元测试
```bash
# 运行所有单元测试
cd ruoyi-im-api && mvn test

# 运行特定测试类
mvn test -Dtest=ImMessageServiceTest

# 生成测试报告
mvn test jacoco:report
```

### 2. 集成测试
```bash
# 运行集成测试
mvn verify -P integration-test

# 数据库集成测试
mvn test -Dtest=**/*IntegrationTest
```

### 3. 前端测试
```bash
cd ruoyi-im-web

# 运行单元测试
npm run test:unit

# 运行端到端测试
npm run test:e2e

# 生成测试覆盖率报告
npm run test:coverage
```

### 4. 性能测试
```bash
# WebSocket 连接性能测试
npm run test:websocket

# 消息发送性能测试
npm run test:message-performance

# 并发用户测试
npm run test:concurrent-users
```

## 测试配置

### 测试数据库
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    username: sa
    password: 
    driver-class-name: org.h2.Driver
```

### 测试 Redis
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 15  # 使用专用测试数据库
```

## 测试覆盖范围

### 后端测试
- Controller 层 API 测试
- Service 层业务逻辑测试
- Mapper 层数据访问测试
- WebSocket 连接测试
- 权限控制测试

### 前端测试
- 组件渲染测试
- 用户交互测试
- API 调用测试
- WebSocket 通信测试
- 状态管理测试

### 性能测试
- 并发连接数测试（目标：500+）
- 消息吞吐量测试（目标：1000+ 消息/秒）
- 响应时间测试（目标：<300ms）
- 内存使用测试

## 测试报告

### 单元测试报告
- 测试覆盖率目标：>80%
- 测试通过率目标：100%
- 报告位置：`target/site/jacoco/index.html`

### 集成测试报告
- API 接口测试结果
- 数据库操作测试结果
- WebSocket 功能测试结果
- 报告位置：`target/surefire-reports/`

### 前端测试报告
- 组件测试覆盖率
- E2E 测试结果
- 报告位置：`coverage/`

## 常见问题解决

### 测试数据库问题
```bash
# 清理测试数据库
mvn clean test -Dtest=**/*Test

# 重新初始化测试数据
mvn test -Dtest=DatabaseInitializerTest
```

### WebSocket 测试问题
```bash
# 检查 WebSocket 端口是否占用
netstat -an | grep 8888

# 释放端口
kill -9 <PID>
```

### 性能测试问题
```bash
# 增加 JVM 内存
export MAVEN_OPTS="-Xmx2g -Xms1g"

# 并行执行测试
mvn test -T 4
```

## 持续集成

### GitHub Actions 配置
```yaml
name: Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 1.8
        uses: actions/setup-java@v2
        with:
          java-version: '8'
      - name: Run tests
        run: mvn test
```

## 测试最佳实践

1. **测试命名**: 使用描述性的测试方法名
2. **测试隔离**: 确保测试之间相互独立
3. **测试数据**: 使用测试专用数据，不影响生产数据
4. **断言清晰**: 使用明确的断言信息
5. **测试覆盖率**: 定期检查并提升测试覆盖率

## 运行所有测试

```bash
# 完整测试流程
mvn clean test jacoco:report
cd ruoyi-im-web && npm run test:coverage
```

记住：高质量的测试是保证系统稳定性的关键！