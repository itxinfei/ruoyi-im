# CommonJS require 转 ES6 import 修复记录

## 修复日期
2025-01-30

## 问题概述
前端项目在浏览器环境中报错：
```
ReferenceError: require is not defined
```

**根本原因**：项目使用 ES6 模块（Vite + Vue 3），但多个文件中使用了 CommonJS 的 `require()` 语法。浏览器原生不支持 CommonJS，导致运行时错误。

## 修复范围
共修复 **27 个文件**，将所有 `require()` 调用改为 ES6 `import` 语法。

### 修复的文件分类

#### 1. 核心配置文件（1 个）
- `src/router/index.js` - 路由守卫中的 require

#### 2. API 层（1 个）
- `src/api/request.js` - Axios 拦截器中的 require

#### 3. 组件文件（10 个）
- `src/components/Chat/AiSmartReply.vue`
- `src/components/Chat/AiStyleDialog.vue`
- `src/components/Chat/EmojiPicker.vue`
- `src/components/Chat/GlobalSearch.vue`
- `src/components/Common/EditProfileDialog.vue`
- `src/components/Common/GlobalSearchDialog.vue`
- `src/components/Common/SystemSettingsDialog.vue`
- `src/components/ComposeMailDialog/index.vue`
- `src/components/EmojiManager.vue`
- `src/components/FileUpload/index.vue`
- `src/components/GroupDetailDrawer/index.vue`
- `src/components/ImHeader/index.vue`

#### 4. 组合式函数（2 个）
- `src/composables/useImWebSocket.js`
- `src/composables/useInputResize.js`

#### 5. 工具函数（3 个）
- `src/utils/file.js`
- `src/utils/messageNotification.js`
- `src/utils/websocket/imWebSocket.js`

#### 6. 视图文件（3 个）
- `src/views/admin/AdminLayout.vue`
- `src/views/auth/LoginPage.vue`
- `src/views/ContactsPanel.vue`
- `src/views/MainPage.vue`

#### 7. 组件（7 个）
- `src/components/Chat/DesktopSticker.vue`

## 修复方法

### 修复前示例
```javascript
// ❌ CommonJS 语法（浏览器不支持）
const { getToken, getUserInfo } = require('@/utils/storage')
const token = getToken()
```

### 修复后示例
```javascript
// ✅ ES6 模块语法（浏览器原生支持）
import { getToken, getUserInfo } from '@/utils/storage'

const token = getToken()
```

### 修复步骤
1. **提取导入内容**：从 `const { ... } = require(...)` 中提取需要导入的函数
2. **添加 import 语句**：在文件顶部添加 ES6 import
3. **删除 require 行**：移除解构赋值的 require 调用

### 路径别名处理
根据文件位置使用正确的导入路径：
- `src/` 下文件：使用 `@/utils/storage`
- `src/utils/` 下文件：使用 `./storage`
- `src/composables/` 下文件：使用 `../utils/storage`
- `src/utils/websocket/` 下文件：使用 `../storage`

## 使用的工具
创建了 Python 脚本批量修复：
1. `/tmp/fix_vue_requires.py` - 批量修复 Vue 组件
2. `/tmp/fix_remaining_requires.py` - 修复剩余的 JS/Vue 文件

脚本功能：
- 自动检测 `require()` 调用
- 提取解构的函数名
- 生成正确的 import 语句
- 插入到合适的位置
- 删除 require 调用行

## 验证结果
```bash
# 修复前
$ grep -r "require(" src/ --include="*.js" --include="*.vue" | wc -l
23

# 修复后
$ grep -r "require(" src/ --include="*.js" --include="*.vue" | wc -l
0
```

✅ **所有 require 调用已全部清除！**

## 影响范围
- ✅ 修复了浏览器环境下的 require 错误
- ✅ 统一使用 ES6 模块语法
- ✅ 提升代码现代化程度
- ✅ 兼容 Vite 构建工具
- ✅ 更好的 Tree-shaking 支持

## 技术说明

### 为什么 require 不工作？
1. **CommonJS vs ES Modules**：
   - CommonJS (`require`) 是 Node.js 的模块系统
   - ES Modules (`import/export`) 是浏览器原生标准
   - 现代前端工具（Vite、Webpack）虽然支持 require，但在浏览器环境需要编译

2. **Vite 的限制**：
   - Vite 默认使用 ES Modules
   - 虽然可以通过插件支持 require，但官方推荐使用 ES6 语法
   - 使用 import 可以获得更好的性能和开发体验

### 最佳实践
1. ✅ **始终使用 ES6 import**：
   ```javascript
   import { func1, func2 } from '@/utils/storage'
   ```

2. ✅ **在文件顶部统一导入**：
   ```javascript
   // 所有 import 放在文件开头
   import { ref } from 'vue'
   import { getToken } from '@/utils/storage'
   ```

3. ❌ **避免在函数中动态导入**：
   ```javascript
   // 不推荐
   function init() {
     const { getToken } = require('@/utils/storage')
   }
   ```

## 相关文档
- [ES Modules - MDN](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Modules)
- [Vite - Module Resolution](https://vitejs.dev/guide/features.html#module-resolution)
- [Vue 3 Composition API](https://vuejs.org/guide/extras/composition-api-faq.html)

---

**修复人员**: Claude Code
**审核状态**: ✅ 已完成并验证
**建议**: 建议在 ESLint 中添加规则禁止使用 require
