import request from './request'
import type { Result, Comment } from '@/types'
import { useUserStore } from '@/stores/user'
import { getGuestId } from '@/utils/guest'

export const commentApi = {
  list: (articleId: number) =>
    request.get<any, Result<Comment[]>>(`/articles/${articleId}/comments`),
  add: (articleId: number, content: string, parentId?: number) =>
    request.post<any, Result<Comment>>(`/articles/${articleId}/comments`, { content, parentId }),
  delete: (articleId: number, commentId: number) =>
    request.delete<any, Result<void>>(`/articles/${articleId}/comments/${commentId}`)
}

export const likeApi = {
  toggle: (targetType: 'article' | 'comment', targetId: number) => {
    const userStore = useUserStore()
    const headers: Record<string, string> = {}
    if (!userStore.isLoggedIn) {
      headers['X-Guest-Id'] = getGuestId()
    }
    return request.post<any, Result<{ liked: boolean }>>(
      `/likes/${targetType}/${targetId}`,
      null,
      { headers }
    )
  }
}

export const fileApi = {
  uploadImage: (file: File) => {
    const fd = new FormData()
    fd.append('file', file)
    return request.post<any, Result<{ url: string }>>('/files/image', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
