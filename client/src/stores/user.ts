import { acceptHMRUpdate, defineStore } from 'pinia'
import { getMe, getMeWithAuth } from '@/services/tankobon-users'
import type { TankobonUserEntity } from '@/types/tankobon-user'

export const useUserStore = defineStore('user', {
  state: () => ({
    me: null as TankobonUserEntity | null,
  }),
  getters: {
    isAdmin: (state) => state.me?.attributes?.roles?.includes('ROLE_ADMIN') === true,
    isAuthenticated: (state) => typeof state.me?.id === 'string',
  },
  actions: {
    async signIn({ email, password }: { email: string, password: string }) {
      const me = await getMeWithAuth(email, password)
      
      this.$patch({ me })
    },

    async checkSession() {
      const me = await getMe()

      this.$patch({ me })
    }
  },
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot))
}