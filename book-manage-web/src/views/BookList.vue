<template>
  <div class="book-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>图书列表</span>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="searchForm.title" placeholder="搜索书名" style="width: 160px" clearable @keyup.enter="handleSearch" />
        <el-input v-model="searchForm.author" placeholder="搜索作者" style="width: 160px" clearable @keyup.enter="handleSearch" />
        <el-select v-model="searchForm.categoryId" placeholder="选择分类" style="width: 140px" clearable>
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <template v-if="loading">
        <div class="card-grid">
          <el-card v-for="i in 8" :key="i" class="book-card-skeleton" shadow="hover">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="width: 100%; height: 180px" />
                <div style="padding: 14px 0 0">
                  <el-skeleton-item variant="h3" style="width: 60%" />
                  <el-skeleton-item variant="text" style="width: 40%; margin-top: 8px" />
                  <el-skeleton-item variant="text" style="width: 30%; margin-top: 8px" />
                </div>
              </template>
            </el-skeleton>
          </el-card>
        </div>
      </template>
      <template v-else>
        <div class="card-grid" v-if="books.length > 0">
          <el-card
            v-for="book in books"
            :key="book.id"
            class="book-card"
            shadow="hover"
            @click="goToDetail(book.id)"
          >
            <div class="book-cover">
              <img v-if="book.coverImage" :src="getCoverUrl(book.coverImage)" :alt="book.title" />
              <div v-else class="default-cover">
                <el-icon :size="48"><Reading /></el-icon>
              </div>
              <el-tag
                class="status-tag"
                :type="book.availableQuantity > 0 ? 'success' : 'danger'"
                size="small"
              >
                {{ book.availableQuantity > 0 ? '可借' : '已借出' }}
              </el-tag>
            </div>
            <div class="book-info">
              <h3 class="book-title" :title="book.title">{{ book.title }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-category">{{ getCategoryName(book.categoryId) }}</p>
            </div>
          </el-card>
        </div>
        <el-empty v-else description="暂无图书数据，换个关键词试试吧～" :image-size="120" />
      </template>

      <el-pagination
        style="margin-top: 20px; text-align: right"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchBooks"
        @current-change="fetchBooks"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading } from '@element-plus/icons-vue'
import { getBooks, getCategories, getMyBorrowedBookIds } from '@/api/book'

const router = useRouter()
const books = ref([])
const categories = ref([])
const loading = ref(false)

const searchForm = ref({
  title: '',
  author: '',
  categoryId: null
})

const pagination = ref({
  page: 1,
  size: 12,
  total: 0
})

const borrowedBookIds = ref(new Set())

const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await getBooks({
      title: searchForm.value.title || undefined,
      author: searchForm.value.author || undefined,
      categoryId: searchForm.value.categoryId || undefined,
      page: pagination.value.page,
      size: pagination.value.size,
      status: 1
    })
    books.value = res.data.records
    pagination.value.total = res.data.total
  } catch (error) {
    console.error('Failed to fetch books:', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

const fetchMyBorrows = async () => {
  try {
    const res = await getMyBorrowedBookIds()
    borrowedBookIds.value = new Set(res.data || [])
  } catch (e) {
    // ignore
  }
}

const getCategoryName = (categoryId) => {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.name : '-'
}

const getCoverUrl = (coverImage) => {
  return coverImage || ''
}

const handleSearch = () => {
  pagination.value.page = 1
  fetchBooks()
}

const handleReset = () => {
  searchForm.value = { title: '', author: '', categoryId: null }
  handleSearch()
}

const goToDetail = (bookId) => {
  router.push(`/books/${bookId}`)
}

onMounted(() => {
  fetchBooks()
  fetchCategories()
  fetchMyBorrows()
})
</script>

<style scoped>
.book-list-container {
  padding: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 18px;
}

.search-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.book-card {
  cursor: pointer;
  border-radius: 12px;
  transition: transform 0.25s, box-shadow 0.25s;
  overflow: hidden;
}

.book-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.book-card :deep(.el-card__body) {
  padding: 0;
}

.book-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f7fa;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0fe 0%, #d4e4fc 100%);
  color: #409eff;
}

.status-tag {
  position: absolute;
  top: 8px;
  right: 8px;
}

.book-info {
  padding: 12px 14px 16px;
}

.book-title {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-author {
  margin: 0 0 4px;
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-category {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.book-card-skeleton {
  border-radius: 12px;
}

.book-card-skeleton :deep(.el-card__body) {
  padding: 0;
}
</style>
