<template>
  <div class="profile-page" v-loading="profileLoading">
    <!-- 头像区域 -->
    <div class="profile-header">
      <el-avatar :size="100" :src="avatarUrl" class="user-avatar">
        <el-icon :size="50"><User /></el-icon>
      </el-avatar>
      <h2 class="user-name">{{ form.realName || form.username }}</h2>
      <div class="user-meta">
        <el-tag :type="form.role === 'ADMIN' ? 'danger' : 'success'" size="small">
          {{ form.role === 'ADMIN' ? '管理员' : '普通用户' }}
        </el-tag>
        <span class="join-date">
          <el-icon><Calendar /></el-icon>
          加入于 {{ formatDate(form.createTime) }}
        </span>
      </div>
    </div>

    <!-- 分区卡片 -->
    <el-tabs v-model="activeTab" class="profile-tabs">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="info">
        <el-card shadow="hover" class="section-card">
          <el-form :model="form" :rules="profileRules" ref="profileFormRef" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="注册时间">
              <el-input :model-value="formatDate(form.createTime)" disabled />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="loading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 修改密码 -->
      <el-tab-pane label="修改密码" name="password">
        <el-card shadow="hover" class="section-card">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input type="password" v-model="passwordForm.oldPassword" placeholder="请输入旧密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="handleChangePassword" :loading="passwordLoading">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 头像设置 -->
      <el-tab-pane label="头像设置" name="avatar">
        <el-card shadow="hover" class="section-card">
          <div class="avatar-setting">
            <div class="avatar-preview">
              <el-avatar :size="140" :src="avatarUrl" class="preview-avatar">
                <el-icon :size="70"><User /></el-icon>
              </el-avatar>
            </div>
            <div class="avatar-actions">
              <el-upload
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
                accept=".jpg,.jpeg,.png"
              >
                <el-button type="primary" :loading="uploading">
                  <el-icon><Upload /></el-icon>
                  {{ uploading ? '上传中...' : '更换头像' }}
                </el-button>
              </el-upload>
              <p class="avatar-tip">支持 JPG/PNG 格式，不超过 2MB</p>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { User, Calendar, Upload } from '@element-plus/icons-vue'
import { getUserInfo, updateUser } from '@/api/user'
import { changePassword } from '@/api/auth'

const activeTab = ref('info')

const form = ref({
  id: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  role: '',
  avatar: '',
  createTime: ''
})

const defaultAvatar = computed(() => {
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(form.value.username || 'U')}&background=random&size=120`
})

const avatarUrl = computed(() => {
  if (!form.value.avatar) return defaultAvatar.value
  if (form.value.avatar.startsWith('http')) return form.value.avatar
  return form.value.avatar
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const profileRules = {
  realName: [
    { min: 2, max: 20, message: '姓名长度为2-20个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const loading = ref(false)
const passwordLoading = ref(false)
const uploading = ref(false)
const profileLoading = ref(false)
const passwordFormRef = ref(null)
const profileFormRef = ref(null)

const fetchUserInfo = async () => {
  profileLoading.value = true
  try {
    const res = await getUserInfo()
    form.value = res.data
  } catch (error) {
    console.error('Failed to fetch user info:', error)
  } finally {
    profileLoading.value = false
  }
}

const handleUpdate = async () => {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await updateUser({
        realName: form.value.realName,
        email: form.value.email,
        phone: form.value.phone
      })
      ElMessage.success('修改成功')
      await fetchUserInfo()
    } catch (error) {
      console.error('Failed to update user:', error)
    } finally {
      loading.value = false
    }
  })
}

const handleChangePassword = async () => {
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await changePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        ElMessage.success('密码修改成功')
        passwordForm.value = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
      } catch (error) {
        console.error('Failed to change password:', error)
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

const handleAvatarUpload = async (options) => {
  const { file } = options
  uploading.value = true
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      form.value.avatar = res.data
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch (error) {
    console.error('Upload failed:', error)
  } finally {
    uploading.value = false
  }
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 36px 20px 28px;
  background: linear-gradient(135deg, #1890ff 0%, #0050b3 100%);
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.25);
}

.user-avatar {
  border: 4px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 0.9);
}

.user-name {
  margin: 14px 0 10px;
  font-size: 24px;
  font-weight: 600;
  color: #fff;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
}

.join-date {
  display: flex;
  align-items: center;
  gap: 4px;
}

.profile-tabs {
  margin-top: 4px;
}

.section-card {
  border-radius: 12px;
}

.profile-form {
  max-width: 500px;
  padding: 10px 0;
}

.avatar-setting {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 20px 0;
}

.preview-avatar {
  border: 3px solid #e4e7ed;
  background: rgba(255, 255, 255, 0.9);
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.avatar-tip {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

@media (max-width: 768px) {
  .avatar-setting {
    flex-direction: column;
    gap: 20px;
  }
}
</style>
