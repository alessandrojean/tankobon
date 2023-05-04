<script lang="ts" setup>
import type { ColumnSort, PaginationState, SortingState } from '@tanstack/vue-table'
import { createColumnHelper } from '@tanstack/vue-table'
import { BuildingOffice2Icon, EllipsisHorizontalIcon } from '@heroicons/vue/20/solid'
import Avatar from '../Avatar.vue'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import type { PublisherEntity, PublisherSort } from '@/types/tankobon-publisher'
import type { Sort } from '@/types/tankobon-api'
import { getRelationship } from '@/utils/api'
import { getFullImageUrl } from '@/modules/api'

export interface PublishersTableProps {
  libraryId: string
  search?: string
}

const props = withDefaults(defineProps<PublishersTableProps>(), {
  search: undefined,
})
const { libraryId, search } = toRefs(props)
const notificator = useToaster()
const { t } = useI18n()

const defaultSorting: ColumnSort = { id: 'name', desc: false }
const pagination = ref<PaginationState>({ pageIndex: 0, pageSize: 20 })
const sorting = ref<SortingState>([defaultSorting])
const rowSelection = ref<Record<string, boolean>>({})

const { data: publishers, isLoading } = useLibraryPublishersQuery({
  libraryId,
  search,
  includes: ['publisher_picture'],
  page: computed(() => pagination.value.pageIndex),
  size: computed(() => pagination.value.pageSize),
  sort: computed<Sort<PublisherSort>[]>(() => {
    return sorting.value.map(sort => ({
      property: sort.id as PublisherSort,
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
const columnHelper = createColumnHelper<PublisherEntity>()

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
    publisher => ({
      name: publisher.attributes.name,
      picture: getRelationship(publisher, 'PUBLISHER_PICTURE'),
    }),
    {
      id: 'name',
      header: () => t('common-fields.name'),
      cell: (info) => {
        const { name, picture } = info.getValue()

        return h('div', { class: 'flex items-center space-x-3' }, [
          h(Avatar, {
            square: true,
            emptyIcon: BuildingOffice2Icon,
            pictureUrl: getFullImageUrl({
              collection: 'publishers',
              fileName: picture?.attributes?.versions?.['64'],
              timeHex: picture?.attributes?.timeHex,
            }),
            size: 'mini',
          }),
          h('span', { innerText: name }),
        ])
      },
      meta: {
        headerClass: 'pl-0',
        cellClass: 'pl-0',
      },
    },
  ),
  columnHelper.accessor('attributes.description', {
    id: 'description',
    enableSorting: false,
    header: () => t('common-fields.description'),
    cell: info => h('div', { class: 'line-clamp-2', innerText: info.getValue() }),
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
        to: { name: 'publishers-id', params: { id: row.original.id } },
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
