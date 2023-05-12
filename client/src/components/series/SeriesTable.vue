<script lang="ts" setup>
import type { ColumnSort, PaginationState, SortingState } from '@tanstack/vue-table'
import { createColumnHelper } from '@tanstack/vue-table'
import { EllipsisHorizontalIcon } from '@heroicons/vue/20/solid'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import type { SeriesEntity, SeriesSort } from '@/types/tankobon-series'
import type { Sort } from '@/types/tankobon-api'

export interface SeriesTableProps {
  libraryId: string
  search?: string
}

const props = withDefaults(defineProps<SeriesTableProps>(), {
  search: undefined,
})
const { libraryId, search } = toRefs(props)
const notificator = useToaster()
const { t } = useI18n()

const defaultSorting: ColumnSort = { id: 'name', desc: false }
const pagination = ref<PaginationState>({ pageIndex: 0, pageSize: 20 })
const sorting = ref<SortingState>([defaultSorting])
const rowSelection = ref<Record<string, boolean>>({})

const { data: series, isLoading } = useLibrarySeriesQuery({
  libraryId,
  search,
  page: computed(() => pagination.value.pageIndex),
  size: computed(() => pagination.value.pageSize),
  sort: computed<Sort<SeriesSort>[]>(() => {
    return sorting.value.map(sort => ({
      property: sort.id as SeriesSort,
      direction: sort.desc ? 'desc' : 'asc',
    }))
  }),
  enabled: computed(() => libraryId.value !== undefined),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('series.fetch-failure'),
      body: error.message,
    })
  },
})
const columnHelper = createColumnHelper<SeriesEntity>()

const columns = [
  columnHelper.display({
    id: 'select',
    header: ({ table }) => h(BasicCheckbox, {
      disabled: table.getRowModel().rows.length === 0,
      checked: table.getIsAllRowsSelected(),
      indeterminate: table.getIsSomeRowsSelected(),
      onChange: table.getToggleAllRowsSelectedHandler(),
    }),
    cell: ({ row }) => h('div', { class: 'flex items-center w-full h-full' }, [
      h(BasicCheckbox, {
        checked: row.getIsSelected(),
        disabled: !row.getCanSelect(),
        indeterminate: row.getIsSomeSelected(),
        onChange: row.getToggleSelectedHandler(),
      }),
    ]),
    meta: {
      headerClass: 'w-12',
      cellClass: 'align-middle',
    },
  }),
  columnHelper.accessor('attributes.name', {
    id: 'name',
    header: () => t('common-fields.name'),
    cell: info => info.getValue(),
  }),
  columnHelper.accessor('attributes.type', {
    id: 'type',
    header: () => t('series.type'),
    cell: (info) => {
      const type = info.getValue()
      const typeKey = type ? type.toLowerCase().replace(/_/g, '-') : 'unknown'

      return t(`series-types.${typeKey}`)
    },
    meta: {
      headerContainerClass: 'justify-end',
      cellClass: 'text-right',
    },
  }),
  columnHelper.display({
    id: 'actions',
    header: () => null,
    cell: ({ row }) => h(
      Button,
      {
        kind: 'ghost-alt',
        isRouterLink: true,
        class: 'w-10 h-10',
        to: { name: 'series-id', params: { id: row.original.id } },
      },
      {
        default: () => [
          h('span', { class: 'sr-only', text: () => t('common-actions.view-details') }),
          h(EllipsisHorizontalIcon, { class: 'w-5 h-5' }),
        ],
      },
    ),
    meta: {
      headerClass: 'w-12',
    },
  }),
]
</script>

<template>
  <Table
    v-model:pagination="pagination"
    v-model:row-selection="rowSelection"
    v-model:sorting="sorting"
    :data="series?.data"
    :columns="columns"
    :page-count="series?.pagination?.totalPages"
    :items-count="series?.pagination?.totalElements"
    :loading="isLoading"
  >
    <template #empty>
      <slot name="empty" />
    </template>
  </Table>
</template>
