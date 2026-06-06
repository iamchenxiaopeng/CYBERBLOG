export interface User {
  id: number
  username: string
  email?: string
  avatarUrl: string
  bio: string
}

export interface Article {
  id: number
  userId: number
  username: string
  avatarUrl: string
  title: string
  content?: string
  contentType: 'markdown' | 'rich'
  fileUrl?: string
  coverImg?: string
  summary: string
  tags: string
  status: string
  viewCount: number
  likeCount: number
  commentCount: number
  liked: boolean
  createdAt: string
  updatedAt: string
}

export interface Comment {
  id: number
  articleId: number
  userId: number
  username: string
  avatarUrl: string
  parentId: number
  content: string
  likeCount: number
  liked: boolean
  createdAt: string
  replies: Comment[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface Result<T> {
  code: number
  message: string
  data: T
}
