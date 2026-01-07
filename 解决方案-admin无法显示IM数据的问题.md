# 解决方案：admin无法显示好友和群组数据的问题

## 问题分析

### 根本原因
**ruoyi-im-admin** 和 **ruoyi-im-api** 使用的是**两套不同的用户表**：

| 系统 | 数据库表 | 说明 |
|------|----------|------|
| ruoyi-im-admin | sys_user | 若依原生用户表 |
| ruoyi-im-api | im_user | IM扩展用户表 |

### 问题表现
- 用户在admin登录后，看到的是 sys_user 表中的数据
- 好友和群组数据存储在 im_user 表中（im_friend, im_group）
- admin系统即使有 im 相关的Mapper，但可能配置指向了不同的数据库

---

## 解决方案

### 方案一：统一数据源（推荐）

让admin系统直接访问 ruoyi_im 数据库，配置如下：

**application.yml**
```yaml
spring:
  datasource:
    # 主数据源 - ruoyi_im数据库
    url: jdbc:mysql://localhost:3306/ruoyi_im?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class: com.mysql.cj.jdbc.Driver
```

这样admin系统就能直接访问：
- im_user - IM用户表
- im_friend - 好友表
- im_group - 群组表
- im_group_member - 群组成员表
- im_message - 消息表
- im_conversation - 会话表
- 等等...

### 方案二：数据同步

如果需要保持两个系统独立，可以创建数据同步服务：

```java
/**
 * IM数据同步服务
 * 将im_user数据同步到sys_user表，供admin系统显示
 */
@Service
public class ImDataSyncService {

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 同步IM用户到sys_user表
     */
    public void syncImUsersToSys() {
        List<ImUser> imUsers = imUserMapper.selectList(new LambdaQueryWrapper<>());

        for (ImUser imUser : imUsers) {
            // 检查用户是否已存在
            Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUserName, imUser.getUsername())
            );

            if (count == 0) {
                // 不存在则插入
                SysUser sysUser = new SysUser();
                sysUser.setUserName(imUser.getUsername());
                sysUser.setNickName(imUser.getNickName());
                sysUser.setEmail(imUser.getEmail());
                sysUser.setPhonenumber(imUser.getPhone());
                sysUser.setAvatar(imUser.getAvatar());
                sysUser.setStatus(imUser.getStatus());
                sysUser.setDelFlag("0");
                sysUserMapper.insert(sysUser);
            }
        }
    }
}
```

### 方案三：创建统一查询接口

在admin系统中创建一个联合查询接口：

```java
@RestController
@RequestMapping("/api/im/data")
public class ImDataController {

    /**
     * 获取IM用户列表（从im_user表）
     */
    @GetMapping("/users")
    public TableDataInfo getImUsers() {
        // 使用MyBatis-Plus的@DS注解指定数据源
        List<ImUser> imUsers = imUserMapper.selectList(null);
        return getDataTable(imUsers);
    }

    /**
     * 获取好友列表（从im_friend表）
     */
    @GetMapping("/friends")
    public TableDataInfo getFriends() {
        List<ImFriend> friends = imFriendMapper.selectList(null);
        return getDataTable(friends);
    }

    /**
     * 获取群组列表（从im_group表）
     */
    @GetMapping("/groups")
    public TableDataInfo getGroups() {
        List<ImGroup> groups = imGroupMapper.selectList(null);
        return getDataTable(groups);
    }
}
```

---

## 推荐方案：方案一

**原因：**
1. ruoyi-im-api 已经是完整的IM系统
2. admin只需要访问相同数据库即可显示所有数据
3. 避免数据同步的复杂性和一致性问题

**操作步骤：**
1. 修改 `ruoyi-im-admin/src/main/resources/application.yml`
2. 将数据源URL改为指向 `ruoyi_im` 数据库
3. 重启admin系统
4. 访问 `http://localhost:8081/system/im` 查看IM数据

---

## 验证方法

执行后运行以下SQL验证：

```sql
-- 检查im_user表是否有数据
SELECT COUNT(*) FROM ruoyi_im.im_user;

-- 检查im_friend表是否有数据
SELECT COUNT(*) FROM ruoyi_im.im_friend;

-- 检查im_group表是否有数据
SELECT COUNT(*) FROM ruoyi_im.im_group;
```

如果都有数据，说明数据库结构完整，只需修改admin的数据库连接配置即可。
