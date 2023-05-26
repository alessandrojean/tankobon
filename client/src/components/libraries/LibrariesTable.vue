<script lang="ts" setup>
import type { ColumnOrderState, PaginationState, SortingState } from '@tanstack/vue-table'
import { createColumnHelper } from '@tanstack/vue-table'
import { EllipsisHorizontalIcon, PlusIcon } from '@heroicons/vue/20/solid'
import { BuildingLibraryIcon } from '@heroicons/vue/24/outline'
import Badge from '@/components/Badge.vue'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import type { LibraryEntity, LibrarySort } from '@/types/tankobon-library'
import { getRelationship } from '@/utils/api'
import type { PaginatedResponse } from '@/types/tankobon-response'
import type { Sort } from '@/types/tankobon-api'

export interface LibrariesTableProps {
  libraries?: PaginatedResponse<LibraryEntity>
  columnOrder?: ColumnOrderState
  columnVisibility?: Record<string, boolean>
  loading?: boolean
  page: number
  size: number
  showNewButton?: boolean
  sort: Sort<LibrarySort>[]
}

const props = withDefaults(defineProps<LibrariesTableProps>(), {
  libraries: undefined,
  columnOrder: () => [],
  columnVisibility: () => ({}),
  loading: false,
  showNewButton: true,
})

const emit = defineEmits<{
  (e: 'click:new'): void
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
  (e: 'update:sort', sort: Sort<LibrarySort>[]): void
}>()

const { page, size, sort, columnVisibility } = toRefs(props)
const { t, d } = useI18n()

const userStore = useUserStore()
const userId = computed(() => userStore.me?.id)

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

const columnHelper = createColumnHelper<LibraryEntity>()

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
  columnHelper.accessor(
    library => getRelationship(library, 'OWNER')?.id !== userId.value,
    {
      id: 'ownership',
      enableSorting: false,
      header: () => t('common-fields.ownership'),
      cell: info => h(
        Badge,
        { color: info.getValue() ? 'red' : 'blue' },
        { default: () => info.getValue() ? t('libraries.owner-shared') : t('libraries.owner-self') },
      ),
      meta: {
        cellClass: 'text-right',
        headerContainerClass: 'justify-end',
      },
    },
  ),
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
        to: { name: 'libraries-id', params: { id: row.original.id } },
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
  const sortToEmit = sorting.map<Sort<LibrarySort>>(sort => ({
    property: sort.id as LibrarySort,
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
    :data="libraries?.data"
    :columns="columns"
    :page-count="libraries?.pagination?.totalPages"
    :items-count="libraries?.pagination?.totalElements"
    :loading="loading"
    :column-order="['select', ...columnOrder, 'actions']"
    :column-visibility="columnVisibility"
    @update:pagination="handlePaginationChange"
    @update:sorting="handleSortingChange"
  >
    <template #empty>
      <slot name="empty">
        <EmptyState
          :icon="BuildingLibraryIcon"
          :title="$t('libraries.empty-header')"
          :description="$t('libraries.empty-description')"
        >
          <template #actions>
            <Button
              v-if="showNewButton"
              kind="primary"
              @click="$emit('click:new')"
            >
              <PlusIcon class="w-5 h-5" />
              <span>{{ $t('libraries.new') }}</span>
            </Button>
          </template>
        </EmptyState>
      </slot>
    </template>
  </Table>
</template>
