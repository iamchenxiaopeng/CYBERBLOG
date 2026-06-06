// 游客身份 ID：存于 localStorage，跨会话保持一致
const GUEST_ID_KEY = 'cyberblog_guest_id'

export function getGuestId(): string {
  let id = localStorage.getItem(GUEST_ID_KEY)
  if (!id) {
    id = crypto.randomUUID()
    localStorage.setItem(GUEST_ID_KEY, id)
  }
  return id
}
