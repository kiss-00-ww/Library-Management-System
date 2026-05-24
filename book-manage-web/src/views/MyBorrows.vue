<template>
  <div class="my-borrows-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的借阅</span>
        </div>
      </template>

      <!-- 状态筛选标签页 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="ALL">
          <template #label>
            <span>全部 <el-badge :value="statusCounts.ALL || 0" class="tab-badge" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="借阅中" name="BORROWED">
          <template #label>
            <span>借阅中 <el-badge :value="statusCounts.BORROWED || 0" class="tab-badge" type="primary" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已续借" name="RENEWED">
          <template #label>
            <span>已续借 <el-badge :value="statusCounts.RENEWED || 0" class="tab-badge" type="warning" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已逾期" name="OVERDUE">
          <template #label>
            <span>已逾期 <el-badge :value="statusCounts.OVERDUE || 0" class="tab-badge" type="danger" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已归还" name="RETURNED">
          <template #label>
            <span>已归还 <el-badge :value="statusCounts.RETURNED || 0" class="tab-badge" type="success" /></span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <template v-if="loading">
        <el-skeleton :rows="8" animated />
      </template>
      <template v-else>
      <el-table :data="borrows" style="width: 100%" :row-class-name="rowClassName">
        <el-table-column prop="book.title" label="书名" width="200" />
        <el-table-column prop="book.author" label="作者" width="150" />
        <el-table-column prop="borrowDate" label="借阅日期" width="170">
          <template #default="{ row }">
            {{ formatDate(row.borrowDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="应还日期" width="170">
          <template #default="{ row }">
            {{ formatDate(row.dueDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="returnDate" label="归还日期" width="170">
          <template #default="{ row }">
            {{ row.returnDate ? formatDate(row.returnDate) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fineAmount" label="罚款" width="100">
          <template #default="{ row }">
            <span :class="{ 'fine-amount': row.fineAmount > 0 }">¥{{ row.fineAmount ? row.fineAmount.toFixed(2) : '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button v-if="row.status === 'BORROWED' || row.status === 'RENEWED'" type="warning" size="small" link @click="handleRenew(row)" :disabled="row.renewCount >= (row.maxRenewCount || 1)">续借</el-button>
            <el-button v-if="row.status !== 'RETURNED'" type="success" size="small" link @click="handleReturn(row)">归还</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无借阅记录，去借本书吧～" :image-size="80" />
        </template>
      </el-table>
      </template>

      <el-pagination
        style="margin-top: 20px; text-align: right"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchBorrows"
        @current-change="fetchBorrows"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyBorrows, getMyBorrowCounts, returnBook, renewBook } from '@/api/book'

const borrows = ref([])
const loading = ref(false)
const activeTab = ref('ALL')
const allBorrows = ref([])

const statusCounts = ref({
  ALL: 0,
  BORROWED: 0,
  RENEWED: 0,
  OVERDUE: 0,
  RETURNED: 0
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const fetchBorrows = async () => {
  loading.value = true
  try {
    const statusParam = activeTab.value === 'ALL' ? undefined : activeTab.value
    const res = await getMyBorrows({
      status: statusParam,
      page: pagination.value.page,
      size: pagination.value.size
    })
    borrows.value = res.data.records
    pagination.value.total = res.data.total
  } catch (error) {
    console.error('Failed to fetch borrows:', error)
  } finally {
    loading.value = false
  }
}

const fetchAllCounts = async () => {
  try {
    const res = await getMyBorrowCounts()
    statusCounts.value = res.data || {}
  } catch (e) {
    // ignore
  }
}

const handleTabChange = () => {
  pagination.value.page = 1
  fetchBorrows()
}

const handleReturn = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要归还《${row.book?.title || '未知图书'}》吗？`,
      '确认归还',
      { type: 'warning' }
    )
    await returnBook(row.id)
    ElMessage.success('归还成功')
    fetchBorrows()
    fetchAllCounts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Return failed:', error)
    }
  }
}

const handleRenew = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要续借《${row.book?.title || '未知图书'}》吗？`,
      '确认续借',
      { type: 'warning' }
    )
    await renewBook(row.id)
    ElMessage.success('续借成功')
    fetchBorrows()
    fetchAllCounts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Renew failed:', error)
    }
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const rowClassName = ({ row }) => {
  if (row.status === 'OVERDUE') return 'overdue-row'
  return ''
}

const getStatusType = (status) => {
  const types = {
    BORROWED: 'primary',
    RETURNED: 'success',
    RENEWED: 'warning',
    OVERDUE: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    BORROWED: '借出中',
    RETURNED: '已归还',
    RENEWED: '已续借',
    OVERDUE: '已逾期'
  }
  return texts[status] || status
}

onMounted(() => {
  fetchBorrows()
  fetchAllCounts()
})
</script>

<style scoped>
.my-borrows-container {
  padding: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 18px;
}

.fine-amount {
  color: #f56c6c;
  font-weight: bold;
}

.tab-badge {
  margin-left: 4px;
}

.tab-badge :deep(.el-badge__content) {
  font-size: 11px;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
}
</style>
