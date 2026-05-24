<template>
  <div class="login-container">
    <!-- 品牌宣传区 -->
    <div class="brand-section">
      <!-- SVG 装饰：浮动书本 -->
      <div class="decor-books">
        <svg class="decor-book book-1" viewBox="0 0 60 80" fill="none">
          <rect x="2" y="2" width="56" height="76" rx="4" fill="#fff" fill-opacity="0.06" stroke="#fff" stroke-opacity="0.12" stroke-width="1"/>
          <rect x="10" y="14" width="40" height="3" rx="1.5" fill="#fff" fill-opacity="0.15"/>
          <rect x="10" y="22" width="30" height="3" rx="1.5" fill="#fff" fill-opacity="0.1"/>
          <rect x="10" y="30" width="36" height="3" rx="1.5" fill="#fff" fill-opacity="0.1"/>
        </svg>
        <svg class="decor-book book-2" viewBox="0 0 60 80" fill="none">
          <rect x="2" y="2" width="56" height="76" rx="4" fill="#fff" fill-opacity="0.04" stroke="#fff" stroke-opacity="0.08" stroke-width="1"/>
          <rect x="10" y="14" width="40" height="3" rx="1.5" fill="#fff" fill-opacity="0.1"/>
          <rect x="10" y="22" width="34" height="3" rx="1.5" fill="#fff" fill-opacity="0.08"/>
        </svg>
        <svg class="decor-book book-3" viewBox="0 0 60 80" fill="none">
          <rect x="2" y="2" width="56" height="76" rx="4" fill="#fff" fill-opacity="0.05" stroke="#fff" stroke-opacity="0.1" stroke-width="1"/>
          <rect x="10" y="14" width="38" height="3" rx="1.5" fill="#fff" fill-opacity="0.12"/>
          <rect x="10" y="22" width="28" height="3" rx="1.5" fill="#fff" fill-opacity="0.08"/>
          <rect x="10" y="30" width="32" height="3" rx="1.5" fill="#fff" fill-opacity="0.08"/>
        </svg>
      </div>

      <!-- 半透明蒙层 -->
      <div class="brand-overlay"></div>

      <div class="brand-content">
        <!-- Logo -->
        <div class="brand-logo">
          <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg" class="logo-icon">
            <rect x="8" y="62" width="64" height="4" rx="2" fill="#fff" fill-opacity="0.3"/>
            <rect x="12" y="18" width="16" height="44" rx="2" fill="#fff" fill-opacity="0.9" transform="rotate(-3 12 18)"/>
            <rect x="30" y="14" width="16" height="48" rx="2" fill="#fff" fill-opacity="0.7"/>
            <rect x="48" y="20" width="16" height="42" rx="2" fill="#fff" fill-opacity="0.5" transform="rotate(3 48 20)"/>
            <rect x="15" y="24" width="10" height="2" rx="1" fill="#409eff" fill-opacity="0.5"/>
            <rect x="15" y="30" width="8" height="2" rx="1" fill="#409eff" fill-opacity="0.3"/>
            <rect x="33" y="20" width="10" height="2" rx="1" fill="#409eff" fill-opacity="0.5"/>
            <rect x="33" y="26" width="8" height="2" rx="1" fill="#409eff" fill-opacity="0.3"/>
            <rect x="51" y="26" width="10" height="2" rx="1" fill="#409eff" fill-opacity="0.5"/>
            <rect x="51" y="32" width="8" height="2" rx="1" fill="#409eff" fill-opacity="0.3"/>
            <circle cx="40" cy="8" r="3" fill="#fff" fill-opacity="0.6"/>
            <circle cx="40" cy="8" r="6" fill="#fff" fill-opacity="0.15"/>
          </svg>
        </div>

        <h1 class="brand-title">图书管理系统</h1>

        <!-- 动态文案轮播 -->
        <div class="slogan-carousel">
          <transition name="slogan-fade" mode="out-in">
            <p class="brand-slogan" :key="currentSlogan">{{ slogans[currentSlogan] }}</p>
          </transition>
        </div>

        <!-- 特色功能 -->
        <div class="brand-features">
          <div class="feature-item" :class="{ active: currentSlogan === 0 }">
            <el-icon :size="18"><Reading /></el-icon>
            <span>海量藏书</span>
          </div>
          <div class="feature-item" :class="{ active: currentSlogan === 1 }">
            <el-icon :size="18"><Clock /></el-icon>
            <span>便捷借阅</span>
          </div>
          <div class="feature-item" :class="{ active: currentSlogan === 2 }">
            <el-icon :size="18"><User /></el-icon>
            <span>智能管理</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 表单区 -->
    <div class="form-section" :class="{ 'register-mode': mode === 'register' }">
      <div class="form-card" :class="{ shake: isShaking }">
        <!-- 登录错误提示 -->
        <transition name="error-fade">
          <div v-if="loginError" class="error-alert">
            <el-icon :size="16"><CircleCloseFilled /></el-icon>
            <span>{{ loginError }}</span>
          </div>
        </transition>

        <!-- 注册错误提示 -->
        <transition name="error-fade">
          <div v-if="registerError" class="error-alert">
            <el-icon :size="16"><CircleCloseFilled /></el-icon>
            <span>{{ registerError }}</span>
          </div>
        </transition>

        <!-- 表单顶部 Logo -->
        <div class="form-logo">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" class="form-logo-icon">
            <rect x="6" y="36" width="36" height="3" rx="1.5" fill="#409eff" fill-opacity="0.3"/>
            <rect x="8" y="10" width="10" height="26" rx="2" fill="#409eff" fill-opacity="0.85" transform="rotate(-2 8 10)"/>
            <rect x="19" y="8" width="10" height="28" rx="2" fill="#409eff" fill-opacity="0.65"/>
            <rect x="30" y="12" width="10" height="24" rx="2" fill="#409eff" fill-opacity="0.45" transform="rotate(2 30 12)"/>
          </svg>
        </div>

        <transition name="form-fade" mode="out-in">
          <!-- ===== 登录表单 ===== -->
          <div v-if="mode === 'login'" key="login">
            <h2 class="form-title">图书管理系统</h2>
            <p class="form-subtitle">欢迎回来，请登录您的账号</p>

            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-position="top" size="large" hide-required-asterisk>
              <el-form-item label="用户名" prop="username">
                <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable>
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin">
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <div class="form-options">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                <el-link type="info" :underline="false" class="forgot-link" @click="showForgotDialog = true">忘记密码？</el-link>
              </div>

              <el-form-item>
                <el-button type="primary" class="submit-btn" @click="handleLogin" :loading="loginLoading">登 录</el-button>
              </el-form-item>
            </el-form>

            <div class="form-footer">
              <span>还没有账号？</span>
              <el-link type="primary" @click="switchMode('register')">立即注册</el-link>
            </div>

            <!-- 第三方登录扩展区域（当前隐藏，后续启用时移除 display: none） -->
            <!-- 用途：微信、QQ等第三方登录入口，未来对接OAuth2.0等协议 -->
            <div class="third-party-login" style="display: none">
              <div class="divider">
                <span>其他登录方式</span>
              </div>
              <div class="third-party-icons">
                <div class="third-party-icon wechat-icon" title="微信登录">
                  <svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28">
                    <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.045c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-7.062-6.122zM14.033 13.3c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982z"/>
                  </svg>
                </div>
                <div class="third-party-icon qq-icon" title="QQ登录">
                  <svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28">
                    <path d="M12.003 2c-2.265 0-6.29 1.364-6.29 7.325v1.195S3.55 14.96 3.55 17.474c0 .665.17 1.025.396 1.025.116 0 .263-.072.42-.216-.156.553-.216 1.086-.216 1.544 0 .9.378 1.173.766 1.173.263 0 .543-.127.788-.348.105.463.363.743.702.743.328 0 .69-.258.98-.69.09.35.33.58.634.58.363 0 .765-.334 1.043-.867.06-.114.17-.14.253-.072a4.51 4.51 0 0 0 2.933 1.086 4.51 4.51 0 0 0 2.933-1.086c.083-.068.193-.042.253.072.278.533.68.867 1.043.867.304 0 .544-.23.634-.58.28.432.682.69 1.043.69.34 0 .597-.28.702-.744.245.221.525.349.788.349.388 0 .766-.274.766-1.174 0-.458-.06-.99-.216-1.544.157.144.304.216.42.216.227 0 .396-.36.396-1.025 0-2.514-2.163-6.954-2.163-6.954V9.325C18.293 3.364 14.268 2 12.003 2z"/>
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- ===== 注册表单 ===== -->
          <div v-else key="register">
            <h2 class="form-title">创建账号</h2>
            <p class="form-subtitle">注册后即可使用图书管理系统</p>

            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-position="top" size="large" hide-required-asterisk>
              <el-form-item label="用户名" prop="username">
                <el-input v-model="registerForm.username" placeholder="请输入用户名" clearable>
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password>
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password @keyup.enter="handleRegister">
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="姓名" prop="realName">
                <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" clearable>
                  <template #prefix>
                    <el-icon><Postcard /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="registerForm.email" placeholder="请输入邮箱（选填）" clearable>
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="手机" prop="phone">
                <el-input v-model="registerForm.phone" placeholder="请输入手机号（选填）" clearable>
                  <template #prefix>
                    <el-icon><Phone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" class="submit-btn" @click="handleRegister" :loading="registerLoading">注 册</el-button>
              </el-form-item>
            </el-form>

            <div class="form-footer">
              <span>已有账号？</span>
              <el-link type="primary" @click="switchMode('login')">立即登录</el-link>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- 忘记密码提示弹窗 -->
    <el-dialog v-model="showForgotDialog" title="忘记密码" width="400px" :append-to-body="true" align-center>
      <div class="forgot-dialog-content">
        <el-icon :size="48" color="#e6a23c"><WarningFilled /></el-icon>
        <p class="forgot-dialog-text">请联系管理员重置密码</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="showForgotDialog = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, Clock, User, Lock, Postcard, Message, Phone, CircleCloseFilled, WarningFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { register } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ===== 模式切换 =====
