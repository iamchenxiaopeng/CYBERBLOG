import request from './request'
import type { Result, User } from '@/types'

export interface RegisterDTO { username: string; password: string; nonce: string; timestamp: number; email?: string }
export interface LoginDTO { username: string; password: string; nonce: string; timestamp: number }
export interface LoginVO { token: string; user: User }

export const authApi = {
  register: (data: RegisterDTO) =>
    request.post<any, Result<LoginVO>>('/auth/register', data),
  login: (data: LoginDTO) =>
    request.post<any, Result<LoginVO>>('/auth/login', data),
  me: () =>
    request.get<any, Result<User>>('/auth/me'),
  updateProfile: (bio?: string, avatarUrl?: string) =>
    request.put<any, Result<User>>('/auth/profile', null, { params: { bio, avatarUrl } })
}
