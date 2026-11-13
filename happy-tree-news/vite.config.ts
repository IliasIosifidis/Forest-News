import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import tailwindcss from '@tailwindcss/vite'

// The backend sends no CORS headers, so the browser calls `/api` on the dev server and Vite forwards it.
// The first matching prefix wins, so the per-service prefixes must stay above `/api`.
const apiProxy = {
  // search-service is not exposed on the live host yet; this expects it running locally.
  '/api/search': {
    target: 'http://localhost:8082',
  },
  '/api/activity': {
    target: 'http://localhost:8082',
  },
  '/api/digests': {
    target: 'http://localhost:8083',
  },
  '/api': {
    target: 'http://localhost:8081',
    changeOrigin: true,
  },
}

// https://vite.dev/config/
export default defineConfig({
  server: {
    proxy: apiProxy,
  },
  preview: {
    proxy: apiProxy,
  },
  plugins: [
    vue(),
    vueDevTools(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})
