# 文件上传配置验证脚本
# 用于验证文件上传路径配置是否正确

Write-Host "========================================" -ForegroundColor Green
Write-Host "  文件上传配置验证工具" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

$ErrorActionPreference = "Stop"

# 获取项目根目录
$projectRoot = Get-Location
$apiModulePath = Join-Path $projectRoot "ruoyi-im-api"
$uploadsPath = Join-Path $apiModulePath "src/main/resources/uploads"

Write-Host "项目根目录: $projectRoot" -ForegroundColor Cyan
Write-Host "API模块路径: $apiModulePath" -ForegroundColor Cyan
Write-Host "上传目录路径: $uploadsPath" -ForegroundColor Cyan
Write-Host ""

# 检查目录结构
Write-Host "【检查目录结构】" -ForegroundColor Yellow
$subDirs = @("avatar", "file", "chunks", "cloud", "emoji")
$allExist = $true

foreach ($dir in $subDirs) {
    $fullPath = Join-Path $uploadsPath $dir
    if (Test-Path $fullPath) {
        Write-Host "  ✓ $dir 目录存在" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $dir 目录不存在" -ForegroundColor Red
        $allExist = $false
    }
}

Write-Host ""

# 检查配置文件
Write-Host "【检查配置文件】" -ForegroundColor Yellow
$configFile = Join-Path $apiModulePath "src/main/resources/application.yml"
if (Test-Path $configFile) {
    $content = Get-Content $configFile -Raw
    if ($content -match "upload.*path.*src/main/resources/uploads") {
        Write-Host "  ✓ application.yml 配置正确" -ForegroundColor Green
    } else {
        Write-Host "  ✗ application.yml 配置可能不正确" -ForegroundColor Red
    }
} else {
    Write-Host "  ✗ application.yml 文件不存在" -ForegroundColor Red
}

Write-Host ""

# 检查 Java 配置文件
Write-Host "【检查 Java 配置类】" -ForegroundColor Yellow
$configClasses = @(
    "src/main/java/com/ruoyi/im/config/FileUploadConfig.java",
    "src/main/java/com/ruoyi/im/config/WebMvcConfig.java"
)

foreach ($class in $configClasses) {
    $fullPath = Join-Path $apiModulePath $class
    if (Test-Path $fullPath) {
        Write-Host "  ✓ $([System.IO.Path]::GetFileName($class)) 存在" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $([System.IO.Path]::GetFileName($class)) 不存在" -ForegroundColor Red
    }
}

Write-Host ""

# 检查 Service 文件
Write-Host "【检查 Service 配置】" -ForegroundColor Yellow
$serviceFiles = @(
    "src/main/java/com/ruoyi/im/service/impl/ImFileServiceImpl.java",
    "src/main/java/com/ruoyi/im/service/impl/ImUserServiceImpl.java",
    "src/main/java/com/ruoyi/im/service/impl/ImFileChunkUploadServiceImpl.java",
    "src/main/java/com/ruoyi/im/service/impl/ImCloudDriveServiceImpl.java"
)

foreach ($file in $serviceFiles) {
    $fullPath = Join-Path $apiModulePath $file
    if (Test-Path $fullPath) {
        $content = Get-Content $fullPath -Raw
        if ($content -match 'file\.upload\.path:src/main/resources/uploads/') {
            Write-Host "  ✓ $([System.IO.Path]::GetFileName($file)) 配置正确" -ForegroundColor Green
        } else {
            Write-Host "  ! $([System.IO.Path]::GetFileName($file)) 需要检查配置" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✗ $([System.IO.Path]::GetFileName($file)) 不存在" -ForegroundColor Red
    }
}

Write-Host ""

# 检查 .gitignore
Write-Host "【检查 .gitignore 配置】" -ForegroundColor Yellow
$gitignore = Join-Path $projectRoot ".gitignore"
if (Test-Path $gitignore) {
    $content = Get-Content $gitignore -Raw
    if ($content -match "ruoyi-im-api/src/main/resources/uploads") {
        Write-Host "  ✓ .gitignore 已配置忽略上传文件" -ForegroundColor Green
    } else {
        Write-Host "  ! .gitignore 可能未配置忽略上传文件" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ✗ .gitignore 文件不存在" -ForegroundColor Red
}

Write-Host ""

# 检查文档
Write-Host "【检查文档】" -ForegroundColor Yellow
$docFile = Join-Path $projectRoot "docs/file-upload-guide.md"
if (Test-Path $docFile) {
    Write-Host "  ✓ 文件上传配置文档存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ 文件上传配置文档不存在" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
if ($allExist) {
    Write-Host "  验证通过！所有配置正确。" -ForegroundColor Green
} else {
    Write-Host "  验证完成，请检查上述警告项。" -ForegroundColor Yellow
}
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "使用说明:" -ForegroundColor Cyan
Write-Host "  1. 启动应用后会自动创建上传目录" -ForegroundColor White
Write-Host "  2. 文件将保存在 ruoyi-im-api/src/main/resources/uploads/ 下" -ForegroundColor White
Write-Host "  3. 通过 http://localhost:8080/uploads/ 访问上传的文件" -ForegroundColor White
Write-Host "  4. 查看 docs/file-upload-guide.md 获取详细文档" -ForegroundColor White
Write-Host ""
