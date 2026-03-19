import globals from 'globals'
import pluginJs from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'

// 判断是否为生产环境
const isProduction = process.env.NODE_ENV === 'production'

/** @type {import('eslint').Linter.Config[]} */
export default [
  // 基础配置
  {
    name: 'app/files-to-lint',
    files: ['**/*.{js,mjs,jsx,vue}']
  },
  {
    name: 'app/files-to-ignore',
    ignores: [
      '**/dist/**',
      '**/node_modules/**',
      '**/.vite/**',
      'postcss.config.cjs',
      'tailwind.config.js',
      'vite.config.js'
    ]
  },
  // JavaScript 推荐规则
  pluginJs.configs.recommended,
  // Vue3 推荐规则 (严格模式)
  ...pluginVue.configs['flat/recommended'],
  // 自定义规则
  {
    name: 'app/custom-rules',
    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.es2021,
        ...globals.node
      },
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module'
      }
    },
    rules: {
      // ========== Vue 相关规则 ==========
      'vue/multi-word-component-names': ['error', {
        ignores: ['index', 'Login', 'Main', 'Chat', 'Dashboard']
      }],
      'vue/require-v-for-key': 'error',
      'vue/no-use-v-if-with-v-for': 'error',
      'vue/this-in-template': 'error',
      'vue/html-indent': ['error', 2, {
        attribute: 1,
        baseIndent: 1,
        closeBracket: 0,
        alignAttributesVertically: true,
        ignores: []
      }],
      'vue/html-self-closing': ['error', {
        html: {
          void: 'never',
          normal: 'always',
          component: 'always'
        },
        svg: 'always',
        math: 'always'
      }],
      'vue/max-attributes-per-line': ['error', {
        singleline: { max: 3 },
        multiline: { max: 1 }
      }],
      'vue/padding-line-between-blocks': 'error',
      // Props 默认值警告降级
      'vue/require-default-prop': 'off',
      // v-html 警告（已知安全）
      'vue/no-v-html': 'off',
      // 事件声明警告
      'vue/require-explicit-emits': 'off',
      // 模板变量阴影警告
      'vue/no-template-shadow': 'off',

      // ========== JavaScript 规范 ==========
      'eqeqeq': ['error', 'always', { null: 'ignore' }],
      'no-var': 'error',
      'prefer-const': ['error', {
        destructuring: 'any',
        ignoreReadBeforeAssign: false
      }],
      'no-unused-vars': ['error', {
        vars: 'all',
        args: 'after-used',
        caughtErrors: 'none',
        ignoreRestSiblings: true,
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_'
      }],
      // 允许未定义的全局变量（Vue 模板中使用）
      'no-undef': ['off'],
      'no-console': isProduction ? 'warn' : 'off',
      'no-debugger': isProduction ? 'error' : 'off',
      'object-shorthand': ['error', 'always'],
      'prefer-destructuring': 'off',
      'no-trailing-spaces': 'error',
      'indent': ['error', 2, { SwitchCase: 1 }],
      'quotes': ['error', 'single', { allowTemplateLiterals: true }],
      'semi': ['error', 'never'],
      'comma-dangle': ['error', 'never'],
      'space-before-function-paren': ['error', {
        anonymous: 'always',
        named: 'never',
        asyncArrow: 'always'
      }],
      'space-before-blocks': ['error', 'always'],
      'keyword-spacing': ['error', { before: true, after: true }],
      'space-infix-ops': 'error',
      'object-curly-spacing': ['error', 'always'],
      'array-bracket-spacing': ['error', 'never'],
      'func-call-spacing': ['error', 'never'],
      'eol-last': ['error', 'always'],
      'max-len': ['off']
    }
  }
]
