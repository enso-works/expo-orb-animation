// ESLint 9 flat config, the shared one from expo-module-scripts.
const { defineConfig, globalIgnores } = require('eslint/config');
const base = require('expo-module-scripts/eslint.config.base');

module.exports = defineConfig([globalIgnores(['build/', 'example/']), base]);
