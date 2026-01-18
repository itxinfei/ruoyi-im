/**
 * RuoYi-IM 统一表格组件
 * 基于 bootstrap-table 封装
 */

(function($) {
    'use strict';

    /**
     * IM 表格组件
     */
    var IMTable = function(options) {
        this.options = $.extend({}, IMTable.DEFAULTS, options);
        this.$element = null;
        this.table = null;
        this.init();
    };

    IMTable.DEFAULTS = {
        url: '',                          // 请求地址
        method: 'post',                   // 请求方法
        contentType: 'application/json',  // 内容类型
        dataType: 'json',                 // 数据类型
        cache: false,                     // 是否缓存
        pagination: true,                 // 是否分页
        sidePagination: 'server',         // 分页方式：client/server
        pageSize: 10,                     // 每页记录数
        pageList: [10, 20, 50, 100],     // 分页选项
        showColumns: false,               // 显示列
        showRefresh: false,               // 刷新按钮
        showToggle: false,                // 视图切换
        cardView: false,                  // 卡片视图
        detailView: false,                // 详情视图
        sortable: true,                   // 是否排序
        sortOrder: 'asc',                 // 排序方式
        sortName: '',                     // 排序字段
        search: false,                    // 搜索
        searchAlign: 'right',             // 搜索框位置
        searchText: '',                   // 搜索提示文本
        strictSearch: false,              // 精确搜索
        showSearchButton: false,          // 显示搜索按钮
        showExtendedSearch: false,        // 显示高级搜索
        singleSelect: false,              // 单选
        checkboxHeader: true,             // 复选框头
        clickToSelect: false,             // 点击行选中
        maintainSelected: false,          // 维护选中状态
        toolbar: '#toolbar',              // 工具栏
        toolbarAlign: 'left',             // 工具栏位置
        buttonsAlign: 'right',            // 按钮位置
        buttonsClass: 'btn btn-sm btn-default', // 按钮样式
        showExport: false,                // 显示导出按钮
        exportTypes: ['json', 'xml', 'csv', 'txt', 'sql', 'excel'], // 导出类型
        exportOptions: {},                // 导出选项
        showFooter: false,                // 显示表尾
        footerStyle: false,               // 表尾样式
        columns: [],                      // 列配置
        data: [],                         // 静态数据
        ajaxOptions: {},                  // ajax 选项
        queryParams: function(params) {   // 查询参数
            return params;
        },
        responseHandler: function(res) {  // 响应处理
            return res;
        },
        onLoadError: function(status) {   // 加载错误
            $.modal.msgError('加载数据失败，请稍后重试');
        },
        onCheck: function(row, $element) {},        // 选中事件
        onUncheck: function(row, $element) {},      // 取消选中事件
        onCheckAll: function(rows, $element) {},     // 全选事件
        onUncheckAll: function(rows, $element) {},   // 取消全选事件
        onClickRow: function(item, $element) {},     // 点击行事件
        onDblClickRow: function(item, $element) {},  // 双击行事件
        onSort: function(name, order) {},            // 排序事件
        onPageChange: function(number, size) {},     // 页码改变事件
        onSearch: function(text) {},                 // 搜索事件
        onShowSearch: function() {},                 // 显示搜索
        onResetSearch: function() {},               // 重置搜索
        onSuccess: function(data, textStatus) {},   // 成功回调
        onError: function(status, res) {}           // 错误回调
    };

    IMTable.prototype.init = function() {
        var that = this;
        var $el = $(this.options.el);

        if (!$el.length) {
            console.error('IMTable: 找不到表格元素');
            return;
        }

        this.$element = $el;
        this.table = $el.bootstrapTable(this.getOptions());

        this.bindEvents();
    };

    IMTable.prototype.getOptions = function() {
        var options = $.extend({}, this.options);

        // 处理 columns
        if (options.columns && options.columns.length > 0) {
            options.columns = this.processColumns(options.columns);
        }

        // 处理 toolbar
        if (options.toolbar) {
            options.toolbarAlign = 'left';
        }

        return options;
    };

    IMTable.prototype.processColumns = function(columns) {
        // 添加序号列
        var processedColumns = [];

        // 如果有复选框，不添加序号列
        var hasCheckbox = columns.some(function(col) {
            return col.checkbox === true;
        });

        if (!hasCheckbox && this.options.showSerialNumber) {
            processedColumns.push({
                field: 'serialNumber',
                title: '序号',
                width: '50',
                align: 'center',
                formatter: function(value, row, index) {
                    var pageNumber = $(this).bootstrapTable('getOptions').pageNumber;
                    var pageSize = $(this).bootstrapTable('getOptions').pageSize;
                    return (pageNumber - 1) * pageSize + index + 1;
                }
            });
        }

        return processedColumns.concat(columns);
    };

    IMTable.prototype.bindEvents = function() {
        var that = this;

        // 刷新按钮
        this.$element.closest('.bootstrap-table').find('.refresh').on('click', function() {
            that.refresh();
        });

        // 搜索按钮
        this.$element.closest('.bootstrap-table').find('.search').on('click', function() {
            that.search();
        });

        // 重置按钮
        this.$element.closest('.bootstrap-table').find('.reset').on('click', function() {
            that.resetSearch();
        });
    };

    /**
     * 刷新表格
     */
    IMTable.prototype.refresh = function(params) {
        this.$element.bootstrapTable('refresh', params);
    };

    /**
     * 加载数据
     */
    IMTable.prototype.load = function(data) {
        this.$element.bootstrapTable('load', data);
    };

    /**
     * 获取选中行
     */
    IMTable.prototype.getSelections = function() {
        return this.$element.bootstrapTable('getSelections');
    };

    /**
     * 获取所有数据（包括服务端和客户端）
     */
    IMTable.prototype.getData = function() {
        return this.$element.bootstrapTable('getData');
    };

    /**
     * 获取选项
     */
    IMTable.prototype.getOptions = function() {
        return this.$element.bootstrapTable('getOptions');
    };

    /**
     * 搜索
     */
    IMTable.prototype.search = function(text) {
        if (typeof text !== 'undefined') {
            this.$element.bootstrapTable('search', text);
        } else {
            var searchText = this.$element.closest('.bootstrap-table').find('.search input').val();
            this.$element.bootstrapTable('search', searchText);
        }
    };

    /**
     * 重置搜索
     */
    IMTable.prototype.resetSearch = function() {
        this.$element.bootstrapTable('resetSearch');
    };

    /**
     * 显示加载中
     */
    IMTable.prototype.showLoading = function() {
        this.$element.bootstrapTable('showLoading');
    };

    /**
     * 隐藏加载中
     */
    IMTable.prototype.hideLoading = function() {
        this.$element.bootstrapTable('hideLoading');
    };

    /**
     * 更新行
     */
    IMTable.prototype.updateRow = function(params) {
        this.$element.bootstrapTable('updateRow', params);
    };

    /**
     * 追加行
     */
    IMTable.prototype.append = function(data) {
        this.$element.bootstrapTable('append', data);
    };

    /**
     * 删除行
     */
    IMTable.prototype.remove = function(params) {
        this.$element.bootstrapTable('remove', params);
    };

    /**
     * 插入行
     */
    IMTable.prototype.insertRow = function(params) {
        this.$element.bootstrapTable('insertRow', params);
    };

    /**
     * 销毁表格
     */
    IMTable.prototype.destroy = function() {
        this.$element.bootstrapTable('destroy');
    };

    /**
     * 重置视图
     */
    IMTable.prototype.resetView = function(params) {
        this.$element.bootstrapTable('resetView', params);
    };

    /**
     * 展开所有行
     */
    IMTable.prototype.expandAllRows = function() {
        this.$element.bootstrapTable('expandAllRows');
    };

    /**
     * 收起所有行
     */
    IMTable.prototype.collapseAllRows = function() {
        this.$element.bootstrapTable('collapseAllRows');
    };

    /**
     * 导出数据
     */
    IMTable.prototype.exportData = function(type) {
        type = type || 'excel';
        var data = this.getData();
        var columns = this.getOptions().columns;

        // 过滤掉复选框和操作列
        var exportColumns = columns.filter(function(col) {
            return !col.checkbox && col.field !== 'operate';
        });

        var table = $('<table>');
        var thead = $('<thead>');
        var tbody = $('<tbody>');

        // 表头
        var headerRow = $('<tr>');
        exportColumns.forEach(function(col) {
            headerRow.append('<th>' + (col.title || col.field) + '</th>');
        });
        thead.append(headerRow);

        // 表体
        data.forEach(function(row) {
            var dataRow = $('<tr>');
            exportColumns.forEach(function(col) {
                var value = row[col.field];
                if (col.formatter) {
                    value = col.formatter(value, row, -1);
                }
                // 清除 HTML 标签
                value = $('<div>').html(value).text();
                dataRow.append('<td>' + (value || '') + '</td>');
            });
            tbody.append(dataRow);
        });

        table.append(thead);
        table.append(tbody);

        // 导出
        if (type === 'excel') {
            this.exportToExcel(table);
        } else if (type === 'csv') {
            this.exportToCSV(table);
        }
    };

    IMTable.prototype.exportToExcel = function(table) {
        var html = table[0].outerHTML;
        var blob = new Blob(['\ufeff', html], {
            type: 'application/vnd.ms-excel'
        });
        var url = URL.createObjectURL(blob);
        var a = $('<a>')
            .attr('href', url)
            .attr('download', '导出数据_' + moment().format('YYYYMMDDHHmmss') + '.xls')
            .appendTo('body');
        a[0].click();
        a.remove();
    };

    IMTable.prototype.exportToCSV = function(table) {
        var csv = [];
        table.find('tr').each(function() {
            var row = [];
            $(this).find('th, td').each(function() {
                var text = $(this).text();
                text = text.replace(/"/g, '""');
                row.push('"' + text + '"');
            });
            csv.push(row.join(','));
        });

        var csvString = csv.join('\n');
        var blob = new Blob(['\ufeff', csvString], {
            type: 'text/csv;charset=utf-8;'
        });
        var url = URL.createObjectURL(blob);
        var a = $('<a>')
            .attr('href', url)
            .attr('download', '导出数据_' + moment().format('YYYYMMDDHHmmss') + '.csv')
            .appendTo('body');
        a[0].click();
        a.remove();
    };

    /**
     * jQuery 插件注册
     */
    $.fn.imTable = function(options) {
        return this.each(function() {
            var $this = $(this);
            var data = $this.data('im.table');

            if (!data) {
                options = $.extend({}, options);
                options.el = this;
                $this.data('im.table', (data = new IMTable(options)));
            }

            if (typeof options === 'string') {
                data[options]();
            }
        });
    };

    $.fn.imTable.Constructor = IMTable;

})(jQuery);
