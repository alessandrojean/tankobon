<script lang="ts" setup>
import { PlusIcon } from '@heroicons/vue/20/solid'
import type { ColumnOrderState, SortDirection } from '@tanstack/vue-table'
import type { UserSort } from '@/types/tankobon-user'
import { safeNumber } from '@/utils/route'
import type { Sort } from '@/types/tankobon-api'
import type { SortPropertyOption } from '@/components/SortControls.vue'
import type { TableColumn } from '@/components/TableColumnsControls.vue'

const libraryStore = useLibraryStore()
const libraryId = computed(() => libraryStore.library!.id)

const { t, locale } = useI18n()
const notificator = useToaster()

useHead({ title: () => t('entities.users') })

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
    property: property as UserSort,
    direction: direction as SortDirection,
  } satisfies Sort<UserSort>
})

const { data: users, isLoading } = useUsersQuery({
  includes: ['avatar'],
  page,
  size,
  sort: computed(() => sort.value ? [sort.value] : undefined),
  enabled: computed(() => libraryId.value !== undefined),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('users.fetch-failure'),
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

function handleSortChange(newSort: Sort<UserSort>[]) {
  sortQuery.value = newSort[0] ? `${newSort[0].property}:${newSort[0].direction}` : null
}

function handleSortPropertyChange(newSortProperty: UserSort) {
  sortQuery.value = `${newSortProperty}:${sort.value?.direction ?? 'asc'}`
}

function handleSortDirectionChange(newSortDirection: SortDirection) {
  sortQuery.value = `${sort.value?.property}:${newSortDirection}`
}

const sortProperties = computed(() => {
  const properties: SortPropertyOption[] = [
    { property: 'name', text: t('common-fields.name') },
    { property: 'createdAt', text: t('common-fields.created-at') },
    { property: 'modifiedAt', text: t('common-fields.modified-at') },
  ]

  return properties.sort((a, b) => a.text.localeCompare(b.text, locale.value))
})

const tableColumns = computed(() => {
  const columns: TableColumn[] = [
    { id: 'name', text: t('common-fields.name'), disabled: true },
    { id: 'email', text: t('common-fields.email') },
    { id: 'role', text: t('common-fields.role') },
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

const { preference: columnVisibility } = useUserPreference<Record<string, boolean>>('users_column_visibility', {
  modifiedAt: false,
  createdAt: true,
  email: false,
  name: true,
  role: true,
})

const { preference: columnOrder } = useUserPreference<ColumnOrderState>('users_column_order', ['name', 'role', 'createdAt'])
</script>

<template>
  <div>
    <Header
      :title="$t('users.header')"
      class="mb-3 md:mb-0"
    >
      <template #actions>
        <Button
          kind="primary"
          is-router-link
          :to="{ name: 'users-new' }"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('users.new') }}</span>
        </Button>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <ViewControls>
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

      <UsersTable
        class="mt-4 sm:mt-6"
        :users="users"
        :loading="isLoading"
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

<route lang="yaml">
meta:
  layout: dashboard
  isAdminOnly: true
</route>
