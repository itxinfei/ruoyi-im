import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

// ESM 兼容的 __dirname 替代方案
const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

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
        api: 'modern-compiler'
      }
    }
  },
  // 生产环境优化配置
  build: {
    // 移除 console 和 debugger
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
        // 保留 error 级别的日志
        pure_funcs: ['console.log', 'console.warn', 'console.info']
      }
    },
    // 代码分割优化
    rollupOptions: {
      output: {
        manualChunks: {
          // Element Plus 单独打包
          'element-plus': ['element-plus', '@element-plus/icons-vue'],
          // Vue 生态单独打包
          'vue-vendor': ['vue', 'vue-router', 'vuex'],
          // 工具库单独打包
          'utils': ['axios', 'dayjs', '@vueuse/core']
        }
      }
    },
    // 压缩报告
    reportCompressedSize: false
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
