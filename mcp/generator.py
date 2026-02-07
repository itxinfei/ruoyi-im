#!/usr/bin/env python3

import json
import sys
import re
from typing import Dict, List, Any

class RuoYiIMGenerator:
    def __init__(self):
        pass

    def generate_controller(self, module_name: str, table_name: str) -> str:
        """生成 Controller 代码"""
        class_name = self.to_pascal_case(module_name)
        entity_name = class_name.replace('Im', '')
        
        template = f'''@RestController
@RequestMapping("/api/im/{module_name}")
@PreAuthorize("hasRole('USER')")
@Slf4j
public class {class_name}Controller {{
    
    @Autowired
    private {class_name}Service {module_name}Service;
    
    @GetMapping("/list")
    public Result<List<{entity_name}VO>> list(@Valid {entity_name}QueryDTO dto) {{
        log.info("查询{entity_name}列表, dto={{}}", dto);
        List<{entity_name}VO> list = {module_name}Service.list(dto);
        return Result.success(list);
    }}
    
    @PostMapping("/create")
    public Result<{entity_name}VO> create(@RequestBody @Valid {entity_name}CreateDTO dto) {{
        log.info("创建{entity_name}, dto={{}}", dto);
        {entity_name}VO vo = {module_name}Service.create(dto);
        return Result.success(vo);
    }}
    
    @PutMapping("/update")
    public Result<{entity_name}VO> update(@RequestBody @Valid {entity_name}UpdateDTO dto) {{
        log.info("更新{entity_name}, dto={{}}", dto);
        {entity_name}VO vo = {module_name}Service.update(dto);
        return Result.success(vo);
    }}
    
    @DeleteMapping("/{{{{id}}}}")
    public Result<Void> delete(@PathVariable Long id) {{
        log.info("删除{entity_name}, id={{}}", id);
        {module_name}Service.delete(id);
        return Result.success();
    }}
}}'''
        
        return template

    def generate_service(self, module_name: str) -> tuple[str, str]:
        """生成 Service 接口和实现类"""
        class_name = self.to_pascal_case(module_name)
        entity_name = class_name.replace('Im', '')
        
        # Service 接口
        service_interface = f'''public interface {class_name}Service {{
    
    List<{entity_name}VO> list({entity_name}QueryDTO dto);
    
    {entity_name}VO create({entity_name}CreateDTO dto);
    
    {entity_name}VO update({entity_name}UpdateDTO dto);
    
    void delete(Long id);
}}'''

        # Service 实现类
        service_impl = f'''@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class {class_name}ServiceImpl implements {class_name}Service {{
    
    @Autowired
    private {class_name}Mapper {module_name}Mapper;
    
    @Autowired
    private ImUserService userService;
    
    @Override
    public List<{entity_name}VO> list({entity_name}QueryDTO dto) {{
        LambdaQueryWrapper<{class_name}> wrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (dto != null) {{
            // TODO: 添加查询条件
        }}
        
        wrapper.orderByDesc({class_name}::getCreateTime);
        
        List<{class_name}> list = {module_name}Mapper.selectList(wrapper);
        return list.stream()
                .map(entity -> {{
                    {entity_name}VO vo = new {entity_name}VO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                }})
                .collect(Collectors.toList());
    }}
    
    @Override
    public {entity_name}VO create({entity_name}CreateDTO dto) {{
        // 参数校验
        if (dto == null) {{
            throw new ServiceException("参数不能为空");
        }}
        
        // 业务逻辑处理
        {class_name} entity = new {class_name}();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setCreateBy(SecurityUtils.getUserId());
        entity.setUpdateBy(SecurityUtils.getUserId());
        
        // 保存数据
        {module_name}Mapper.insert(entity);
        
        // 返回结果
        {entity_name}VO vo = new {entity_name}VO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }}
    
    @Override
    public {entity_name}VO update({entity_name}UpdateDTO dto) {{
        if (dto == null || dto.getId() == null) {{
            throw new ServiceException("参数不能为空");
        }}
        
        // 查询原数据
        {class_name} entity = {module_name}Mapper.selectById(dto.getId());
        if (entity == null) {{
            throw new ServiceException("数据不存在");
        }}
        
        // 更新数据
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdateTime(new Date());
        entity.setUpdateBy(SecurityUtils.getUserId());
        
        {module_name}Mapper.updateById(entity);
        
        // 返回结果
        {entity_name}VO vo = new {entity_name}VO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }}
    
    @Override
    public void delete(Long id) {{
        if (id == null) {{
            throw new ServiceException("参数不能为空");
        }}
        
        {module_name}Mapper.deleteById(id);
        log.info("删除{entity_name}成功, id={{}}", id);
    }}
}}'''

        return service_interface, service_impl

    def generate_mapper(self, module_name: str) -> str:
        """生成 Mapper 接口"""
        class_name = self.to_pascal_case(module_name)
        
        template = f'''@Mapper
public interface {class_name}Mapper extends BaseMapper<{class_name}> {{
    
    /**
     * 根据用户ID查询列表
     */
    List<{class_name}> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 批量删除
     */
    int deleteBatch(@Param("ids") List<Long> ids);
}}'''

        return template

    def generate_entity(self, module_name: str, table_name: str) -> str:
        """生成 Entity 类"""
        class_name = self.to_pascal_case(module_name)
        
        template = f'''@TableName("{table_name}")
public class {class_name} {{
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    
    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    
    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    
    // TODO: 添加其他字段
    
    // Getter and Setter methods
    public Long getId() {{
        return id;
    }}
    
    public void setId(Long id) {{
        this.id = id;
    }}
    
    public Date getCreateTime() {{
        return createTime;
    }}
    
    public void setCreateTime(Date createTime) {{
        this.createTime = createTime;
    }}
    
    public Date getUpdateTime() {{
        return updateTime;
    }}
    
    public void setUpdateTime(Date updateTime) {{
        this.updateTime = updateTime;
    }}
    
    public Long getCreateBy() {{
        return createBy;
    }}
    
    public void setCreateBy(Long createBy) {{
        this.createBy = createBy;
    }}
    
    public Long getUpdateBy() {{
        return updateBy;
    }}
    
    public void setUpdateBy(Long updateBy) {{
        this.updateBy = updateBy;
    }}
}}'''

        return template

    def generate_vue_component(self, module_name: str) -> str:
        """生成 Vue 组件"""
        component_name = self.to_pascal_case(module_name).replace('Im', '')
        
        template = f'''<template>
  <div class="{module_name}-container">
    <el-card>
      <div class="header">
        <h3>{component_name}管理</h3>
        <el-button type="primary" @click="handleCreate">
          <i class="material-icons-outlined">add</i>
          新增
        </el-button>
      </div>
      
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="名称">
            <el-input v-model="searchForm.name" placeholder="请输入名称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <i class="material-icons-outlined">search</i>
              搜索
            </el-button>
            <el-button @click="handleReset">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="table-container">
        <el-table :data="list" v-loading="loading" stripe>
          <!-- TODO: 根据实际字段调整列定义 -->
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="createTime" label="创建时间">
            <template #default="scope">
              {{{{ formatTime(scope.row.createTime) }}}}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="handleEdit(scope.row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 表单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <{component_name}-form
        ref="formRef"
        :data="formData"
        @submit="handleSubmit"
        @cancel="dialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import {{ ref, reactive, onMounted }} from 'vue'
import {{ ElMessage, ElMessageBox }} from 'element-plus'
import {{ get{component_name}List, delete{component_name} }} from '@/api/{module_name}'
import {component_name}Form from './components/{component_name}Form.vue'
import {{ formatTime }} from '@/utils/date'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const searchForm = reactive({{
  name: ''
}})

const pagination = reactive({{
  current: 1,
  size: 20,
  total: 0
}})

const formData = ref({{}}

const getList = async () => {{
  loading.value = true
  try {{
    const params = {{
      ...searchForm,
      page: pagination.current,
      size: pagination.size
    }}
    const res = await get{component_name}List(params)
    list.value = res.data.records
    pagination.total = res.data.total
  }} catch (error) {{
    ElMessage.error('获取数据失败')
  }} finally {{
    loading.value = false
  }}
}}

const handleSearch = () => {{
  pagination.current = 1
  getList()
}}

const handleReset = () => {{
  Object.assign(searchForm, {{
    name: ''
  }})
  pagination.current = 1
  getList()
}}

const handleCreate = () => {{
  dialogTitle.value = '新增{component_name}'
  formData.value = {{}}
  dialogVisible.value = true
}}

const handleEdit = (row) => {{
  dialogTitle.value = '编辑{component_name}'
  formData.value = {{ ...row }}
  dialogVisible.value = true
}}

const handleDelete = async (row) => {{
  try {{
    await ElMessageBox.confirm('确定要删除这条数据吗？', '提示', {{
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }})
    
    await delete{component_name}(row.id)
    ElMessage.success('删除成功')
    getList()
  }} catch (error) {{
    if (error !== 'cancel') {{
      ElMessage.error('删除失败')
    }}
  }}
}}

const handleSubmit = async (data) => {{
  try {{
    dialogVisible.value = false
    ElMessage.success(data.id ? '更新成功' : '创建成功')
    getList()
  }} catch (error) {{
    ElMessage.error('操作失败')
  }}
}}

const handleDialogClose = () => {{
  formRef.value?.resetFields()
}}

const handleSizeChange = (size) => {{
  pagination.size = size
  getList()
}}

const handleCurrentChange = (current) => {{
  pagination.current = current
  getList()
}}

onMounted(() => {{
  getList()
}})
</script>

<style scoped>
.{module_name}-container {{
  padding: 20px;
}}

.header {{
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}}

.search-form {{
  margin-bottom: 20px;
}}

.table-container {{
  min-height: 400px;
}}

.el-pagination {{
  margin-top: 20px;
  text-align: right;
}}
</style>'''

        return template

    def generate_api_file(self, module_name: str) -> str:
        """生成 API 文件"""
        component_name = self.to_pascal_case(module_name).replace('Im', '')
        
        template = f'''import request from '@/utils/request'

// 获取{component_name}列表
export function get{component_name}List(params) {{
  return request({{
    url: '/api/im/{module_name}/list',
    method: 'get',
    params
  }})
}}

// 创建{component_name}
export function create{component_name}(data) {{
  return request({{
    url: '/api/im/{module_name}/create',
    method: 'post',
    data
  }})
}}

// 更新{component_name}
export function update{component_name}(data) {{
  return request({{
    url: '/api/im/{module_name}/update',
    method: 'put',
    data
  }})
}}

// 删除{component_name}
export function delete{component_name}(id) {{
  return request({{
    url: `/api/im/{module_name}/${{id}}`,
    method: 'delete'
  }})
}}'''

        return template

    def to_pascal_case(self, snake_str: str) -> str:
        """将下划线命名转换为帕斯卡命名"""
        components = snake_str.split('_')
        return ''.join(x.title() for x in components)

