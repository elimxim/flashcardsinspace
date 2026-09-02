import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv, PluginOption } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

const PRELOADED_FONT_FILES = [
  'lato-latin-400-normal',
  'quicksand-latin-wght-normal',
  'nunito-latin-wght-normal',
]

/**
 * Adds one tag per entry of PRELOADED_FONT_FILES to the built index.html:
 *
 *   <link rel="preload" as="font" type="font/woff2"
 *         href="/assets/lato-latin-400-normal-BEhtfm5r.woff2" crossorigin="">
 *
 * The browser then fetches those faces while it parses the HTML, rather than a
 * round trip later once it has read the stylesheet that references them.
 *
 * It runs over the finished bundle because that is the first point at which the
 * hashed file names exist, and fails the build if an entry matches none of them.
 */
function preloadFonts(): PluginOption {
  return {
    name: 'preload-fonts',
    apply: 'build',
    transformIndexHtml: {
      order: 'post',
      handler(_html, ctx) {
        const emitted = Object.keys(ctx.bundle ?? {}).filter((file) => file.endsWith('.woff2'))

        const files = PRELOADED_FONT_FILES.map((name) => {
          const match = emitted.find((file) => file.includes(name))
          if (!match) {
            throw new Error(
              `preload-fonts: no built font matches '${name}'.
              Update PRELOADED_FONT_FILES in vite.config.ts.
              Available: ${emitted.join(', ')}`,
            )
          }
          return match
        })

        return files.map((file) => ({
          tag: 'link',
          injectTo: 'head-prepend' as const,
          attrs: {
            rel: 'preload',
            as: 'font',
            type: 'font/woff2',
            href: `/${file}`,
            // Fonts are fetched in CORS mode even same-origin; without this the
            // preload doesn't match the fetch and the files are downloaded twice.
            crossorigin: '',
          },
        }))
      },
    },
  }
}

export default defineConfig(({ mode }) => {
  loadEnv(mode, process.cwd())
  const plugins: PluginOption[] = [vue(), preloadFonts()]

  if (mode !== 'production') {
    plugins.push(vueDevTools())
  }

  return {
    plugins: plugins,
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    optimizeDeps: {
      exclude: ['@jsquash/webp'],
    },
    worker: {
      format: 'es',
    },
    build: {
      outDir: '../../../build/resources/main/static',
      emptyOutDir: true,
    },
    server: {
      port: 5174,
      proxy: {
        '/api': {
          target: 'http://localhost:8442',
          changeOrigin: true,
          secure: false,
        },
        '/api-public': {
          target: 'http://localhost:8442',
          changeOrigin: true,
          secure: false,
        },
        '/auth': {
          target: 'http://localhost:8442',
          changeOrigin: true,
          secure: false,
        },
        '/actuator': {
          target: 'http://localhost:8442',
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})
