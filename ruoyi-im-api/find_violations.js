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

// 1. Check ClassNamingShouldBeCamelRule
console.log('=== ClassNamingShouldBeCamelRule violations ===');
files.forEach(f => {
  const content = fs.readFileSync(f, 'utf8');
  const rel = path.relative(ROOT, f);
  // Find class/interface/enum declarations
  const classPattern = /(?:public\s+|private\s+|protected\s+)?(?:static\s+)?(?:abstract\s+)?(?:final\s+)?(class|interface|enum)\s+(\w+)/g;
  let m;
  while ((m = classPattern.exec(content)) !== null) {
    const name = m[2];
    // CamelCase: starts with uppercase, no underscores, no spaces
    if (name.startsWith('_') || /[a-z]/.test(name.charAt(0)) || name.includes('_') || name.includes(' ')) {
      console.log(`  ${rel}: ${m[1]} ${name}`);
    }
  }
});

// 2. Check MethodTooLongRule (>80 lines)
console.log('\n=== MethodTooLongRule violations (>80 lines) ===');
files.forEach(f => {
  const content = fs.readFileSync(f, 'utf8');
  const lines = content.split('\n');
  const rel = path.relative(ROOT, f);
  
  let braceCount = 0;
  let inMethod = false;
  let methodStart = -1;
  let methodName = '';
  let methodLine = '';
  let methodSigLine = 0;
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    if (!inMethod) {
      // Detect method start: access modifier + return type + name + (
      // Simplified: look for lines with method signature pattern
      const methodMatch = line.match(/^\s*(?:@\w+\s+)*(?:public|private|protected)\s+(?:static\s+)?(?:final\s+)?(?:synchronized\s+)?(?:\w+(?:<[^>]+>)?)\s+(\w+)\s*\(/);
      if (methodMatch) {
        inMethod = true;
        methodStart = i;
        methodName = methodMatch[1];
        methodSigLine = i;
        methodLine = line.trim();
        braceCount = 0;
      }
    }
    
    if (inMethod) {
      // Count braces
      for (const ch of line) {
        if (ch === '{') braceCount++;
        if (ch === '}') braceCount--;
      }
      
      if (braceCount === 0 && (line.includes('}') || lines.slice(methodSigLine, i + 1).join('').includes('{'))) {
        const methodLength = i - methodStart + 1;
        if (methodLength > 80) {
          console.log(`  ${rel}:${methodSigLine + 1}: ${methodName}() = ${methodLength} lines`);
        }
        inMethod = false;
      }
    }
  }
});
