<template>
  <div class="auth-view">
    <div class="auth-box cyber-card cyber-corners">
      <div class="auth-header">
        <h1 class="glitch neon" data-text="REGISTER">REGISTER</h1>
        <p class="auth-sub">接入神经网络 // 创建身份证明</p>
      </div>
      <form @submit.prevent="submit" class="auth-form">
        <div class="form-group">
          <label class="cyber-label">用户名</label>
          <input v-model="form.username" class="cyber-input" placeholder="输入用户名" required />
        </div>
        <div class="form-group">
          <label class="cyber-label">密码</label>
          <input v-model="form.password" class="cyber-input" type="password" placeholder="设置密码（至少6位）" required />
        </div>
        <div class="form-group">
          <label class="cyber-label">邮箱（可选）</label>
          <input v-model="form.email" class="cyber-input" type="email" placeholder="your@email.com" />
        </div>
        <div v-if="error" class="error-msg">{{ error }}</div>
        <button class="btn-cyber w-full" type="submit" :disabled="loading">
          {{ loading ? 'INITIALIZING...' : '一键注册' }}
        </button>
      </form>
      <p class="auth-footer">
        已有账号？<RouterLink to="/login" class="neon">登录</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { encryptPassword } from '@/utils/crypto'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const error = ref('')
const form = ref({ username: '', password: '', email: '' })

async function submit() {
  error.value = ''
  loading.value = true
  try {
    // 传输前对密码做 SHA-256 hash
    const hashedPwd = await encryptPassword(form.value.password)
    const res = await authApi.register({
      username: form.value.username,
      password: hashedPwd,
      email: form.value.email || undefined,
    })
    if (res.code === 200) {
      userStore.setAuth(res.data.token, res.data.user)
      router.push('/')
    } else {
      error.value = res.message
    }
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-view {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}
.auth-box {
  width: 100%;
  max-width: 440px;
  padding: 40px;
}
.auth-header { text-align: center; margin-bottom: 32px; }
.auth-header h1 { font-family: var(--font-cyber); font-size: 28px; font-weight: 900; letter-spacing: 6px; margin-bottom: 8px; }
.auth-sub { font-family: var(--font-mono); font-size: 12px; color: var(--cyber-text-muted); letter-spacing: 2px; }
.auth-form { display: flex; flex-direction: column; gap: 20px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.error-msg { color: var(--cyber-red); font-family: var(--font-mono); font-size: 12px; }
.w-full { width: 100%; justify-content: center; }
.auth-footer { text-align: center; margin-top: 24px; font-family: var(--font-mono); font-size: 12px; color: var(--cyber-text-muted); }
</style>
