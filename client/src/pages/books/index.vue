<script lang="ts" setup>
import { ArrowDownOnSquareIcon, PlusIcon } from '@heroicons/vue/20/solid'
import Button from '@/components/form/Button.vue'
import type { Sort, SortDirection } from '@/types/tankobon-api'
import type { BookSort } from '@/types/tankobon-book'
import { safeNumber } from '@/utils/route'

const libraryStore = useLibraryStore()
const libraryId = computed(() => libraryStore.library!.id)

const search = ref('')

const { t } = useI18n()
const notificator = useToaster()

useHead({ title: () => t('entities.books') })

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
      property: property as BookSort,
      direction: direction as SortDirection,
    } satisfies Sort<BookSort>
  },
  set: (newSort) => {
    sortQuery.value = newSort ? `${newSort.property}:${newSort.direction}` : null
  }
})

const { data: books, isLoading } = useLibraryBooksQuery({
  libraryId,
  search,
  includes: ['cover_art', 'collection', 'series', 'publisher'],
  page,
  size,
  sort: computed(() => sort.value ? [sort.value] : undefined),
  enabled: computed(() => libraryId.value !== undefined),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('books.fetch-failure'),
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
    <Header :title="$t('entities.books')">
      <template #actions>
        <Toolbar class="flex gap-2">
          <Button
            class="w-11 h-11"
            is-router-link
            size="small"
            :to="{ name: 'import-search' }"
            :title="$t('common-actions.import')"
          >
            <span class="sr-only">{{ $t('common-actions.import') }}</span>
            <ArrowDownOnSquareIcon class="w-5 h-5" />
          </Button>
          <Button
            kind="primary"
            is-router-link
            :to="{ name: 'books-new' }"
          >
            <PlusIcon class="w-5 h-5" />
            <span>{{ $t('books.new') }}</span>
          </Button>
        </Toolbar>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <BooksListViewer
        v-model:search="search"
        v-model:sort="sort"
        v-model:page="page"
        v-model:size="size"
        column-order-key="books_column_order"
        column-visibility-key="books_column_visibility"
        view-mode-key="books_view_mode"
        :books="books"
        :loading="isLoading"
      />
    </div>
  </div>
</template>
