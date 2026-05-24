<template>
  <div class="my-reservations">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的预约</span>
        </div>
      </template>

      <div class="search-bar">
        <el-select v-model="filterStatus" placeholder="选择预约状态" style="width: 150px" clearable @change="fetchReservations">
          <el-option label="等待中" value="WAITING" />
          <el-option label="已通知" value="NOTIFIED" />
          <el-option label="已完成" value="FULFILLED" />
          <el-option label="已取消" value="CANCELLED" />
          <el-option label="已过期" value="EXPIRED" />
        </el-select>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <template v-if="loading">
        <el-skeleton :rows="8" animated />
      </template>
      <template v-else>
      <el-table :data="reservations" style="width: 100%; margin-top: 20px">
        <el-table-column label="图书" min-width="180">
          <template #default="{ row }">
            {{ row.book ? row.book.title : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="作者" width="120">
          <template #default="{ row }">
            {{ row.book ? row.book.author : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="预约时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.reserveTime) }}
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="170">
          <template #default="{ row }">
            {{ row.expireTime ? formatTime(row.expireTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button v-if="row.status === 'NOTIFIED'" type="primary" size="small" link @click="handleBorrow(row)">借阅</el-button>
            <el-button v-if="row.status === 'WAITING'" type="danger" size="small" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无预约记录，去预约一本心仪的书吧～" :image-size="80" />
        </template>
      </el-table>
      </template>

      <el-pagination
        style="margin-top: 20px; text-align: right"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchReservations"
        @current-change="fetchReservations"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyReservations, borrowFromReservation, cancelReservation } from '@/api/book'

const reservations = ref([])
const loading = ref(false)
const filterStatus = ref('')

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const statusLabel = (status) => {
  const map = { WAITING: '等待中', NOTIFIED: '已通知', FULFILLED: '已完成', CANCELLED: '已取消', EXPIRED: '已过期' }
  return map[status] || status
}

const statusTagType = (status) => {
  const map = { WAITING: 'info', NOTIFIED: 'warning', FULFILLED: 'success', CANCELLED: 'danger', EXPIRED: 'danger' }
  return map[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const fetchReservations = async () => {
  loading.value = true
  try {
    const res = await getMyReservations({
      status: filterStatus.value || undefined,
      page: pagination.value.page,
      size: pagination.value.size
    })
    reservations.value = res.data.records
    pagination.value.total = res.data.total
  } catch (e) {
    console.error('Failed to fetch reservations:', e)
  } finally {
    loading.value = false
  }
}

const handleBorrow = async (row) => {
  try {
    await borrowFromReservation(row.id)
    ElMessage.success('借阅成功')
    fetchReservations()
  } catch (e) {
    console.error('Borrow from reservation failed:', e)
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '提示', { type: 'warning' })
    await cancelReservation(row.id)
    ElMessage.success('已取消预约')
    fetchReservations()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Cancel reservation failed:', e)
    }
  }
}

const handleReset = () => {
  filterStatus.value = ''
  pagination.value.page = 1
  fetchReservations()
}

onMounted(() => {
  fetchReservations()
})
</script>

<style scoped>
.my-reservations {
  padding: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 18px;
}
</style>
