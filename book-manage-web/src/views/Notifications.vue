<template>
  <div class="notification-center">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息中心</span>
          <el-button type="primary" size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">
            全部标为已读
          </el-button>
        </div>
      </template>

      <template v-if="loading">
        <el-skeleton :rows="8" animated />
      </template>
      <template v-else>
      <el-table :data="notifications" style="width: 100%">
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-badge is-dot :hidden="row.isRead === 1" class="read-dot">
              <span :class="{ 'unread-text': row.isRead === 0 }">{{ row.isRead === 0 ? '未读' : '已读' }}</span>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.isRead === 0" type="primary" size="small" link @click="handleMarkRead(row)">标为已读</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无消息，一切安好～" :image-size="80" />
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
        @size-change="fetchNotifications"
        @current-change="fetchNotifications"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotifications, markAsRead, markAllAsRead, getUnreadCount } from '@/api/notification'

const notifications = ref([])
const loading = ref(false)
const unreadCount = ref(0)

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await getNotifications({
      page: pagination.value.page,
      size: pagination.value.size
    })
    notifications.value = res.data.records
    pagination.value.total = res.data.total
  } catch (e) {
    console.error('Failed to fetch notifications:', e)
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data
  } catch (e) {
    // ignore
  }
}

const handleMarkRead = async (row) => {
  try {
    await markAsRead(row.id)
    row.isRead = 1
    ElMessage.success('已标为已读')
    fetchUnreadCount()
  } catch (e) {
    console.error('Mark read failed:', e)
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('已全部标为已读')
    fetchNotifications()
    fetchUnreadCount()
  } catch (e) {
    console.error('Mark all read failed:', e)
  }
}

onMounted(() => {
  fetchNotifications()
  fetchUnreadCount()
})
</script>

<style scoped>
.notification-center {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 18px;
}

.unread-text {
  font-weight: bold;
  color: #409eff;
}

.read-dot :deep(.el-badge__content.is-dot) {
  right: calc(-6px + var(--el-badge-size) / 2);
}
</style>
