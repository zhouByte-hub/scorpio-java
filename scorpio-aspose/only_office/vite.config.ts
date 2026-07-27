import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/aspose': {
        target: 'http://localhost:9527',
        changeOrigin: true,
      },
      // OnlyOffice Document Server 代理
      '/web-apps': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/cache': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/coauthoring': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/spellchecker': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus', '@element-plus/icons-vue'],
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'pdf-viewer': ['vue-pdf-embed'],
        },
      },
    },
  },
})
