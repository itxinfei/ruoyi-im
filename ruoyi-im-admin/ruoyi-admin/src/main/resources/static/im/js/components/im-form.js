/**
 * RuoYi-IM 统一表单组件
 */

(function($) {
    'use strict';

    /**
     * IM 表单组件
     */
    var IMForm = function(options) {
        this.options = $.extend({}, IMForm.DEFAULTS, options);
        this.$element = null;
        this.validator = null;
        this.init();
    };

    IMForm.DEFAULTS = {
        el: '#dataForm',                   // 表单元素
        url: '',                           // 提交地址
        method: 'post',                    // 提交方法
        dataType: 'json',                  // 返回数据类型
        ajaxSubmit: true,                  // 是否ajax提交
        resetAfterSubmit: true,            // 提交后重置表单
        closeAfterSubmit: false,           // 提交后关闭弹窗
        rules: {},                         // 验证规则
        messages: {},                      // 验证消息
        beforeSubmit: function() {},       // 提交前回调
        onSuccess: function(data) {},      // 成功回调
        onError: function() {}             // 失败回调
    };

    IMForm.prototype.init = function() {
        var that = this;
        var $el = $(this.options.el);

        if (!$el.length) {
            console.error('IMForm: 找不到表单元素');
            return;
        }

        this.$element = $el;
        this.initValidator();
        this.bindEvents();
    };

    /**
     * 初始化验证器
     */
    IMForm.prototype.initValidator = function() {
        if (typeof $.validator === 'undefined') {
            console.warn('IMForm: jQuery Validate 插件未加载');
            return;
        }

        var that = this;

        this.validator = this.$element.validate({
            rules: this.options.rules,
            messages: this.options.messages,
            errorElement: 'span',
            errorClass: 'help-block help-block-error',
            errorPlacement: function(error, element) {
                error.appendTo(element.closest('.form-group'));
            },
            highlight: function(element) {
                $(element).closest('.form-group').addClass('has-error');
            },
            success: function(label) {
                label.closest('.form-group').removeClass('has-error');
                label.remove();
            }
        });
    };

    /**
     * 绑定事件
     */
    IMForm.prototype.bindEvents = function() {
        var that = this;

        // 提交按钮
        this.$element.find('.btn-submit').on('click', function(e) {
            e.preventDefault();
            that.submit();
        });

        // 重置按钮
        this.$element.find('.btn-reset').on('click', function(e) {
            e.preventDefault();
            that.reset();
        });

        // 取消按钮
        this.$element.find('.btn-cancel').on('click', function(e) {
            e.preventDefault();
            that.cancel();
        });
    };

    /**
     * 提交表单
     */
    IMForm.prototype.submit = function() {
        var that = this;

        // 验证表单
        if (this.validator && !this.$element.valid()) {
            return false;
        }

        // 提交前回调
        if (this.options.beforeSubmit() === false) {
            return false;
        }

        var data = this.getData();

        if (this.options.ajaxSubmit) {
            // AJAX 提交
            $.ajax({
                url: this.options.url,
                type: this.options.method,
                contentType: 'application/json',
                data: JSON.stringify(data),
                dataType: this.options.dataType,
                success: function(result) {
                    if (result.code === 0 || result.code === 200) {
                        that.options.onSuccess(result);

                        if (that.options.resetAfterSubmit) {
                            that.reset();
                        }

                        if (that.options.closeAfterSubmit) {
                            that.close();
                        }

                        $.modal.msgSuccess(result.msg || '操作成功');
                    } else {
                        that.options.onError();
                        $.modal.msgError(result.msg || '操作失败');
                    }
                },
                error: function() {
                    that.options.onError();
                    $.modal.msgError('网络请求失败');
                }
            });
        } else {
            // 原生提交
            this.$element[0].submit();
        }
    };

    /**
     * 获取表单数据
     */
    IMForm.prototype.getData = function() {
        var data = {};
        var that = this;

        // 序列化表单
        var formData = this.$element.serializeArray();

        formData.forEach(function(item) {
            // 跳过空值
            if (item.value && item.value.trim() !== '') {
                // 处理同名多选框
                if (data[item.name]) {
                    if (Array.isArray(data[item.name])) {
                        data[item.name].push(item.value);
                    } else {
                        data[item.name] = [data[item.name], item.value];
                    }
                } else {
                    data[item.name] = item.value;
                }
            }
        });

        return data;
    };

    /**
     * 设置表单数据
     */
    IMForm.prototype.setData = function(data) {
        var that = this;

        Object.keys(data).forEach(function(key) {
            var value = data[key];
            var input = that.$element.find('[name="' + key + '"]');

            if (input.is(':checkbox')) {
                input.prop('checked', value);
            } else if (input.is(':radio')) {
                input.filter('[value="' + value + '"]').prop('checked', true);
            } else if (input.is('select')) {
                input.val(value);
            } else {
                input.val(value);
            }
        });
    };

    /**
     * 重置表单
     */
    IMForm.prototype.reset = function() {
        this.$element[0].reset();

        // 重置验证状态
        if (this.validator) {
            this.validator.resetForm();
        }

        // 移除错误样式
        this.$element.find('.form-group').removeClass('has-error');
        this.$element.find('.help-block-error').remove();
    };

    /**
     * 取消操作
     */
    IMForm.prototype.cancel = function() {
        // 关闭弹窗
        if (typeof layer !== 'undefined') {
            var index = layer.getFrameIndex(window.name);
            layer.close(index);
        }
    };

    /**
     * 关闭弹窗
     */
    IMForm.prototype.close = function() {
        this.cancel();
    };

    /**
     * 验证表单
     */
    IMForm.prototype.valid = function() {
        if (this.validator) {
            return this.$element.valid();
        }
        return true;
    };

    /**
     * 添加验证规则
     */
    IMForm.prototype.addRule = function(field, rule, message) {
        this.options.rules[field] = rule;
        if (message) {
            this.options.messages[field] = this.options.messages[field] || {};
            this.options.messages[field][rule] = message;
        }

        // 重新初始化验证器
        if (this.validator) {
            this.$element.removeData('validator');
            this.initValidator();
        }
    };

    /**
     * 移除验证规则
     */
    IMForm.prototype.removeRule = function(field) {
        delete this.options.rules[field];
        delete this.options.messages[field];

        // 重新初始化验证器
        if (this.validator) {
            this.$element.removeData('validator');
            this.initValidator();
        }
    };

    /**
     * jQuery 插件注册
     */
    $.fn.imForm = function(options) {
        return this.each(function() {
            var $this = $(this);
            var data = $this.data('im.form');

            if (!data) {
                options = $.extend({}, options);
                options.el = this;
                $this.data('im.form', (data = new IMForm(options)));
            }

            if (typeof options === 'string') {
                data[options]();
            }
        });
    };

    $.fn.imForm.Constructor = IMForm;

})(jQuery);
