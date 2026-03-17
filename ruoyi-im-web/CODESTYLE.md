# RuoYi-IM 前端代码规范 (阿里巴巴规范)

## 安装依赖

```bash
cd ruoyi-im-web
npm install --save-dev eslint @eslint/js eslint-plugin-vue @stylistic/eslint-plugin globals prettier
```

## 运行检查

```bash
# 检查所有文件
npm run lint

# 自动修复
npm run lint:fix

# 格式化代码
npm run format
```

## package.json 添加脚本

```json
{
  "scripts": {
    "lint": "eslint . --ext .js,.jsx,.vue",
    "lint:fix": "eslint . --ext .js,.jsx,.vue --fix",
    "format": "prettier --write \"src/**/*.{js,vue,scss,css,json}\""
  }
}
```

## 规范总览

### Vue 组件规范

1. **组件名**: 使用多单词命名 (防止与 HTML 标签冲突)
   - ✅ `UserProfile` `ChatMessage`
   - ❌ `User` `Message`

2. **Props 定义**: 必须指定类型和默认值
   ```vue
   const props = defineProps({
     userId: { type: String, required: true },
     showAvatar: { type: Boolean, default: true }
   })
   ```

3. **事件命名**: 使用 kebab-case
   ```vue
   emit('user-clicked')  // 不是 userClicked
   ```

4. **v-for 必须带 key**
   ```vue
   <div v-for="item in list" :key="item.id">
   ```

5. **组件属性顺序**
   - is, v-for, v-if/v-else-if/v-else, v-show, v-cloak
   - ref, key, slot
   - v-model, v-on
   - v-html, v-text

### JavaScript 规范

1. **使用 === 替代 ==**
2. **优先使用 const, 其次 let, 禁止 var**
3. **使用对象简写**
   ```js
   const obj = { foo, bar }  // 不是 { foo: foo, bar: bar }
   ```
4. **箭头函数简写**
   ```js
   const fn = x => x * 2  // 不是 (x) => { return x * 2 }
   ```

### 代码风格

- 缩进: 2 空格
- 引号: 单引号
- 分号: 不使用
- 行尾逗号: 不使用
- 最大行宽: 120

### 文件组织

```
src/
├── api/           # API 接口
├── assets/        # 静态资源
├── components/    # 公共组件
│   ├── Chat/
│   ├── Common/
│   └── Contacts/
├── composables/   # 组合式函数
├── router/        # 路由配置
├── store/         # Vuex Store
├── styles/        # 全局样式
├── utils/         # 工具函数
└── views/         # 页面组件
```
