import globals from 'globals'
import pluginJs from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import stylistic from '@stylistic/eslint-plugin'

/** @type {import('eslint').Linter.Config[]} */
export default [
  // 基础配置
  {
    name: 'app/files-to-lint',
    files: ['**/*.{js,mjs,jsx,vue}'],
  },
  {
    name: 'app/files-to-ignore',
    ignores: ['**/dist/**', '**/node_modules/**', '**/.vite/**'],
  },
  // JavaScript 推荐规则
  pluginJs.configs.recommended,
  // Vue3 推荐规则 (严格模式)
  ...pluginVue.configs['flat/recommended'],
  // Stylistic 格式规则
  stylistic.configs.recommended,
  // 自定义规则
  {
    name: 'app/custom-rules',
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.es2021,
      },
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
    rules: {
      // ========== Vue 相关规则 (阿里巴巴规范) ==========
      // 组件名必须是多单词 (防止与 HTML 标签冲突)
      'vue/multi-word-component-names': ['error', {
        ignores: ['index', 'Login', 'Main', 'Chat']
      }],
      // Props 必须定义类型
      'vue/require-prop-types': 'error',
      // Props 默认值必须有效
      'vue/require-default-prop': 'error',
      // v-for 必须搭配 key
      'vue/require-v-for-key': 'error',
      // 禁止在 v-for 中使用 v-if
      'vue/no-use-v-if-with-v-for': 'error',
      // Template 中不允许使用 this
      'vue/this-in-template': 'error',
      // 组件属性顺序
      'vue/attributes-order': ['error', {
        order: [
          'DEFINITION',
          'LIST_RENDERING',
          'CONDITIONALS',
          'RENDER_MODIFIERS',
          'GLOBAL',
          'UNIQUE',
          'TWO_WAY_BINDING',
          'OTHER_DIRECTIVES',
          'OTHER_ATTR',
          'EVENTS',
          'CONTENT',
        ],
        alphabetical: false,
      }],
      // 组件/实例选项顺序
      'vue/order-in-components': ['error', {
        order: [
          'el',
          'name',
          'key',
          'parent',
          'functional',
          ['delimiters', 'comments'],
          ['components', 'directives', 'filters'],
          'extends',
          'mixins',
          ['provide', 'inject'],
          'ROUTER_GUARDS',
          'layout',
          'middleware',
          'validate',
          'scrollToTop',
          'transition',
          'loading',
          'inheritAttrs',
          'model',
          ['props', 'propsData'],
          'emits',
          'setup',
          'asyncData',
          'data',
          'fetch',
          'head',
          'computed',
          'watch',
          'watchQuery',
          'LIFECYCLE_HOOKS',
          'methods',
          ['template', 'render'],
          'renderError',
        ],
      }],
      // 使用 ~ 替代 deprecated ::v-deep
      'vue/deep-selector-style': 'warn',
      // Template 缩进
      'vue/html-indent': ['error', 2, {
        attribute: 1,
        baseIndent: 1,
        closeBracket: 0,
        alignAttributesVertically: true,
        ignores: [],
      }],
      // 自闭合标签风格
      'vue/html-self-closing': ['error', {
        html: {
          void: 'never',
          normal: 'always',
          component: 'always',
        },
        svg: 'always',
        math: 'always',
      }],
      // 每行最多属性数
      'vue/max-attributes-per-line': ['error', {
        singleline: { max: 3 },
        multiline: { max: 1 },
      }],
      // 必须有空行分隔 script/template/style
      'vue/padding-line-between-blocks': 'error',
      // 强制使用简写
      'vue/prefer-separate-static-class': 'warn',
      // 必须使用 defineEmits 和 defineProps
      'vue/require-macro-variable-name': 'error',
      // Setup 中禁止直接使用 props
      'vue/no-setup-props-destructure': 'error',

      // ========== JavaScript 规范 ==========
      // 强制使用 === 和 !==
      'eqeqeq': ['error', 'always', { null: 'ignore' }],
      // 禁止 var
      'no-var': 'error',
      // 优先使用 const
      'prefer-const': ['error', {
        destructuring: 'any',
        ignoreReadBeforeAssign: false,
      }],
      // 禁止未使用变量
      'no-unused-vars': ['error', {
        vars: 'all',
        args: 'after-used',
        caughtErrors: 'none',
        ignoreRestSiblings: true,
      }],
      // 禁止 console (生产环境)
      'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
      // 禁止 debugger
      'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
      // 对象简写
      'object-shorthand': ['error', 'always'],
      // 优先使用解构
      'prefer-destructuring': ['warn', {
        array: true,
        object: true,
      }, {
        enforceForRenamedProperties: false,
      }],
      // 禁止多余空格
      'no-trailing-spaces': 'error',
      // 缩进
      'indent': ['error', 2, { SwitchCase: 1 }],
      // 引号
      'quotes': ['error', 'single', { allowTemplateLiterals: true }],
      // 分号
      'semi': ['error', 'never'],
      // 逗号
      'comma-dangle': ['error', 'never'],
      // 括号空格
      'space-before-function-paren': ['error', {
        anonymous: 'always',
        named: 'never',
        asyncArrow: 'always',
      }],
      // 块语句空格
      'space-before-blocks': ['error', 'always'],
      // 关键字空格
      'keyword-spacing': ['error', { before: true, after: true }],
      // 操作符空格
      'space-infix-ops': 'error',
      // 对象大括号空格
      'object-curly-spacing': ['error', 'always'],
      // 数组中括号空格
      'array-bracket-spacing': ['error', 'never'],
      // 函数调用参数空格
      'func-call-spacing': ['error', 'never'],
      // 行尾空格
      'eol-last': ['error', 'always'],
      // 最大行长度
      'max-len': ['warn', {
        code: 120,
        tabWidth: 2,
        ignoreUrls: true,
        ignoreStrings: true,
        ignoreTemplateLiterals: true,
        ignoreRegExpLiterals: true,
      }],

      // ========== Stylistic 规则 ==========
      '@stylistic/indent': ['error', 2],
      '@stylistic/quotes': ['error', 'single'],
      '@stylistic/semi': ['error', 'never'],
      '@stylistic/comma-dangle': ['error', 'never'],
      '@stylistic/space-before-function-paren': ['error', {
        anonymous: 'always',
        named: 'never',
        asyncArrow: 'always',
      }],
    },
  },
]