const mode = ref(route.path === '/register' ? 'register' : 'login')

watch(() => route.path, (path) => {
  mode.value = path === '/register' ? 'register' : 'login'
})

const switchMode = (newMode) => {
  mode.value = newMode
  loginError.value = ''
  registerError.value = ''
  const targetPath = newMode === 'register' ? '/register' : '/login'
  if (route.path !== targetPath) {
    router.replace(targetPath)
  }
}

// ===== 忘记密码弹窗 =====
const showForgotDialog = ref(false)

// ===== 震动动画 =====
const isShaking = ref(false)

const triggerShake = () => {
  isShaking.value = true
  setTimeout(() => {
    isShaking.value = false
  }, 600)
}

// ===== 登录 =====
const loginForm = ref({
  username: '',
  password: ''
})
const rememberMe = ref(false)
const loginFormRef = ref(null)
const loginLoading = ref(false)
const loginError = ref('')

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  loginError.value = ''
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loginLoading.value = true
      try {
        await userStore.login(loginForm.value)
        ElMessage.success('登录成功，正在跳转...')
        setTimeout(() => {
          router.push('/')
        }, 500)
      } catch (error) {
        loginForm.value.password = ''
        loginError.value = error.response?.data?.message || '用户名或密码错误，请重试'
        triggerShake()
      } finally {
        loginLoading.value = false
      }
    }
  })
}

