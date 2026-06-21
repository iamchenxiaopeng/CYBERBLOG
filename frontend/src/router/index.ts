import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/', component: () => import('@/views/HomeView.vue') },
  { path: '/articles', component: () => import('@/views/ArticleListView.vue') },
  { path: '/articles/:id', component: () => import('@/views/ArticleDetailView.vue') },
  { path: '/write', component: () => import('@/views/WriteView.vue'), meta: { requiresAuth: true } },
  { path: '/write/:id', component: () => import('@/views/WriteView.vue'), meta: { requiresAuth: true } },
  { path: '/login', component: () => import('@/views/LoginView.vue') },
  { path: '/register', component: () => import('@/views/RegisterView.vue') },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/NotFoundView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return '/login'
  }
})

export default router
