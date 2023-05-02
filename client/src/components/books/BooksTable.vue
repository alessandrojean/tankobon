<script lang="ts" setup>
import type { ColumnSort, PaginationState, SortingState } from '@tanstack/vue-table'
import { createColumnHelper } from '@tanstack/vue-table'
import { EllipsisHorizontalIcon } from '@heroicons/vue/20/solid'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import type { BookEntity, BookSort } from '@/types/tankobon-book'
import type { Sort } from '@/types/tankobon-api'

export interface BooksTableProps {
  libraryId: string
  search?: string
}

const props = withDefaults(defineProps<BooksTableProps>(), {
  search: undefined,
})
const { libraryId, search } = toRefs(props)
const notificator = useToaster()
const { t, locale } = useI18n()

const defaultSorting: ColumnSort = { id: 'title', desc: false }
const pagination = ref<PaginationState>({ pageIndex: 0, pageSize: 20 })
const sorting = ref<SortingState>([defaultSorting])
const rowSelection = ref<Record<string, boolean>>({})

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

const { data: publishers, isLoading } = useLibraryBooksQuery({
  libraryId,
  search,
  page: computed(() => pagination.value.pageIndex),
  size: computed(() => pagination.value.pageSize),
  sort: computed<Sort<BookSort>[]>(() => {
    return sorting.value.map(sort => ({
      property: sort.id as BookSort,
      direction: sort.desc ? 'desc' : 'asc',
    }))
  }),
  enabled: computed(() => libraryId.value !== undefined),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('publishers.fetch-failure'),
      body: error.message,
    })
  },
})
const columnHelper = createColumnHelper<BookEntity>()

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
  columnHelper.accessor('attributes.title', {
    id: 'title',
    header: () => t('common-fields.book'),
    cell: info => info.getValue(),
  }),
  columnHelper.accessor('attributes.createdAt', {
    id: 'createdAt',
    header: () => t('common-fields.created-at'),
    cell: info => dateFormatter.value.format(new Date(info.getValue())),
    meta: { tabular: true },
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
        to: { name: 'books-id', params: { id: row.original.id } },
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
    :data="publishers?.data"
    :columns="columns"
    :page-count="publishers?.pagination?.totalPages"
    :items-count="publishers?.pagination?.totalElements"
    :loading="isLoading"
  >
    <template #empty>
      <slot name="empty" />
    </template>
  </Table>
</template>
