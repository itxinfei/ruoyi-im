# Java + Vue 双端开发规范（RuoYi-IM）

## 1. 目的

保证项目开发始终是"前后端一体交付"，不是单端演示。

## 2. 固定技术栈

1. Java：JDK `1.8`
2. 前端：Vue `3.x`
3. 数据库：MySQL `5.7`
4. 开发环境：Windows 11
5. 生产环境：CentOS 7

## 3. 开发顺序（必须）

1. 先定义接口契约（请求DTO、响应VO、错误码）
2. 再实现后端 API（Controller -> Service -> Mapper）
3. 再接 Vue 页面与状态管理
4. 最后做联调和异常流程验证

## 4. Java 侧硬规范

1. 仅允许 JDK8 语法
2. 禁止魔法值，统一常量/枚举
3. 禁止复杂表达式链和多层三元
4. 优先显式 `if/for` 写法
5. 必须有输入校验、异常日志、事务边界

## 5. 阿里巴巴Java开发手册要点（强制对齐）

### 5.1 命名风格

- **类名**：`UpperCamelCase`（如 `UserService`）
- **方法名/变量名**：`lowerCamelCase`（如 `getUserById`）
- **常量名**：`UPPER_SNAKE_CASE`（如 `MAX_COUNT`）
- **包名**：全部小写
- **禁止**：拼音命名、下划线开头/结尾

### 5.2 常量定义

- 禁止魔法值，必须提取为 `static final` 或枚举
- 枚举类字段为 `public static final`

### 5.3 格式规约

- 缩进4空格（禁止Tab）
- 单行不超过120字符
- if/for/while 单行也必须加 `{}`

### 5.4 OOP规约

- 禁止浮点数精确比较，用 `BigDecimal`
- 循环体内字符串拼接用 `StringBuilder`
- POJO类使用 `@Data`

### 5.5 并发处理

- 共享变量加 `volatile` 或原子类
- `SimpleDateFormat` 线程不安全，用 `DateTimeFormatter`
- 推荐使用 `java.time` 包

### 5.6 控制语句

- 禁止多层嵌套（不超过2层）
- 循环体内禁止 IO 操作

### 5.7 注释规约

- 类添加 Javadoc
- 公共方法添加 Javadoc
- 禁止 `// TODO` 提交生产代码

### 5.8 其他规约

- 参数必须校验（`@Validated/@Valid`）
- 异常禁止生吞
- 使用 `try-with-resources` 管理资源
- 推荐使用 `Optional` 避免空指针

## 6. Vue 侧硬规范

1. 仅 Vue3（Composition API）
2. API 调用必须统一封装在 `src/api`
3. UI动作必须有成功/失败反馈
4. 表单弹窗必须有 loading 和防重复提交

## 7. 联调验收清单

1. 前端构建通过：`npm run build`
2. 后端编译通过：`mvn -DskipTests compile`
3. 成功路径可用：新增、查询、更新、删除
4. 失败路径可用：参数错误、权限不足、服务异常
5. 数据一致：前端状态与数据库状态一致