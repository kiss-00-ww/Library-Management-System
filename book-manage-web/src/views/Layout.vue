<template>
  <div class="layout">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
        <div class="sidebar-logo" @click="$router.push('/books')">
          <el-icon :size="22"><Reading /></el-icon>
          <span v-show="!isCollapse" class="logo-text">图书管理系统</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="sidebar-menu"
          background-color="#1d1e2c"
          text-color="#a6aab7"
          active-text-color="#409eff"
        >
          <el-menu-item index="/books">
            <el-icon><Collection /></el-icon>
            <template #title>图书列表</template>
          </el-menu-item>
          <el-menu-item index="/my-borrows">
            <el-icon><Tickets /></el-icon>
            <template #title>我的借阅</template>
          </el-menu-item>
          <el-menu-item index="/my-reservations">
            <el-icon><Clock /></el-icon>
            <template #title>我的预约</template>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>

          <template v-if="userInfo.role === 'ADMIN'">
            <el-menu-item index="/admin/dashboard">
              <el-icon><DataAnalysis /></el-icon>
              <template #title>数据统计</template>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <el-icon><UserFilled /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/books">
              <el-icon><Notebook /></el-icon>
              <template #title>图书管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/categories">
              <el-icon><Grid /></el-icon>
              <template #title>分类管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/borrows">
              <el-icon><List /></el-icon>
              <template #title>借阅管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/config">
              <el-icon><Setting /></el-icon>
              <template #title>系统设置</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <el-container class="main-container">
        <el-header class="top-header">
          <div class="header-left">
            <el-icon
              :size="20"
              class="collapse-btn"
              @click="isCollapse = !isCollapse"
            >
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>

          <div class="header-right">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
              <el-button :icon="Bell" circle text @click="$router.push('/notifications')" class="bell-btn" />
            </el-badge>

            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="avatar-trigger">
                <el-avatar :size="32" :src="avatarUrl" class="user-avatar">
                  <el-icon :size="18"><User /></el-icon>
                </el-avatar>
                <span class="user-name">{{ userInfo.realName || userInfo.username }}</span>
                <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'success'" size="small" effect="dark" class="role-tag">
                  {{ userInfo.role === 'ADMIN' ? '管理员' : '读者' }}
                </el-tag>
                <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="page-main">
          <router-view />
        </el-main>

        <el-footer class="page-footer">
          <span>© 2026 图书管理系统 · Library Management System</span>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  Bell, Reading, Collection, Tickets, Clock, User, DataAnalysis,
  UserFilled, Notebook, Grid, List, Setting, Fold, Expand,
  ArrowDown, SwitchButton
} from '@element-plus/icons-vue'
import { getUnreadCount } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const avatarUrl = computed(() => {
  const avatar = userInfo.value.avatar
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return avatar
})

const unreadCount = ref(0)
let pollTimer = null

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data
  } catch (e) {
    // ignore
  }
}

const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

onMounted(() => {
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }
})
</script>

<style scoped>
.layout {
  height: 100vh;
  overflow: hidden;
}

.layout :deep(.el-container) {
  height: 100%;
}

.sidebar {
  background-color: #1d1e2c;
  overflow-y: auto;
  overflow-x: hidden;
  transition: width 0.28s;
}

.sidebar::-webkit-scrollbar {
  width: 4px;
}

.sidebar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
  overflow: hidden;
  white-space: nowrap;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.sidebar-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 6px;
  font-size: 14px;
}

.sidebar-menu .el-menu-item:hover {
  background-color: rgba(64, 158, 255, 0.08) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.15), rgba(64, 158, 255, 0.08)) !important;
  border-right: 2px solid #409eff;
}

.main-container {
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.top-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  z-index: 10;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  color: #606266;
  cursor: pointer;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-badge {
  line-height: 1;
  margin-right: 4px;
}

.bell-btn {
  color: #606266;
  font-size: 20px;
  transition: color 0.2s;
}

.bell-btn:hover {
  color: #409eff;
}

.avatar-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px 4px 6px;
  border-radius: 8px;
  transition: background 0.2s;
}

.avatar-trigger:hover {
  background: #f0f2f5;
}

.user-avatar {
  flex-shrink: 0;
}

.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-tag {
  flex-shrink: 0;
}

.dropdown-arrow {
  font-size: 12px;
  color: #909399;
  transition: transform 0.2s;
}

.page-main {
  flex: 1;
  overflow-y: auto !important;
  overflow-x: hidden !important;
  background: #f5f7fa;
  padding: 20px;
  min-height: 0 !important;
  height: 0;
}

.page-footer {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-top: 1px solid #ebeef5;
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .sidebar {
    width: 64px !important;
  }

  .logo-text {
    display: none;
  }

  .user-name,
  .role-tag,
  .dropdown-arrow {
    display: none;
  }

  .top-header {
    padding: 0 12px;
  }
}
</style>
