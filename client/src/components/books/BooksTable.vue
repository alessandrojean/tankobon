<script lang="ts" setup>
import type { PaginationState, SortingState } from '@tanstack/vue-table'
import { createColumnHelper } from '@tanstack/vue-table'
import { EllipsisHorizontalIcon } from '@heroicons/vue/20/solid'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import type { BookEntity, BookSort } from '@/types/tankobon-book'
import type { Sort } from '@/types/tankobon-api'
import { BookOpenIcon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import { PaginatedResponse } from '@/types/tankobon-response'
import { getRelationship } from '@/utils/api'
import Avatar from '../Avatar.vue'
import { createImageUrl } from '@/modules/api'

export interface BooksTableProps {
  books?: PaginatedResponse<BookEntity>
  loading?: boolean
  page: number
  search?: string
  size: number
  sort: Sort<BookSort>[]
}

const props = withDefaults(defineProps<BooksTableProps>(), {
  books: undefined,
  loading: false,
  search: '',
})

const emit = defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
  (e: 'update:sort', sort: Sort<BookSort>[]): void
}>()

const { page, search, size, sort } = toRefs(props)
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
  columnHelper.accessor(
    book => ({
      title: book.attributes.title,
      subtitle: book.attributes.subtitle,
      coverArt: getRelationship(book, 'COVER_ART'),
    }),
    {
      id: 'title',
      header: () => t('common-fields.title'),
      cell: (info) => {
        const { title, subtitle, coverArt } = info.getValue()

        return h('div', { class: 'flex items-center space-x-3' }, [
          h(Avatar, {
            pictureUrl: createImageUrl({
              fileName: coverArt?.attributes?.versions?.['256'],
              timeHex: coverArt?.attributes?.timeHex,
            }),
            square: true,
            emptyIcon: BookOpenIcon,
          }),
          h('div', { class: 'flex flex-col' }, [
            h('span', { innerText: title, class: 'font-medium' }),
            subtitle.length > 0 ? h('span', { innerText: subtitle, class: 'text-xs text-gray-700 dark:text-gray-400' }) : undefined,
          ])
        ])
      },
      meta: {
        headerClass: 'pl-0',
        cellClass: 'pl-0',
      },
    },
  ),
  columnHelper.accessor(book => getRelationship(book, 'COLLECTION'), {
    id: 'collection',
    header: () => t('common-fields.collection'),
    cell: info => info.getValue()?.attributes?.name,
    meta: {
      headerContainerClass: 'justify-end',
      cellClass: 'text-right',
    },
  }),
  columnHelper.accessor('attributes.createdAt', {
    id: 'createdAt',
    header: () => t('common-fields.created-at'),
    cell: info => d(new Date(info.getValue()), 'dateTime'),
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

function handlePaginationChange(pagination: PaginationState) {
  emit('update:page', pagination.pageIndex)
  emit('update:size', pagination.pageSize)
}

function handleSortingChange(sorting: SortingState) {
  const sortToEmit = sorting.map<Sort<BookSort>>(sort => ({
    property: sort.id as BookSort,
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
    :data="books?.data"
    :columns="columns"
    :page-count="books?.pagination?.totalPages"
    :items-count="books?.pagination?.totalElements"
    :loading="loading"
    @update:pagination="handlePaginationChange"
    @update:sorting="handleSortingChange"
  >
    <template #empty>
      <slot name="empty">
        <EmptyState
            :icon="search?.length ? MagnifyingGlassIcon : BookOpenIcon"
            :title="$t('books.empty-header')"
            :description="
              search?.length
                ? $t('books.empty-search-description', [search])
                : $t('books.empty-description')
            "
          >
            <template v-if="!search?.length" #actions>
              <Button
                kind="primary"
                is-router-link
                :to="{ name: 'books-new' }"
              >
                <PlusIcon class="w-5 h-5" />
                <span>{{ $t('books.new') }}</span>
              </Button>
            </template>
          </EmptyState>
      </slot>
    </template>
  </Table>
</template>
