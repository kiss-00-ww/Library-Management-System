<template>
  <div class="dashboard-container">
    <el-row :gutter="16" class="stat-row" v-loading="statsLoading">
      <el-col :xs="12" :sm="8" :md="4" v-for="(card, index) in statCards" :key="index">
        <el-card class="stat-card" :style="{ '--card-hue': card.hue }" shadow="hover">
          <div class="stat-inner">
            <div class="stat-icon-wrap">
              <el-icon :size="22"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-body">
              <span class="stat-value">{{ formatNumber(card.displayValue) }}</span>
              <span class="stat-label">{{ card.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12" :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span class="chart-title">📊 分类统计</span></template>
          <div ref="categoryChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12" :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span class="chart-title">🔥 热门图书 Top10</span></template>
          <div ref="popularChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header-flex">
              <span class="chart-title">📈 借阅趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="fetchAndDrawTrend">
                <el-radio-button :value="7">近7天</el-radio-button>
                <el-radio-button :value="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-box chart-box-tall"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card">
          <template #header><span class="chart-title">📁 报表导出</span></template>
          <div class="export-bar">
            <el-date-picker
              v-model="exportDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DD"
              size="small"
            />
            <el-button type="primary" size="small" @click="handleExport('borrow')">借阅记录</el-button>
            <el-button type="success" size="small" @click="handleExport('popular')">热门图书</el-button>
            <el-button type="warning" size="small" @click="handleExport('overdue')">逾期记录</el-button>
            <el-button type="info" size="small" @click="handleExport('circulation')">流通率</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document, Reading, Collection, WarningFilled, UserFilled
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getStatistics, getPopularStats, getBorrowTrend, exportReport } from '@/api/statistics'

const stats = ref({})
const popularBooks = ref([])
const popularBookBorrowCounts = ref({})
const trendDays = ref(30)
const exportDateRange = ref(null)
const statsLoading = ref(false)

const categoryChartRef = ref(null)
const popularChartRef = ref(null)
const trendChartRef = ref(null)

let categoryChart = null
let popularChart = null
let trendChart = null

const statCards = reactive([
  { label: '总藏书量', icon: Document, hue: 210, displayValue: 0, realValue: 0, key: 'totalBooks' },
  { label: '可借数量', icon: Reading, hue: 130, displayValue: 0, realValue: 0, key: 'availableBooks' },
  { label: '借出数量', icon: Collection, hue: 40, displayValue: 0, realValue: 0, key: 'borrowedBooks' },
  { label: '逾期数量', icon: WarningFilled, hue: 0, displayValue: 0, realValue: 0, key: 'overdueCount' },
  { label: '注册用户', icon: UserFilled, hue: 280, displayValue: 0, realValue: 0, key: 'totalUsers' }
])

function formatNumber(val) {
  if (val == null) return '0'
  return Number(val).toLocaleString('zh-CN')
}

function animateNumbers() {
  const duration = 800
  const fps = 30
  const frames = Math.ceil(duration / (1000 / fps))
  statCards.forEach((card) => {
    const start = 0
    const end = card.realValue
    let frame = 0
    const timer = setInterval(() => {
      frame++
      card.displayValue = Math.round(start + (end - start) * (frame / frames))
      if (frame >= frames) {
        card.displayValue = end
        clearInterval(timer)
      }
    }, 1000 / fps)
  })
}

function createCategoryChart() {
  if (!categoryChartRef.value) return
  if (categoryChart) categoryChart.dispose()
  categoryChart = echarts.init(categoryChartRef.value)

  const data = stats.value.categoryStats || {}
  const keys = Object.keys(data)
  if (keys.length === 0) {
    categoryChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } }
    })
    return
  }
  const values = keys.map(k => data[k])
  const total = values.reduce((a, b) => a + b, 0)

  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: (p) => `${p.name}: ${p.value} 册 (${p.percent}%)`
    },
    legend: { orient: 'vertical', right: 10, top: 'center', textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['50%', '75%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: keys.map((k, i) => ({ name: `${k} (${(values[i] * 100 / total).toFixed(1)}%)`, value: values[i] }))
    }],
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#9b59b6', '#1abc9c', '#e67e22', '#2ecc71']
  })
}

