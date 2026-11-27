import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const backend = process.env.VITE_API_TARGET ?? 'http://localhost:8080';

// Під час розробки запити /api проксіюються на бекенд Spring Boot.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: backend, changeOrigin: true },
      '/swagger-ui': { target: backend, changeOrigin: true },
      '/v3/api-docs': { target: backend, changeOrigin: true },
    },
  },
});
