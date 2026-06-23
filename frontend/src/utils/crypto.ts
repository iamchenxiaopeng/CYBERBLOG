/**
 * 密码传输前加密工具
 * 方案：SHA-256(password) 一次（防明文传输），nonce + timestamp 防重放攻击
 *
 * 重放防护原理：
 * - 前端发送 SHA-256(password) + nonce + timestamp
 * - 后端验证 timestamp 在 5 分钟窗口内
 * - 超过时效的请求被拒绝，截获的哈希无法重放
 *
 * 兼容 HTTP 环境（crypto.subtle 需要 HTTPS/localhost）
 */

import sha256lib from 'js-sha256'

/**
 * 计算字符串的 SHA-256 hash，返回小写 hex 字符串（64字符）
 */
export async function sha256(text: string): Promise<string> {
  if (typeof crypto !== 'undefined' && typeof crypto.subtle === 'object') {
    try {
      const encoder = new TextEncoder()
      const data = encoder.encode(text)
      const hashBuffer = await crypto.subtle.digest('SHA-256', data)
      const hashArray = Array.from(new Uint8Array(hashBuffer))
      return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
    } catch {
      // subtle 调用失败 → 降级
    }
  }
  return Promise.resolve(sha256lib.sha256(text))
}

/**
 * 生成随机 nonce（8 字符十六进制），用于请求唯一标识
 */
function generateNonce(): string {
  const arr = new Uint8Array(4)
  if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
    crypto.getRandomValues(arr)
  } else {
    for (let i = 0; i < 4; i++) arr[i] = Math.floor(Math.random() * 256)
  }
  return Array.from(arr).map(b => b.toString(16).padStart(2, '0')).join('')
}

/**
 * 对密码做传输前预处理：SHA-256(password) + nonce + timestamp
 * nonce 和 timestamp 用于防止重放攻击（后端验证时效性）
 *
 * @example
 * const result = await encryptPassword('my_password')
 * // result = { password: '5e884...', nonce: 'a1b2c3d4', timestamp: 1719000000 }
 */
export async function encryptPassword(password: string): Promise<{
  password: string
  nonce: string
  timestamp: number
}> {
  const nonce = generateNonce()
  const timestamp = Math.floor(Date.now() / 1000)
  const hash = await sha256(password)
  return { password: hash, nonce, timestamp }
}
