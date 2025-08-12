import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv, PluginOption } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import fs from 'node:fs'
import path from 'node:path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  const plugins: PluginOption[] = [vue()]

  if (mode !== 'production') {
    plugins.push(vueDevTools())
  }

  return {
    plugins: plugins,
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    build: {
      outDir: '../../../build/resources/main/static',
      emptyOutDir: true,
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
        },
        '/api-public': {
          target: 'https://localhost:8443',
          changeOrigin: true,
          secure: false,
        },
        '/auth': {
          target: 'https://localhost:8443',
          changeOrigin: true,
          secure: false,
        },
      }
    }
  }
})
