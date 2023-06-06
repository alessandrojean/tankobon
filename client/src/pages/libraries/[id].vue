<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import type { LibraryUpdate } from '@/types/tankobon-library'
import { createEmptyPaginatedResponse } from '@/utils/api';

const { t } = useI18n()
const router = useRouter()
const libraryId = useRouteParams<string | undefined>('id', undefined)
const notificator = useToaster()
const userStore = useUserStore()
const userId = computed(() => userStore.me?.id)

const { mutate: deleteLibrary, isLoading: isDeleting, isSuccess: isDeleted } = useDeleteLibraryMutation()
const { mutate: editLibrary, isLoading: isEditing } = useUpdateLibraryMutation()

const { data: canDelete } = useUserLibrariesByUserQuery<boolean>({
  userId: userId as ComputedRef<string>,
  includeShared: false,
  enabled: computed(() => userStore.isAuthenticated),
  select: response => response.data.length > 1,
  initialData: createEmptyPaginatedResponse(),
})

const { data: library, isLoading } = useLibraryQuery({
  libraryId: libraryId as Ref<string>,
  includes: ['owner'],
  enabled: computed(() => !!libraryId.value && !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-one-failure'),
      body: error.message,
    })
  },
})

function handleDelete() {
  deleteLibrary(libraryId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('libraries.deleted-with-success') })
      await router.replace({ name: 'libraries' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('libraries.deleted-with-failure'),
        body: error.message,
      })
    },
  })
}

const showEditDialog = ref(false)

function handleEditLibrary(library: LibraryUpdate) {
  editLibrary(library, {
    onSuccess: async () => {
      await notificator.success({ title: t('libraries.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('libraries.edited-with-failure'),
        body: error.message,
      })
    },
  })
}

useHead({ title: () => library.value?.attributes?.name ?? '' })
</script>

<template>
  <div>
    <Header
      :title="library?.attributes.name ?? ''"
      :subtitle="library?.attributes.description"
      :loading="isLoading"
      class="mb-3 md:mb-0"
    >
      <template #actions>
        <Toolbar class="flex space-x-2">
          <Button
            class="w-11 h-11"
            :loading="isEditing"
            :disabled="isDeleting"
            :title="$t('common-actions.edit')"
            @click="showEditDialog = true"
          >
            <span class="sr-only">{{ $t('common-actions.edit') }}</span>
            <PencilIcon class="w-6 h-6" />
          </Button>

          <Button
            class="w-11 h-11"
            kind="danger"
            :disabled="isEditing || !canDelete"
            :loading="isDeleting"
            :title="$t('common-actions.delete')"
            @click="handleDelete"
          >
            <span class="sr-only">{{ $t('common-actions.delete') }}</span>
            <TrashIcon class="w-6 h-6" />
          </Button>
        </Toolbar>
      </template>
    </Header>
    <div class="max-w-7xl mx-auto p-4 sm:p-6 space-y-10" />

    <LibraryEditDialog
      v-if="library"
      :is-open="showEditDialog"
      :library-entity="library"
      @submit="handleEditLibrary"
      @close="showEditDialog = false"
    />
  </div>
</template>

<route lang="yaml">
meta:
  layout: dashboard
</route>
