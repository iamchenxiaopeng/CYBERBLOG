<template>
  <div class="auth-view">
    <div class="auth-box cyber-card cyber-corners">
      <div class="auth-header">
        <h1 class="glitch neon" data-text="LOGIN">LOGIN</h1>
        <p class="auth-sub">身份验证 // 神经链接中</p>
      </div>
      <form @submit.prevent="submit" class="auth-form">
        <div class="form-group">
          <label class="cyber-label">用户名</label>
          <input v-model="form.username" class="cyber-input" placeholder="输入用户名" required />
        </div>
        <div class="form-group">
          <label class="cyber-label">密码</label>
          <input v-model="form.password" class="cyber-input" type="password" placeholder="输入密码" required />
        </div>
        <div v-if="error" class="error-msg">{{ error }}</div>
        <button class="btn-cyber w-full" type="submit" :disabled="loading">
          {{ loading ? 'VERIFYING...' : '登录' }}
        </button>
      </form>
      <p class="auth-footer">
        没有账号？<RouterLink to="/register" class="neon-pink">注册</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '@/api/auth'
import { encryptPassword } from '@/utils/crypto'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const error = ref('')
const form = ref({ username: '', password: '' })

// 如果是从过期跳转来的，显示提示
onMounted(() => {
  if (route.query.expired) {
    error.value = '登录已过期，请重新登录'
  }
})

async function submit() {
  error.value = ''
  loading.value = true
  try {
    // 传输前对密码做 SHA-256(password + nonce + timestamp)，防重放攻击
    const encrypted = await encryptPassword(form.value.password)
    const res = await authApi.login({
      username: form.value.username,
      password: encrypted.password,
      nonce: encrypted.nonce,
      timestamp: encrypted.timestamp,
    })
    if (res.code === 200) {
      userStore.setAuth(res.data.token, res.data.user)
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
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
.auth-box { width: 100%; max-width: 440px; padding: 40px; }
.auth-header { text-align: center; margin-bottom: 32px; }
.auth-header h1 { font-family: var(--font-cyber); font-size: 28px; font-weight: 900; letter-spacing: 6px; margin-bottom: 8px; }
.auth-sub { font-family: var(--font-mono); font-size: 12px; color: var(--cyber-text-muted); letter-spacing: 2px; }
.auth-form { display: flex; flex-direction: column; gap: 20px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.error-msg { color: var(--cyber-red); font-family: var(--font-mono); font-size: 12px; }
.w-full { width: 100%; justify-content: center; }
.auth-footer { text-align: center; margin-top: 24px; font-family: var(--font-mono); font-size: 12px; color: var(--cyber-text-muted); }
</style>
