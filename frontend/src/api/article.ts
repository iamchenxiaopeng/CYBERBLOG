import request from './request'
import type { Result, Article, PageResult } from '@/types'
import { useUserStore } from '@/stores/user'
import { getGuestId } from '@/utils/guest'

function withGuestHeader() {
  const userStore = useUserStore()
  return userStore.isLoggedIn ? {} : { headers: { 'X-Guest-Id': getGuestId() } }
}

export interface ArticleDTO {
  title: string
  content: string
  contentType?: string
  coverImg?: string
  summary?: string
  tags?: string
  status?: string
}

export const articleApi = {
  list: (page = 1, size = 10, keyword?: string) =>
    request.get<any, Result<PageResult<Article>>>('/articles', {
      params: { page, size, keyword },
      ...withGuestHeader()
    }),
  get: (id: number) =>
    request.get<any, Result<Article>>(`/articles/${id}`, { ...withGuestHeader() }),
  create: (data: ArticleDTO) =>
    request.post<any, Result<Article>>('/articles', data),
  upload: (formData: FormData) =>
    request.post<any, Result<Article>>('/articles/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  update: (id: number, data: Partial<ArticleDTO>) =>
    request.put<any, Result<Article>>(`/articles/${id}`, data),
  delete: (id: number) =>
    request.delete<any, Result<void>>(`/articles/${id}`)
}
