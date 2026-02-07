---
name: ruoyi-im-init
description: 初始化新的 RuoYi-IM 功能模块（Controller、Service、Mapper、Vue组件）
---

# RuoYi-IM 模块初始化命令

快速创建 RuoYi-IM 新功能模块的标准代码结构，包括后端各层和前端组件。

## 使用方法

```bash
/ruoyi-im-init [功能名称] [表名]
```

例如：
```bash
/ruoyi-im-init notice im_notice
```

## 生成的文件结构

### 后端文件
```
ruoyi-im-api/src/main/java/com/im/controller/
├── ImNoticeController.java

ruoyi-im-api/src/main/java/com/im/service/
├── ImNoticeService.java
└── impl/
    └── ImNoticeServiceImpl.java

ruoyi-im-api/src/main/java/com/im/mapper/
├── ImNoticeMapper.java

ruoyi-im-api/src/main/java/com/im/entity/
├── ImNotice.java

ruoyi-im-api/src/main/java/com/im/dto/
├── NoticeCreateDTO.java
├── NoticeUpdateDTO.java
└── NoticeQueryDTO.java

ruoyi-im-api/src/main/java/com/im/vo/
├── NoticeVO.java
```

### 前端文件
```
ruoyi-im-web/src/api/
└── notice.js

ruoyi-im-web/src/views/notice/
├── index.vue
└── components/
    └── NoticeForm.vue
```

## 代码模板特性

- 符合 RuoYi-IM 开发规范
- 包含完整的增删改查功能
- 自动生成权限注解
- 包含参数校验
- 统一的返回结果格式
- 标准的 Vue 3 Composition API
- Element Plus UI 组件
- 统一的 API 调用封装

## 注意事项

1. 表名必须以 `im_` 开头
2. 功能名称使用小写字母和下划线
3. 生成的代码需要根据实际业务调整
4. 记得在数据库中创建对应的表结构
5. 在菜单表中添加菜单配置

## 示例输出

### Controller 示例
```java
@RestController
@RequestMapping("/api/im/notice")
@PreAuthorize("hasRole('USER')")
@Slf4j
public class ImNoticeController {
    
    @Autowired
    private ImNoticeService noticeService;
    
    @GetMapping("/list")
    public Result<List<NoticeVO>> list(@Valid NoticeQueryDTO dto) {
        List<NoticeVO> list = noticeService.list(dto);
        return Result.success(list);
    }
}
```

### Vue 组件示例
```vue
<template>
  <div class="notice-container">
    <el-card>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="content" label="内容" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNoticeList } from '@/api/notice'

const loading = ref(false)
const list = ref([])

const getList = async () => {
  loading.value = true
  try {
    const res = await getNoticeList()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.notice-container {
  padding: 20px;
}
</style>
```

使用此命令可以快速创建符合项目规范的代码模板，提高开发效率！