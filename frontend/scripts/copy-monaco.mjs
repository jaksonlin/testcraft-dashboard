import { cp, mkdir, rm, stat, readdir } from 'node:fs/promises';
import { dirname, resolve, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = resolve(__dirname, '..');
const sourceDir = resolve(projectRoot, 'node_modules/monaco-editor/min/vs');
const targetDir = resolve(projectRoot, 'public/monaco/vs');

// Critical files that Monaco absolutely needs
const CRITICAL_FILES = [
  'loader.js',
  'editor/editor.main.js',
  'editor/editor.main.css',
  'editor.api-CalNCsUg.js',
];

async function ensureSourceExists() {
  try {
    await stat(sourceDir);
  } catch {
    throw new Error(
      `Monaco assets not found at ${sourceDir}. Make sure dependencies are installed before running this script.`
    );
  }
}

async function getAllFiles(dir, base = dir) {
  const files = [];
  const entries = await readdir(dir, { withFileTypes: true });
  
  for (const entry of entries) {
    const fullPath = join(dir, entry.name);
    const relPath = fullPath.replace(base + '/', '');
    
    if (entry.isDirectory()) {
      files.push(...(await getAllFiles(fullPath, base)));
    } else {
      files.push(relPath);
    }
  }
  
  return files;
}

async function verifyCriticalFiles(targetDir) {
  const missing = [];
  for (const file of CRITICAL_FILES) {
    try {
      await stat(join(targetDir, file));
    } catch {
      missing.push(file);
    }
  }
  return missing;
}

async function copyMonacoAssets() {
  await ensureSourceExists();
  await mkdir(dirname(targetDir), { recursive: true });
  await rm(targetDir, { recursive: true, force: true });
  
  console.log(`Copying Monaco assets from ${sourceDir} to ${targetDir}...`);
  await cp(sourceDir, targetDir, { recursive: true });
  
  // Verify copy was successful
  const sourceFiles = await getAllFiles(sourceDir);
  const targetFiles = await getAllFiles(targetDir);
  const sourceCount = sourceFiles.length;
  const targetCount = targetFiles.length;
  
  console.log(`Source files: ${sourceCount}`);
  console.log(`Copied files: ${targetCount}`);
  
  if (sourceCount !== targetCount) {
    const missing = sourceFiles.filter(f => !targetFiles.includes(f));
    console.warn(`Warning: File count mismatch! Expected ${sourceCount} files, but copied ${targetCount} files.`);
    if (missing.length > 0) {
      console.warn(`Missing files (first 10):`, missing.slice(0, 10).join(', '));
    }
  }
  
  // Check critical files
  const missingCritical = await verifyCriticalFiles(targetDir);
  if (missingCritical.length > 0) {
    throw new Error(
      `Missing critical Monaco files: ${missingCritical.join(', ')}`
    );
  }
  
  console.log(`✓ Successfully copied Monaco files to ${targetDir}`);
  console.log(`✓ All critical files verified`);
}

copyMonacoAssets().catch((error) => {
  console.error('Error copying Monaco assets:', error);
  process.exitCode = 1;
});
