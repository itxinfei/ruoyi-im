/**
 * IM模块公共函数库
 * 用于统一IM后台管理系统的公共功能
 */
var ImCommon = {
    /**
     * 默认头像路径
     */
    DEFAULT_AVATAR: '/img/profile.jpg',

    /**
     * 格式化头像显示
     * @param avatar 头像路径
     * @param cssClass CSS类名
     * @param style 额外样式
     * @returns {string} HTML字符串
     */
    formatAvatar: function(avatar, cssClass, style) {
        cssClass = cssClass || 'user-avatar';
        style = style || '';

        if (avatar) {
            return '<img src="' + avatar + '" class="' + cssClass + '" style="' + style + '" onerror="this.src=\'' + this.DEFAULT_AVATAR + '\'"/>';
        }
        return '<i class="fa fa-user-circle fa-2x text-gray"></i>';
    },

    /**
     * 格式化用户头像（带圆形边框）
     * @param avatar 头像路径
     * @param size 尺寸，默认32px
     * @returns {string} HTML字符串
     */
    formatUserAvatar: function(avatar, size) {
        size = size || 32;
        return this.formatAvatar(avatar, 'user-avatar', 'width:' + size + 'px;height:' + size + 'px;');
    },

    /**
     * 格式化圆形头像
     * @param avatar 头像路径
     * @param size 尺寸，默认32px
     * @returns {string} HTML字符串
     */
    formatCircleAvatar: function(avatar, size) {
        size = size || 32;
        return this.formatAvatar(avatar, 'img-circle', 'width:' + size + 'px;height:' + size + 'px;');
    },

    /**
     * 格式化成员头像（40px圆形）
     * @param avatar 头像路径
     * @returns {string} HTML字符串
     */
    formatMemberAvatar: function(avatar) {
        return this.formatAvatar(avatar, 'member-avatar', 'width:40px;height:40px;');
    },

    /**
     * 格式化性别显示
     * @param gender 性别值：0-未知，1-男，2-女
     * @returns {string} HTML字符串
     */
    formatGender: function(gender) {
        if (gender == 1) return '<span class="badge badge-primary">男</span>';
        if (gender == 2) return '<span class="badge badge-danger">女</span>';
        return '<span class="badge badge-default">未知</span>';
    },

    /**
     * 格式化用户状态显示
     * @param status 状态值：0-停用，1-正常
     * @returns {string} HTML字符串
     */
    formatUserStatus: function(status) {
        if (status == 1) return '<span class="badge badge-success">正常</span>';
        return '<span class="badge badge-danger">停用</span>';
    },

    /**
     * 格式化在线状态显示
     * @param isOnline 在线状态：0-离线，1-在线
     * @returns {string} HTML字符串
     */
    formatOnlineStatus: function(isOnline) {
        if (isOnline == 1) return '<span class="badge badge-success">在线</span>';
        return '<span class="badge badge-default">离线</span>';
    },

    /**
     * 格式化会话类型显示
     * @param type 会话类型：PRIVATE-私聊，GROUP-群聊
     * @returns {string} HTML字符串
     */
    formatConversationType: function(type) {
        if (type === 'PRIVATE') {
            return '<span class="badge badge-primary">私聊</span>';
        } else if (type === 'GROUP') {
            return '<span class="badge badge-success">群聊</span>';
        }
        return type || '-';
    },

    /**
     * 格式化消息类型显示
     * @param messageType 消息类型：TEXT-文本，IMAGE-图片，FILE-文件，VOICE-语音，VIDEO-视频
     * @returns {string} HTML字符串
     */
    formatMessageType: function(messageType) {
        var typeMap = {
            'TEXT': '<span class="badge badge-primary">文本</span>',
            'IMAGE': '<span class="badge badge-info">图片</span>',
            'FILE': '<span class="badge badge-warning">文件</span>',
            'VOICE': '<span class="badge badge-purple">语音</span>',
            'VIDEO': '<span class="badge badge-purple">视频</span>',
            'SYSTEM': '<span class="badge badge-default">系统</span>'
        };
        return typeMap[messageType] || '<span class="badge badge-default">' + (messageType || '未知') + '</span>';
    },

    /**
     * 格式化群组角色显示
     * @param role 角色类型：OWNER-群主，ADMIN-管理员，MEMBER-成员
     * @returns {string} HTML字符串
     */
    formatGroupRole: function(role) {
        var roleMap = {
            'OWNER': '<span class="role-badge role-owner">群主</span>',
            'ADMIN': '<span class="role-badge role-admin">管理员</span>',
            'MEMBER': '<span class="role-badge role-member">成员</span>'
        };
        return roleMap[role] || '<span class="role-badge role-member">成员</span>';
    },

    /**
     * 格式化禁言状态显示
     * @param isMuted 禁言状态：0-正常，1-已禁言
     * @returns {string} HTML字符串
     */
    formatMuteStatus: function(isMuted) {
        if (isMuted == 1) {
            return '<span class="badge badge-warning" style="font-size:11px;">已禁言</span>';
        }
        return '<span class="badge badge-success" style="font-size:11px;">正常</span>';
    },

    /**
     * 格式化时间显示（只显示日期和时间，不显示T）
     * @param dateTime 时间字符串
     * @returns {string} 格式化后的时间
     */
    formatDateTime: function(dateTime) {
        if (!dateTime) return '-';
        if (typeof dateTime === 'string') {
            return dateTime.substring(0, 19).replace('T', ' ');
        }
        return dateTime;
    },

    /**
     * 截断长文本
     * @param text 原文本
     * @param maxLength 最大长度，默认50
     * @returns {string} 截断后的文本
     */
    truncateText: function(text, maxLength) {
        maxLength = maxLength || 50;
        if (!text) return '-';
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    },

    /**
     * 批量操作前的数据验证
     * @param rows 选中的行数据
     * @param minCount 最小选择数量，默认1
     * @param maxCount 最大选择数量，默认null（不限制）
     * @returns {boolean} 是否通过验证
     */
    validateBatchOperation: function(rows, minCount, maxCount) {
        minCount = minCount || 1;
        if (rows.length == 0) {
            $.modal.alertWarning("请至少选择一条记录");
            return false;
        }
        if (rows.length < minCount) {
            $.modal.alertWarning("请至少选择 " + minCount + " 条记录");
            return false;
        }
        if (maxCount && rows.length > maxCount) {
            $.modal.alertWarning("一次最多只能选择 " + maxCount + " 条记录");
            return false;
        }
        return true;
    },

    /**
     * 带缓存的统计数据加载
     * @param url 请求URL
     * @param updateUI 更新UI的回调函数
     * @param cacheKey 缓存键名，默认使用URL
     * @param cacheDuration 缓存时长（分钟），默认5分钟
     */
    loadStatisticsWithCache: function(url, updateUI, cacheKey, cacheDuration) {
        cacheKey = cacheKey || 'statistics_' + url;
        cacheDuration = cacheDuration || 5;
        var now = new Date().getTime();

        // 检查缓存
        try {
            var cached = sessionStorage.getItem(cacheKey);
            if (cached) {
                var cacheData = JSON.parse(cached);
                if (now - cacheData.timestamp < cacheDuration * 60 * 1000) {
                    updateUI(cacheData.data);
                    return;
                }
            }
        } catch (e) {
            console.warn('读取缓存失败:', e);
        }

        // 请求数据
        $.get(url, function(result) {
            if (result.code == 0) {
                updateUI(result.data);

                // 缓存数据
                try {
                    sessionStorage.setItem(cacheKey, JSON.stringify({
                        timestamp: now,
                        data: result.data
                    }));
                } catch (e) {
                    console.warn('写入缓存失败:', e);
                }
            }
        }).fail(function() {
            console.error('请求统计数据失败:', url);
        });
    },

    /**
     * 搜索防抖函数
     * @param func 要执行的函数
     * @param delay 延迟时间（毫秒），默认500ms
     * @returns {Function} 防抖后的函数
     */
    debounce: function(func, delay) {
        delay = delay || 500;
        var timer = null;
        return function() {
            var context = this;
            var args = arguments;
            clearTimeout(timer);
            timer = setTimeout(function() {
                func.apply(context, args);
            }, delay);
        };
    },

    /**
     * 统一分页配置
     * @param pageSize 默认每页条数，默认20
     * @param pageList 可选的每页条数列表
     * @returns {Object} 分页配置对象
     */
    getPaginationConfig: function(pageSize, pageList) {
        return {
            pagination: true,
            pageSize: pageSize || 20,
            pageList: pageList || [10, 20, 50, 100],
            sidePagination: 'server'
        };
    }
};
