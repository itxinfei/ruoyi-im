<template>
  <div class="chart-container">
    <canvas ref="chart"></canvas>
  </div>
</template>

<script>
import Chart from 'chart.js'
import { debounce } from 'lodash'

export default {
  name: 'LineChart',
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
        type: 'line',
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
                    return value + '%'
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
                label += tooltipItem.yLabel + '%'
                return label
              },
            },
          },
          hover: {
            mode: 'nearest',
            intersect: true,
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
          chartData.datasets = datasets
        }
        this.chart.update()
      }
    },
  },
}
</script>

<style lang="scss" scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
}
</style>
