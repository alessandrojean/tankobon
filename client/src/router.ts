import { createRouter, createWebHistory } from 'vue-router'
import { setupLayouts } from 'virtual:generated-layouts'
import generatedRoutes from 'virtual:generated-pages'

declare module 'vue-router' {
  interface RouteMeta {
    title: string
    isAdminOnly?: boolean
    transparentNavbar?: boolean
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

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  if (to.name === 'sign-in' && userStore.isAuthenticated) {
    return { name: 'index' }
  } else if (to.name === 'sign-in' && (await userStore.sessionExists())) {
    return { name: 'index' }
  }

  if (
    to.name !== 'sign-in' &&
    to.name !== 'claim-server' &&
    to.name !== 'startup' &&
    !userStore.isAuthenticated
  ) {
    const query = Object.assign({}, to.query, { redirect: to.fullPath })
    return { name: 'startup', query }
  }
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  if (to.meta.isAdminOnly && !userStore.isAdmin) {
    return { name: 'index' }
  }
})

export default router
