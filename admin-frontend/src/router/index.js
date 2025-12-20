import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requireAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'listings',
        name: 'ListingManagement',
        component: () => import('@/views/ListingManagement.vue'),
        meta: { title: '书籍管理' }
      },
      {
        path: 'orders',
        name: 'OrderManagement',
        component: () => import('@/views/OrderManagement.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'query',
        name: 'DataQuery',
        component: () => import('@/views/DataQuery.vue'),
        meta: { title: '数据查询' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const adminStore = useAdminStore()
  
  if (to.meta.requireAuth && !adminStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && adminStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router

