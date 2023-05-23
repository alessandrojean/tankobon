<script lang="ts" setup>
import type { ColumnOrderState, PaginationState, SortingState } from '@tanstack/vue-table'
import { createColumnHelper } from '@tanstack/vue-table'
import { BuildingStorefrontIcon as BuildingStorefrontSolidIcon, EllipsisHorizontalIcon, PlusIcon } from '@heroicons/vue/20/solid'
import { BuildingStorefrontIcon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import Avatar from '@/components/Avatar.vue'
import type { BadgeColor } from '@/components/Badge.vue'
import Badge from '@/components/Badge.vue'
import Flag from '@/components/Flag.vue'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import type { Sort } from '@/types/tankobon-api'
import { getRelationship } from '@/utils/api'
import { createImageUrl } from '@/modules/api'
import type { PaginatedResponse } from '@/types/tankobon-response'
import type { StoreEntity, StoreSort, StoreType } from '@/types/tankobon-store'

export interface PublishersTableProps {
  stores?: PaginatedResponse<StoreEntity>
  columnOrder?: ColumnOrderState
  columnVisibility?: Record<string, boolean>
  loading?: boolean
  page: number
  search?: string
  size: number
  sort: Sort<StoreSort>[]
}

const props = withDefaults(defineProps<PublishersTableProps>(), {
  stores: undefined,
  columnOrder: () => [],
  columnVisibility: () => ({}),
  loading: false,
  search: '',
})

const emit = defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
  (e: 'update:sort', sort: Sort<StoreSort>[]): void
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

const columnHelper = createColumnHelper<StoreEntity>()

const regionNames = computed(() => new Intl.DisplayNames(locale.value, {
  type: 'region',
  style: 'long',
}))

const typeBadge: Record<StoreType, BadgeColor> = {
  COMIC_SHOP: 'blue',
  BOOKSTORE: 'green',
  NEWSSTAND: 'purple',
  RETAIL_CHAIN: 'indigo',
}

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
    store => ({
      name: store.attributes.name,
      picture: getRelationship(store, 'STORE_PICTURE'),
    }),
    {
      id: 'name',
      header: () => t('common-fields.name'),
      cell: (info) => {
        const { name, picture } = info.getValue()

        return h('div', { class: 'flex items-center space-x-3' }, [
          h(Avatar, {
            square: true,
            emptyIcon: BuildingStorefrontSolidIcon,
            pictureUrl: createImageUrl({
              fileName: picture?.attributes?.versions?.['64'],
              timeHex: picture?.attributes?.timeHex,
            }),
            size: 'sm',
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
  columnHelper.accessor('attributes.type', {
    id: 'type',
    header: () => t('series.type'),
    cell: (info) => {
      const type = info.getValue()
      const typeKey = type ? type.toLowerCase().replace(/_/g, '-') : 'unknown'

      return h(Badge, {
        color: type ? (typeBadge[type] ?? 'gray') : 'gray',
        innerText: t(`stores-types.${typeKey}`),
      })
    },
    enableSorting: false,
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
        to: { name: 'stores-id', params: { id: row.original.id } },
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
  const sortToEmit = sorting.map<Sort<StoreSort>>(sort => ({
    property: sort.id as StoreSort,
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
    :data="stores?.data"
    :columns="columns"
    :page-count="stores?.pagination?.totalPages"
    :items-count="stores?.pagination?.totalElements"
    :loading="loading"
    :column-order="['select', ...columnOrder, 'actions']"
    :column-visibility="columnVisibility"
    @update:pagination="handlePaginationChange"
    @update:sorting="handleSortingChange"
  >
    <template #empty>
      <slot name="empty">
        <EmptyState
          :icon="search?.length ? MagnifyingGlassIcon : BuildingStorefrontIcon"
          :title="$t('stores.empty-header')"
          :description="
            search?.length
              ? $t('stores.empty-search-description', [search])
              : $t('stores.empty-description')
          "
        >
          <template v-if="!search?.length" #actions>
            <Button
              kind="primary"
              is-router-link
              :to="{ name: 'stores-new' }"
            >
              <PlusIcon class="w-5 h-5" />
              <span>{{ $t('stores.new') }}</span>
            </Button>
          </template>
        </EmptyState>
      </slot>
    </template>
  </Table>
</template>
