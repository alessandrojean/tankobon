<script setup lang="ts">
import type { ColumnOrderState } from '@tanstack/vue-table'
import { MagnifyingGlassIcon } from '@heroicons/vue/20/solid'
import type { TableColumn } from '../TableColumnsControls.vue'
import type { SortPropertyOption } from '../SortControls.vue'
import type { ViewMode } from '@/components/ViewModeSelector.vue'
import type { PaginatedResponse } from '@/types/tankobon-response'
import type { BookEntity, BookSort } from '@/types/tankobon-book'
import type { Sort, SortDirection } from '@/types/tankobon-api'

export interface BooksListViewerProps {
  books?: PaginatedResponse<BookEntity>
  columnOrderKey: string
  columnVisibilityKey: string
  defaultColumnOrder?: ColumnOrderState
  defaultColumnVisibility?: Record<string, boolean>
  loading: boolean
  unpaged?: boolean
  viewModeKey: string
  withSearch?: boolean
}

const props = withDefaults(defineProps<BooksListViewerProps>(), {
  books: undefined,
  defaultColumnOrder: () => ['title', 'collection', 'createdAt'],
  defaultColumnVisibility: () => ({
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
  }),
  unpaged: false,
  withSearch: true,
})

const search = defineModel('search', { default: '' })
const sort = defineModel<Sort<BookSort> | null>('sort', { required: true })
const page = defineModel<number>('page', { default: 0 })
const size = defineModel<number>('size', { default: 20 })

const { t, locale } = useI18n()
const notificator = useToaster()

const searchTerm = refDebounced(search, 500)

const { preference: viewMode } = useUserPreference<ViewMode>(props.viewModeKey, 'grid-comfortable', {
  onError: (error) => {
    notificator.failure({
      title: t('preferences.view-mode-failure'),
      body: error.message,
    })
  },
})

const sortProperties = computed(() => {
  const properties: SortPropertyOption[] = [
    { property: 'title', text: t('common-fields.title') },
    { property: 'createdAt', text: t('common-fields.created-at') },
    { property: 'modifiedAt', text: t('common-fields.modified-at') },
    { property: 'boughtAt', text: t('common-fields.bought-at') },
    { property: 'billedAt', text: t('common-fields.billed-at') },
    { property: 'arrivedAt', text: t('common-fields.arrived-at') },
    { property: 'number', text: t('common-fields.number') },
    { property: 'pageCount', text: t('common-fields.page-count') },
    { property: 'weightKg', text: t('common-fields.weight-kg') },
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
    { id: 'pageCount', text: t('common-fields.page-count') },
  ]

  const disabled = columns.filter(c => c.disabled)
  const enabled = columns.filter(c => !c.disabled)

  return [
    ...disabled,
    ...enabled.sort((a, b) => a.text.localeCompare(b.text, locale.value)),
  ]
})

const { preference: columnVisibility } = useUserPreference<Record<string, boolean>>(props.columnVisibilityKey, props.defaultColumnVisibility)

const { preference: columnOrder } = useUserPreference<ColumnOrderState>(props.columnOrderKey, props.defaultColumnOrder)

function handleSortPropertyChange(newSortProperty: BookSort) {
  sort.value = {
    property: newSortProperty,
    direction: sort.value?.direction ?? 'asc',
  }
}

function handleSortDirectionChange(newSortDirection: SortDirection) {
  sort.value = {
    property: sort.value?.property ?? 'title',
    direction: newSortDirection,
  }
}

function handleSortChange(newSort: Sort<BookSort>[]) {
  sort.value = newSort[0] ?? null
}
</script>

<template>
  <div>
    <ViewControls>
      <div v-if="withSearch">
        <label class="sr-only" for="search-publisher">
          {{ $t('books.search') }}
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

      <div
        :class="[
          'md:ml-auto flex flex-col-reverse sm:flex-row gap-4 items-center',
          'justify-center md:justify-normal',
        ]"
      >
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
            :preference-key="viewModeKey"
            :loading="loading"
          />
        </div>
      </div>
    </ViewControls>

    <FadeTransition>
      <BooksTable
        v-if="viewMode === 'table'"
        v-model:page="page"
        v-model:size="size"
        class="mt-4 sm:mt-6"
        :books="books"
        :loading="loading"
        :search="searchTerm"
        :sort="sort ? [sort] : []"
        :column-visibility="columnVisibility"
        :column-order="columnOrder"
        :unpaged="unpaged"
        @update:sort="handleSortChange"
      />

      <BooksGrid
        v-else
        v-model:page="page"
        v-model:size="size"
        class="mt-4 sm:mt-6"
        :books="books"
        :loading="loading"
        :search="searchTerm"
        :unpaged="unpaged"
        :mode="viewMode === 'grid-comfortable' ? 'comfortable' : 'compact'"
      />
    </FadeTransition>
  </div>
</template>
