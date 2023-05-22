<script lang="ts" setup>
import { SortPropertyOption } from '@/components/SortControls.vue'
import { TableColumn } from '@/components/TableColumnsControls.vue'
import { Sort } from '@/types/tankobon-api'
import { PublisherSort } from '@/types/tankobon-publisher'
import { safeNumber } from '@/utils/route'
import { MagnifyingGlassIcon, PlusIcon } from '@heroicons/vue/20/solid'
import { ColumnOrderState } from '@tanstack/vue-table'
import { SortDirection } from '@tanstack/vue-table'

const libraryStore = useLibraryStore()
const libraryId = computed(() => libraryStore.library!.id)

const search = ref('')
const searchTerm = refDebounced(search, 500)

const { t, locale } = useI18n()
const notificator = useToaster()

useHead({ title: () => t('entities.publishers') })

const size = useRouteQuery('size', '20', {
  mode: 'push',
  transform: v => safeNumber(v, 20, { min: 10 }),
})
const page = useRouteQuery('page', '0', {
  mode: 'push',
  transform: v => safeNumber(v, 0, { min: 0 }),
})
const sortQuery = useRouteQuery<string | null>('sort')

const sort = computed(() => {
  const [property, direction] = sortQuery.value?.split(':') ?? []

  if (!property && !direction) {
    return null
  }

  return {
    property: property as PublisherSort,
    direction: direction as SortDirection,
  } satisfies Sort<PublisherSort>
})

const { data: publishers, isLoading } = useLibraryPublishersQuery({
  libraryId,
  search,
  includes: ['publisher_picture'],
  page,
  size,
  sort: computed(() => sort.value ? [sort.value] : undefined),
  enabled: computed(() => libraryId.value !== undefined),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('series.fetch-failure'),
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

function handleSortChange(newSort: Sort<PublisherSort>[]) {
  sortQuery.value = newSort[0] ? `${newSort[0].property}:${newSort[0].direction}` : null
}

function handleSortPropertyChange(newSortProperty: PublisherSort) {
  sortQuery.value = `${newSortProperty}:${sort.value?.direction ?? 'asc'}`
}

function handleSortDirectionChange(newSortDirection: SortDirection) {
  sortQuery.value = `${sort.value?.property}:${newSortDirection}`
}

const sortProperties = computed(() => {
  const properties: SortPropertyOption[] = [
    { property: 'location', text: t('common-fields.location') },
    { property: 'legalName', text: t('common-fields.legal-name') },
    { property: 'name', text: t('common-fields.name') },
    { property: 'createdAt', text: t('common-fields.created-at') },
    { property: 'modifiedAt', text: t('common-fields.modified-at') },
  ]

  return properties.sort((a, b) => a.text.localeCompare(b.text, locale.value))
})

const tableColumns = computed(() => {
  const columns: TableColumn[] = [
    { id: 'name', text: t('common-fields.name'), disabled: true },
    { id: 'legalName', text: t('common-fields.legal-name') },
    { id: 'location', text: t('common-fields.location') },
    { id: 'createdAt', text: t('common-fields.created-at') },
    { id: 'modifiedAt', text: t('common-fields.modified-at') },
  ]

  const disabled = columns.filter(c => c.disabled)
  const enabled = columns.filter(c => !c.disabled)

  return [
    ...disabled,
    ...enabled.sort((a, b) => a.text.localeCompare(b.text, locale.value)),
  ]
})

const { preference: columnVisibility } = useUserPreference<Record<string, boolean>>('publishers_column_visibility', {
  modifiedAt: false,
  createdAt: true,
  location: true,
  legalName: false,
  name: true,
})

const { preference: columnOrder } = useUserPreference<ColumnOrderState>('publishers_column_order', ['name', 'location', 'createdAt'])
</script>

<route lang="yaml">
meta:
  layout: dashboard
</route>

<template>
  <div>
    <Header
      class="mb-3 md:mb-0"
      :title="$t('entities.publishers')"
    >
      <template #actions>
        <Button
          kind="primary"
          is-router-link
          :to="{ name: 'publishers-new' }"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('publishers.new') }}</span>
        </Button>
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
          <TableColumnsControls
            v-model:column-visibility="columnVisibility"
            v-model:column-order="columnOrder"
            :columns="tableColumns"
          />

          <div class="flex gap-6 items-center justify-between md:justify-normal">
            <SortControls
              :properties="sortProperties"
              :property="sort?.property"
              :direction="sort?.direction"
              @update:property="handleSortPropertyChange"
              @update:direction="handleSortDirectionChange"
            />
          </div>
        </div>
      </ViewControls>

      <PublishersTable
        class="mt-4 sm:mt-6"
        :publishers="publishers"
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
    </div>
  </div>
</template>
