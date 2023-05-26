<script lang="ts" setup>
import { PlusIcon } from '@heroicons/vue/20/solid'
import type { SortDirection } from '@tanstack/vue-table'
import type { LibraryCreation, LibrarySort } from '@/types/tankobon-library'
import { safeNumber } from '@/utils/route'
import type { Sort } from '@/types/tankobon-api'

const userStore = useUserStore()
const userId = computed(() => userStore.me?.id)

const libraryStore = useLibraryStore()

const { t } = useI18n()
const notificator = useToaster()
const router = useRouter()

const showCreateDialog = ref(false)

const { mutate } = useCreateLibraryMutation()

function handleCreateLibrary(library: LibraryCreation) {
  mutate({ ...library, owner: userId.value }, {
    onSuccess: async ({ id }) => {
      notificator.success({ title: t('libraries.created-with-success') })
      await router.push({ name: 'libraries-id', params: { id } })

      if (libraryStore.library === null && userStore.isAuthenticated) {
        await libraryStore.fetchAndSelectFirstStore(userId.value!)
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

const size = useRouteQuery('size', '20', {
  mode: 'push',
  transform: v => safeNumber(v, 20, { min: 10 }),
})
const page = useRouteQuery('page', '0', {
  mode: 'push',
  transform: v => safeNumber(v, 0, { min: 0 }),
})
const sortQuery = useRouteQuery<string | null>('sort')

const sort = computed({
  get: () => {
    const [property, direction] = sortQuery.value?.split(':') ?? []

    if (!property && !direction) {
      return null
    }

    return {
      property: property as LibrarySort,
      direction: direction as SortDirection,
    } satisfies Sort<LibrarySort>
  },
  set: (newSort) => {
    sortQuery.value = newSort ? `${newSort.property}:${newSort.direction}` : null
  },
})

const { data: libraries, isLoading } = useUserLibrariesByUserQuery({
  userId,
  includes: ['owner'],
  page,
  size,
  sort: computed(() => sort.value ? [sort.value] : undefined),
  enabled: computed(() => userId.value !== undefined),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-failure'),
      body: error.message,
    })
  },
})
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
      <LibrariesListViewer
        v-model:sort="sort"
        v-model:page="page"
        v-model:size="size"
        column-order-key="libraries_column_order"
        column-visibility-key="libraries_column_visibility"
        :libraries="libraries"
        :loading="isLoading"
      />
    </div>

    <LibraryCreateDialog
      :is-open="showCreateDialog"
      @submit="handleCreateLibrary"
      @close="showCreateDialog = false"
    />
  </div>
</template>
