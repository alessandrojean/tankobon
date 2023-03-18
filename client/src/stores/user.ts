import { acceptHMRUpdate, defineStore } from 'pinia'
import { getMe, getMeWithAuth, signOut } from '@/services/tankobon-users'
import type { UserEntity } from '@/types/tankobon-user'

export const useUserStore = defineStore('user', {
  state: () => ({
    me: null as UserEntity | null,
  }),
  getters: {
    isAdmin: (state) => state.me?.attributes?.roles?.includes('ROLE_ADMIN') === true,
    isAuthenticated: (state) => typeof state.me?.id === 'string',
    avatar: (state) => {
      return state.me?.relationships
        ?.find((r) => r.type === 'AVATAR')
    }
  },
  actions: {
    async signIn({ email, password }: { email: string, password: string }) {
      const me = await getMeWithAuth(email, password)
      
      this.$patch({ me })
    },

    async signOut() {
      try {
        await signOut()
      } catch (_) {}

      this.$patch({ me: null })
    },

    async checkSession() {
      const me = await getMe()

      this.$patch({
        me: {
          ...this.me,
          ...me,
          relationships: [...(me.relationships ?? [])],
        }
      })
    },

    async sessionExists() {
      try {
        const me = await getMe()
        this.$patch({
          me: {
            ...this.me,
            ...me,
            relationships: [...(me.relationships ?? [])],
          }
        })
        return true
      } catch (_) {
        return false
      }
    }
  },
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot))
}
