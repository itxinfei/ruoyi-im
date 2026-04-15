# 67 AI API参考手册

> **版本**: v1.0.0
> **更新日期**: 2026-04-14
> **API版本**: v1
> **协议**: RESTful + WebSocket

---

## 概述 (Overview)

本文档提供AI大模型推理API的完整参考，包含中英文参数对照、返回值定义、错误码表等内容。所有API均遵循RESTful规范，返回JSON格式数据。

### API调用时序图

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant Gateway as API网关
    participant Auth as 认证服务
    participant LLM as LLM推理服务
    participant Cache as 缓存服务

    Client->>Gateway: POST /v1/chat/completions
    Gateway->>Auth: 验证API Key
    Auth-->>Gateway: 认证成功

    Gateway->>LLM: 转发请求
    LLM->>Cache: 检查缓存
    Cache-->>LLM: 缓存未命中

    Note over LLM: 模型推理生成

    LLM->>Cache: 写入缓存
    Cache-->>LLM: 缓存成功
    LLM-->>Gateway: 返回结果
    Gateway-->>Client: 响应JSON

    Note over Client: 处理响应
```

### 错误处理流程

```mermaid
flowchart TD
    A[发起请求] --> B{认证检查}

    B -->|成功| C{参数校验}
    B -->|401 未授权| D[返回错误]
    D --> E[提示检查API Key]

    C -->|通过| F{内容审核}
    C -->|400 参数错误| G[返回错误详情]

    F -->|通过| H{模型推理}
    F -->|内容违规| I[返回内容审核失败]

    H -->|成功| J[返回结果]
    H -->|500 服务错误| K{重试}
    K -->|重试<3次| H
    K -->|重试>=3次| L[返回服务不可用]

    J --> M[写入缓存]
    M --> N[返回成功]
```

## 一、API基础规范

### 1.1 基础URL

| 环境 | 基础URL | 说明 |
|-----|--------|-----|
| 生产环境 | `https://api.example.com/v1` | 正式服务 |
| 测试环境 | `https://api-test.example.com/v1` | 测试服务 |
| 本地开发 | `http://localhost:8000/v1` | 本地开发 |

### 1.2 认证方式

```http
Authorization: Bearer <API_KEY>
Content-Type: application/json
```

### 1.3 请求格式

```javascript
// 标准请求头
{
  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "Content-Type": "application/json",
  "X-Request-ID": "req-uuid-xxxx",
  "X-Trace-ID": "trace-uuid-xxxx"
}
```

---

## 二、聊天补全 API (Chat Completion)

### 2.1 接口定义

```http
POST /v1/chat/completions
```

### 2.2 请求参数 (Request Parameters)

| 参数名 | 中文说明 | English Description | 类型 | 必填 | 默认值 | 说明 |
|-------|---------|---------------------|------|-----|-------|-----|
| `model` | 模型标识 | Model identifier | string | 是 | - | 模型名称或版本ID |
| `messages` | 消息列表 | Message list | array | 是 | - | 对话历史消息 |
| `temperature` | 温度参数 | Sampling temperature | float | 否 | 0.7 | 控制随机性，范围[0,2] |
| `top_p` | 核采样概率 | Nucleus sampling probability | float | 否 | 1.0 | 控制token选择范围 |
| `max_tokens` | 最大生成长度 | Maximum tokens to generate | integer | 否 | 2048 | 最大生成token数 |
| `stream` | 流式输出 | Enable streaming | boolean | 否 | false | 是否启用流式响应 |
| `stop` | 停止词列表 | Stop sequences | array/string | 否 | null | 遇到这些词停止生成 |
| `presence_penalty` | 存在惩罚 | Presence penalty | float | 否 | 0.0 | 范围[-2,2] |
| `frequency_penalty` | 频率惩罚 | Frequency penalty | float | 否 | 0.0 | 范围[-2,2] |
| `user` | 用户标识 | User identifier | string | 否 | - | 用于滥用检测 |

#### messages 参数结构

| 字段 | 中文 | English | 类型 | 说明 |
|-----|------|---------|------|-----|
| `role` | 角色 | Message role | string | system/user/assistant |
| `content` | 内容 | Message content | string | 消息文本内容 |
| `name` | 名称 | Sender name | string | 可选，用户名称 |

### 2.3 请求示例 (Request Examples)

```javascript
// 标准请求
POST /v1/chat/completions
{
  "model": "llama2-7b-chat",
  "messages": [
    {
      "role": "system",
      "content": "你是一个有帮助的技术助手。"
    },
    {
      "role": "user",
      "content": "解释什么是RESTful API"
    }
  ],
  "temperature": 0.7,
  "max_tokens": 1024
}
```