def main():
    """主函数 - 处理 MCP 工具调用"""
    generator = RuoYiIMGenerator()
    
    # 读取输入
    input_data = json.loads(sys.stdin.read())
    
    if input_data.get('method') == 'tools/call':
        tool_name = input_data['params']['name']
        arguments = input_data['params']['arguments']
        
        if tool_name == 'generate_module':
            module_name = arguments['module_name']
            table_name = arguments['table_name']
            
            # 生成所有代码
            result = {
                'controller': generator.generate_controller(module_name, table_name),
                'service_interface': generator.generate_service(module_name)[0],
                'service_impl': generator.generate_service(module_name)[1],
                'mapper': generator.generate_mapper(module_name),
                'entity': generator.generate_entity(module_name, table_name),
                'vue_component': generator.generate_vue_component(module_name),
                'api_file': generator.generate_api_file(module_name)
            }
            
            print(json.dumps({
                'content': [
                    {
                        'type': 'text',
                        'text': json.dumps(result, ensure_ascii=False, indent=2)
                    }
                ]
            }))
        
        elif tool_name == 'generate_single_file':
            file_type = arguments['file_type']
            module_name = arguments['module_name']
            table_name = arguments.get('table_name', '')
            
            if file_type == 'controller':
                content = generator.generate_controller(module_name, table_name)
            elif file_type == 'service':
                content = generator.generate_service(module_name)[0]
            elif file_type == 'service_impl':
                content = generator.generate_service(module_name)[1]
            elif file_type == 'mapper':
                content = generator.generate_mapper(module_name)
            elif file_type == 'entity':
                content = generator.generate_entity(module_name, table_name)
            elif file_type == 'vue':
                content = generator.generate_vue_component(module_name)
            elif file_type == 'api':
                content = generator.generate_api_file(module_name)
            else:
                content = 'Unknown file type'
            
            print(json.dumps({
                'content': [
                    {
                        'type': 'text',
                        'text': content
                    }
                ]
            }))

if __name__ == '__main__':
    main()