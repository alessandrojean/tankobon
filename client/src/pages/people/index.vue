<script lang="ts" setup>
import { MagnifyingGlassIcon, PlusIcon } from '@heroicons/vue/20/solid'
import type { ColumnOrderState, SortDirection } from '@tanstack/vue-table'
import type { SortPropertyOption } from '@/components/SortControls.vue'
import type { TableColumn } from '@/components/TableColumnsControls.vue'
import type { Sort } from '@/types/tankobon-api'
import { safeNumber } from '@/utils/route'
import type { PersonSort } from '@/types/tankobon-person'

const libraryStore = useLibraryStore()
const libraryId = computed(() => libraryStore.library!.id)

const search = ref('')
const searchTerm = refDebounced(search, 500)

const { t, locale } = useI18n()
const notificator = useToaster()

useHead({ title: () => t('entities.people') })

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
    property: property as PersonSort,
    direction: direction as SortDirection,
  } satisfies Sort<PersonSort>
})

const { data: people, isLoading } = useLibraryPeopleQuery({
  libraryId,
  search,
  includes: ['person_picture'],
  page,
  size,
  sort: computed(() => sort.value ? [sort.value] : undefined),
  enabled: computed(() => libraryId.value !== undefined),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('people.fetch-failure'),
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

function handleSortChange(newSort: Sort<PersonSort>[]) {
  sortQuery.value = newSort[0] ? `${newSort[0].property}:${newSort[0].direction}` : null
}

function handleSortPropertyChange(newSortProperty: PersonSort) {
  sortQuery.value = `${newSortProperty}:${sort.value?.direction ?? 'asc'}`
}

function handleSortDirectionChange(newSortDirection: SortDirection) {
  sortQuery.value = `${sort.value?.property}:${newSortDirection}`
}

const sortProperties = computed(() => {
  const properties: SortPropertyOption[] = [
    { property: 'nativeName', text: t('common-fields.native-name') },
    { property: 'name', text: t('common-fields.name') },
    { property: 'bornAt', text: t('common-fields.born-at') },
    { property: 'diedAt', text: t('common-fields.died-at') },
    { property: 'createdAt', text: t('common-fields.created-at') },
    { property: 'modifiedAt', text: t('common-fields.modified-at') },
  ]

  return properties.sort((a, b) => a.text.localeCompare(b.text, locale.value))
})

const tableColumns = computed(() => {
  const columns: TableColumn[] = [
    { id: 'name', text: t('common-fields.name'), disabled: true },
    { id: 'nativeName', text: t('common-fields.native-name') },
    { id: 'nationality', text: t('common-fields.nationality') },
    { id: 'bornAt', text: t('common-fields.born-at') },
    { id: 'diedAt', text: t('common-fields.died-at') },
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

const { preference: columnVisibility } = useUserPreference<Record<string, boolean>>('people_column_visibility', {
  modifiedAt: false,
  createdAt: true,
  nationality: true,
  nativeName: false,
  name: true,
  bornAt: false,
  diedAt: false,
})

const { preference: columnOrder } = useUserPreference<ColumnOrderState>('people_column_order', ['name', 'nationality', 'createdAt'])
</script>

<route lang="yaml">
meta:
  layout: dashboard
</route>

<template>
  <div>
    <Header
      class="mb-3 md:mb-0"
      :title="$t('entities.people')"
    >
      <template #actions>
        <Button
          kind="primary"
          is-router-link
          :to="{ name: 'people-new' }"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('people.new') }}</span>
        </Button>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <ViewControls>
        <div>
          <label class="sr-only" for="search-people">
            {{ $t('people.search') }}
          </label>
          <BasicTextInput
            id="search-people"
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

      <PeopleTable
        class="mt-4 sm:mt-6"
        :people="people"
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
