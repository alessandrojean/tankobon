<script lang="ts" setup>
import { SortPropertyOption } from '@/components/SortControls.vue'
import { TableColumn } from '@/components/TableColumnsControls.vue'
import type { ViewMode } from '@/components/ViewModeSelector.vue'
import Button from '@/components/form/Button.vue'
import { Sort, SortDirection } from '@/types/tankobon-api'
import { BookSort } from '@/types/tankobon-book'
import { safeNumber } from '@/utils/route'
import { ArrowDownOnSquareIcon, MagnifyingGlassIcon, PlusIcon } from '@heroicons/vue/20/solid'
import { ColumnOrderState } from '@tanstack/vue-table'

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

const { preference: viewMode } = useUserPreference<ViewMode>(BOOKS_VIEW_MODE_KEY, 'grid-comfortable', {
  onError: (error) => {
    notificator.failure({
      title: t('preferences.view-mode-failure'),
      body: error.message,
    })
  }
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

const tableColumns = computed(() => {
  const columns: TableColumn[] = [
    { id: 'title', text: t('common-fields.title'), disabled: true },
    { id: 'collection', text: t('common-fields.collection') },
    { id: 'series', text: t('common-fields.series') },
    { id: 'boughtAt', text: t('common-fields.bought-at') },
    { id: 'billedAt', text: t('common-fields.billed-at') },
    { id: 'arrivedAt', text: t('common-fields.arrived-at') },
    { id: 'createdAt', text: t('common-fields.created-at') },
    { id: 'modifiedAt', text: t('common-fields.modified-at') },
    { id: 'weightKg', text: t('common-fields.weight-kg') },
    { id: 'publishers', text: t('entities.publishers') },
    { id: 'number', text: t('common-fields.number') },
    { id: 'pageCount', text: t('common-fields.page-count') }
  ]

  const disabled = columns.filter(c => c.disabled)
  const enabled = columns.filter(c => !c.disabled)

  return [
    ...disabled,
    ...enabled.sort((a, b) => a.text.localeCompare(b.text, locale.value))
  ]
})

const { preference: columnVisibility } = useUserPreference<Record<string, boolean>>('books_column_visibility', {
  collection: true,
  series: false,
  createdAt: true,
  modifiedAt: false,
  boughtAt: false,
  billedAt: false,
  arrivedAt: false,
  weightKg: false,
  publishers: false,
  title: true,
  number: false,
  pageCount: false,
})

const { preference: columnOrder } = useUserPreference<ColumnOrderState>('books_column_order', ['title', 'collection', 'createdAt'])
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
      <ViewControls>
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
        
        <div class="md:ml-auto flex flex-col-reverse sm:flex-row gap-4 items-center justify-center md:justify-normal">
          <FadeTransition>
            <TableColumnsControls
              v-if="viewMode === 'table'"
              v-model:column-visibility="columnVisibility"
              v-model:column-order="columnOrder"
              :columns="tableColumns"
            />
          </FadeTransition>

          <div class="flex gap-6 items-center justify-between md:justify-normal">
            <SortControls
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
          </div>
        </div>
      </ViewControls>

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
          :column-visibility="columnVisibility"
          :column-order="columnOrder"
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
