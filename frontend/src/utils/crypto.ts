/**
 * 密码传输前加密工具
 * 方案：SHA-256 一次（客户端 hash），后端再 BCrypt 一次
 * 即使传输被截获，也无法还原明文密码
 */

/**
 * 计算字符串的 SHA-256 hash，返回 hex 字符串
 */
export async function sha256(text: string): Promise<string> {
  const encoder = new TextEncoder()
  const data = encoder.encode(text)
  const hashBuffer = await crypto.subtle.digest('SHA-256', data)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
}

/**
 * 对密码做传输前预处理：SHA-256 hash
 * 注册和登录时统一调用此函数
 */
export async function encryptPassword(password: string): Promise<string> {
  return sha256(password)
}
