/**
 * RuoYi-IM 统计卡片组件
 */

(function($) {
    'use strict';

    /**
     * IM 统计卡片组件
     */
    var IMStatCards = function(options) {
        this.options = $.extend({}, IMStatCards.DEFAULTS, options);
        this.$element = null;
        this.init();
    };

    IMStatCards.DEFAULTS = {
        el: '.statistics-section',      // 卡片容器
        url: '',                          // 数据接口
        method: 'get',                   // 请求方法
        cards: [],                        // 卡片配置
        data: {},                         // 初始数据
        autoLoad: true,                   // 自动加载
        onLoadSuccess: function(data) {}, // 加载成功回调
        onLoadError: function() {}        // 加载失败回调
    };

    IMStatCards.prototype.init = function() {
        var that = this;
        var $el = $(this.options.el);

        if (!$el.length) {
            console.error('IMStatCards: 找不到统计卡片容器');
            return;
        }

        this.$element = $el;
        this.renderCards();

        if (this.options.autoLoad) {
            this.load();
        }

        this.bindEvents();
    };

    /**
     * 渲染卡片
     */
    IMStatCards.prototype.renderCards = function() {
        var that = this;

        // 如果已有卡片，不重新渲染
        if (this.$element.find('.statistics-card').length > 0) {
            return;
        }

        var container = this.$element.find('.row');
        var colWidth = Math.floor(12 / this.options.cards.length);

        this.options.cards.forEach(function(card) {
            var html = '<div class="col-sm-' + colWidth + '">';
            html += '<div class="statistics-card ' + (card.gradient || 'stat-gradient-blue') + '" data-field="' + card.field + '">';
            html += '<div class="stat-number" id="' + card.field + '">0</div>';
            html += '<div class="stat-label">' + card.label + '</div>';
            if (card.icon) {
                html += '<i class="fa ' + card.icon + ' stat-icon"></i>';
            }
            html += '</div>';
            html += '</div>';
            container.append(html);
        });
    };

    /**
     * 绑定事件
     */
    IMStatCards.prototype.bindEvents = function() {
        var that = this;

        // 卡片点击事件
        this.$element.on('click', '.statistics-card', function() {
            var field = $(this).data('field');
            that.options.onCardClick(field);
        });
    };

    /**
     * 加载数据
     */
    IMStatCards.prototype.load = function(params) {
        var that = this;

        if (!this.options.url) {
            console.warn('IMStatCards: 未设置数据接口');
            return;
        }

        $.ajax({
            url: this.options.url,
            method: this.options.method,
            data: params || {},
            dataType: 'json',
            success: function(result) {
                if (result.code === 0 || result.code === 200) {
                    that.updateData(result.data);
                    that.options.onLoadSuccess(result.data);
                } else {
                    that.options.onLoadError(result.msg || '加载统计数据失败');
                }
            },
            error: function() {
                that.options.onLoadError('网络请求失败');
            }
        });
    };

    /**
     * 更新数据
     */
    IMStatCards.prototype.updateData = function(data) {
        var that = this;

        Object.keys(data).forEach(function(key) {
            var value = data[key];
            that.$element.find('#' + key).text(value);
        });
    };

    /**
     * 设置数据
     */
    IMStatCards.prototype.setData = function(data) {
        this.updateData(data);
    };

    /**
     * 刷新
     */
    IMStatCards.prototype.refresh = function() {
        this.load();
    };

    /**
     * 获取数据
     */
    IMStatCards.prototype.getData = function() {
        var data = {};
        this.$element.find('.stat-number').each(function() {
            var field = $(this).attr('id');
            var value = $(this).text();
            data[field] = value;
        });
        return data;
    };

    /**
     * jQuery 插件注册
     */
    $.fn.imStatCards = function(options) {
        return this.each(function() {
            var $this = $(this);
            var data = $this.data('im.statCards');

            if (!data) {
                options = $.extend({}, options);
                options.el = this;
                $this.data('im.statCards', (data = new IMStatCards(options)));
            }

            if (typeof options === 'string') {
                data[options]();
            }
        });
    };

    $.fn.imStatCards.Constructor = IMStatCards;

})(jQuery);
