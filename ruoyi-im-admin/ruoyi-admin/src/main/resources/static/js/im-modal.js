/**
 * IM后台管理系统 - 弹窗子页面统一交互
 * 轻量扁平风设计规范
 */

var ImModal = (function() {
    'use strict';

    // ==================== 工具函数 ====================

    /**
     * 显示加载状态
     */
    function showLoading() {
        $('.table-loading').addClass('show');
    }

    /**
     * 隐藏加载状态
     */
    function hideLoading() {
        $('.table-loading').removeClass('show');
    }

    /**
     * 格式化时间
     * @param {string} dateTime - 时间字符串
     * @param {boolean} showTime - 是否显示时间
     * @returns {string} 格式化后的时间
     */
    function formatDateTime(dateTime, showTime) {
        if (!dateTime) return '-';
        var time = dateTime.toString().substring(0, showTime ? 19 : 10);
        return time.replace('T', ' ');
    }

    /**
     * 防抖函数
     * @param {Function} func - 要防抖的函数
     * @param {number} wait - 等待时间(ms)
     * @returns {Function} 防抖后的函数
     */
    function debounce(func, wait) {
        var timeout;
        return function executedFunction() {
            var context = this;
            var args = arguments;
            clearTimeout(timeout);
            timeout = setTimeout(function() {
                func.apply(context, args);
            }, wait);
        };
    }

    // ==================== 页面操作 ====================

    /**
     * 返回上一页（关闭弹窗）
     */
    function goBack() {
        $.modal.close();
    }

    /**
     * 刷新页面数据
     * @param {Function} refreshCallback - 刷新回调函数
     */
    function refresh(refreshCallback) {
        var $btn = $('button[onclick="refreshData()"]');
        if ($btn.length === 0) {
            $btn = $('.header-actions .btn-im-outline:last');
        }

        $btn.prop('disabled', true).html('<i class="fa fa-spinner fa-spin"></i> 刷新中');

        var complete = function() {
            $btn.prop('disabled', false).html('<i class="fa fa-refresh"></i> 刷新');
        };

        if (typeof refreshCallback === 'function') {
            $.when(refreshCallback()).always(complete);
        } else {
            complete();
            location.reload();
        }
    }

    // ==================== 数据操作 ====================

    /**
     * 统一的Ajax请求
     * @param {Object} options - 请求配置
     * @returns {jQuery.Deferred} Ajax对象
     */
    function requestData(options) {
        showLoading();

        var defaults = {
            type: 'POST',
            dataType: 'json'
        };

        var settings = $.extend({}, defaults, options);

        return $.ajax(settings)
            .fail(function() {
                $.modal.msgError('网络请求失败');
            })
            .always(function() {
                hideLoading();
            });
    }

    /**
     * 统一的操作确认
     * @param {string} actionName - 操作名称
     * @param {string} url - 请求地址
     * @param {Object} data - 请求数据
     * @param {Function} callback - 成功回调
     */
    function doAction(actionName, url, data, callback) {
        $.modal.confirm('确定要' + actionName + '吗？', function() {
            $.modal.msg('正在' + actionName + '...', {icon: 16, shade: 0.3, time: 0});

            requestData({
                url: url,
                data: data,
                success: function(result) {
                    layer.closeAll();
                    if (result.code == 0 || result.code == 200) {
                        $.modal.msgSuccess(actionName + '成功');
                        if (typeof callback === 'function') {
                            callback(result);
                        }
                    } else {
                        $.modal.msgError(result.msg || actionName + '失败');
                    }
                }
            });
        });
    }

    /**
     * 批量操作
     * @param {string} actionName - 操作名称
     * @param {string} url - 请求地址
     * @param {Array} ids - ID数组
     * @param {Function} callback - 成功回调
     */
    function batchAction(actionName, url, ids, callback) {
        if (!ids || ids.length === 0) {
            $.modal.alertWarning('请至少选择一条记录');
            return;
        }

        var data = {};
        if (url.indexOf('ids=') === -1) {
            data.ids = ids.join(',');
        }

        doAction('批量' + actionName, url, data, callback);
    }

    // ==================== 表格操作 ====================

    /**
     * 初始化Bootstrap Table
     * @param {Object} options - 表格配置
     * @returns {jQuery} 表格对象
     */
    function initTable(options) {
        var defaults = {
            cache: false,
            pagination: true,
            pageSize: 20,
            pageList: [10, 20, 50, 100],
            sidePagination: 'client',
            search: false,
            showColumns: false,
            showRefresh: false,
            classes: 'table table-im table-hover',
            theadClasses: 'thead-light'
        };

        var settings = $.extend({}, defaults, options);

        return $('#bootstrap-table').bootstrapTable(settings);
    }

    /**
     * 刷新表格
     */
    function refreshTable() {
        $('#bootstrap-table').bootstrapTable('refresh');
    }

    /**
     * 加载数据到表格
     * @param {Array} data - 数据数组
     */
    function loadTableData(data) {
        $('#bootstrap-table').bootstrapTable('load', data);
    }

    /**
     * 获取选中的行
     * @param {string} field - 字段名，默认为id
     * @returns {Array} 选中行的字段值数组
     */
    function getSelections(field) {
        field = field || 'id';
        return $.table.selectColumns(field);
    }

    /**
     * 获取选中的第一行
     * @returns {Object|null} 第一行数据或null
     */
    function getFirstSelected() {
        var rows = $('#bootstrap-table').bootstrapTable('getSelections');
        return rows.length > 0 ? rows[0] : null;
    }

    // ==================== 搜索筛选 ====================

    /**
     * 初始化实时搜索
     * @param {Function} searchCallback - 搜索回调函数
     * @param {number} delay - 防抖延迟(ms)
     */
    function initSearch(searchCallback, delay) {
        delay = delay || 300;

        $('#searchInput').on('keyup', debounce(function() {
            if (typeof searchCallback === 'function') {
                searchCallback();
            }
        }, delay));
    }

    /**
     * 初始化筛选器
     * @param {Function} filterCallback - 筛选回调函数
     */
    function initFilters(filterCallback) {
        $('#roleFilter, #statusFilter, #typeFilter').on('change', function() {
            if (typeof filterCallback === 'function') {
                filterCallback();
            }
        });
    }

    /**
     * 重置筛选
     * @param {Function} resetCallback - 重置回调函数
     */
    function resetFilters(resetCallback) {
        $('#searchInput').val('');
        $('#roleFilter, #statusFilter, #typeFilter').val('');

        if (typeof resetCallback === 'function') {
            resetCallback();
        }
    }

    /**
     * 前端筛选数据
     * @param {Array} allData - 所有数据
     * @param {Object} filters - 筛选条件
     * @returns {Array} 筛选后的数据
     */
    function filterData(allData, filters) {
        if (!allData || allData.length === 0) {
            return [];
        }

        var keyword = (filters.keyword || '').toLowerCase();
        var role = filters.role || '';
        var status = filters.status || '';
        var type = filters.type || '';

        return allData.filter(function(item) {
            // 关键词搜索
            if (keyword) {
                var searchFields = ['name', 'nickname', 'userName', 'userNickname', 'title'];
                var match = searchFields.some(function(field) {
                    var value = item[field] || '';
                    return value.toString().toLowerCase().indexOf(keyword) !== -1;
                });
                if (!match) return false;
            }

            // 角色筛选
            if (role && item.role !== role) {
                return false;
            }

            // 状态筛选
            if (status === 'pinned' && item.isPinned != 1) {
                return false;
            }
            if (status === 'muted' && item.isMuted != 1) {
                return false;
            }
            if (status === 'unread' && (!item.unreadCount || item.unreadCount == 0)) {
                return false;
            }

            // 类型筛选
            if (type && item.type !== type) {
                return false;
            }

            return true;
        });
    }

    // ==================== 统计更新 ====================

    /**
     * 更新统计数据
     * @param {Array} data - 数据数组
     * @param {Object} statsConfig - 统计配置
     */
    function updateStatistics(data, statsConfig) {
        if (!data || !statsConfig) {
            return;
        }

        var stats = {
            total: data.length
        };

        // 计算各分组统计
        if (statsConfig.groups) {
            for (var key in statsConfig.groups) {
                var field = statsConfig.groups[key];
                stats[key] = data.filter(function(item) {
                    return item[field] === key.toUpperCase();
                }).length;
            }
        }

        // 计算条件统计
        if (statsConfig.conditions) {
            for (var condKey in statsConfig.conditions) {
                var condition = statsConfig.conditions[condKey];
                stats[condKey] = data.filter(function(item) {
                    for (var field in condition) {
                        if (item[field] != condition[field]) {
                            return false;
                        }
                    }
                    return true;
                }).length;
            }
        }

        // 更新DOM
        for (var statKey in stats) {
            var $el = $('#' + statKey + 'Count');
            if ($el.length > 0) {
                $el.text(stats[statKey]);
            }
        }
    }

    // ==================== 格式化器 ====================

    /**
     * 格式化角色
     * @param {string} role - 角色值
     * @returns {string} HTML格式的角色徽章
     */
    function formatRole(role) {
        var roleMap = {
            'OWNER': '<span class="badge-im badge-im-danger">群主</span>',
            'ADMIN': '<span class="badge-im badge-im-warning">管理员</span>',
            'MEMBER': '<span class="badge-im badge-im-primary">成员</span>'
        };
        return roleMap[role] || '<span class="badge-im badge-im-default">成员</span>';
    }

    /**
     * 格式化状态
     * @param {number} value - 状态值
     * @param {string} type - 状态类型
     * @returns {string} HTML格式的状态徽章
     */
    function formatStatus(value, type) {
        switch (type) {
            case 'yesno':
                return value == 1
                    ? '<span class="badge-im badge-im-success">是</span>'
                    : '<span class="badge-im badge-im-default">否</span>';
            case 'enable':
                return value == 1
                    ? '<span class="badge-im badge-im-success">正常</span>'
                    : '<span class="badge-im badge-im-danger">停用</span>';
            case 'online':
                return value == 1
                    ? '<span class="badge-im badge-im-success">在线</span>'
                    : '<span class="badge-im badge-im-default">离线</span>';
            default:
                return '<span class="badge-im badge-im-default">' + value + '</span>';
        }
    }

    /**
     * 格式化头像
     * @param {string} avatar - 头像URL
     * @param {string} size - 尺寸(sm/lg)
     * @returns {string} HTML格式的头像
     */
    function formatAvatar(avatar, size) {
        var sizeClass = size ? 'avatar-im-' + size : '';
        if (avatar) {
            return '<img src="' + avatar + '" class="avatar-im-circle ' + sizeClass + '" onerror="this.src=\'/img/profile.jpg\'"/>';
        }
        return '<i class="fa fa-user-circle fa-2x text-muted"></i>';
    }

    // ==================== 导出接口 ====================
    return {
        // 工具函数
        showLoading: showLoading,
        hideLoading: hideLoading,
        formatDateTime: formatDateTime,
        debounce: debounce,

        // 页面操作
        goBack: goBack,
        refresh: refresh,

        // 数据操作
        requestData: requestData,
        doAction: doAction,
        batchAction: batchAction,

        // 表格操作
        initTable: initTable,
        refreshTable: refreshTable,
        loadTableData: loadTableData,
        getSelections: getSelections,
        getFirstSelected: getFirstSelected,

        // 搜索筛选
        initSearch: initSearch,
        initFilters: initFilters,
        resetFilters: resetFilters,
        filterData: filterData,

        // 统计更新
        updateStatistics: updateStatistics,

        // 格式化器
        formatRole: formatRole,
        formatStatus: formatStatus,
        formatAvatar: formatAvatar
    };

})();
