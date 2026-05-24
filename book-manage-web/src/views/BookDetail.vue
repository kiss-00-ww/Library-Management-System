<template>
  <div class="book-detail-container" v-loading="loading">
    <el-card v-if="book" shadow="hover" class="detail-card">
      <div class="detail-layout">
        <!-- 左侧：图书封面 -->
        <div class="detail-cover">
          <img v-if="book.coverImage" :src="getCoverUrl(book.coverImage)" :alt="book.title" class="cover-img" />
          <div v-else class="default-cover">
            <el-icon :size="80"><Reading /></el-icon>
            <span>暂无封面</span>
          </div>
        </div>

        <!-- 右侧：图书信息 -->
        <div class="detail-info">
          <h1 class="book-title">{{ book.title }}</h1>
          <p class="book-author">{{ book.author }}</p>

          <el-divider />

          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="ISBN">{{ book.isbn || '-' }}</el-descriptions-item>
            <el-descriptions-item label="出版社">{{ book.publisher || '-' }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ categoryName }}</el-descriptions-item>
            <el-descriptions-item label="出版日期">{{ book.publishDate ? formatDate(book.publishDate) : '-' }}</el-descriptions-item>
            <el-descriptions-item label="馆藏位置">{{ book.location || '-' }}</el-descriptions-item>
            <el-descriptions-item label="库存">
              <el-tag :type="book.availableQuantity > 0 ? 'success' : 'danger'" size="small">
                可借 {{ book.availableQuantity }} / 总量 {{ book.totalQuantity }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <div class="book-desc" v-if="book.description">
            <h3>简介</h3>
            <p>{{ book.description }}</p>
          </div>

          <!-- 操作按钮区域 -->
          <div class="action-area">
            <template v-if="currentBorrow">
              <el-alert
                :title="`您已借阅此书，应还日期：${formatDate(currentBorrow.dueDate)}`"
                :type="currentBorrow.status === 'OVERDUE' ? 'error' : 'info'"
                show-icon
                :closable="false"
                style="margin-bottom: 16px"
              />
              <el-button
                v-if="currentBorrow.status === 'BORROWED' || currentBorrow.status === 'RENEWED'"
                type="warning"
                @click="handleRenew"
                :disabled="currentBorrow.renewCount >= (currentBorrow.maxRenewCount || 1)"
              >
                {{ currentBorrow.renewCount >= (currentBorrow.maxRenewCount || 1) ? '已达续借上限' : '续借' }}
              </el-button>
              <el-button
                v-if="currentBorrow.status !== 'RETURNED'"
                type="success"
                @click="handleReturn"
              >
                归还
              </el-button>
            </template>
            <template v-else>
              <el-button
                v-if="book.availableQuantity > 0"
                type="primary"
                size="large"
                @click="handleBorrow"
              >
                借阅
              </el-button>
              <el-button
                v-else
                type="warning"
                size="large"
                @click="handleReserve"
              >
                预约
              </el-button>
            </template>
            <el-button @click="goBack">返回列表</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-empty v-if="!loading && !book" description="图书不存在或已下架" :image-size="120">
      <el-button type="primary" @click="goBack">返回列表</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading } from '@element-plus/icons-vue'
import { getBookDetail, getCategories, borrowBook, createReservation, returnBook, renewBook, checkBorrowStatus } from '@/api/book'

const route = useRoute()
const router = useRouter()

const book = ref(null)
const loading = ref(false)
const categories = ref([])
const currentBorrow = ref(null)

const categoryName = computed(() => {
  if (!book.value || !categories.value.length) return '-'
  const cat = categories.value.find(c => c.id === book.value.categoryId)
  return cat ? cat.name : '-'
})

const fetchBook = async () => {
  loading.value = true
  try {
    const bookId = route.params.id
    const res = await getBookDetail(bookId)
    book.value = res.data
  } catch (error) {
    console.error('Failed to fetch book:', error)
    book.value = null
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (e) {
    // ignore
  }
}

const fetchCurrentBorrow = async () => {
  try {
    const bookId = route.params.id
    const res = await checkBorrowStatus(bookId)
    currentBorrow.value = res.data || null
  } catch (e) {
    // ignore
  }
}

const handleBorrow = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要借阅《${book.value.title}》吗？`,
      '确认借阅',
      { type: 'info' }
    )
    await borrowBook(book.value.id)
    ElMessage.success('借阅成功')
    fetchBook()
    fetchCurrentBorrow()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Borrow failed:', error)
    }
  }
}

const handleReserve = async () => {
  try {
    await ElMessageBox.confirm(
      `《${book.value.title}》暂无可借库存，确定要预约吗？归还后将通知您`,
      '确认预约',
      { type: 'info' }
    )
    await createReservation(book.value.id)
    ElMessage.success('预约成功，图书归还后将通知您')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Reserve failed:', error)
    }
  }
}

const handleReturn = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要归还《${book.value.title}》吗？`,
      '确认归还',
      { type: 'warning' }
    )
    await returnBook(currentBorrow.value.id)
    ElMessage.success('归还成功')
    fetchBook()
    fetchCurrentBorrow()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Return failed:', error)
    }
  }
}

const handleRenew = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要续借《${book.value.title}》吗？`,
      '确认续借',
      { type: 'warning' }
    )
    await renewBook(currentBorrow.value.id)
    ElMessage.success('续借成功')
    fetchCurrentBorrow()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Renew failed:', error)
    }
  }
}

const goBack = () => {
  router.push('/books')
}

const getCoverUrl = (coverImage) => {
  return coverImage || ''
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

onMounted(() => {
  fetchBook()
  fetchCategories()
  fetchCurrentBorrow()
})
</script>

<style scoped>
.book-detail-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.detail-card {
  border-radius: 16px;
}

.detail-layout {
  display: flex;
  gap: 40px;
}

.detail-cover {
  flex-shrink: 0;
  width: 280px;
}

.cover-img {
  width: 280px;
  height: 380px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.default-cover {
  width: 280px;
  height: 380px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: linear-gradient(135deg, #e8f0fe 0%, #d4e4fc 100%);
  color: #409eff;
  border-radius: 12px;
  font-size: 14px;
}

.detail-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}

.book-author {
  margin: 0 0 4px;
  font-size: 16px;
  color: #606266;
}

.book-desc {
  margin-top: 20px;
}

.book-desc h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
}

.book-desc p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
}

.action-area {
  margin-top: 28px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .detail-layout {
    flex-direction: column;
    align-items: center;
  }

  .detail-cover {
    width: 100%;
    max-width: 280px;
  }

  .cover-img,
  .default-cover {
    width: 100%;
  }
}
</style>
