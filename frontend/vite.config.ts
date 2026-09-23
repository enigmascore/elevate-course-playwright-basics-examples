import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    // the front end calls relative /api/... urls; Vite forwards them to the
    // backend container on 8086, so the browser sees ONE origin
    proxy: {
      "/api": "http://localhost:8086",
    },
  },
});
