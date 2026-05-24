import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/books',
    children: [
      {
        path: 'books',
        name: 'BookList',
        component: () => import('@/views/BookList.vue')
      },
      {
        path: 'books/:id',
        name: 'BookDetail',
        component: () => import('@/views/BookDetail.vue')
      },
      {
        path: 'my-borrows',
        name: 'MyBorrows',
        component: () => import('@/views/MyBorrows.vue')
      },
      {
        path: 'my-reservations',
        name: 'MyReservations',
        component: () => import('@/views/MyReservations.vue')
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/Notifications.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/books',
        name: 'AdminBooks',
        component: () => import('@/views/admin/Books.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/borrows',
        name: 'AdminBorrows',
        component: () => import('@/views/admin/Borrows.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/config',
        name: 'AdminSystemConfig',
        component: () => import('@/views/admin/SystemConfig.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/Categories.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.path === '/login' || to.path === '/register') {
    next()
  } else {
    if (!token) {
      next('/login')
    } else if (to.meta.requiresAdmin) {
      // 管理员页面需要从后端验证角色
      try {
        const userStore = useUserStore()
        await userStore.getUserInfo()
        if (userStore.userInfo.role !== 'ADMIN') {
          ElMessage.error('需要管理员权限')
          next('/books')
        } else {
          next()
        }
      } catch (e) {
        // token 过期或无效，跳转登录
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        next('/login')
      }
    } else {
      next()
    }
  }
})

export default router
