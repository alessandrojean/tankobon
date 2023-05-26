<script lang="ts" setup>
import type { ColumnOrderState, PaginationState, SortingState } from '@tanstack/vue-table'
import { createColumnHelper } from '@tanstack/vue-table'
import { IdentificationIcon } from '@heroicons/vue/24/outline'
import type { Sort } from '@/types/tankobon-api'
import Badge from '@/components/Badge.vue'
import type {
  AuthenticationActivityEntity,
  AuthenticationActivitySort,
} from '@/types/tankobon-authentication-activity'
import type { PaginatedResponse } from '@/types/tankobon-response'

export interface UserAuthenticationActivityTableProps {
  authenticationActivity?: PaginatedResponse<AuthenticationActivityEntity>
  columnOrder?: ColumnOrderState
  columnVisibility?: Record<string, boolean>
  loading?: boolean
  page: number
  size: number
  sort: Sort<AuthenticationActivitySort>[]
}

const props = withDefaults(defineProps<UserAuthenticationActivityTableProps>(), {
  authenticationActivity: undefined,
  columnOrder: () => [],
  columnVisibility: () => ({}),
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
  (e: 'update:sort', sort: Sort<AuthenticationActivitySort>[]): void
}>()

const { page, size, sort, columnVisibility } = toRefs(props)
const { t, d } = useI18n()

const pagination = computed<PaginationState>(() => ({
  pageIndex: page.value,
  pageSize: size.value,
}))

const sorting = computed<SortingState>(() => {
  return sort.value.map(sorting => ({
    id: sorting.property,
    desc: sorting.direction === 'desc',
  }))
})

const rowSelection = ref<Record<string, boolean>>({})

const columnHelper = createColumnHelper<AuthenticationActivityEntity>()

const columns = computed(() => [
  columnHelper.accessor('attributes.ip', {
    id: 'ip',
    header: () => t('authentication-activity.ip'),
    cell: info => info.getValue(),
    meta: { tabular: true },
  }),
  columnHelper.accessor('attributes.success', {
    id: 'status',
    header: () => t('authentication-activity.status'),
    cell: info => h(
      Badge,
      { color: info.getValue() ? 'green' : 'red', class: '!font-medium' },
      { default: () => info.getValue() ? t('authentication-activity.success') : t('authentication-activity.error') },
    ),
    meta: {
      headerContainerClass: 'justify-center',
      cellClass: 'text-center',
    },
  }),
  columnHelper.accessor('attributes.source', {
    id: 'source',
    header: () => t('authentication-activity.source'),
    cell: info => info.getValue(),
  }),
  columnHelper.accessor('attributes.error', {
    id: 'error',
    enableSorting: false,
    header: () => t('authentication-activity.error'),
    cell: info => info.getValue(),
    meta: { cellClass: 'font-mono text-xs' },
  }),
  columnHelper.accessor('attributes.timestamp', {
    id: 'timestamp',
    header: () => t('common-fields.timestamp'),
    cell: info => d(new Date(info.getValue()), 'dateTime'),
    meta: { tabular: true },
  }),
  columnHelper.accessor('attributes.userAgent', {
    id: 'userAgent',
    header: () => t('authentication-activity.user-agent'),
    cell: info => info.getValue(),
  }),
])

function handlePaginationChange(pagination: PaginationState) {
  emit('update:page', pagination.pageIndex)
  emit('update:size', pagination.pageSize)
}

function handleSortingChange(sorting: SortingState) {
  const sortToEmit = sorting.map<Sort<AuthenticationActivitySort>>(sort => ({
    property: sort.id as AuthenticationActivitySort,
    direction: sort.desc ? 'desc' : 'asc',
  }))

  emit('update:sort', sortToEmit)
}
</script>

<template>
  <Table
    v-model:row-selection="rowSelection"
    :pagination="pagination"
    :sorting="sorting"
    :data="authenticationActivity?.data"
    :columns="columns"
    :page-count="authenticationActivity?.pagination?.totalPages"
    :items-count="authenticationActivity?.pagination?.totalElements"
    :loading="loading"
    :column-order="columnOrder"
    :column-visibility="columnVisibility"
    @update:pagination="handlePaginationChange"
    @update:sorting="handleSortingChange"
  >
    <template #empty>
      <EmptyState
        :icon="IdentificationIcon"
        :title="$t('authentication-activity.empty-header')"
        :description="$t('authentication-activity.empty-description')"
      />
    </template>
  </Table>
</template>
