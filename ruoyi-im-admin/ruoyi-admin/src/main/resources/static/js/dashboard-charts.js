/**
 * Dashboard图表jQuery插件
 * 基于ECharts 4.2.1封装，简化Dashboard图表创建和管理
 *
 * 使用示例：
 * $('#chart').dashboardChart('line', {
 *     url: '/im/dashboard/trend?type=user&days=7',
 *     title: '用户增长趋势',
 *     height: 300
 * });
 *
 * @author ruoyi
 * @version 1.0
 */

(function($) {
    'use strict';

    // 图表实例存储
    var chartInstances = {};

    /**
     * DashboardChart核心类
     */
    function DashboardChart(element, options) {
        this.$element = $(element);
        this.settings = $.extend({}, DashboardChart.DEFAULTS, options);
        this.chart = null;
        this.timer = null;
        this.init();
    }

    /**
     * 默认配置
     */
    DashboardChart.DEFAULTS = {
        url: '',                  // 数据URL
        type: 'line',            // 图表类型：line/bar/pie/horizontalBar/gauge
        title: '',               // 图表标题
        subtitle: '',            // 副标题
        height: 300,             // 图表高度
        width: null,             // 图表宽度（null=自适应）
        refreshInterval: null,   // 自动刷新间隔（毫秒）
        xAxisKey: 'date_label',  // X轴数据键名
        yAxisKey: 'count',       // Y轴数据键名
        nameKey: 'type_label',   // 名称键名（饼图使用）
        showDataZoom: false,     // 是否显示数据缩放
        showLegend: true,        // 是否显示图例
        showToolbox: false,      // 是否显示工具栏
        onClick: null,           // 点击事件回调
        onDataLoad: null,        // 数据加载完成回调
        onError: null,           // 错误回调
        formatter: null          // 数据格式化函数
    };

    /**
     * 初始化图表
     */
    DashboardChart.prototype.init = function() {
        var self = this;

        // 设置容器尺寸
        if (this.settings.height) {
            this.$element.height(this.settings.height);
        }
        if (this.settings.width) {
            this.$element.width(this.settings.width);
        }

        // 初始化ECharts实例
        this.chart = echarts.init(this.$element[0]);

        // 绑定点击事件
        if (this.settings.onClick) {
            this.chart.on('click', function(params) {
                self.settings.onClick.call(self, params);
            });
        }

        // 响应式调整
        window.addEventListener('resize', function() {
            self.chart.resize();
        });

        // 加载数据
        if (this.settings.url) {
            this.loadData();
        }
    };

    /**
     * 加载数据
     */
    DashboardChart.prototype.loadData = function(params) {
        var self = this;
        var url = this.settings.url;

        // 添加额外参数
        if (params) {
            url += (url.indexOf('?') > -1 ? '&' : '?') + $.param(params);
        }

        $.ajax({
            url: url,
            type: 'GET',
            dataType: 'json',
            success: function(response) {
                if (response.code === 0 || response.code === 200) {
                    var data = response.data || response;
                    self.render(data);

                    // 触发回调
                    if (self.settings.onDataLoad) {
                        self.settings.onDataLoad.call(self, data);
                    }
                } else {
                    self.handleError(response.msg || '数据加载失败');
                }
            },
            error: function(xhr, status, error) {
                self.handleError('请求失败: ' + error);
            }
        });
    };

    /**
     * 渲染图表
     */
    DashboardChart.prototype.render = function(data) {
        var option = this.getOption(data);

        // 应用数据格式化函数
        if (this.settings.formatter) {
            option = this.settings.formatter.call(this, option, data);
        }

        this.chart.setOption(option, true);
    };

    /**
     * 获取图表配置
     */
    DashboardChart.prototype.getOption = function(data) {
        var baseOption = {
            title: {
                text: this.settings.title,
                subtext: this.settings.subtitle,
                left: 'center'
            },
            tooltip: {
                trigger: this.getTooltipTrigger(),
                axisPointer: {
                    type: 'shadow'
                }
            },
            legend: {
                show: this.settings.showLegend,
                top: 30
            },
            toolbox: this.getToolboxOption(),
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            }
        };

        // 根据图表类型合并特定配置
        var typeOption = this.getTypeOption(data);
        return $.extend(true, baseOption, typeOption);
    };

    /**
     * 获取tooltip触发器类型
     */
    DashboardChart.prototype.getTooltipTrigger = function() {
        if (this.settings.type === 'pie' || this.settings.type === 'gauge') {
            return 'item';
        }
        return 'axis';
    };

    /**
     * 获取工具栏配置
     */
    DashboardChart.prototype.getToolboxOption = function() {
        if (!this.settings.showToolbox) {
            return { show: false };
        }

        return {
            show: true,
            feature: {
                dataView: { show: true, readOnly: false },
                magicType: { show: true, type: ['line', 'bar'] },
                restore: { show: true },
                saveAsImage: { show: true }
            },
            right: 20
        };
    };

    /**
     * 根据类型获取配置
     */
    DashboardChart.prototype.getTypeOption = function(data) {
        switch (this.settings.type) {
            case 'line':
                return this.getLineOption(data);
            case 'bar':
                return this.getBarOption(data);
            case 'pie':
                return this.getPieOption(data);
            case 'horizontalBar':
                return this.getHorizontalBarOption(data);
            case 'gauge':
                return this.getGaugeOption(data);
            default:
                return this.getLineOption(data);
        }
    };

    /**
     * 折线图配置
     */
    DashboardChart.prototype.getLineOption = function(data) {
        var xAxisData = [];
        var seriesData = [];

        if ($.isArray(data)) {
            xAxisData = $.map(data, function(item) {
                return item[this.settings.xAxisKey];
            }.bind(this));
            seriesData = $.map(data, function(item) {
                return item[this.settings.yAxisKey];
            }.bind(this));
        }

        return {
            xAxis: {
                type: 'category',
                data: xAxisData,
                boundaryGap: false
            },
            yAxis: {
                type: 'value'
            },
            series: [{
                name: this.settings.title,
                type: 'line',
                data: seriesData,
                smooth: true,
                areaStyle: {
                    opacity: 0.3
                },
                lineStyle: {
                    width: 2
                },
                itemStyle: {
                    color: '#409EFF'
                }
            }],
            dataZoom: this.settings.showDataZoom ? [{
                type: 'inside',
                start: 0,
                end: 100
            }, {
                start: 0,
                end: 100
            }] : []
        };
    };

    /**
     * 柱状图配置
     */
    DashboardChart.prototype.getBarOption = function(data) {
        var xAxisData = [];
        var seriesData = [];

        if ($.isArray(data)) {
            xAxisData = $.map(data, function(item) {
                return item[this.settings.xAxisKey];
            }.bind(this));
            seriesData = $.map(data, function(item) {
                return item[this.settings.yAxisKey];
            }.bind(this));
        }

        return {
            xAxis: {
                type: 'category',
                data: xAxisData
            },
            yAxis: {
                type: 'value'
            },
            series: [{
                name: this.settings.title,
                type: 'bar',
                data: seriesData,
                itemStyle: {
                    color: '#67C23A'
                }
            }],
            dataZoom: this.settings.showDataZoom ? [{
                type: 'inside',
                start: 0,
                end: 100
            }, {
                start: 0,
                end: 100
            }] : []
        };
    };

    /**
     * 饼图配置
     */
    DashboardChart.prototype.getPieOption = function(data) {
        var pieData = [];

        if ($.isArray(data)) {
            pieData = $.map(data, function(item) {
                return {
                    name: item[this.settings.nameKey],
                    value: item[this.settings.yAxisKey]
                };
            }.bind(this));
        }

        return {
            series: [{
                name: this.settings.title,
                type: 'pie',
                radius: '70%',
                center: ['50%', '55%'],
                data: pieData,
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                },
                label: {
                    formatter: '{b}: {d}%\n({c})'
                }
            }]
        };
    };

    /**
     * 横向柱状图配置
     */
    DashboardChart.prototype.getHorizontalBarOption = function(data) {
        var yAxisData = [];
        var seriesData = [];

        if ($.isArray(data)) {
            // 排序
            data.sort(function(a, b) {
                return b[this.settings.yAxisKey] - a[this.settings.yAxisKey];
            }.bind(this));

            yAxisData = $.map(data, function(item) {
                return item[this.settings.xAxisKey] || item.username || item.nickname || item.group_name;
            }.bind(this));
            seriesData = $.map(data, function(item) {
                return item[this.settings.yAxisKey];
            }.bind(this));
        }

        return {
            xAxis: {
                type: 'value'
            },
            yAxis: {
                type: 'category',
                data: yAxisData
            },
            series: [{
                name: this.settings.title,
                type: 'bar',
                data: seriesData,
                itemStyle: {
                    color: '#E6A23C'
                }
            }]
        };
    };

    /**
     * 仪表盘配置
     */
    DashboardChart.prototype.getGaugeOption = function(data) {
        var value = 0;
        var max = 100;

        if ($.isNumeric(data)) {
            value = data;
        } else if ($.isPlainObject(data)) {
            value = data.value || data.count || 0;
            max = data.max || 100;
        }

        return {
            series: [{
                name: this.settings.title,
                type: 'gauge',
                min: 0,
                max: max,
                detail: {
                    formatter: '{value}'
                },
                data: [{
                    value: value,
                    name: this.settings.title
                }]
            }]
        };
    };

    /**
     * 处理错误
     */
    DashboardChart.prototype.handleError = function(message) {
        console.error('Dashboard图表错误:', message);

        this.chart.setOption({
            title: {
                text: '加载失败',
                subtext: message,
                left: 'center',
                textStyle: {
                    color: '#F56C6C'
                }
            }
        });

        if (this.settings.onError) {
            this.settings.onError.call(this, message);
        }
    };

    /**
     * 刷新数据
     */
    DashboardChart.prototype.refresh = function(params) {
        this.loadData(params);
    };

    /**
     * 启动自动刷新
     */
    DashboardChart.prototype.startAutoRefresh = function() {
        var self = this;
        if (this.settings.refreshInterval && !this.timer) {
            this.timer = setInterval(function() {
                self.loadData();
            }, this.settings.refreshInterval);
        }
    };

    /**
     * 停止自动刷新
     */
    DashboardChart.prototype.stopAutoRefresh = function() {
        if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
        }
    };

    /**
     * 销毁图表
     */
    DashboardChart.prototype.destroy = function() {
        this.stopAutoRefresh();
        if (this.chart) {
            this.chart.dispose();
            this.chart = null;
        }
    };

    /**
     * 导出为图片
     */
    DashboardChart.prototype.exportImage = function(filename) {
        var url = this.chart.getDataURL({
            type: 'png',
            pixelRatio: 2,
            backgroundColor: '#fff'
        });

        var $link = $('<a>')
            .attr('href', url)
            .attr('download', filename || 'chart.png')
            .appendTo('body');

        $link[0].click();
        $link.remove();
    };

    /**
     * 导出数据为CSV
     */
    DashboardChart.prototype.exportCSV = function(filename) {
        // 从图表中提取数据
        var option = this.chart.getOption();
        var csv = '';

        // 添加BOM以支持中文
        csv = '\uFEFF';

        if (option.xAxis && option.xAxis[0] && option.xAxis[0].data) {
            // X轴 + Y轴数据
            var xAxisData = option.xAxis[0].data;
            var seriesData = option.series[0].data;

            csv += '类别,数值\n';
            for (var i = 0; i < xAxisData.length; i++) {
                csv += xAxisData[i] + ',' + seriesData[i] + '\n';
            }
        } else if (option.series && option.series[0] && option.series[0].data) {
            // 饼图数据
            var pieData = option.series[0].data;
            csv += '名称,数值\n';
            for (var j = 0; j < pieData.length; j++) {
                csv += pieData[j].name + ',' + pieData[j].value + '\n';
            }
        }

        var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        var link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename || 'data.csv';
        link.click();
    };

    // ==================== jQuery插件定义 ====================

    $.fn.dashboardChart = function(options) {
        var args = Array.prototype.slice.call(arguments, 1);

        return this.each(function() {
            var $this = $(this);
            var instance = $this.data('dashboard-chart');

            if (!instance) {
                // 创建新实例
                if (typeof options === 'string') {
                    options = { type: options };
                }
                instance = new DashboardChart(this, options);
                $this.data('dashboard-chart', instance);
                chartInstances[$this.attr('id')] = instance;
            }

            // 调用方法
            if (typeof options === 'string') {
                if (options === 'destroy') {
                    $this.removeData('dashboard-chart');
                    delete chartInstances[$this.attr('id')];
                }
                if ($.isFunction(instance[options])) {
                    instance[options].apply(instance, args);
                }
            }
        });
    };

    /**
     * 批量刷新所有图表
     */
    $.fn.dashboardChart.refreshAll = function() {
        $.each(chartInstances, function(id, instance) {
            instance.refresh();
        });
    };

    /**
     * 批量销毁所有图表
     */
    $.fn.dashboardChart.destroyAll = function() {
        $.each(chartInstances, function(id, instance) {
            instance.destroy();
        });
        chartInstances = {};
    };

    /**
     * 获取图表实例
     */
    $.fn.dashboardChart.getInstance = function(id) {
        return chartInstances[id];
    };

    // ==================== 辅助函数 ====================

    /**
     * 转换数据为ECharts格式
     */
    $.fn.dashboardChart.transformData = function(data, config) {
        config = $.extend({
            xAxisKey: 'name',
            yAxisKey: 'value'
        }, config);

        var xAxisData = [];
        var seriesData = [];

        if ($.isArray(data)) {
            $.each(data, function(i, item) {
                xAxisData.push(item[config.xAxisKey]);
                seriesData.push(item[config.yAxisKey]);
            });
        }

        return {
            xAxis: xAxisData,
            series: seriesData
        };
    };

    /**
     * 格式化数字（带千分位）
     */
    $.fn.dashboardChart.formatNumber = function(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    };

    /**
     * 格式化文件大小
     */
    $.fn.dashboardChart.formatFileSize = function(bytes) {
        if (bytes === 0) return '0 B';
        var k = 1024;
        var sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        var i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

})(jQuery);
