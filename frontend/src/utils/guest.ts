// 游客身份 ID：存于 localStorage + Cookie，双保险提高持久性
const STORAGE_KEY = 'cyberblog_guest_id'
const COOKIE_NAME = 'cb_gid'

// 生成基于浏览器属性的稳定指纹（用于 ID 种子，不唯一，仅提高持久性）
function getBrowserFingerprint(): string {
  const parts = [
    navigator.userAgent,
    navigator.language,
    screen.width + 'x' + screen.height + 'x' + screen.colorDepth,
    new Intl.DateTimeFormat().resolvedOptions().timeZone,
    navigator.platform,
    navigator.hardwareConcurrency ?? '',
    (navigator as any).deviceMemory ?? '',
  ]
  const raw = parts.join('|')
  // 简单哈希转 hex
  let hash = 0
  for (let i = 0; i < raw.length; i++) {
    hash = ((hash << 5) - hash + raw.charCodeAt(i)) | 0
  }
  return Math.abs(hash).toString(36)
}

function setCookie(value: string, days = 365) {
  const expires = new Date(Date.now() + days * 864e5).toUTCString()
  document.cookie = `${COOKIE_NAME}=${encodeURIComponent(value)}; expires=${expires}; path=/; SameSite=Lax`
}

function getCookie(): string | null {
  const match = document.cookie.match(new RegExp('(^| )' + COOKIE_NAME + '=([^;]+)'))
  return match ? decodeURIComponent(match[2]) : null
}

export function getGuestId(): string {
  // 1. 优先读 localStorage
  let id = localStorage.getItem(STORAGE_KEY)
  if (id) {
    // 同步刷新 Cookie
    setCookie(id)
    return id
  }

  // 2. 回退到 Cookie（用户只清了 localStorage）
  id = getCookie()
  if (id) {
    localStorage.setItem(STORAGE_KEY, id)
    return id
  }

  // 3. 都未找到：生成新 ID（指纹 + 随机串防碰撞）
  const fingerprint = getBrowserFingerprint()
  // crypto.randomUUID() 只在 HTTPS/localhost 可用，fallback 用 Math.random
  const rand = typeof crypto.randomUUID === 'function'
    ? crypto.randomUUID()
    : Array.from({ length: 16 }, () => Math.floor(Math.random() * 16).toString(16)).join('')
  id = `cyber-${fingerprint}-${rand}`
  localStorage.setItem(STORAGE_KEY, id)
  setCookie(id)
  return id
}
