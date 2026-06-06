import { reactive } from 'vue'

export const toastState = reactive({
  message: '',
  type: 'info' as 'success' | 'error' | 'info',
  visible: false
})

let timer: ReturnType<typeof setTimeout>

export function showToast(msg: string, type: 'success' | 'error' | 'info' = 'info') {
  clearTimeout(timer)
  toastState.message = msg
  toastState.type = type
  toastState.visible = true
  timer = setTimeout(() => { toastState.visible = false }, 3000)
}
