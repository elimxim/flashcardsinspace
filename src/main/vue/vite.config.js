import { fileURLToPath, URL } from 'node:url';
import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueDevTools from 'vite-plugin-vue-devtools';
export default defineConfig(({ mode }) => {
    loadEnv(mode, process.cwd());
    const plugins = [vue()];
    if (mode !== 'production') {
        plugins.push(vueDevTools());
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
            }
        }
    };
});
