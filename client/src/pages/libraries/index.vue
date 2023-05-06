<script lang="ts" setup>
import { PlusIcon } from '@heroicons/vue/20/solid'
import type { LibraryCreation } from '@/types/tankobon-library'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const libraryStore = useLibraryStore()
const userId = computed(() => userStore.me!.id)
const notificator = useToaster()

const showCreateDialog = ref(false)

const { mutate } = useCreateLibraryMutation()

function handleCreateLibrary(library: LibraryCreation) {
  mutate({ ...library, owner: userId.value }, {
    onSuccess: async ({ id }) => {
      notificator.success({ title: t('libraries.created-with-success') })
      await router.push({ name: 'libraries-id', params: { id } })

      if (libraryStore.library === null) {
        await libraryStore.fetchAndSelectFirstStore(userId.value)
      }
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('libraries.created-with-failure'),
        body: error.message,
      })
    },
  })
}

useHead({ title: () => t('entities.libraries') })
</script>

<route lang="yaml">
meta:
  layout: dashboard
</route>

<template>
  <div>
    <Header
      class="mb-3 md:mb-0"
      :title="$t('entities.libraries')"
    >
      <template #actions>
        <Button
          kind="primary"
          @click="showCreateDialog = true"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('libraries.new') }}</span>
        </Button>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <LibrariesTable :user-id="userId" />
    </div>

    <LibraryCreateDialog
      :is-open="showCreateDialog"
      @submit="handleCreateLibrary"
      @close="showCreateDialog = false"
    />
  </div>
</template>
