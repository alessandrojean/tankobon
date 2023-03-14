import { createRouter, createWebHistory } from 'vue-router'
import { setupLayouts } from 'virtual:generated-layouts'
import generatedRoutes from 'virtual:generated-pages'

declare module 'vue-router' {
  interface RouteMeta {
    title: string
  }
}

const routes = setupLayouts(generatedRoutes)

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    return (
      savedPosition ?? {
        top: 0,
        behavior: 'smooth',
      }
    )
  },
  routes,
})

router.beforeEach((to) => {
  const authStore = useUserStore()

  if (to.name !== 'sign-in' && to.name !== 'claim-server' && !authStore.isAuthenticated) {
    return { name: 'sign-in', query: { redirect: to.fullPath } }
  }
})

export default router
