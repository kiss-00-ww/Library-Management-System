<template>
  <div class="admin-books-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>图书管理</span>
          <div>
            <el-button type="success" @click="handleDownloadTemplate">下载导入模板</el-button>
            <el-button type="warning" @click="importDialogVisible = true">批量导入</el-button>
            <el-button type="primary" @click="handleAdd">添加图书</el-button>
          </div>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="searchForm.title" placeholder="搜索书名" style="width: 160px" clearable />
        <el-input v-model="searchForm.author" placeholder="搜索作者" style="width: 160px" clearable />
        <el-select v-model="searchForm.categoryId" placeholder="选择分类" style="width: 140px" clearable>
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <template v-if="loading">
        <el-skeleton :rows="8" animated />
      </template>
      <template v-else>
      <el-table :data="books" style="width: 100%; margin-top: 20px">
        <el-table-column prop="title" label="书名" width="180" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="isbn" label="ISBN" width="130" />
        <el-table-column prop="publisher" label="出版社" width="120" />
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="availableQuantity" label="可借" width="70" />
        <el-table-column prop="totalQuantity" label="总量" width="70" />
        <el-table-column prop="location" label="位置" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button :type="row.status === 1 ? 'info' : 'success'" size="small" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无图书数据，快去添加第一本吧～" :image-size="80" />
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
        @size-change="fetchBooks"
        @current-change="fetchBooks"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" :disabled="!!form.id" placeholder="请输入ISBN编号" />
        </el-form-item>
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入书名" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="form.publisher" placeholder="请输入出版社" maxlength="100" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="出版日期">
          <el-date-picker v-model="form.publishDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="总数量" prop="totalQuantity">
          <el-input-number v-model="form.totalQuantity" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入馆藏位置" maxlength="50" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入简介" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="封面" prop="coverImage">
          <el-input v-model="form.coverImage" placeholder="请输入封面图片URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="批量导入图书" width="500px" destroy-on-close :close-on-click-modal="false">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-exceed="() => ElMessage.warning('只能上传一个文件')"
      >
        <el-icon style="font-size: 40px; color: #409eff;"><upload-filled /></el-icon>
        <div>将 Excel 文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xlsx / .xls 文件</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importLoading">确认导入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importResultVisible" title="导入结果" width="600px" destroy-on-close>
      <el-result
        :icon="importResult.failCount === 0 ? 'success' : 'warning'"
        :title="`成功导入 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`"
      />
      <div v-if="importResult.failReasons && importResult.failReasons.length > 0" style="margin-top: 10px;">
        <p style="font-weight: bold;">失败原因：</p>
        <el-scrollbar max-height="200px">
          <ul style="margin: 0; padding-left: 20px;">
            <li v-for="(reason, index) in importResult.failReasons" :key="index" style="color: #f56c6c; font-size: 13px;">{{ reason }}</li>
          </ul>
        </el-scrollbar>
      </div>
      <template #footer>
        <el-button type="primary" @click="importResultVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getBooks, addBook, updateBook, deleteBook, toggleBookStatus, getCategories, downloadBookTemplate, importBooks } from '@/api/book'

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
  size: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({
  id: null,
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  categoryId: null,
  publishDate: null,
  totalQuantity: 1,
  location: '',
  description: '',
  coverImage: ''
})

const rules = {
  isbn: [
    { required: true, message: '请输入ISBN', trigger: 'blur' },
    { pattern: /^[\d-]{10,17}$/, message: 'ISBN格式不正确，应为10-13位数字（可含连字符）', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { min: 1, max: 100, message: '书名长度为1-100个字符', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 1, max: 50, message: '作者长度为1-50个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  totalQuantity: [
    { required: true, message: '请输入总数量', trigger: 'blur' },
    { type: 'number', min: 1, max: 9999, message: '总数量应为1-9999之间的整数', trigger: 'blur' }
  ],
  publisher: [
    { max: 100, message: '出版社名称不超过100个字符', trigger: 'blur' }
  ],
  location: [
    { max: 50, message: '位置信息不超过50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '简介不超过500个字符', trigger: 'blur' }
  ],
  coverImage: [
    { type: 'url', message: '请输入正确的URL地址', trigger: 'blur' }
  ]
}

const formRef = ref(null)

const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await getBooks({
      title: searchForm.value.title || undefined,
      author: searchForm.value.author || undefined,
      categoryId: searchForm.value.categoryId || undefined,
      page: pagination.value.page,
      size: pagination.value.size
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

const getCategoryName = (categoryId) => {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.name : '-'
}

const handleSearch = () => {
  pagination.value.page = 1
  fetchBooks()
}

const handleReset = () => {
  searchForm.value = { title: '', author: '', categoryId: null }
  handleSearch()
}

const handleAdd = () => {
  form.value = {
    id: null,
    isbn: '',
    title: '',
    author: '',
    publisher: '',
    categoryId: null,
    publishDate: null,
    totalQuantity: 1,
    location: '',
    description: '',
    coverImage: ''
  }
  dialogTitle.value = '添加图书'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogTitle.value = '编辑图书'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.value.id) {
          await updateBook(form.value.id, form.value)
          ElMessage.success('修改成功')
        } else {
          await addBook(form.value)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchBooks()
      } catch (error) {
        console.error('Submit failed:', error)
      }
    }
  })
}

const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '下架' : '上架'}图书《${row.title}》吗？`,
      '提示',
      { type: 'warning' }
    )
    await toggleBookStatus(row.id)
    ElMessage.success(row.status === 1 ? '下架成功' : '上架成功')
    fetchBooks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Toggle status failed:', error)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除图书《${row.title}》吗？删除后不可恢复！`,
      '警告',
      { type: 'error', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await deleteBook(row.id)
    ElMessage.success('删除成功')
    fetchBooks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete failed:', error)
    }
  }
}

const importDialogVisible = ref(false)
const importResultVisible = ref(false)
const importLoading = ref(false)
const uploadRef = ref(null)
const importFile = ref(null)
const importResult = ref({ successCount: 0, failCount: 0, failReasons: [] })

const handleFileChange = (file) => {
  importFile.value = file.raw
}

const handleDownloadTemplate = async () => {
  try {
    const res = await downloadBookTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '图书导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Download template failed:', error)
    ElMessage.error('下载模板失败')
  }
}

const handleImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  importLoading.value = true
  try {
    const res = await importBooks(importFile.value)
    importResult.value = res.data
    importDialogVisible.value = false
    importResultVisible.value = true
    importFile.value = null
    if (uploadRef.value) uploadRef.value.clearFiles()
    fetchBooks()
  } catch (error) {
    console.error('Import failed:', error)
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

onMounted(() => {
  fetchBooks()
  fetchCategories()
})
</script>

<style scoped>
.admin-books-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 18px;
}
</style>
