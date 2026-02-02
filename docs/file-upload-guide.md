# 文件上传功能配置指南

## 概述

本文档详细说明 IM 系统文件上传功能的配置方式、存储路径和注意事项。

## 存储路径配置

### 默认配置

所有上传文件统一存储在以下目录：

```
ruoyi-im-api/src/main/resources/uploads/
```

### 子目录结构

```
uploads/
├── avatar/          # 用户头像
├── file/            # 普通文件上传
├── chunks/          # 分片上传临时文件
├── cloud/           # 企业云盘文件
└── emoji/           # 自定义表情
```

## 配置文件说明

### application.yml

```yaml
app:
  upload:
    path: src/main/resources/uploads/     # 上传文件存储路径

file:
  upload:
    path: src/main/resources/uploads/     # 文件上传路径（兼容性配置）
    url-prefix: /uploads/                 # 文件访问URL前缀
```

### 配置优先级

1. 环境变量（最高优先级）
2. application.yml 中的配置
3. 默认值 `src/main/resources/uploads/`（最低优先级）

## 代码中使用

### 服务层注入配置

```java
@Service
public class ImFileServiceImpl implements ImFileService {

    @Value("${file.upload.path:src/main/resources/uploads/}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    // 使用 FileUploadConfig 获取完整路径
    @Autowired
    private FileUploadConfig fileUploadConfig;

    public String getAbsolutePath() {
        return fileUploadConfig.getAbsoluteUploadPath();
    }
}
```

### FileUploadConfig 工具类

```java
@Autowired
private FileUploadConfig fileUploadConfig;

// 获取绝对路径
String absolutePath = fileUploadConfig.getAbsoluteUploadPath();

// 构建文件访问URL
String fileUrl = fileUploadConfig.buildFileUrl("avatar/2024/01/01/xxx.jpg");
```

## 文件访问方式

### 通过 URL 访问

上传的文件可以通过以下 URL 访问：

```
http://localhost:8080/uploads/{文件相对路径}
```

例如：
```
http://localhost:8080/uploads/avatar/2024/01/01/abc123.jpg
```

### 静态资源配置

`WebMvcConfig` 中已配置静态资源映射：

```java
registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:绝对路径");
```

## 注意事项

### 1. 目录权限

确保应用对 `uploads` 目录及其子目录有读写权限：

```bash
# Linux/Mac
chmod -R 755 ruoyi-im-api/src/main/resources/uploads/

# Windows
# 右键目录 → 属性 → 安全 → 编辑权限
```

### 2. 磁盘空间

监控上传目录磁盘空间，建议：
- 定期清理临时文件（chunks 目录）
- 设置文件保留策略
- 配置磁盘空间告警

### 3. 文件大小限制

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 20MB      # 单个文件最大大小
      max-request-size: 50MB   # 整个请求最大大小
```

### 4. 生产环境配置

生产环境建议修改配置：

```yaml
app:
  upload:
    path: /data/im/uploads/    # 使用独立的数据目录
```

并配置 Nginx 反向代理：

```nginx
location /uploads/ {
    alias /data/im/uploads/;
    expires 30d;
    add_header Cache-Control "public, immutable";
}
```

### 5. 备份策略

建议定期备份上传目录：

```bash
# 使用 rsync 增量备份
rsync -avz --delete ruoyi-im-api/src/main/resources/uploads/ backup/uploads/

# 或使用 tar 打包
tar -czf uploads-backup-$(date +%Y%m%d).tar.gz ruoyi-im-api/src/main/resources/uploads/
```

## 常见问题

### Q1: 文件上传后无法访问

**排查步骤：**
1. 检查文件是否成功保存到 uploads 目录
2. 检查 WebMvcConfig 中的资源映射配置
3. 检查文件访问 URL 是否正确
4. 检查应用是否有读取权限

### Q2: 如何修改上传路径

**方法：**
1. 修改 `application.yml` 中的 `file.upload.path`
2. 重启应用
3. 确保新目录存在且有权限

### Q3: 分片上传失败

**可能原因：**
1. chunks 目录无写入权限
2. 磁盘空间不足
3. 分片大小设置不合理

**解决方案：**
1. 检查目录权限
2. 清理磁盘空间
3. 调整分片大小（建议 2-5MB）

## 更新日志

### 2025-02-02

- 统一文件上传路径为 `resources/uploads/`
- 创建 FileUploadConfig 配置类
- 完善目录结构（avatar/file/chunks/cloud/emoji）
- 添加静态资源映射配置

## 相关文件

- `FileUploadConfig.java` - 上传配置类
- `WebMvcConfig.java` - 静态资源映射配置
- `ImFileServiceImpl.java` - 文件服务实现
- `ImFileChunkUploadServiceImpl.java` - 分片上传服务
- `application.yml` - 配置文件
