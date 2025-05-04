import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    specPattern: 'cypress/e2e/**/*.{cy,spec}.{js,jsx,ts,tsx}',
    baseUrl: 'http://ip24nw3.sit.kmutt.ac.th',
    baseAPI: 'http://ip24nw3.sit.kmutt.ac.th:8080',
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