// ===== 注册 =====
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: ''
})
const registerFormRef = ref(null)
const registerLoading = ref(false)
const registerError = ref('')

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.value.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为2-20个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  registerError.value = ''
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      registerLoading.value = true
      try {
        await register({
          username: registerForm.value.username,
          password: registerForm.value.password,
          realName: registerForm.value.realName,
          email: registerForm.value.email,
          phone: registerForm.value.phone
        })
        ElMessage.success('注册成功，请登录')
        loginForm.value.username = registerForm.value.username
        switchMode('login')
      } catch (error) {
        registerError.value = error.response?.data?.message || '注册失败，请重试'
        triggerShake()
      } finally {
        registerLoading.value = false
      }
    }
  })
}

// ===== 动态文案轮播 =====
const slogans = ['海量藏书，尽在指尖', '便捷借阅，一键搞定', '智能管理，高效运营']
const currentSlogan = ref(0)
let sloganTimer = null

onMounted(() => {
  sloganTimer = setInterval(() => {
    currentSlogan.value = (currentSlogan.value + 1) % slogans.length
  }, 3000)
})

onUnmounted(() => {
  if (sloganTimer) clearInterval(sloganTimer)
})
</script>

<style scoped>
/* CSS 变量：方便后续替换背景图 */
.login-container {
  --login-bg-image: none; /* 预留：后续可设为 url('/img/login-bg.jpg') */
  --login-bg-gradient: linear-gradient(135deg, #1a3a5c 0%, #0d2137 50%, #162d4a 100%);
  --brand-section-bg: linear-gradient(135deg, #1a3a5c 0%, #0d2137 50%, #162d4a 100%);
  --input-min-height: 44px;
  --btn-height: 44px;
  --font-size-title: 40px;
  --font-size-subtitle: 18px;
  --font-size-form-title: 24px;
  --font-size-input: 16px;
  --font-size-btn: 16px;
}

.login-container {
  min-height: 100vh;
  display: flex;
  background: var(--login-bg-gradient);
  /* 预留背景图：设置 --login-bg-image 后自动生效 */
  background-image: var(--login-bg-image);
  background-size: cover;
  background-position: center;
}

/* ========== 品牌宣传区 ========== */
.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
  background: var(--brand-section-bg);
  animation: slide-in-left 0.8s ease-out;
}

@keyframes slide-in-left {
  from {
    opacity: 0;
    transform: translateX(-60px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.brand-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.15);
  z-index: 0;
}

.decor-books {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.decor-book {
  position: absolute;
  width: 60px;
  height: 80px;
  opacity: 0.6;
}

.book-1 {
  top: 12%;
  left: 8%;
  animation: float-book 8s ease-in-out infinite;
}

.book-2 {
  bottom: 18%;
  left: 15%;
  animation: float-book 10s ease-in-out infinite 2s;
}

.book-3 {
  top: 20%;
  right: 12%;
  animation: float-book 9s ease-in-out infinite 4s;
}

@keyframes float-book {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-12px) rotate(2deg); }
  50% { transform: translateY(-6px) rotate(-1deg); }
  75% { transform: translateY(-14px) rotate(1deg); }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
}

.brand-logo {
  margin-bottom: 28px;
  animation: fade-in-up 0.6s ease-out 0.2s both;
}

.logo-icon {
  width: 80px;
  height: 80px;
  filter: drop-shadow(0 4px 16px rgba(64, 158, 255, 0.3));
}

.brand-title {
  font-size: var(--font-size-title);
  font-weight: 700;
  margin: 0 0 20px 0;
  letter-spacing: 6px;
  text-shadow:
    0 0 20px rgba(64, 158, 255, 0.4),
    0 2px 8px rgba(0, 0, 0, 0.4);
  animation: fade-in-up 0.6s ease-out 0.35s both;
}

.slogan-carousel {
  height: 36px;
  margin-bottom: 48px;
  overflow: hidden;
  animation: fade-in-up 0.6s ease-out 0.5s both;
}

.brand-slogan {
  font-size: var(--font-size-subtitle);
  margin: 0;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 4px;
  font-weight: 300;
  text-shadow: 0 0 12px rgba(64, 158, 255, 0.3);
}

.slogan-fade-enter-active,
.slogan-fade-leave-active {
  transition: all 0.5s ease;
}

.slogan-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.slogan-fade-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

.brand-features {
  display: flex;
  gap: 16px;
  justify-content: center;
  animation: fade-in-up 0.6s ease-out 0.65s both;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.55);
  padding: 8px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.5s ease;
}

