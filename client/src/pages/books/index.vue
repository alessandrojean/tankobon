<script lang="ts" setup>
import { SortPropertyOption } from '@/components/SortControls.vue'
import type { ViewMode } from '@/components/ViewModeSelector.vue'
import Button from '@/components/form/Button.vue'
import { Sort, SortDirection } from '@/types/tankobon-api'
import { BookSort } from '@/types/tankobon-book'
import { safeNumber } from '@/utils/route'
import { ArrowDownOnSquareIcon, MagnifyingGlassIcon, PlusIcon } from '@heroicons/vue/20/solid'

const libraryStore = useLibraryStore()
const libraryId = computed(() => libraryStore.library!.id)

const search = ref('')
const searchTerm = refDebounced(search, 500)

const { t, locale } = useI18n()
const notificator = useToaster()

useHead({ title: () => t('entities.books') })

const size = useRouteQuery('size', '20', {
  mode: 'push',
  transform: v => safeNumber(v, 20, { min: 10 })
})
const page = useRouteQuery('page', '0', { 
  mode: 'push',
  transform: v => safeNumber(v, 0, { min: 0 })
})
const sortQuery = useRouteQuery<string | null>('sort')

const sort = computed(() => {
  const [property, direction] = sortQuery.value?.split(':') ?? []

  if (!property && !direction) {
    return null
  }

  return {
    property: property as BookSort,
    direction: direction as SortDirection,
  } satisfies Sort<BookSort>
})

const { data: books, isLoading } = useLibraryBooksQuery({
  libraryId,
  search,
  includes: ['cover_art', 'collection'],
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

function handlePageChange(newPage: number) {
  page.value = newPage
}

function handleSizeChange(newSize: number) {
  size.value = newSize
}

function handleSortChange(newSort: Sort<BookSort>[]) {
  sortQuery.value = newSort[0] ? `${newSort[0].property}:${newSort[0].direction}` : null
}

function handleSortPropertyChange(newSortProperty: BookSort) {
  sortQuery.value = `${newSortProperty}:${sort.value?.direction ?? 'asc'}`
}

function handleSortDirectionChange(newSortDirection: SortDirection) {
  sortQuery.value = `${sort.value?.property}:${newSortDirection}`
}

const BOOKS_VIEW_MODE_KEY = 'books_view_mode'

const { data: viewMode } = useUserPreferencesQuery({
  select: (preferences) => {
    return preferences[BOOKS_VIEW_MODE_KEY]
      ? preferences[BOOKS_VIEW_MODE_KEY] as ViewMode
      : 'grid-comfortable'
  },
  initialData: { [BOOKS_VIEW_MODE_KEY]: 'grid-comfortable' },
  onError: async (error) => {
    await notificator.failure({
      title: t('preferences.view-mode-failure'),
      body: error.message,
    })
  },
})

const sortProperties = computed(() => {
  const properties: SortPropertyOption[] = [
    { property: 'title', text: t(`common-fields.title`) },
    { property: 'createdAt', text: t(`common-fields.created-at`) },
    { property: 'modifiedAt', text: t(`common-fields.modified-at`) },
    { property: 'boughtAt', text: t(`common-fields.bought-at`) },
    { property: 'billedAt', text: t(`common-fields.billed-at`) },
    { property: 'arrivedAt', text: t(`common-fields.arrived-at`) },
    { property: 'number', text: t(`common-fields.number`) },
    { property: 'pageCount', text: t(`common-fields.page-count`) },
    { property: 'weightKg', text: t(`common-fields.weight-kg`) },
  ]

  return properties.sort((a, b) => a.text.localeCompare(b.text, locale.value))
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
      <TableControls>
        <div>
          <label class="sr-only" for="search-publisher">
            {{ $t('publishers.search') }}
          </label>
          <BasicTextInput
            id="search-publisher"
            v-model="search"
            class="w-48"
            size="small"
            type="search"
            :placeholder="$t('common-placeholders.search')"
          >
            <template #left-icon>
              <MagnifyingGlassIcon class="w-4 h-4" />
            </template>
          </BasicTextInput>
        </div>

        <SortControls
          class="ml-auto"
          :properties="sortProperties"
          :property="sort?.property"
          :direction="sort?.direction"
          @update:property="handleSortPropertyChange"
          @update:direction="handleSortDirectionChange"
        />

        <ViewModeSelector
          :preference-key="BOOKS_VIEW_MODE_KEY"
          :loading="isLoading"
        />
      </TableControls>

      <FadeTransition>
        <BooksTable
          v-if="viewMode === 'table'"
          class="mt-4 sm:mt-6"
          :books="books"
          :loading="isLoading"
          :search="searchTerm"
          :sort="sort ? [sort] : []"
          :page="page"
          :size="size"
          @update:page="handlePageChange"
          @update:size="handleSizeChange"
          @update:sort="handleSortChange"
        />

        <BooksGrid
          v-else
          class="mt-4 sm:mt-6"
          :books="books"
          :loading="isLoading"
          :search="searchTerm"
          :page="page"
          :size="size"
          :mode="viewMode === 'grid-comfortable' ? 'comfortable' : 'compact'"
          @update:page="handlePageChange"
          @update:size="handleSizeChange"
        />
      </FadeTransition>
    </div>
  </div>
</template>
