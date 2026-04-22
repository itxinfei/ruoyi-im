import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler',
        additionalData: `@use "@/styles/_mixins.scss" as *;\n`
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            // Vue 核心
            if (id.includes('vue') || id.includes('vuex') || id.includes('vue-router')) {
              return 'vue-vendor'
            }
            // Element Plus
            if (id.includes('element-plus')) {
              return 'element-plus'
            }
            // ECharts
            if (id.includes('echarts') || id.includes('zrender')) {
              return 'echarts'
            }
            // 工具库
            if (id.includes('axios') || id.includes('dayjs') || id.includes('dompurify')) {
              return 'utils'
            }
          }
        }
      }
    },
    chunkSizeWarningLimit: 800
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