```python
# Python SDK示例
from openai import OpenAI

client = OpenAI(
    api_key="your-api-key",
    base_url="https://api.example.com/v1"
)

response = client.chat.completions.create(
    model="llama2-7b-chat",
    messages=[
        {"role": "system", "content": "你是一个有帮助的技术助手。"},
        {"role": "user", "content": "解释什么是RESTful API"}
    ],
    temperature=0.7,
    max_tokens=1024
)

print(response.choices[0].message.content)
```

```bash
# cURL示例
curl -X POST "https://api.example.com/v1/chat/completions" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama2-7b-chat",
    "messages": [
      {"role": "system", "content": "你是一个有帮助的技术助手。"},
      {"role": "user", "content": "解释什么是RESTful API"}
    ],
    "temperature": 0.7,
    "max_tokens": 1024
  }'
```

### 2.4 响应参数 (Response Parameters)

| 字段 | 中文说明 | English Description | 类型 | 说明 |
|-----|---------|---------------------|------|-----|
| `id` | 请求ID | Request identifier | string | 唯一请求ID |
| `object` | 对象类型 | Object type | string | 固定值 "chat.completion" |
| `created` | 创建时间戳 | Creation timestamp | integer | Unix时间戳 |
| `model` | 使用的模型 | Model used | string | 实际处理的模型 |
| `choices` | 生成结果列表 | Completion choices | array | 生成选项数组 |
| `usage` | 使用量统计 | Usage statistics | object | Token消耗统计 |

#### choices 结构

| 字段 | 中文 | English | 类型 | 说明 |
|-----|------|---------|------|-----|
| `index` | 索引 | Choice index | integer | 选项索引 |
| `message` | 消息 | Message object | object | 生成的回复 |
| `finish_reason` | 结束原因 | Finish reason | string | stop/length/content_filter |

#### usage 结构

| 字段 | 中文说明 | English Description | 类型 | 说明 |
|-----|---------|---------------------|------|-----|
| `prompt_tokens` | 提示Token数 | Prompt tokens | integer | 输入消耗的Token |
| `completion_tokens` | 完成Token数 | Completion tokens | integer | 输出消耗的Token |
| `total_tokens` | 总Token数 | Total tokens | integer | 总消耗Token |

### 2.5 响应示例 (Response Examples)

```json
{
  "id": "chatcmpl-8a1b2c3d4e5f",
  "object": "chat.completion",
  "created": 1713000000,
  "model": "llama2-7b-chat",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "RESTful API 是一种基于 REST 架构风格的 Web 服务设计原则..."
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 35,
    "completion_tokens": 128,
    "total_tokens": 163
  }
}
```

### 2.6 流式响应 (Streaming Response)

```text
// 流式响应格式 (SSE)
data: {"id":"chatcmpl-xxx","object":"chat.completion.chunk","created":1234567890,"model":"llama2-7b","choices":[{"index":0,"delta":{"content":"REST"},"finish_reason":null}]}

data: {"id":"chatcmpl-xxx","object":"chat.completion.chunk","created":1234567890,"model":"llama2-7b","choices":[{"index":0,"delta":{"content":"ful"},"finish_reason":null}]}

data: [DONE]
```

### 2.7 错误码 (Error Codes)

| 错误码 | HTTP状态码 | 中文说明 | English Description | 处理建议 |
|-------|-----------|---------|--------------------|---------|
| `INVALID_REQUEST` | 400 | 请求格式错误 | Invalid request format | 检查请求参数格式 |
| `INVALID_PARAMETER` | 400 | 参数值无效 | Invalid parameter value | 检查参数取值范围 |
| `MISSING_PARAMETER` | 400 | 缺少必填参数 | Missing required parameter | 添加缺失参数 |
| `UNAUTHORIZED` | 401 | 认证失败 | Authentication failed | 检查API Key |
| `FORBIDDEN` | 403 | 无权限访问 | Access forbidden | 检查访问权限 |
| `NOT_FOUND` | 404 | 资源不存在 | Resource not found | 检查模型名称 |
| `RATE_LIMIT_EXCEEDED` | 429 | 请求频率超限 | Rate limit exceeded | 降低请求频率 |
| `TOKEN_LIMIT_EXCEEDED` | 429 | Token额度超限 | Token limit exceeded | 减少请求长度 |
| `CONTENT_MODERATED` | 400 | 内容审核未通过 | Content moderation failed | 修改输入内容 |
| `MODEL_OVERLOADED` | 503 | 模型服务繁忙 | Model overloaded | 稍后重试 |
| `SERVICE_UNAVAILABLE` | 503 | 服务不可用 | Service unavailable | 检查服务状态 |
| `INTERNAL_ERROR` | 500 | 内部错误 | Internal server error | 联系技术支持 |

