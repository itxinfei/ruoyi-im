# LLM任务模板（Task Cards）

> 用途：直接复制给 Claude/Codex 执行。

---

## 1. 规划任务模板

```yaml
task_id: IM-X-001
type: planning
mandate_ref: "GEMINI.md" (Must read first, highest priority)
goal: 一句话描述目标
stack_constraints:
  jdk: "1.8"
  vue: "3.x"
  node: "18 LTS"
  mysql: "5.7"
  dev_os: "Windows 11"
  prod_os: "CentOS 7"
java_quality_constraints:
  - "禁止魔法值，业务常量必须抽取"
  - "禁止多层三元和复杂表达式"
  - "优先 if/for 显式写法"
  - "方法超60行必须拆分"
ui_context:
  l1: 消息|通讯录|工作台
  l2: 二级菜单名称
  l3_dialog: 三级弹窗名称（无则填无）
scope_in:
  - D:/MyCode/im/ruoyi-im-web/src/...
scope_out:
  - D:/MyCode/im/ruoyi-im-web/src/...
acceptance:
  - AC1: ...
  - AC2: ...
  - AC3: ...
verify_cmd:
  - npm run build
risk: medium
output_required:
  - 实现步骤
  - 改动文件
  - 风险与回滚
```

## 2. 开发任务模板

```yaml
task_id: IM-X-002
type: development
mandate_ref: "GEMINI.md" (Must read first, highest priority)
goal: 一句话描述目标
stack_constraints:
  jdk: "1.8"
  vue: "3.x"
  node: "18 LTS"
  mysql: "5.7"
  dev_os: "Windows 11"
  prod_os: "CentOS 7"
java_quality_constraints:
  - "禁止魔法值，业务常量必须抽取"
  - "禁止多层三元和复杂表达式"
  - "优先 if/for 显式写法"
  - "方法超60行必须拆分"
ui_context:
  l1: 消息|通讯录|工作台
  l2: 二级菜单名称
  l3_dialog: 三级弹窗名称（无则填无）
scope_in:
  - D:/MyCode/im/ruoyi-im-web/src/...
scope_out:
  - D:/MyCode/im/ruoyi-im-web/src/...
acceptance:
  - AC1: ...
  - AC2: ...
  - AC3: ...
verify_cmd:
  - npm run build
  - npm run lint
risk: high
output_required:
  - 改动文件绝对路径
  - 关键实现
  - 验证结果
  - AC对照
  - 遗留风险
```

## 3. 审查任务模板

```yaml
task_id: IM-X-003
type: review
mandate_ref: "GEMINI.md" (Must read first, highest priority)
stack_constraints:
  jdk: "1.8"
  vue: "3.x"
  node: "18 LTS"
  mysql: "5.7"
  dev_os: "Windows 11"
  prod_os: "CentOS 7"
java_quality_constraints:
  - "禁止魔法值，业务常量必须抽取"
  - "禁止多层三元和复杂表达式"
  - "优先 if/for 显式写法"
  - "方法超60行必须拆分"
ui_context:
  l1: 消息|通讯录|工作台
  l2: 二级菜单名称
  l3_dialog: 三级弹窗名称（无则填无）
review_scope:
  - D:/MyCode/im/ruoyi-im-web/src/...
focus:
  - 功能回归
  - UI可用性
  - 状态一致性
output_required:
  - P0问题
  - P1问题
  - P2问题
  - 测试盲区
```

---

## 4. 快速执行卡（最小版）

```text
task_id: IM-A-001
goal: 打通语音通话链路
mandate_ref: "GEMINI.md"
stack_constraints:
 jdk=1.8, vue=3.x, node=18, mysql=5.7, dev_os=Windows11, prod_os=CentOS7
l1/l2/l3: 消息/最近会话/发起通话弹窗
scope_in: ChatHeader.vue, CallDialog.vue, UserDetailDrawer.vue, ChatPanel.vue
acceptance: AC1入口可发起, AC2可接听/挂断, AC3无“开发中”文案
verify_cmd: npm run build
```
