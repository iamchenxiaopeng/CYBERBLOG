/**
 * 密码传输前加密工具
 * 方案：SHA-256 一次（客户端 hash），后端再 BCrypt 一次
 * 即使传输被截获，也无法还原明文密码
 *
 * 兼容 HTTP 环境（crypto.subtle 需要 HTTPS/localhost）
 * 优先用 Web Crypto API，不可用时降级到 js-sha256（纯 JS 实现）
 */

import sha256lib from 'js-sha256'

/**
 * 计算字符串的 SHA-256 hash，返回小写 hex 字符串（64字符）
 */
export async function sha256(text: string): Promise<string> {
  // Web Crypto API 只在安全上下文可用（HTTPS / localhost / 127.0.0.1）
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
  // HTTP 环境下 crypto.subtle 不可用 → 使用纯 JS 实现（js-sha256）
  return Promise.resolve(sha256lib.sha256(text))
}

/**
 * 对密码做传输前预处理：SHA-256 hash
 * 注册和登录时统一调用此函数
 *
 * @example
 * const hashedPwd = await encryptPassword('my_password')
 * // → '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8'
 */
export async function encryptPassword(password: string): Promise<string> {
  return sha256(password)
}
