import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import AutoImport from 'unplugin-auto-import/vite'
import viteCompression from 'vite-plugin-compression'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

// ESM 兼容的 __dirname 替代方案
const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

export default defineConfig({
  plugins: [
    vue(),
    // 自动导入 Vue 相关函数（ref, computed 等）
    AutoImport({
      imports: ['vue', 'vue-router', 'vuex'],
      dts: 'src/auto-imports.d.ts',
      eslintrc: {
        enabled: true // 生成 .eslintrc-auto-import.json
      },
      // 只监听相关文件变化
      include: [
        /\.[jt]sx?$/, // .js, .ts, .jsx, .tsx
        /\.vue$/,
        /\.vue\?vue/
      ],
      exclude: [
        /node_modules/,
        /dist/,
        /\.git/
      ]
    }),
    // 自动导入组件（包括 Element Plus 组件）
    Components({
      resolvers: [
        ElementPlusResolver({
          // 自动导入样式
          importStyle: 'sass',
          // 按需导入
          useBuiltInComponents: true
        })
      ],
      dts: 'src/components.d.ts',
      // 只扫描 components 目录，避免重复扫描
      include: [/\.vue$/, /\.vue\?vue/],
      // 排除不需要自动注册的目录
      exclude: [
        /node_modules/,
        /dist/,
        /\.git/,
        /tests/
        // views 目录需要自动导入 Element Plus 组件
        // /src\/views/,
        // composables 目录不需要组件自动导入
        // /src\/composables/
      ],
      // 减少深度扫描
      deep: false,
      // 禁用生成时的日志
      silent: false
    }),
    // PWA 插件
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'robots.txt', 'apple-touch-icon.png'],
      manifest: {
        name: '若依IM',
        short_name: 'RuoYi IM',
        description: '企业级即时通讯系统',
        theme_color: '#ffffff',
        background_color: '#ffffff',
        display: 'standalone',
        icons: [
          {
            src: '/logo-192.png',
            sizes: '192x192',
            type: 'image/png'
          },
          {
            src: '/logo-512.png',
            sizes: '512x512',
            type: 'image/png'
          }
        ]
      },
      workbox: {
        // 增加最大文件大小限制（CSS 文件较大）
        maximumFileSizeToCacheInBytes: 5 * 1024 * 1024, // 5MB

        // 缓存策略
        runtimeCaching: [
          {
            // 静态资源（JS、CSS、图片等）
            urlPattern: /^https:\/\/js\.delivr\.net\/.*/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'jsdelivr-cdn',
              expiration: {
                maxEntries: 100,
                maxAgeSeconds: 60 * 60 * 24 * 30 // 30 天
              },
              cacheableResponse: {
                statuses: [0, 200]
              }
            }
          },
          {
            // API 请求（使用 NetworkFirst，确保数据新鲜）
            urlPattern: /^\/api\/.*/i,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              networkTimeoutSeconds: 10,
              expiration: {
                maxEntries: 100,
                maxAgeSeconds: 60 * 60 * 24 // 1 天
              },
              cacheableResponse: {
                statuses: [0, 200]
              }
            }
          },
          {
            // 图片资源
            urlPattern: /\.(?:png|jpg|jpeg|svg|gif|webp|ico)$/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'image-cache',
              expiration: {
                maxEntries: 200,
                maxAgeSeconds: 60 * 60 * 24 * 30 // 30 天
              }
            }
          }
        ],
        // 清理过时的缓存
        cleanupOutdatedCaches: true,
        // 导航预加载
        navigateFallback: null
      },
      devOptions: {
        enabled: false // 开发环境不启用 SW
      }
    }),
    // Gzip 和 Brotli 压缩
    viteCompression({
      verbose: true, // 输出压缩信息
      algorithm: 'gzip',
      ext: '.gz',
      threshold: 10240, // 只压缩大于 10KB 的文件
      deleteOriginFile: false // 保留原文件
    }),
    viteCompression({
      verbose: true,
      algorithm: 'brotliCompress',
      ext: '.br',
      threshold: 10240,
      deleteOriginFile: false
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  // 优化依赖预构建
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'vuex',
      'axios',
      'element-plus',
      '@element-plus/icons-vue',
      'dompurify',
      'dayjs'
    ],
    exclude: ['@vueuse/core']
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler'
      }
    },
    // 启用 CSS 代码分割
    codeSplit: true,
    // CSS 模块化
    modules: {
      // 将全局样式提取到单独文件
      globalCssPaths: [
        'src/styles/global.scss',
        'src/styles/animations.scss'
      ]
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
    // 代码分割优化（简化版本，避免循环依赖）
    rollupOptions: {
      output: {
        manualChunks: {
          // Element Plus 单独打包
          'element-plus': ['element-plus'],
          // Vue 生态单独打包
          'vue-vendor': ['vue', 'vue-router', 'vuex', '@vueuse/core'],
          // 工具库单独打包
          'utils': ['axios', 'dayjs', 'dompurify']
        },
        // 分块文件名命名规则
        chunkFileNames: 'assets/[name]-[hash].js',
        entryFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    },
    // 压缩报告
    reportCompressedSize: false,
    // 调整 chunk 大小警告阈值
    chunkSizeWarningLimit: 1000
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    open: true,
    // 严格端口（避免端口占用）
    strictPort: false,
    // 预热常用文件，加快启动
    warmup: {
      clientFiles: [
        './src/main.js',
        './src/App.vue',
        './src/views/MainPage.vue',
        './src/router/index.js',
        './src/store/index.js'
      ]
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8888',
        changeOrigin: true,
        // 代理超时配置
        timeout: 30000
      }
    }
  },
  // 启用文件系统缓存
  fs: {
    allow: ['..'],
    strict: false,
    maxCacheSize: 100 * 1024 * 1024, // 100MB
    // 排除不需要扫描的目录
    deny: ['node_modules', '.git', 'tests']
  },
  test: {
    environment: 'jsdom',
    globals: true
  }
})