.feature-item.active {
  color: rgba(255, 255, 255, 0.95);
  background: rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.35);
  box-shadow: 0 0 16px rgba(64, 158, 255, 0.15);
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 表单区 ========== */
.form-section {
  width: 480px;
  min-width: 400px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 60px 40px;
  background: #fff;
  border-radius: 24px 0 0 24px;
  box-shadow: -8px 0 32px rgba(0, 0, 0, 0.15);
  overflow-y: auto;
  max-height: 100vh;
  transition: padding 0.3s ease;
  animation: slide-in-right 0.8s ease-out;
}

@keyframes slide-in-right {
  from {
    opacity: 0;
    transform: translateX(60px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.form-section.register-mode {
  align-items: flex-start;
  padding: 40px 40px;
}

.form-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
}

/* 震动动画 */
.form-card.shake {
  animation: shake 0.6s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10% { transform: translateX(-8px); }
  20% { transform: translateX(8px); }
  30% { transform: translateX(-6px); }
  40% { transform: translateX(6px); }
  50% { transform: translateX(-4px); }
  60% { transform: translateX(4px); }
  70% { transform: translateX(-2px); }
  80% { transform: translateX(2px); }
  90% { transform: translateX(-1px); }
}

/* 错误提示 */
.error-alert {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  margin-bottom: 16px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 8px;
  color: #f56c6c;
  font-size: 13px;
  line-height: 1.5;
}

.error-fade-enter-active,
.error-fade-leave-active {
  transition: all 0.3s ease;
}

.error-fade-enter-from,
.error-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.form-logo {
  text-align: center;
  margin-bottom: 8px;
}

.form-logo-icon {
  width: 48px;
  height: 48px;
}

.form-title {
  font-size: var(--font-size-form-title);
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
  text-align: center;
}

.form-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0 0 28px 0;
  text-align: center;
}

/* 表单切换过渡动画 */
.form-fade-enter-active,
.form-fade-leave-active {
  transition: all 0.35s ease;
}

.form-fade-enter-from {
  opacity: 0;
  transform: translateX(24px);
}

.form-fade-leave-to {
  opacity: 0;
  transform: translateX(-24px);
}

/* 输入框样式增强 */
.form-card :deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 4px 12px;
  min-height: var(--input-min-height);
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: box-shadow 0.3s ease;
}

