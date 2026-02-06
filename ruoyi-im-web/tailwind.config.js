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
        // 钉钉品牌色 DingTalk Blue
        primary: {
          DEFAULT: '#0089FF',
          hover: '#006ECC',
          active: '#0057A3',
          light: 'rgba(0, 137, 255, 0.1)',
          bg: '#E5F2FF'
        },
        // 背景色
        background: {
          light: '#F5F7FA',
          dark: '#0f172a'
        },
        // 语义色（钉钉规范）
        success: '#00C853',
        warning: '#FF9800',
        error: '#F44336',
        danger: '#F44336',
        // Slate 色系（用于暗色模式）
        slate: {
          50: '#F8FAFC',
          100: '#F1F5F9',
          200: '#E2E8F0',
          300: '#CBD5E1',
          400: '#94A3B8',
          500: '#64748B',
          600: '#475569',
          700: '#334155',
          800: '#1E293B',
          900: '#0F172A',
          950: '#020617'
        },
        // 钉钉灰色系
        gray: {
          50: '#FAFBFC',
          100: '#F5F7FA',
          200: '#EEF1F6',
          300: '#E5E9EF',
          400: '#CED4DB',
          500: '#A0A8B8',
          600: '#858E9E',
          700: '#5F6672',
          800: '#373D45',
          900: '#171A1D'
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
      fontSize: {
        '2xl': ['1.5rem', { lineHeight: '2rem' }],
        '3xl': ['1.875rem', { lineHeight: '2.25rem' }],
        '4xl': ['2.25rem', { lineHeight: '2.5rem' }],
        '5xl': ['3rem', { lineHeight: '1' }],
        '6xl': ['3.75rem', { lineHeight: '1' }]
      },
      fontWeight: {
        thin: '100',
        extralight: '200',
        light: '300',
        normal: '400',
        medium: '500',
        semibold: '600',
        bold: '700',
        extrabold: '800',
        black: '900'
      },
      boxShadow: {
        xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        '2xl': '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        '3xl': '0 35px 60px -15px rgba(0, 0, 0, 0.3)',
        inner: 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.06)',
        // 钉钉风格阴影
        ding: '0 2px 8px rgba(0, 0, 0, 0.08)',
        'ding-lg': '0 4px 16px rgba(0, 0, 0, 0.12)',
        'ding-xl': '0 8px 24px rgba(0, 0, 0, 0.16)'
      },
      animation: {
        'bounce-slow': 'bounce 3s infinite',
        'fade-in': 'fadeIn 0.5s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'slide-down': 'slideDown 0.3s ease-out'
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' }
        },
        slideUp: {
          '0%': { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' }
        },
        slideDown: {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' }
        },
        rotate: {
          '0%': { transform: 'rotate(0deg)' },
          '100%': { transform: 'rotate(90deg)' }
        },
        pulse: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.5' }
        },
        bounce: {
          '0%, 20%, 50%, 80%, 100%': { transform: 'translateY(0)' },
          '40%': { transform: 'translateY(-10px)' },
          '60%': { transform: 'translateY(-5px)' }
        }
      },
      transformOrigin: {
        '0': '0',
        'center': 'center',
        'full': '100% 100%'
      },
      strokeWidth: {
        '0.5': '0.5px',
        '1': '1px',
        '2': '2px',
        '3': '3px',
        '4': '4px'
      },
      lineHeight: {
        '3': '0.75rem',
        '10': '2.5rem',
        '11': '2.75rem',
        '12': '3rem',
        '16': '4rem'
      },
      letterSpacing: {
        tighter: '-0.05em',
        tight: '-0.025em',
        normal: '0em',
        wide: '0.025em',
        wider: '0.05em',
        widest: '0.1em'
      },
      scale: {
        '0': '0',
        '50': '.5',
        '75': '.75',
        '90': '.9',
        '95': '.95',
        '100': '1',
        '105': '1.05',
        '110': '1.1',
        '120': '1.2',
        '125': '1.25',
        '150': '1.5'
      },
      translate: {
        '0': '0',
        '1': '0.25rem',
        '2': '0.5rem',
        '3': '0.75rem',
        '4': '1rem',
        '5': '1.25rem',
        '6': '1.5rem',
        '8': '2rem',
        '10': '2.5rem',
        '12': '3rem',
        '16': '4rem',
        '20': '5rem',
        '24': '6rem',
        '1/2': '50%',
        '1/3': '33.333333%',
        '2/3': '66.666667%',
        'full': '100%'
      },
      skew: {
        '0': '0deg',
        '1': '0.05turn',
        '2': '0.1turn',
        '3': '0.15turn',
        '6': '0.3turn',
        '12': '0.6turn'
      },
      opacity: {
        '0': '0',
        '5': '0.05',
        '10': '0.1',
        '20': '0.2',
        '25': '0.25',
        '30': '0.3',
        '40': '0.4',
        '50': '0.5',
        '60': '0.6',
        '70': '0.7',
        '75': '0.75',
        '80': '0.8',
        '90': '0.9',
        '95': '0.95',
        '100': '1'
      },
      ringColor: {
        DEFAULT: '#1677ff',
        primary: '#1677ff',
        white: '#ffffff',
        slate: {
          800: '#1e293b'
        }
      },
      ringWidth: {
        DEFAULT: '1px',
        '0': '0px',
        '1': '1px',
        '2': '2px',
        '3': '3px',
        '4': '4px',
        '8': '8px'
      },
      ringOffsetWidth: {
        '0': '0px',
        '1': '1px',
        '2': '2px',
        '4': '4px',
        '8': '8px'
      },
      transitionTimingFunction: {
        DEFAULT: 'cubic-bezier(0.4, 0, 0.2, 1)',
        'in': 'cubic-bezier(0.4, 0, 1, 1)',
        'out': 'cubic-bezier(0, 0, 0.2, 1)',
        'in-out': 'cubic-bezier(0.4, 0, 0.2, 1)'
      },
      transitionDuration: {
        '0': '0ms',
        '75': '75ms',
        '100': '100ms',
        '150': '150ms',
        '200': '200ms',
        '250': '250ms',
        '300': '300ms',
        '500': '500ms',
        '700': '700ms',
        '1000': '1000ms'
      },
      divideColor: {
        DEFAULT: '#e5e7eb',
        slate: {
          50: '#f8fafc',
          100: '#f1f5f9',
          800: '#1e293b',
          80050: 'rgba(30, 41, 59, 0.5)'
        }
      },
      divideOpacity: {
        DEFAULT: '1',
        '0': '0',
        '5': '0.05',
        '10': '0.1',
        '20': '0.2',
        '30': '0.3',
        '40': '0.4',
        '50': '0.5',
        '60': '0.6',
        '70': '0.7',
        '80': '0.8',
        '90': '0.9',
        '100': '1'
      }
    }
  },
  plugins: []
}
