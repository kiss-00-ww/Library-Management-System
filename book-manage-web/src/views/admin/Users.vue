<template>
  <div class="users-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="searchForm.keyword" placeholder="搜索用户名/姓名/邮箱" style="width: 220px" clearable />
        <el-select v-model="searchForm.status" placeholder="选择状态" style="width: 130px" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <template v-if="loading">
        <el-skeleton :rows="8" animated />
      </template>
      <template v-else>
      <el-table :data="users" style="width: 100%; margin-top: 20px">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="role" label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'TEACHER' ? 'warning' : 'success'">{{ row.role === 'ADMIN' ? '管理员' : row.role === 'TEACHER' ? '教师' : '读者' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="warning" size="small" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="primary" size="small" link @click="handleChangeRole(row)">修改角色</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无用户数据" :image-size="80" />
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
        @size-change="fetchUsers"
        @current-change="fetchUsers"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUserStatus, updateUserRole } from '@/api/user'

const users = ref([])
const loading = ref(false)

const searchForm = ref({
  keyword: '',
  status: null
})

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUsers({
      keyword: searchForm.value.keyword || undefined,
      status: searchForm.value.status,
      page: pagination.value.page,
      size: pagination.value.size
    })
    users.value = res.data.records
    pagination.value.total = res.data.total
  } catch (error) {
    console.error('Failed to fetch users:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  fetchUsers()
}

const handleReset = () => {
  searchForm.value = { keyword: '', status: null }
  handleSearch()
}

const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '禁用' : '启用'}用户 ${row.username} 吗？`,
      '提示',
      { type: 'warning' }
    )
    await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('操作成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Toggle status failed:', error)
    }
  }
}

const roleLabels = { ADMIN: '管理员', TEACHER: '教师', READER: '读者' }

const handleChangeRole = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `当前角色：${roleLabels[row.role] || row.role}，请输入新角色（ADMIN / TEACHER / READER）`,
      '修改用户角色',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: row.role,
        inputPattern: /^(ADMIN|TEACHER|READER)$/,
        inputErrorMessage: '请输入有效角色：ADMIN、TEACHER 或 READER'
      }
    )
    if (value === row.role) {
      ElMessage.info('角色未变更')
      return
    }
    await updateUserRole(row.id, value)
    ElMessage.success('角色修改成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Change role failed:', error)
    }
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-container {
  padding: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 18px;
}
</style>
