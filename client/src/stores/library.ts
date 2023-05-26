import { acceptHMRUpdate, defineStore } from 'pinia'
import type { LibraryEntity } from '@/types/tankobon-library'
import { getAllLibrariesByUser } from '@/services/tankobon-libraries'

export const useLibraryStore = defineStore('library', {
  state: () => ({
    library: null as LibraryEntity | null,
  }),
  actions: {
    async fetchAndSelectFirstStore(userId: string) {
      const { data: libraries } = await getAllLibrariesByUser({
        includes: ['owner'],
        userId,
      })

      this.setLibrary(libraries.length > 0 ? libraries[0] : null)
    },

    setLibrary(library: LibraryEntity | null) {
      this.$patch({ library })
    },
  },
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useLibraryStore, import.meta.hot))
}
