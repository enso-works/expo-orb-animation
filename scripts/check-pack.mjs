// Fails when the npm tarball would ship something it should not, or miss
// something a consumer needs. Run after a build.
import { execFileSync } from 'node:child_process';

const out = execFileSync('npm', ['pack', '--dry-run', '--json', '--ignore-scripts'], {
  encoding: 'utf8',
});
const files = JSON.parse(out.slice(out.indexOf('[')))[0].files.map((f) => f.path);

const required = [
  'build/index.js',
  'build/index.d.ts',
  'build/breathing-pattern.js',
  'expo-module.config.json',
  'ios/ExpoOrb.podspec',
  'android/build.gradle',
  'README.md',
  'LICENSE',
];
const forbidden = [/__tests__/, /^example\//, /^docs\//, /^\.github\//, /\.tgz$/, /^scripts\//];

const missing = required.filter((path) => !files.includes(path));
const leaked = files.filter((path) => forbidden.some((re) => re.test(path)));

if (missing.length || leaked.length) {
  if (missing.length) console.error(`Missing from the package:\n  ${missing.join('\n  ')}`);
  if (leaked.length) console.error(`Should not be in the package:\n  ${leaked.join('\n  ')}`);
  process.exit(1);
}
console.log(`Package contents OK: ${files.length} files.`);
