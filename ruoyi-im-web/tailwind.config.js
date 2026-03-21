/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        // 品牌颜色 - 钉钉风格
        primary: {
          DEFAULT: '#0089ff',
          hover: '#33a1ff',
          active: '#006ed6',
          light: 'rgba(0, 137, 255, 0.1)',
          bg: '#f0f7ff'
        },
        // 背景色 - 钉钉风格
        background: {
          light: '#f5f6f7',
          dark: '#0f172a'
        },
        // 语义颜色 - 钉钉风格
        success: '#00b578',
        warning: '#ff8f1f',
        error: '#f54a45',
        danger: '#f54a45',
        // Slate 色系（用于暗色模式）
        slate: {
          50: '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          300: '#cbd5e1',
          400: '#94a3b8',
          500: '#64748b',
          600: '#475569',
          700: '#334155',
          800: '#1e293b',
          900: '#0f172a',
          950: '#020617'
        }
      },
      borderRadius: {
        DEFAULT: '8px',
        sm: '4px',
        md: '8px',
        lg: '12px',
        xl: '16px',
        '2xl': '20px',
        full: '50%'
      },
      spacing: {
        '18': '4.5rem',
        '88': '22rem',
        '128': '32rem'
      },
      zIndex: {
        'dropdown': 1000,
        'sticky': 1020,
        'fixed': 1030,
        'modal': 1040,
        'popover': 1050,
        'tooltip': 1060,
        'notification': 1070
      },
      transitionDuration: {
        '250': '250ms'
      }
    }
  },
  plugins: []
}
