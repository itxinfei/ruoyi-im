/**
 * RuoYi-IM 统一搜索组件
 */

(function($) {
    'use strict';

    /**
     * IM 搜索组件
     */
    var IMSearch = function(options) {
        this.options = $.extend({}, IMSearch.DEFAULTS, options);
        this.$element = null;
        this.init();
    };

    IMSearch.DEFAULTS = {
        el: '#searchForm',                  // 表单元素
        fields: [],                          // 搜索字段配置
        onSearch: function(params) {},      // 搜索回调
        onReset: function() {},             // 重置回调
        showAdvanced: false,                // 是否显示高级搜索
        advancedFields: [],                 // 高级搜索字段
        dateRangeFields: []                 // 日期范围字段
    };

    IMSearch.prototype.init = function() {
        var that = this;
        var $el = $(this.options.el);

        if (!$el.length) {
            console.error('IMSearch: 找不到搜索表单');
            return;
        }

        this.$element = $el;
        this.initFields();
        this.bindEvents();
    };

    IMSearch.prototype.initFields = function() {
        var that = this;

        // 初始化普通搜索字段
        this.options.fields.forEach(function(field) {
            var input = that.$element.find('[name="' + field.name + '"]');
            if (field.placeholder) {
                input.attr('placeholder', field.placeholder);
            }
            if (field.type) {
                input.attr('type', field.type);
            }
        });

        // 初始化日期范围字段
        this.options.dateRangeFields.forEach(function(field) {
            var startInput = that.$element.find('[name="' + field.start + '"]');
            var endInput = that.$element.find('[name="' + field.end + '"]');

            if (startInput.length && endInput.length) {
                // 使用 laydate 日期选择器
                if (typeof layui !== 'undefined' && layui.laydate) {
                    layui.laydate.render({
                        elem: startInput[0],
                        type: field.type || 'date',
                        format: field.format || 'yyyy-MM-dd'
                    });

                    layui.laydate.render({
                        elem: endInput[0],
                        type: field.type || 'date',
                        format: field.format || 'yyyy-MM-dd'
                    });
                }
            }
        });
    };

    IMSearch.prototype.bindEvents = function() {
        var that = this;

        // 搜索按钮
        this.$element.find('.btn-search').on('click', function(e) {
            e.preventDefault();
            that.search();
        });

        // 重置按钮
        this.$element.find('.btn-reset').on('click', function(e) {
            e.preventDefault();
            that.reset();
        });

        // 高级搜索按钮
        this.$element.find('.btn-advanced').on('click', function(e) {
            e.preventDefault();
            that.toggleAdvanced();
        });

        // 回车搜索
        this.$element.find('input[type="text"]').on('keypress', function(e) {
            if (e.which === 13) {
                e.preventDefault();
                that.search();
            }
        });
    };

    /**
     * 执行搜索
     */
    IMSearch.prototype.search = function() {
        var params = this.getParams();
        this.options.onSearch(params);
    };

    /**
     * 获取搜索参数
     */
    IMSearch.prototype.getParams = function() {
        var params = {};
        var that = this;

        // 获取普通字段
        this.options.fields.forEach(function(field) {
            var input = that.$element.find('[name="' + field.name + '"]');
            var value = input.val();

            if (value && value.trim() !== '') {
                params[field.name] = value.trim();
            }
        });

        // 获取日期范围字段
        this.options.dateRangeFields.forEach(function(field) {
            var startInput = that.$element.find('[name="' + field.start + '"]');
            var endInput = that.$element.find('[name="' + field.end + '"]');
            var startValue = startInput.val();
            var endValue = endInput.val();

            if (startValue || endValue) {
                if (!params.params) {
                    params.params = {};
                }
                params.params[field.start] = startValue;
                params.params[field.end] = endValue;
            }
        });

        return params;
    };

    /**
     * 重置搜索
     */
    IMSearch.prototype.reset = function() {
        // 清空所有输入
        this.$element.find('input').val('');
        this.$element.find('select').val('');
        this.options.onReset();
    };

    /**
     * 切换高级搜索
     */
    IMSearch.prototype.toggleAdvanced = function() {
        var advancedPanel = this.$element.find('.advanced-search');
        advancedPanel.slideToggle();
    };

    /**
     * 设置参数
     */
    IMSearch.prototype.setParams = function(params) {
        var that = this;

        // 设置普通字段
        Object.keys(params).forEach(function(key) {
            if (key !== 'params') {
                var input = that.$element.find('[name="' + key + '"]');
                if (input.length) {
                    input.val(params[key]);
                }
            }
        });

        // 设置日期范围字段
        if (params.params) {
            Object.keys(params.params).forEach(function(key) {
                var input = that.$element.find('[name="params[' + key + ']"]');
                if (input.length) {
                    input.val(params.params[key]);
                }
            });
        }
    };

    /**
     * jQuery 插件注册
     */
    $.fn.imSearch = function(options) {
        return this.each(function() {
            var $this = $(this);
            var data = $this.data('im.search');

            if (!data) {
                options = $.extend({}, options);
                options.el = this;
                $this.data('im.search', (data = new IMSearch(options)));
            }

            if (typeof options === 'string') {
                data[options]();
            }
        });
    };

    $.fn.imSearch.Constructor = IMSearch;

})(jQuery);
