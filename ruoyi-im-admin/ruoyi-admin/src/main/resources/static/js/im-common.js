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
     * 格式化相对时间显示（如“5分钟前”、“刚刚”）
     * @param dateTime 时间字符串或Date对象
     * @returns {string} 相对时间描述
     */
    formatRelativeTime: function(dateTime) {
        if (!dateTime) return '-';

        var date;
        if (typeof dateTime === 'string') {
            date = new Date(dateTime.replace('T', ' '));
        } else if (dateTime instanceof Date) {
            date = dateTime;
        } else {
            return '-';
        }

        if (isNaN(date.getTime())) return '-';

        var now = new Date();
        var diff = now.getTime() - date.getTime();
        var seconds = Math.floor(diff / 1000);
        var minutes = Math.floor(seconds / 60);
        var hours = Math.floor(minutes / 60);
        var days = Math.floor(hours / 24);

        if (seconds < 60) {
            return '刚刚';
        } else if (minutes < 60) {
            return minutes + '分钟前';
        } else if (hours < 24) {
            return hours + '小时前';
        } else if (days < 7) {
            return days + '天前';
        } else if (days < 30) {
            return Math.floor(days / 7) + '周前';
        } else if (days < 365) {
            return Math.floor(days / 30) + '月前';
        } else {
            return Math.floor(days / 365) + '年前';
        }
    },

    /**
     * 格式化时间为完整格式（包含相对时间）
     * @param dateTime 时间字符串
     * @param showRelative 是否显示相对时间，默认true
     * @returns {string} 格式化后的时间，包含title属性显示完整时间
     */
    formatDateTimeWithRelative: function(dateTime, showRelative) {
        if (showRelative === false) {
            return '<span title="' + this.formatDateTime(dateTime) + '">' + this.formatDateTime(dateTime) + '</span>';
        }

        var relativeTime = this.formatRelativeTime(dateTime);
        var fullTime = this.formatDateTime(dateTime);

        if (relativeTime === '刚刚' || relativeTime === '-') {
            return '<span title="' + fullTime + '">' + relativeTime + '</span>';
        }

        return '<span title="' + fullTime + '">' + fullTime + ' <small class="text-muted">(' + relativeTime + ')</small></span>';
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
    },

    /**
     * 显示加载状态
     * @param target 目标元素选择器，默认为表格容器
     */
    showLoading: function(target) {
        target = target || '.select-table';
        var $target = $(target);

        // 移除已存在的loading
        this.hideLoading(target);

        var loadingHtml = '<div class="im-table-loading" style="position:absolute;top:0;left:0;right:0;bottom:0;background:rgba(255,255,255,0.9);display:flex;align-items:center;justify-content:center;z-index:100;min-height:200px;">' +
            '<div><i class="fa fa-spinner fa-spin fa-2x text-primary"></i><p class="margin-top:10">数据加载中...</p></div>' +
            '</div>';

        if ($target.find('.fixed-table-container').length > 0) {
            $target.find('.fixed-table-container').append(loadingHtml);
        } else {
            $target.append(loadingHtml);
        }
    },

    /**
     * 隐藏加载状态
     * @param target 目标元素选择器，默认为表格容器
     */
    hideLoading: function(target) {
        target = target || '.select-table';
        $(target).find('.im-table-loading').remove();
    },

    /**
     * 格式化空数据提示
     * @param message 提示信息
     * @param icon 图标类名，默认为'fa-inbox'
     * @returns {string} HTML字符串
     */
    formatEmptyData: function(message, icon) {
        message = message || '暂无数据';
        icon = icon || 'fa-inbox';
        return '<div class="text-center" style="padding:40px 20px;color:#999;">' +
            '<i class="fa ' + icon + ' fa-3x" style="margin-bottom:15px;opacity:0.3;"></i>' +
            '<p style="margin:0;font-size:14px;">' + message + '</p>' +
            '</div>';
    },

    /**
     * 格式化带链接的空数据提示
     * @param message 提示信息
     * @param linkText 链接文本
     * @param linkAction 链接点击事件
     * @param icon 图标类名，默认为'fa-inbox'
     * @returns {string} HTML字符串
     */
    formatEmptyDataWithLink: function(message, linkText, linkAction, icon) {
        message = message || '暂无数据';
        linkText = linkText || '立即添加';
        icon = icon || 'fa-inbox';

        return '<div class="text-center" style="padding:40px 20px;color:#999;">' +
            '<i class="fa ' + icon + ' fa-3x" style="margin-bottom:15px;opacity:0.3;"></i>' +
            '<p style="margin:0;font-size:14px;margin-bottom:15px;">' + message + '</p>' +
            '<a class="btn btn-primary btn-sm" onclick="' + linkAction + '">' + linkText + '</a>' +
            '</div>';
    },

    /**
     * 表格响应处理（自动处理空数据和错误）
     * @param res 响应数据
     * @param emptyMessage 空数据提示信息
     * @param target 目标表格
     * @returns {Array|Object} 处理后的数据
     */
    handleTableResponse: function(res, emptyMessage, target) {
        if (!res) {
            if (emptyMessage) {
                this.showEmptyDataInTable(emptyMessage, target);
            }
            return [];
        }

        if (res.code !== undefined && res.code !== 0) {
            $.modal.msgError(res.msg || '数据加载失败');
            return [];
        }

        var rows = res.rows || res.data || [];

        // 处理空数据
        if (rows.length === 0 && emptyMessage) {
            this.showEmptyDataInTable(emptyMessage, target);
        }

        return rows;
    },

    /**
     * 在表格中显示空数据提示
     * @param message 提示信息
     * @param target 目标表格
     */
    showEmptyDataInTable: function(message, target) {
        target = target || '#bootstrap-table';
        var $table = $(target);

        // 隐藏表格内容，显示空数据提示
        $table.next('.fixed-table-body').hide();
        $table.next('.im-empty-data').remove();

        var emptyHtml = '<div class="im-empty-data" style="text-align:center;padding:40px 20px;color:#999;">' +
            '<i class="fa fa-inbox fa-3x" style="margin-bottom:15px;opacity:0.3;"></i>' +
            '<p style="margin:0;font-size:14px;">' + message + '</p>' +
            '</div>';

        $table.after(emptyHtml);
    },

    /**
     * 移除空数据提示（准备加载新数据）
     * @param target 目标表格
     */
    removeEmptyData: function(target) {
        target = target || '#bootstrap-table';
        var $table = $(target);
        $table.next('.im-empty-data').remove();
        $table.next('.fixed-table-body').show();
    },

    /**
     * 格式化带tooltip的长文本
     * @param text 原文本
     * @param maxLength 最大长度，默认50
     * @returns {string} HTML字符串，带title属性
     */
    formatTextWithTooltip: function(text, maxLength) {
        maxLength = maxLength || 50;
        if (!text) return '-';

        if (text.length <= maxLength) {
            return text;
        }

        var truncated = text.substring(0, maxLength) + '...';
        return '<span title="' + this.escapeHtml(text) + '">' + this.escapeHtml(truncated) + '</span>';
    },

    /**
     * HTML转义，防止XSS
     * @param text 原文本
     * @returns {string} 转义后的文本
     */
    escapeHtml: function(text) {
        if (!text) return '';

        var map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };

        return text.toString().replace(/[&<>"']/g, function(m) { return map[m]; });
    },

    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @returns {string} 格式化后的大小（如"1.5MB"）
     */
    formatFileSize: function(size) {
        if (!size || size === 0) return '0 B';

        var units = ['B', 'KB', 'MB', 'GB', 'TB'];
        var index = 0;
        var fileSize = size;

        while (fileSize >= 1024 && index < units.length - 1) {
            fileSize /= 1024;
            index++;
        }

        return fileSize.toFixed(index === 0 ? 0 : 1) + ' ' + units[index];
    },

    /**
     * 列配置管理
     * 用于保存和恢复表格列的显示状态和宽度
     */
    ColumnConfig: {
        /**
         * 保存列配置到 localStorage
         * @param tableId 表格ID
         * @param columns 列配置数组
         */
        save: function(tableId, columns) {
            try {
                var config = columns.map(function(col) {
                    return {
                        field: col.field,
                        visible: col.visible,
                        width: col.width
                    };
                });
                localStorage.setItem(tableId + '_columns', JSON.stringify(config));
            } catch (e) {
                console.warn('保存列配置失败:', e);
            }
        },

        /**
         * 从 localStorage 获取列配置
         * @param tableId 表格ID
         * @returns {Array|null} 列配置数组
         */
        get: function(tableId) {
            try {
                var config = localStorage.getItem(tableId + '_columns');
                return config ? JSON.parse(config) : null;
            } catch (e) {
                console.warn('读取列配置失败:', e);
                return null;
            }
        },

        /**
         * 应用列配置到列定义
         * @param tableId 表格ID
         * @param columns 列定义数组
         * @returns {Array} 应用配置后的列定义数组
         */
        apply: function(tableId, columns) {
            var config = this.get(tableId);
            if (config) {
                columns.forEach(function(col) {
                    var saved = config.find(function(c) { return c.field === col.field; });
                    if (saved) {
                        col.visible = saved.visible;
                        if (saved.width) col.width = saved.width;
                    }
                });
            }
            return columns;
        }
    },

    /**
     * 更新表格选中状态显示
     * 显示"已选 X 项"并启用/禁用批量操作按钮
     * @param tableId 表格ID或jQuery对象
     * @param options 配置选项
     */
    updateSelectedInfo: function(tableId, options) {
        options = options || {};
        var selectedInfoId = options.selectedInfoId || '#selectedInfo';
        var selectedCountId = options.selectedCountId || '#selectedCount';
        var multipleClass = options.multipleClass || '.multiple';

        var $table = typeof tableId === 'string' ? $(tableId) : tableId;
        if ($table.length === 0) return;

        // 获取选中的行
        var rows = $table.bootstrapTable('getSelections');
        var count = rows.length;

        // 更新选中数量
        $(selectedCountId).text(count);

        // 显示/隐藏选中信息
        if (count > 0) {
            $(selectedInfoId).show();
        } else {
            $(selectedInfoId).hide();
        }

        // 启用/禁用批量按钮
        if (count > 0) {
            $(multipleClass).removeClass('disabled').removeAttr('disabled');
        } else {
            $(multipleClass).addClass('disabled').attr('disabled', 'disabled');
        }
    },

    /**
     * 清空表格选择
     * @param tableId 表格ID
     */
    clearSelection: function(tableId) {
        var $table = typeof tableId === 'string' ? $(tableId) : tableId;
        $table.bootstrapTable('uncheckAll');
    },

    /**
     * 生成操作列的下拉菜单HTML
     * @param row 行数据
     * @param menuItems 菜单项数组，每项包含 {text, icon, action, permissionClass}
     * @param mainAction 主操作按钮配置 {text, icon, action, permissionClass, btnClass}
     * @returns {string} HTML字符串
     */
    buildActionDropdown: function(row, menuItems, mainAction) {
        var html = '<div class="btn-group">';

        // 主操作按钮
        if (mainAction) {
            var permissionClass = mainAction.permissionClass || '';
            var btnClass = mainAction.btnClass || 'btn-success';
            var action = mainAction.action
                .replace(/row\.id/g, row.id)
                .replace(/row\.username/g, "'" + (row.username || '') + "'")
                .replace(/row\.nickname/g, "'" + (row.nickname || '') + "'");

            html += '<a class="btn ' + btnClass + ' btn-xs ' + permissionClass +
                    '" href="javascript:void(0)" onclick="' + action + '" title="' + mainAction.text + '">' +
                    '<i class="fa ' + mainAction.icon + '"></i> ' + mainAction.text +
                    '</a> ';
        }

        // 下拉菜单按钮
        html += '<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">' +
                '<i class="fa fa-ellipsis-h"></i></button>' +
                '<ul class="dropdown-menu pull-right">';

        // 菜单项
        for (var i = 0; i < menuItems.length; i++) {
            var item = menuItems[i];
            if (item.divider) {
                html += '<li class="divider"></li>';
            } else {
                var permissionClass = item.permissionClass || '';
                var action = item.action
                    .replace(/row\.id/g, row.id)
                    .replace(/row\.username/g, "'" + (row.username || '') + "'")
                    .replace(/row\.nickname/g, "'" + (row.nickname || '') + "'");

                html += '<li><a class="' + permissionClass + '" href="javascript:void(0)" onclick="' + action + '">' +
                        '<i class="fa ' + item.icon + '"></i> ' + item.text +
                        '</a></li>';
            }
        }

        html += '</ul></div>';
        return html;
    },

    /**
     * 显示搜索条件状态
     * @param formId 表单ID
     * @param statusId 状态显示元素ID
     * @param clearAction 清除操作函数名
     */
    showSearchConditions: function(formId, statusId, clearAction) {
        formId = formId || '#user-form';
        statusId = statusId || '#searchStatus';
        clearAction = clearAction || 'clearSearch()';

        var conditions = [];
        $(formId + ' input[type="text"], ' + formId + ' select').each(function() {
            var $input = $(this);
            var $label = $input.closest('li').find('label');
            var name = $label.text().replace('：', '').replace(':', '');
            var value = $input.val();
            if (value) {
                conditions.push(name + '=' + value);
            }
        });

        if (conditions.length > 0) {
            var $status = $(statusId);
            $status.find('.search-conditions').text(conditions.join('，'));
            $status.show();
        } else {
            $(statusId).hide();
        }
    },

    /**
     * 优化后的空状态显示
     * @param message 提示信息
     * @param clearAction 清除操作函数名（可选）
     * @returns {string} HTML字符串
     */
    formatEmptyState: function(message, clearAction) {
        message = message || '暂无数据';
        var clearBtn = clearAction ?
            '<button class="btn btn-primary btn-sm" onclick="' + clearAction + '" style="margin-top:15px;">清空筛选</button>' : '';

        return '<div class="text-center" style="padding:40px 20px;color:#999;">' +
            '<i class="fa fa-inbox fa-3x" style="margin-bottom:15px;opacity:0.3;"></i>' +
            '<p style="margin:0;font-size:14px;">' + message + '</p>' +
            clearBtn +
            '</div>';
    }
};
