const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, 'src', 'main', 'java', 'com', 'ruoyi', 'im');

function walkDir(dir) {
  let results = [];
  const list = fs.readdirSync(dir);
  list.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);
    if (stat.isDirectory()) {
      results = results.concat(walkDir(filePath));
    } else if (file.endsWith('.java')) {
      results.push(filePath);
    }
  });
  return results;
}

const files = walkDir(ROOT);

console.log('=== MethodTooLongRule violations (>80 lines) ===');
files.forEach(f => {
  const content = fs.readFileSync(f, 'utf8');
  const lines = content.split('\n');
  const rel = path.relative(ROOT, f);
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    // Detect method declaration: optional modifiers, return type, name, params, then {
    const methodMatch = line.match(/^\s*(?:public|private|protected)\s+(?:static\s+)?(?:final\s+)?(?:synchronized\s+)?(?:<[^>]+>\s+)?(\S+)\s+(\w+)\s*\(.*\)\s*(?:throws\s+\S+\s*)?\{/);
    if (!methodMatch) continue;
    
    const methodName = methodMatch[2];
    const methodLine = i; // 0-indexed
    
    // Find matching closing brace
    let braceCount = 0;
    let foundOpen = false;
    let endLine = -1;
    
    for (let j = methodLine; j < lines.length; j++) {
      for (let k = 0; k < lines[j].length; k++) {
        if (lines[j][k] === '{') {
          braceCount++;
          foundOpen = true;
        } else if (lines[j][k] === '}') {
          braceCount--;
        }
      }
      if (foundOpen && braceCount === 0) {
        endLine = j;
        break;
      }
    }
    
    if (endLine > 0) {
      const methodLength = endLine - methodLine + 1;
      if (methodLength > 80) {
        console.log(`  ${rel}:${methodLine + 1}: ${methodName}() = ${methodLength} lines`);
      }
    }
  }
});
