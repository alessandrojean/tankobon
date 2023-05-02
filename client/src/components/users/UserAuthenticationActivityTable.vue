<script lang="ts" setup>
import {
  type ColumnSort,
  type PaginationState,
  type SortingState,
  createColumnHelper,
} from '@tanstack/vue-table'
import { IdentificationIcon } from '@heroicons/vue/24/outline'
import type { Sort } from '@/types/tankobon-api'
import Badge from '@/components/Badge.vue'
import type {
  AuthenticationActivityEntity,
  AuthenticationActivitySort,
} from '@/types/tankobon-authentication-activity'

export interface UserAuthenticationActivityTableProps {
  userId: string
}

const props = defineProps<UserAuthenticationActivityTableProps>()
const { userId } = toRefs(props)
const notificator = useToaster()
const { t, locale } = useI18n()

const defaultSorting: ColumnSort = { id: 'timestamp', desc: true }
const pagination = ref<PaginationState>({ pageIndex: 0, pageSize: 10 })
const sorting = ref<SortingState>([defaultSorting])
const rowSelection = ref<Record<string, boolean>>({})

const { data: authenticationActivity, isLoading } = useUserAuthenticationActivityQuery({
  userId,
  page: computed(() => pagination.value.pageIndex),
  size: computed(() => pagination.value.pageSize),
  sort: computed<Sort<AuthenticationActivitySort>[]>(() => {
    return sorting.value.map(sort => ({
      property: sort.id as AuthenticationActivitySort,
      direction: sort.desc ? 'desc' : 'asc',
    }))
  }),
  onError: async (error) => {
    await notificator.failure({
      title: t('authentication-activity.fetch-failure'),
      body: error.message,
    })
  },
})

const columnHelper = createColumnHelper<AuthenticationActivityEntity>()
const dateFormatter = computed(() => {
  return new Intl.DateTimeFormat(locale.value, {
    day: '2-digit',
    month: '2-digit',
    year: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
})

const columns = [
  columnHelper.accessor('attributes.ip', {
    id: 'ip',
    header: () => t('authentication-activity.ip'),
    cell: info => info.getValue(),
    meta: { tabular: true },
  }),
  columnHelper.accessor('attributes.success', {
    id: 'success',
    header: () => t('authentication-activity.success'),
    cell: info => h(
      Badge,
      { color: info.getValue() ? 'green' : 'red', class: '!font-medium' },
      { default: () => info.getValue() ? t('common-values.true-value') : t('common-values.false-value') },
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
    header: () => t('authentication-activity.error'),
    cell: info => info.getValue(),
    meta: { cellClass: 'font-mono text-xs' },
  }),
  columnHelper.accessor('attributes.timestamp', {
    id: 'timestamp',
    header: () => t('common-fields.timestamp'),
    cell: info => dateFormatter.value.format(new Date(info.getValue())),
    meta: { tabular: true },
  }),
]
</script>

<template>
  <Table
    v-model:pagination="pagination"
    v-model:row-selection="rowSelection"
    v-model:sorting="sorting"
    :data="authenticationActivity?.data"
    :columns="columns"
    :page-count="authenticationActivity?.pagination?.totalPages"
    :items-count="authenticationActivity?.pagination?.totalElements"
    :loading="isLoading"
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
