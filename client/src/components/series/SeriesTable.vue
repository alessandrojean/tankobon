<script lang="ts" setup>
import type { ColumnOrderState, PaginationState, SortingState } from '@tanstack/vue-table'
import { createColumnHelper } from '@tanstack/vue-table'
import { EllipsisHorizontalIcon, PlusIcon, Square2StackIcon as Square2StackSolidIcon } from '@heroicons/vue/20/solid'
import { MagnifyingGlassIcon, Square2StackIcon } from '@heroicons/vue/24/outline'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import type { SeriesEntity, SeriesSort } from '@/types/tankobon-series'
import type { Sort } from '@/types/tankobon-api'
import { getRelationship } from '@/utils/api'
import Avatar from '@/components/Avatar.vue'
import { createImageUrl } from '@/modules/api'
import { getLanguageName } from '@/utils/language'
import Flag from '@/components/Flag.vue'
import type { PaginatedResponse } from '@/types/tankobon-response'
import { getOriginalName } from '@/services/tankobon-series'

export interface SeriesTableProps {
  series?: PaginatedResponse<SeriesEntity>
  columnOrder?: ColumnOrderState
  columnVisibility?: Record<string, boolean>
  loading?: boolean
  page: number
  search?: string
  size: number
  sort: Sort<SeriesSort>[]
}

const props = withDefaults(defineProps<SeriesTableProps>(), {
  series: undefined,
  columnOrder: () => [],
  columnVisibility: () => ({}),
  loading: false,
  search: '',
})

const emit = defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
  (e: 'update:sort', sort: Sort<SeriesSort>[]): void
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
  columnHelper.accessor(
    series => ({
      name: series.attributes.name,
      series,
      originalLanguage: series.attributes.originalLanguage,
      cover: getRelationship(series, 'SERIES_COVER'),
    }),
    {
      id: 'name',
      header: () => t('common-fields.name'),
      cell: (info) => {
        const { name, series, originalLanguage, cover } = info.getValue()
        const originalName = getOriginalName(series)

        return h('div', { class: 'flex items-center space-x-3' }, [
          h(Avatar, {
            pictureUrl: createImageUrl({
              fileName: cover?.attributes?.versions?.['256'],
              timeHex: cover?.attributes?.timeHex,
            }),
            square: true,
            emptyIcon: Square2StackSolidIcon,
          }),
          h('div', { class: 'flex flex-col' }, [
            h('span', { innerText: name, class: 'font-medium', title: name }),
            originalName ? h('span', { lang: originalLanguage, innerText: originalName.name, class: 'text-xs text-gray-700 dark:text-gray-400' }) : undefined,
          ]),
        ])
      },
      meta: {
        headerClass: 'pl-0',
        cellClass: 'pl-0',
      },
    },
  ),
  columnHelper.accessor('attributes.type', {
    id: 'type',
    header: () => t('series.type'),
    cell: (info) => {
      const type = info.getValue()
      const typeKey = type ? type.toLowerCase().replace(/_/g, '-') : 'unknown'

      return t(`series-types.${typeKey}`)
    },
    enableSorting: false,
    meta: {
      headerContainerClass: 'justify-end',
      cellClass: 'text-right',
    },
  }),
  columnHelper.accessor('attributes.originalLanguage', {
    id: 'originalLanguage',
    header: () => t('original-language.label'),
    cell: (info) => {
      const originalLanguage = info.getValue()
      const parts = originalLanguage?.split('-') ?? []
      const region = parts[parts.length - 1]

      return h('div', { class: 'flex items-center gap-3' }, [
        h(Flag, { region }),
        h('span', {
          innerText: getLanguageName({
            language: info.getValue(),
            locale: locale.value,
            romanizedLabel: t('original-language.romanized'),
            unknownLabel: t('original-language.unknown'),
          }),
        }),
      ])
    },
    enableSorting: false,
  }),
  columnHelper.accessor('attributes.lastNumber', {
    id: 'lastNumber',
    header: () => t('series.last-number'),
    cell: info => info.getValue(),
    meta: {
      headerContainerClass: 'justify-end',
      cellClass: 'text-right',
      tabular: true,
    },
    enableSorting: false,
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

function handlePaginationChange(pagination: PaginationState) {
  emit('update:page', pagination.pageIndex)
  emit('update:size', pagination.pageSize)
}

function handleSortingChange(sorting: SortingState) {
  const sortToEmit = sorting.map<Sort<SeriesSort>>(sort => ({
    property: sort.id as SeriesSort,
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
    :data="series?.data"
    :columns="columns"
    :page-count="series?.pagination?.totalPages"
    :items-count="series?.pagination?.totalElements"
    :loading="loading"
    :column-order="['select', ...columnOrder, 'actions']"
    :column-visibility="columnVisibility"
    @update:pagination="handlePaginationChange"
    @update:sorting="handleSortingChange"
  >
    <template #empty>
      <slot name="empty">
        <EmptyState
          :icon="search?.length ? MagnifyingGlassIcon : Square2StackIcon"
          :title="$t('series.empty-header')"
          :description="
            search?.length
              ? $t('series.empty-search-description', [search])
              : $t('series.empty-description')
          "
        >
          <template v-if="!search?.length" #actions>
            <Button
              kind="primary"
              is-router-link
              :to="{ name: 'series-new' }"
            >
              <PlusIcon class="w-5 h-5" />
              <span>{{ $t('series.new') }}</span>
            </Button>
          </template>
        </EmptyState>
      </slot>
    </template>
  </Table>
</template>
