/**
 * 阿里巴巴前端样式规范配置
 * 参考：https://github.com/alibaba/f2e-spec
 * 对标：钉钉/野火IM 样式标准
 */

module.exports = {
  extends: [
    'stylelint-config-standard',
    'stylelint-config-recess-order',
    'stylelint-config-prettier'
  ],
  plugins: ['stylelint-scss'],
  rules: {
    // ==================== 基础规范 ====================
    'indentation': 2, // 2 空格缩进
    'string-quotes': 'single', // 单引号
    'no-duplicate-selectors': true, // 禁止重复选择器
    'color-hex-length': 'long', // 使用完整 hex 颜色（6位）
    'color-named': 'never', // 禁止使用命名颜色（如 red, blue）
    'selector-class-pattern': '^[a-z][a-z0-9-]*(__[a-z0-9-]+)?(--[a-z0-9-]+)?$', // BEM 命名规范

    // ==================== Design Tokens 强制使用 ====================
    'color-no-hex': [
      true,
      {
        message: '请使用 Design Tokens (var(--dt-*)) 替代硬编码颜色',
        ignoreProperties: ['content'], // content 属性允许
        ignore: [
          '/^--dt-/', // 允许定义 Design Tokens
          '/transparent/', // 允许 transparent
          '/rgba?\\s*\\(\\s*0\\s*,\\s*0\\s*,\\s*0\\s*,\\s*0\\s*\\)/', // 允许 rgba(0,0,0,0)
          '/rgba?\\s*\\(\\s*255\\s*,\\s*255\\s*,\\s*255\\s*,\\s*[01]\\s*\\)/' // 允许白色
        ]
      }
    ],

    // ==================== 禁止 !important ====================
    'declaration-no-important': [
      true,
      {
        message: '禁止使用 !important，请提高选择器特异性或使用 CSS 变量',
        ignore: [
          // 允许的场景
          '/\\.a11y-reduced-motion/', // 无障碍模式
          '/\\:global\\(.+\\)/', // 覆盖第三方库
          '/\\.dark/', // 暗色模式覆盖
          '/\\.hidden/' // 工具类
        ]
      }
    ],

    // ==================== 禁止内联样式 ====================
    'selector-max-compound-selectors': 4, // 限制选择器嵌套深度
    'selector-max-id': 0, // 禁止使用 ID 选择器
    'selector-max-type': 3, // 限制标签选择器数量

    // ==================== SCSS 规范 ====================
    'scss/at-rule-no-unknown': [
      true,
      {
        ignoreAtRules: ['use', 'forward', 'mixin', 'include', 'each', 'for', 'if', 'else', 'function', 'return']
      }
    ],
    'scss/dollar-variable-default': null, // 允许变量默认值
    'scss/dollar-variable-pattern': '^dt-[a-z][a-z0-9-]*$', // 变量命名规范
    'scss/at-mixin-pattern': '^[a-z][a-z0-9-]*$', // Mixin 命名规范

    // ==================== 单位规范 ====================
    'unit-allowed-list': ['px', '%', 'em', 'rem', 'vw', 'vh', 'deg', 's', 'ms'], // 允许的单位
    'length-zero-no-unit': true, // 0 不带单位
    'shorthand-property-no-redundant-values': true, // 简写属性不写冗余值

    // ==================== 其他规范 ====================
    'max-nesting-depth': 3, // 最大嵌套深度
    'no-descending-specificity': true, // 禁止特异性下降
    'no-empty-source': true, // 禁止空样式块
    'no-invalid-double-slash-comments': true, // 禁止无效的 // 注释

    // ==================== 钉钉/野火IM 特定规则 ====================
    'property-no-vendor-prefix': true, // 禁止浏览器前缀（使用 PostCSS 自动添加）
    'value-no-vendor-prefix': true,
    'selector-no-vendor-prefix': true
  },
  ignoreFiles: [
    // 忽略第三方库样式
    'node_modules/**',
    'dist/**',
    // 忽略自动生成的文件
    '**/auto-imports.d.ts',
    '**/components.d.ts'
  ],
  overrides: [
    {
      files: ['**/*.scss'],
      rules: {
        'at-rule-no-unknown': null // SCSS 允许 @use, @mixin 等
      }
    },
    {
      files: ['**/*.vue'],
      rules: {
        'selector-pseudo-class-no-unknown': [
          true,
          {
            ignorePseudoClasses: ['deep', 'global', 'slotted'] // Vue 3 特有伪类
          }
        ]
      }
    }
  ]
}