function createPopularChart() {
  if (!popularChartRef.value) return
  if (popularChart) popularChart.dispose()
  popularChart = echarts.init(popularChartRef.value)

  if (!popularBooks.value.length) {
    popularChart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } }
    })
    return
  }

  const names = popularBooks.value.map(b => b.title.length > 8 ? b.title.slice(0, 8) + '…' : b.title).reverse()
  const values = popularBooks.value.map((b) => popularBookBorrowCounts.value[b.id] || 0).reverse()

  popularChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (p) => {
        const idx = p[0].dataIndex
        const fullName = popularBooks.value[popularBooks.value.length - 1 - idx].title
        return `${fullName}: ${p[0].value} 次`
      }
    },
    grid: { left: 100, right: 30, top: 10, bottom: 20 },
    xAxis: { type: 'value', axisLabel: { fontSize: 11 } },
    yAxis: { type: 'category', data: names, axisLabel: { fontSize: 11 }, inverse: true },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#66b1ff' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      barMaxWidth: 24
    }]
  })
}

function createTrendChart(data) {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)

  const dates = Object.keys(data)
  const values = Object.values(data)

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (p) => `${p[0].axisValue}<br/>借阅量: ${p[0].value} 本`
    },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: dates.map(d => d.slice(5)),
      axisLabel: { fontSize: 11, rotate: trendDays.value > 14 ? 45 : 0 },
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { fontSize: 11 }
    },
    series: [{
      type: 'line',
      data: values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#409eff', width: 2 },
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.25)' },
          { offset: 1, color: 'rgba(64,158,255,0.02)' }
        ])
      }
    }]
  })
}

async function fetchStats() {
  statsLoading.value = true
  try {
    const [statsRes, popularRes, trendRes] = await Promise.all([
      getStatistics(),
      getPopularStats(),
      getBorrowTrend(trendDays.value)
    ])
    stats.value = statsRes.data
    popularBooks.value = popularRes.data.popularBooks || []
    popularBookBorrowCounts.value = popularRes.data.popularBookBorrowCounts || {}

    statCards.forEach(card => {
      card.realValue = Number(stats.value[card.key]) || 0
    })
    animateNumbers()

    await nextTick()
    createCategoryChart()
    createPopularChart()
    createTrendChart(trendRes.data)
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error)
  } finally {
    statsLoading.value = false
  }
}

async function fetchAndDrawTrend() {
  try {
    const res = await getBorrowTrend(trendDays.value)
    await nextTick()
    createTrendChart(res.data)
  } catch (e) {
    console.error('Failed to fetch trend:', e)
  }
}

const handleExport = async (type) => {
  try {
    const startDate = exportDateRange.value?.[0]
    const endDate = exportDateRange.value?.[1]
    const res = await exportReport(type, startDate, endDate)
    const blob = res.data
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const typeNames = { borrow: '借阅记录', popular: '热门图书排行', overdue: '逾期记录', circulation: '图书流通率' }
    link.download = `${typeNames[type]}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('Export failed:', error)
    ElMessage.error('导出失败')
  }
}

function handleResize() {
  categoryChart?.resize()
  popularChart?.resize()
  trendChart?.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart?.dispose()
  popularChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.dashboard-container { padding: 4px; }

.stat-row { margin-bottom: 16px; }

.stat-card {
  --card-hue: 210;
  border: none;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.25s, box-shadow 0.25s;
  cursor: default;
}
.stat-card:hover { transform: translateY(-3px); }

.stat-inner { display: flex; align-items: center; gap: 14px; padding: 4px 8px; }

.stat-icon-wrap {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  background: hsl(var(--card-hue), 70%, 60%);
  color: #fff; flex-shrink: 0;
}

.stat-body { display: flex; flex-direction: column; min-width: 0; }

.stat-value { font-size: 26px; font-weight: 700; color: #303133; line-height: 1.2; }

.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }

.chart-row { margin-bottom: 16px; }

.chart-card { border-radius: 12px; border: none; }

.chart-title { font-size: 15px; font-weight: 600; color: #303133; }

.chart-header-flex { display: flex; justify-content: space-between; align-items: center; }

.chart-box { width: 100%; height: 320px; }
.chart-box-tall { height: 300px; }

.export-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

@media (max-width: 768px) {
  .chart-box { height: 260px; }
}
</style>
