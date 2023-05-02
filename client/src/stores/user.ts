import { acceptHMRUpdate, defineStore } from 'pinia'
import { getMe, getMeWithAuth, signOut } from '@/services/tankobon-users'
import type { UserEntity } from '@/types/tankobon-user'
import { getRelationship } from '@/utils/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    me: null as UserEntity | null,
  }),
  getters: {
    isAdmin: state => state.me?.attributes?.roles?.includes('ROLE_ADMIN') === true,
    isAuthenticated: state => typeof state.me?.id === 'string',
    avatar: state => getRelationship(state.me, 'AVATAR'),
  },
  actions: {
    async signIn({ email, password }: { email: string; password: string }) {
      const me = await getMeWithAuth(email, password)
      this.$patch({ me })

      const libraryStore = useLibraryStore()
      await libraryStore.fetchAndSelectFirstStore(this.me!.id)
    },

    async signOut() {
      try {
        await signOut()
      }
      catch (_) {}

      this.$patch({ me: null })

      const libraryStore = useLibraryStore()
      libraryStore.setLibrary(null)
    },

    async checkSession() {
      const me = await getMe()
      const meWasNull = this.me === null

      this.$patch({
        me: {
          ...this.me,
          ...me,
          relationships: [...(me.relationships ?? [])],
        },
      })

      if (meWasNull) {
        const libraryStore = useLibraryStore()
        await libraryStore.fetchAndSelectFirstStore(this.me!.id)
      }
    },

    async sessionExists() {
      try {
        const me = await getMe()
        const meWasNull = this.me === null

        this.$patch({
          me: {
            ...this.me,
            ...me,
            relationships: [...(me.relationships ?? [])],
          },
        })

        if (meWasNull) {
          const libraryStore = useLibraryStore()
          await libraryStore.fetchAndSelectFirstStore(this.me!.id)
        }

        return true
      }
      catch (_) {
        return false
      }
    },
  },
})

if (import.meta.hot)
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot))
