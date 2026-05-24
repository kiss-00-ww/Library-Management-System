<template>
  <div class="borrows-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>借阅管理</span>
        </div>
      </template>

      <div class="search-bar">
        <el-select v-model="filterForm.status" placeholder="选择借阅状态" style="width: 150px" clearable>
          <el-option label="借出中" value="BORROWED" />
          <el-option label="已归还" value="RETURNED" />
          <el-option label="已续借" value="RENEWED" />
          <el-option label="已逾期" value="OVERDUE" />
        </el-select>
        <el-button type="primary" @click="handleFilter">筛选</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <template v-if="loading">
        <el-skeleton :rows="8" animated />
      </template>
      <template v-else>
      <el-table :data="borrows" style="width: 100%; margin-top: 20px" :row-class-name="rowClassName">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="user.username" label="用户名" width="100" />
        <el-table-column prop="user.realName" label="姓名" width="100" />
        <el-table-column prop="book.title" label="书名" width="180" />
        <el-table-column prop="book.author" label="作者" width="120" />
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
        <el-table-column prop="fineAmount" label="罚款" width="80">
          <template #default="{ row }">
            <span :class="{ 'fine-amount': row.fineAmount > 0 }">¥{{ row.fineAmount ? row.fineAmount.toFixed(2) : '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllBorrows, returnBook } from '@/api/book'

const borrows = ref([])
const loading = ref(false)

const filterForm = ref({
  status: ''
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const fetchBorrows = async () => {
  loading.value = true
  try {
    const res = await getAllBorrows({
      status: filterForm.value.status || undefined,
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

const handleFilter = () => {
  pagination.value.page = 1
  fetchBorrows()
}

const handleReset = () => {
  filterForm.value = { status: '' }
  handleFilter()
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
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Return failed:', error)
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
})
</script>

<style scoped>
.borrows-container {
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
</style>
