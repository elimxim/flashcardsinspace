import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import fs from 'node:fs'
import path from 'node:path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      https: {
        key: fs.readFileSync(path.resolve(__dirname, env.VITE_TLS_KEY_FILE)),
        cert: fs.readFileSync(path.resolve(__dirname, env.VITE_TLS_CERT_FILE)),
      },
      port: 5174,
      proxy: {
        '/api': {
          target: 'https://localhost:8443',
          changeOrigin: true,
          secure: false,
        }
      }
    }
  }
})