### 2.8 错误响应格式

```json
{
  "error": {
    "code": "INVALID_PARAMETER",
    "message": "temperature参数值超出有效范围[0,2]",
    "param": "temperature",
    "type": "invalid_request_error",
    "request_id": "req-uuid-xxxx"
  }
}
```

---

## 三、模型列表 API (Model List)

### 3.1 接口定义

```http
GET /v1/models
```

### 3.2 响应示例

```json
{
  "object": "list",
  "data": [
    {
      "id": "llama2-7b-chat",
      "object": "model",
      "created": 1712000000,
      "owned_by": "organization",
      "permission": ["inference"],
      "context_window": 4096,
      "type": "chat"
    },
    {
      "id": "llama2-13b-chat",
      "object": "model",
      "created": 1712000001,
      "owned_by": "organization",
      "permission": ["inference"],
      "context_window": 4096,
      "type": "chat"
    }
  ]
}
```

---

## 四、Embeddings API

### 4.1 接口定义

```http
POST /v1/embeddings
```

### 4.2 请求参数

| 参数名 | 中文说明 | English Description | 类型 | 必填 | 默认值 |
|-------|---------|---------------------|------|-----|-------|
| `model` | 模型标识 | Model identifier | string | 是 | - |
| `input` | 输入文本 | Input text | string/array | 是 | - |
| `encoding_format` | 编码格式 | Encoding format | string | 否 | "float" |

### 4.3 请求示例

```http
POST /v1/embeddings
Content-Type: application/json

{
  "model": "text-embedding-ada-002",
  "input": "The food was delicious and the waiter..."
}
```

### 4.4 响应示例

```json
{
  "object": "list",
  "data": [
    {
      "object": "embedding",
      "embedding": [0.0023064255, -0.009327292, -0.0028465834],
      "index": 0
    }
  ],
  "model": "text-embedding-ada-002",
  "usage": {
    "prompt_tokens": 8,
    "total_tokens": 8
  }
}
```

---

## 五、Completions API (续写)

### 5.1 接口定义

```http
POST /v1/completions
```

### 5.2 请求参数

| 参数名 | 中文说明 | English Description | 类型 | 必填 | 默认值 |
|-------|---------|---------------------|------|-----|-------|
| `model` | 模型标识 | Model identifier | string | 是 | - |
| `prompt` | 提示文本 | Prompt text | string/array | 是 | - |
| `temperature` | 温度参数 | Temperature | float | 否 | 1.0 |
| `max_tokens` | 最大长度 | Max tokens | integer | 否 | 256 |
| `stream` | 流式输出 | Streaming | boolean | 否 | false |
| `echo` | 回显提示 | Echo prompt | boolean | 否 | false |

---

## 六、错误码详解 (Error Code Reference)

### 6.1 认证错误 (Authentication Errors)

| 错误码 | 描述 | 可能原因 | 解决方案 |
|-------|-----|---------|---------|
| `AUTH_001` | API Key无效 | Key已过期或格式错误 | 重新获取API Key |
| `AUTH_002` | API Key已禁用 | 账户被禁用 | 联系管理员 |
| `AUTH_003` | 权限不足 | 未授权访问该模型 | 升级套餐或申请权限 |

### 6.2 限流错误 (Rate Limit Errors)

| 错误码 | 描述 | 限流规则 | 解决方案 |
|-------|-----|---------|---------|
| `RATE_001` | 请求QPS超限 | 每秒最大请求数 | 使用指数退避重试 |
| `RATE_002` | 每日请求量超限 | 每日最大请求数 | 等待次日重置或升级套餐 |
| `RATE_003` | Token额度用尽 | 月度Token配额 | 等待配额重置或购买额外Token |

### 6.3 内容安全错误 (Content Safety Errors)

| 错误码 | 描述 | 检测类别 | 处理建议 |
|-------|-----|---------|---------|
| `SAFE_001` | 包含敏感内容 | 政治敏感 | 移除敏感内容 |
| `SAFE_002` | 包含违规内容 | 违法内容 | 拒绝处理 |
| `SAFE_003` | 暴力血腥内容 | 暴力内容 | 过滤或移除 |
| `SAFE_004` | 低俗色情内容 | 色情内容 | 过滤或移除 |