.form-card :deep(.el-input__inner) {
  font-size: var(--font-size-input);
}

.form-card :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.form-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset, 0 0 0 3px rgba(64, 158, 255, 0.1) inset !important;
}

.form-card :deep(.el-input__prefix .el-icon) {
  color: #c0c4cc;
  font-size: 16px;
  transition: color 0.3s ease;
}

.form-card :deep(.el-input__wrapper.is-focus .el-input__prefix .el-icon) {
  color: #409eff;
}

/* 记住我 & 忘记密码 */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.form-options :deep(.el-checkbox__label) {
  font-size: 14px;
  color: #666;
}

.forgot-link {
  font-size: 13px;
  color: #c0c4cc;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: var(--btn-height);
  font-size: var(--font-size-btn);
  letter-spacing: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.submit-btn:hover {
  filter: brightness(1.1);
}

.submit-btn:active {
  transform: scale(0.98);
}

.form-footer {
  text-align: center;
  color: #999;
  font-size: 14px;
  margin-top: 4px;
}

.form-footer .el-link {
  font-size: 14px;
}

/* 第三方登录扩展区域样式 */
.third-party-login {
  margin-top: 24px;
}

.third-party-login .divider {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.third-party-login .divider::before,
.third-party-login .divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e4e7ed;
}

.third-party-login .divider span {
  padding: 0 12px;
  font-size: 12px;
  color: #c0c4cc;
  white-space: nowrap;
}

.third-party-icons {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.third-party-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

.third-party-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.wechat-icon {
  color: #07c160;
}

.wechat-icon:hover {
  border-color: #07c160;
  background: rgba(7, 193, 96, 0.05);
}

.qq-icon {
  color: #12b7f5;
}

.qq-icon:hover {
  border-color: #12b7f5;
  background: rgba(18, 183, 245, 0.05);
}

/* 忘记密码弹窗 */
.forgot-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0 8px;
}

.forgot-dialog-text {
  margin: 16px 0 0;
  font-size: 16px;
  color: #303133;
}

/* ========== 响应式 ========== */

/* 平板适配（768px-1024px） */
@media (max-width: 1024px) {
  .brand-section {
    padding: 40px;
  }

  .brand-title {
    font-size: 32px;
    letter-spacing: 4px;
  }

  .brand-slogan {
    font-size: 16px;
    letter-spacing: 2px;
  }

  .form-section {
    width: 420px;
    min-width: 360px;
    padding: 48px 32px;
  }
}

/* 移动端适配（<768px） */
@media (max-width: 768px) {
  .login-container {
    --font-size-title: 28px;
    --font-size-subtitle: 15px;
    --font-size-form-title: 20px;
    --font-size-input: 15px;
    --font-size-btn: 15px;
    --input-min-height: 46px;
    --btn-height: 46px;

    flex-direction: column;
    background: #f5f7fa;
  }

  .brand-section {
    display: none;
  }

  .form-section {
    width: 90%;
    min-width: unset;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    padding: 32px 20px;
    max-height: unset;
    margin: auto;
    animation: fade-in-up 0.6s ease-out;
  }

  .form-section.register-mode {
    padding: 24px 20px;
  }

  .form-card {
    max-width: 100%;
  }

  .form-logo-icon {
    width: 40px;
    height: 40px;
  }

  .form-subtitle {
    font-size: 13px;
    margin-bottom: 20px;
  }

  .form-options {
    margin-bottom: 16px;
  }

  .form-card :deep(.el-form-item__label) {
    font-size: 13px;
    padding-bottom: 4px;
  }

  .form-card :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  .form-footer {
    font-size: 13px;
  }

  .error-alert {
    font-size: 12px;
    padding: 8px 12px;
  }

  /* 移动端第三方登录图标稍大便于触摸 */
  .third-party-icon {
    width: 48px;
    height: 48px;
  }
}

/* 小屏手机适配（<375px） */
@media (max-width: 375px) {
  .login-container {
    --font-size-form-title: 18px;
    --font-size-input: 14px;
    --font-size-btn: 14px;
  }

  .form-section {
    width: 95%;
    padding: 24px 16px;
  }

  .form-section.register-mode {
    padding: 20px 16px;
  }
}
</style>
