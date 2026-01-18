/**
 * RuoYi-IM 统一弹窗组件
 */

(function($) {
    'use strict';

    /**
     * IM 弹窗组件
     */
    var IMModal = function(options) {
        this.options = $.extend({}, IMModal.DEFAULTS, options);
        this.modal = null;
        this.init();
    };

    IMModal.DEFAULTS = {
        title: '提示',                       // 标题
        content: '',                         // 内容
        type: 'info',                        // 类型：info/success/warning/danger
        size: 'md',                          // 大小：sm/md/lg/xl
        buttons: [],                         // 按钮
        showClose: true,                     // 显示关闭按钮
        closeOnClickOutside: false,          // 点击外部关闭
        onShow: function() {},               // 显示回调
        onHide: function() {},               // 隐藏回调
        onConfirm: function() {},            // 确认回调
        onCancel: function() {}              // 取消回调
    };

    IMModal.prototype.init = function() {
        // 不自动初始化，等待调用 show 方法
    };

    /**
     * 显示弹窗
     */
    IMModal.prototype.show = function(options) {
        var that = this;

        if (options) {
            this.options = $.extend({}, this.options, options);
        }

        var content = this.buildContent();

        // 使用 layer 弹窗
        if (typeof layer !== 'undefined') {
            this.modal = layer.open({
                type: 1,
                title: this.options.title,
                area: this.getSizeClass(),
                content: content,
                btn: this.buildButtons(),
                yes: function(index) {
                    if (that.options.onConfirm()) {
                        layer.close(index);
                    }
                },
                btn2: function(index) {
                    that.options.onCancel();
                    layer.close(index);
                },
                cancel: function() {
                    that.options.onCancel();
                },
                end: function() {
                    that.options.onHide();
                }
            });

            this.options.onShow();
        } else {
            // 回退到原生 alert
            if (this.options.type === 'confirm') {
                if (confirm(this.options.content)) {
                    this.options.onConfirm();
                } else {
                    this.options.onCancel();
                }
            } else {
                alert(this.options.content);
            }
        }
    };

    /**
     * 构建内容
     */
    IMModal.prototype.buildContent = function() {
        var content = this.options.content;

        if (typeof content === 'string') {
            content = '<div style="padding: 20px;">' + content + '</div>';
        } else if (content instanceof jQuery) {
            content = '<div style="padding: 20px;"></div>').append(content);
        }

        return content;
    };

    /**
     * 构建按钮
     */
    IMModal.prototype.buildButtons = function() {
        var buttons = this.options.buttons;

        if (buttons.length === 0) {
            // 默认按钮
            if (this.options.type === 'confirm') {
                return ['确认', '取消'];
            } else {
                return ['确定'];
            }
        }

        return buttons;
    };

    /**
     * 获取尺寸类
     */
    IMModal.prototype.getSizeClass = function() {
        var sizeMap = {
            'sm': ['400px', 'auto'],
            'md': ['600px', 'auto'],
            'lg': ['800px', 'auto'],
            'xl': ['1000px', 'auto']
        };
        return sizeMap[this.options.size] || sizeMap['md'];
    };

    /**
     * 关闭弹窗
     */
    IMModal.prototype.close = function() {
        if (this.modal) {
            layer.close(this.modal);
            this.modal = null;
        }
    };

    /**
     * 静态方法：信息提示
     */
    IMModal.info = function(message, title, onConfirm) {
        var modal = new IMModal({
            title: title || '信息',
            content: message,
            type: 'info',
            onConfirm: onConfirm || function() { return true; }
        });
        modal.show();
    };

    /**
     * 静态方法：成功提示
     */
    IMModal.success = function(message, title, onConfirm) {
        var modal = new IMModal({
            title: title || '成功',
            content: message,
            type: 'success',
            onConfirm: onConfirm || function() { return true; }
        });
        modal.show();
    };

    /**
     * 静态方法：警告提示
     */
    IMModal.warning = function(message, title, onConfirm) {
        var modal = new IMModal({
            title: title || '警告',
            content: message,
            type: 'warning',
            onConfirm: onConfirm || function() { return true; }
        });
        modal.show();
    };

    /**
     * 静态方法：错误提示
     */
    IMModal.error = function(message, title, onConfirm) {
        var modal = new IMModal({
            title: title || '错误',
            content: message,
            type: 'danger',
            onConfirm: onConfirm || function() { return true; }
        });
        modal.show();
    };

    /**
     * 静态方法：确认对话框
     */
    IMModal.confirm = function(message, onConfirm, onCancel) {
        var modal = new IMModal({
            title: '确认',
            content: message,
            type: 'confirm',
            onConfirm: onConfirm || function() { return true; },
            onCancel: onCancel || function() {}
        });
        modal.show();
    };

    /**
     * 静态方法：详情查看
     */
    IMModal.detail = function(title, content, size) {
        var modal = new IMModal({
            title: title,
            content: content,
            type: 'info',
            size: size || 'lg'
        });
        modal.show();
    };

    /**
     * 静态方法：表单弹窗
     */
    IMModal.form = function(title, formContent, onSubmit, size) {
        var modal = new IMModal({
            title: title,
            content: formContent,
            type: 'form',
            size: size || 'md',
            onConfirm: onSubmit || function() { return true; }
        });
        modal.show();
    };

    /**
     * 静态方法：iframe 弹窗
     */
    IMModal.iframe = function(title, url, size) {
        var content = '<iframe src="' + url + '" style="width:100%;height:100%;border:none;"></iframe>';
        var modal = new IMModal({
            title: title,
            content: content,
            type: 'iframe',
            size: size || 'xl'
        });
        modal.show();
    };

    /**
     * jQuery 插件注册
     */
    $.fn.imModal = function(options) {
        return this.each(function() {
            var $this = $(this);
            var data = $this.data('im.modal');

            if (!data) {
                options = $.extend({}, options);
                $this.data('im.modal', (data = new IMModal(options)));
            }

            if (typeof options === 'string') {
                data[options]();
            } else {
                data.show();
            }
        });
    };

    $.fn.imModal.Constructor = IMModal;

    // 全局暴露
    window.IMModal = IMModal;

})(jQuery);