### 6.4 重试策略 (Retry Strategy)

```python
import time
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

def create_session_with_retries():
    """创建带重试机制的HTTP会话"""
    session = requests.Session()

    retry_strategy = Retry(
        total=3,
        backoff_factor=1,
        status_forcelist=[429, 500, 502, 503, 504],
        allowed_methods=["HEAD", "GET", "POST"]
    )

    adapter = HTTPAdapter(max_retries=retry_strategy)
    session.mount("https://", adapter)
    session.mount("http://", adapter)

    return session

def call_api_with_retry(url: str, payload: dict, api_key: str) -> dict:
    """
    带重试的API调用

    参数说明 (Parameters):
        url: str - API端点 (API endpoint)
        payload: dict - 请求载荷 (Request payload)
        api_key: str - API密钥 (API key)

    返回值 (Return Value):
        dict - API响应
    """
    session = create_session_with_retries()
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }

    max_retries = 3
    for attempt in range(max_retries):
        try:
            response = session.post(url, json=payload, headers=headers, timeout=30)
            response.raise_for_status()
            return response.json()

        except requests.exceptions.HTTPError as e:
            if response.status_code == 429:
                wait_time = 2 ** attempt
                print(f"限流，{wait_time}秒后重试...")
                time.sleep(wait_time)
            elif response.status_code >= 500:
                wait_time = 2 ** attempt
                print(f"服务端错误，{wait_time}秒后重试...")
                time.sleep(wait_time)
            else:
                raise

        except requests.exceptions.RequestException as e:
            print(f"请求失败: {e}")
            if attempt == max_retries - 1:
                raise

    raise Exception("达到最大重试次数")
```

---

## 七、SDK文档 (SDK Reference)

### 7.1 Python SDK

```python
# 安装
pip install llm-sdk

# 初始化
from llm_sdk import LLMClient

client = LLMClient(
    api_key="your-api-key",
    base_url="https://api.example.com/v1",
    timeout=60
)

# 聊天补全
response = client.chat.completions.create(
    model="llama2-7b-chat",
    messages=[
        {"role": "user", "content": "你好"}
    ]
)

# 获取Embedding
embedding = client.embeddings.create(
    model="text-embedding-ada-002",
    input="待编码文本"
)

# 异步调用
import asyncio

async def async_chat():
    response = await client.chat.completions.acreate(
        model="llama2-7b-chat",
        messages=[{"role": "user", "content": "Hello"}]
    )
    return response
```

### 7.2 Java SDK

```java
// Maven依赖
<dependency>
    <groupId>com.example</groupId>
    <artifactId>llm-sdk</artifactId>
    <version>1.0.0</version>
</dependency>

// 使用示例
import com.example.llm.*;

LLMClient client = new LLMClient.Builder()
    .apiKey("your-api-key")
    .baseUrl("https://api.example.com/v1")
    .timeout(60)
    .build();

ChatRequest request = ChatRequest.builder()
    .model("llama2-7b-chat")
    .message(Message.user("你好"))
    .temperature(0.7)
    .maxTokens(1024)
    .build();

ChatResponse response = client.chat().completions(request);
System.out.println(response.getContent());
```

### 7.3 JavaScript/TypeScript SDK

```typescript
// 安装
npm install @example/llm-sdk

// 使用示例
import { LLMClient } from '@example/llm-sdk';

const client = new LLMClient({
  apiKey: 'your-api-key',
  baseUrl: 'https://api.example.com/v1'
});

// 聊天补全
const response = await client.chat.completions.create({
  model: 'llama2-7b-chat',
  messages: [
    { role: 'user', content: '你好' }
  ]
});

// 流式响应
const stream = await client.chat.completions.create({
  model: 'llama2-7b-chat',
  messages: [{ role: 'user', content: '写一个故事' }],
  stream: true
});

for await (const chunk of stream) {
  console.log(chunk.choices[0].delta.content);
}
```

---

## 变更记录

| 日期 | 版本 | 变更内容 |
|-----|------|---------|
| 2026-04-14 | v1.0.0 | 初始版本 |

---

## 相关文档

- [60-AI大模型开发总览.md](60-AI大模型开发总览.md) - 文档体系索引
- [63-推理部署与上线.md](63-推理部署与上线.md) - 部署指南
- [69-质量门禁与验收标准.md](69-质量门禁与验收标准.md) - 验收标准
