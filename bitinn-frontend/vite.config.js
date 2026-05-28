import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    // 自动导入 Vue/Pinia API（ref, computed, onMounted 等无需手动 import）
    AutoImport({
      imports: ['vue', 'pinia'],
      dts: false,
      eslintrc: false,
    }),
    // Element Plus 按需自动导入（只打包用到的组件）
    Components({
      resolvers: [ElementPlusResolver()],
      dts: false,
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true,
      },
    },
  },
  build: {
    // 启用 CSS 代码分割
    cssCodeSplit: true,
    // 启用源码映射（生产可改为 'hidden-source-map' 防盗用）
    sourcemap: false,
    // 关闭警告阈值
    chunkSizeWarningLimit: 500,
    rollupOptions: {
      output: {
        // 手动代码分割策略
        manualChunks: {
          // Vue 生态单独打包
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // Element Plus 单独打包
          'element-plus': ['element-plus'],
          // 编辑器单独打包（富文本编辑器体积大）
          'editor': ['@vueup/vue-quill', 'vditor'],
          // Axios 单独打包
          'axios': ['axios'],
          // 其他工具库
          'utils': ['pinia-persistedstate-plugin'],
        },
        // 静态资源命名哈希
        assetFileNames: 'assets/[name]-[hash][extname]',
        chunkFileNames: 'chunks/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
      },
    },
    // 压缩配置
    minify: 'esbuild',
    esbuildOptions: {
      drop: ['console', 'debugger'],
    },
  },
  // 优化依赖预构建
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      'element-plus',
    ],
  },
})
