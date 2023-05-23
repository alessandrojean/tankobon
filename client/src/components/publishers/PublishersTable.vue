<script lang="ts" setup>
import type { ColumnOrderState, PaginationState, SortingState } from '@tanstack/vue-table'
import { createColumnHelper } from '@tanstack/vue-table'
import { EllipsisHorizontalIcon } from '@heroicons/vue/20/solid'
import { BuildingOffice2Icon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import { BuildingOffice2Icon as BuildingOffice2SolidIcon } from '@heroicons/vue/24/solid'
import Avatar from '../Avatar.vue'
import Flag from '../Flag.vue'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import type { PublisherEntity, PublisherSort } from '@/types/tankobon-publisher'
import type { Sort } from '@/types/tankobon-api'
import { getRelationship } from '@/utils/api'
import { createImageUrl } from '@/modules/api'
import type { PaginatedResponse } from '@/types/tankobon-response'

export interface PublishersTableProps {
  publishers?: PaginatedResponse<PublisherEntity>
  columnOrder?: ColumnOrderState
  columnVisibility?: Record<string, boolean>
  loading?: boolean
  page: number
  search?: string
  size: number
  sort: Sort<PublisherSort>[]
}

const props = withDefaults(defineProps<PublishersTableProps>(), {
  publishers: undefined,
  columnOrder: () => [],
  columnVisibility: () => ({}),
  loading: false,
  search: '',
})

const emit = defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
  (e: 'update:sort', sort: Sort<PublisherSort>[]): void
}>()

const { page, search, size, sort } = toRefs(props)
const { t, d, locale } = useI18n()

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

const columnHelper = createColumnHelper<PublisherEntity>()

const regionNames = computed(() => new Intl.DisplayNames(locale.value, {
  type: 'region',
  style: 'long',
}))

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
      legalName: publisher.attributes.legalName,
      picture: getRelationship(publisher, 'PUBLISHER_PICTURE'),
    }),
    {
      id: 'name',
      header: () => t('common-fields.name'),
      cell: (info) => {
        const { name, legalName, picture } = info.getValue()

        return h('div', { class: 'flex items-center space-x-3' }, [
          h(Avatar, {
            square: true,
            emptyIcon: BuildingOffice2SolidIcon,
            pictureUrl: createImageUrl({
              fileName: picture?.attributes?.versions?.['64'],
              timeHex: picture?.attributes?.timeHex,
            }),
          }),
          h('div', { class: 'flex flex-col' }, [
            h('span', { innerText: name, class: 'font-medium', title: name }),
            legalName ? h('span', { innerText: legalName, class: 'text-xs text-gray-700 dark:text-gray-400' }) : undefined,
          ]),
        ])
      },
      meta: {
        headerClass: 'pl-0',
        cellClass: 'pl-0',
      },
    },
  ),
  columnHelper.accessor('attributes.legalName', {
    id: 'legalName',
    header: () => t('common-fields.legal-name'),
    cell: info => info.getValue() ?? t('publishers.legal-name-unknown'),
  }),
  columnHelper.accessor('attributes.location', {
    id: 'location',
    header: () => t('common-fields.location'),
    cell: (info) => {
      const location = info.getValue()

      return h('div', { class: 'flex items-center gap-3' }, [
        h(Flag, { region: location }),
        h('span', {
          innerText: location
            ? (regionNames.value.of(location) ?? t('location.unknown'))
            : t('location.unknown'),
        }),
      ])
    },
  }),
  columnHelper.accessor('attributes.foundingYear', {
    id: 'foundingYear',
    header: () => t('common-fields.founding-year'),
    cell: info => info.getValue() ?? t('publishers.founding-unknown'),
    meta: { tabular: true },
  }),
  columnHelper.accessor('attributes.dissolutionYear', {
    id: 'dissolutionYear',
    header: () => t('common-fields.dissolution-year'),
    cell: info => info.getValue(),
    meta: { tabular: true },
  }),
  columnHelper.accessor('attributes.createdAt', {
    id: 'createdAt',
    header: () => t('common-fields.created-at'),
    cell: info => d(new Date(info.getValue()), 'dateTime'),
    meta: { tabular: true },
  }),
  columnHelper.accessor('attributes.modifiedAt', {
    id: 'modifiedAt',
    header: () => t('common-fields.modified-at'),
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

function handlePaginationChange(pagination: PaginationState) {
  emit('update:page', pagination.pageIndex)
  emit('update:size', pagination.pageSize)
}

function handleSortingChange(sorting: SortingState) {
  const sortToEmit = sorting.map<Sort<PublisherSort>>(sort => ({
    property: sort.id as PublisherSort,
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
    :data="publishers?.data"
    :columns="columns"
    :page-count="publishers?.pagination?.totalPages"
    :items-count="publishers?.pagination?.totalElements"
    :loading="loading"
    :column-order="['select', ...columnOrder, 'actions']"
    :column-visibility="columnVisibility"
    @update:pagination="handlePaginationChange"
    @update:sorting="handleSortingChange"
  >
    <template #empty>
      <slot name="empty">
        <EmptyState
          :icon="search?.length ? MagnifyingGlassIcon : BuildingOffice2Icon"
          :title="$t('publishers.empty-header')"
          :description="
            search?.length
              ? $t('publishers.empty-search-description', [search])
              : $t('publishers.empty-description')
          "
        >
          <template v-if="!search?.length" #actions>
            <Button
              kind="primary"
              is-router-link
              :to="{ name: 'publishers-new' }"
            >
              <PlusIcon class="w-5 h-5" />
              <span>{{ $t('publishers.new') }}</span>
            </Button>
          </template>
        </EmptyState>
      </slot>
    </template>
  </Table>
</template>
