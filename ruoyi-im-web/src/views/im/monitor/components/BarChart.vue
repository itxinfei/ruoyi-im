<template>
  <div class="chart-container">
    <canvas ref="chart"></canvas>
  </div>
</template>

<script>
import Chart from 'chart.js'
import { debounce } from 'lodash'

export default {
  name: 'BarChart',
  props: {
    chartData: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      chart: null,
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler(val) {
        this.setOptions(val)
      },
    },
  },
  mounted() {
    this.initChart()
    this.__resizeHandler = debounce(() => {
      if (this.chart) {
        this.chart.resize()
      }
    }, 100)
    window.addEventListener('resize', this.__resizeHandler)
  },
  beforeUnmount() {
    if (!this.chart) {
      return
    }
    window.removeEventListener('resize', this.__resizeHandler)
    this.chart.destroy()
    this.chart = null
  },
  methods: {
    initChart() {
      const chartCanvas = this.$refs.chart.getContext('2d')

      this.chart = new Chart(chartCanvas, {
        type: 'bar',
        data: this.chartData,
        options: {
          responsive: true,
          maintainAspectRatio: false,
          legend: {
            display: true,
            position: 'top',
          },
          scales: {
            xAxes: [
              {
                gridLines: {
                  display: false,
                },
                barPercentage: 0.6,
                categoryPercentage: 0.8,
              },
            ],
            yAxes: [
              {
                gridLines: {
                  display: true,
                },
                ticks: {
                  beginAtZero: true,
                  callback: function (value) {
                    if (value >= 1000) {
                      return (value / 1000).toFixed(1) + 'k'
                    }
                    return value
                  },
                },
              },
            ],
          },
          tooltips: {
            mode: 'index',
            intersect: false,
            callbacks: {
              label: function (tooltipItem, data) {
                let label = data.datasets[tooltipItem.datasetIndex].label || ''
                if (label) {
                  label += ': '
                }
                const value = tooltipItem.yLabel
                if (value >= 1000) {
                  label += (value / 1000).toFixed(1) + 'k'
                } else {
                  label += value
                }
                return label
              },
            },
          },
          hover: {
            mode: 'nearest',
            intersect: true,
          },
          animation: {
            duration: 1000,
            easing: 'easeInOutQuart',
          },
        },
      })
    },
    setOptions({ labels, datasets } = {}) {
      if (this.chart) {
        const chartData = this.chart.data
        if (labels) {
          chartData.labels = labels
        }
        if (datasets) {
          // 为每个数据集设置不同的颜色
          datasets.forEach((dataset, index) => {
            dataset.backgroundColor = this.getColor(index)
            dataset.borderColor = dataset.backgroundColor
            dataset.borderWidth = 1
            dataset.hoverBackgroundColor = this.getHoverColor(dataset.backgroundColor)
          })
          chartData.datasets = datasets
        }
        this.chart.update()
      }
    },
    // 获取图表颜色
    getColor(index) {
      const colors = [
        'rgba(64, 158, 255, 0.8)', // 蓝色
        'rgba(103, 194, 58, 0.8)', // 绿色
        'rgba(230, 162, 60, 0.8)', // 黄色
        'rgba(245, 108, 108, 0.8)', // 红色
        'rgba(144, 147, 153, 0.8)', // 灰色
      ]
      return colors[index % colors.length]
    },
    // 获取悬停时的颜色
    getHoverColor(color) {
      return color.replace('0.8', '1')
    },
  },
}
</script>

<style lang="scss" scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;

  // 添加图表加载动画
  @keyframes chartFadeIn {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  canvas {
    animation: chartFadeIn 0.6s ease-out;
  }
}
</style>
