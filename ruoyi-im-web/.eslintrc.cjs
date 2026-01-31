module.exports = {
  root: true,
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended'
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module'
  },
  rules: {
    // ====================
    // Vue 3 规则
    // ====================
    'vue/multi-word-component-names': 'off', // 允许单词组件名
    'vue/no-v-html': 'warn', // 使用 v-html 时警告（应使用 DOMPurify）
    'vue/require-default-prop': 'off', // 不强制要求 props 默认值
    'vue/require-prop-types': 'off', // 不强制要求 props 类型定义
    'vue/no-unused-vars': 'warn', // 未使用的变量警告

    // ====================
    // 代码质量规则
    // ====================
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-unused-vars': ['warn', {
      argsIgnorePattern: '^_',
      varsIgnorePattern: '^_'
    }],
    'no-var': 'error', // 禁止使用 var
    'prefer-const': 'warn', // 优先使用 const
    'prefer-arrow-callback': 'warn', // 优先使用箭头函数

    // ====================
    // 代码重复防护规则
    // ====================
    'no-duplicate-imports': 'error', // 禁止重复导入
    'no-useless-concat': 'warn', // 禁止无意义的字符串拼接

    // ====================
    // 代码风格规则
    // ====================
    'semi': ['error', 'never'], // 不使用分号
    'quotes': ['error', 'single', { avoidEscape: true }], // 使用单引号
    'indent': 'off', // 格式化交给 Prettier
    'arrow-parens': ['error', 'as-needed'], // 箭头函数参数括号
    'comma-dangle': ['error', 'never'], // 不允许尾随逗号

    // ====================
    // 最佳实践规则
    // ====================
    'eqeqeq': ['error', 'always'], // 强制使用 ===
    'curly': ['error', 'all'], // 强制使用大括号
    'no-eval': 'error', // 禁止使用 eval
    'no-implied-eval': 'error', // 禁止隐式 eval
    'no-new-func': 'error', // 禁止 new Function
    'no-return-await': 'off', // 允许 return await（便于错误处理）

    // ====================
    // 性能规则
    // ====================
    'no-loop-func': 'warn', // 循环中定义函数警告
    'no-await-in-loop': 'warn', // 循环中的 await 警告

    // ====================
    // 安全规则
    // ====================
    'no-script-url': 'error', // 禁止 javascript: URL
  },
  globals: {
    // PWA 相关全局变量
    '__SW_UPDATE__': 'readonly',
    '__SW_ENABLED__': 'readonly'
  }
}
