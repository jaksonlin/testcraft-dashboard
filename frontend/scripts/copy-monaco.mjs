import { cp, mkdir, rm, stat } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = resolve(__dirname, '..');
const sourceDir = resolve(projectRoot, 'node_modules/monaco-editor/min/vs');
const targetDir = resolve(projectRoot, 'public/monaco/vs');

async function ensureSourceExists() {
  try {
    await stat(sourceDir);
  } catch {
    throw new Error(
      `Monaco assets not found at ${sourceDir}. Make sure dependencies are installed before running this script.`
    );
  }
}

async function copyMonacoAssets() {
  await ensureSourceExists();
  await mkdir(dirname(targetDir), { recursive: true });
  await rm(targetDir, { recursive: true, force: true });
  await cp(sourceDir, targetDir, { recursive: true });
  console.log(`Copied Monaco assets to ${targetDir}`);
}

copyMonacoAssets().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});

