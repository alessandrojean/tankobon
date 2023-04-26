<script lang="ts" setup>
import {
  FlexRender,
  getCoreRowModel,
  useVueTable,
  type SortingState,
  type PaginationState,
  type ColumnDef,
} from '@tanstack/vue-table'
import { ArrowSmallDownIcon, ArrowSmallUpIcon } from '@heroicons/vue/20/solid'

export interface TableProps {
  data?: any[],
  columns: ColumnDef<any, any>[],
  pageCount?: number,
  itemsCount?: number,
  pagination?: PaginationState,
  rowSelection?: Record<string, boolean>,
  sorting?: SortingState,
  loading?: boolean,
}

export type TableEmits = {
  (e: 'update:pagination', pagination: PaginationState): void,
  (e: 'update:row-selection', rowSelection: Record<string, boolean>): void,
  (e: 'update:sorting', sorting: SortingState): void,
}

const props = withDefaults(defineProps<TableProps>(), {
  data: () => [],
  pageCount: -1,
  itemsCount: -1,
  pagination: undefined,
  rowSelection: undefined,
  sorting: undefined,
  loading: false,
})

const emit = defineEmits<TableEmits>()

const {
  data,
  columns,
  pageCount,
  itemsCount,
  pagination,
  sorting,
  rowSelection,
} = toRefs(props)

const table = useVueTable({
  get data() {
    return data.value
  },
  get columns() {
    return columns.value
  },
  get pageCount() {
    return pageCount.value
  },
  state: {
    get pagination () {
      return pagination.value
    },
    get rowSelection() {
      return rowSelection.value
    },
    get sorting() {
      return sorting.value
    }
  },
  onPaginationChange: (updaterOrValue) => {
    emit(
      'update:pagination',
      typeof updaterOrValue === 'function'
        ? updaterOrValue(pagination.value!)
        : updaterOrValue
    )
  },
  onRowSelectionChange: (updaterOrValue) => {
    emit(
      'update:row-selection',
      typeof updaterOrValue === 'function'
        ? updaterOrValue(rowSelection.value!)
        : updaterOrValue
    )
  },
  onSortingChange: (updaterOrValue) => {
    emit(
      'update:sorting',
      typeof updaterOrValue === 'function'
        ? updaterOrValue(sorting.value!)
        : updaterOrValue
    )
  },
  getCoreRowModel: getCoreRowModel(),
  manualPagination: true,
  manualSorting: true,
})

const hasPagination = computed(() => {
  return pagination.value && (table.getCanNextPage() || table.getCanPreviousPage())
})

const showFooter = computed(() => {
  return itemsCount.value >= 10 && pagination.value
})

watch(data, () => emit('update:row-selection', {}))
</script>

<template>
  <section class="overflow-hidden sm:rounded-lg border border-gray-200 dark:border-gray-800 relative">
    <table class="w-full">
      <thead>
        <tr
          v-for="headerGroup in table.getHeaderGroups()"
          :key="headerGroup.id"
          class="border-b border-gray-200 dark:border-gray-800"
        >
          <th
            v-for="header in headerGroup.headers"
            :key="header.id"
            :colspan="header.colSpan"
            :class="[
              'bg-gray-50 dark:bg-gray-900 py-2.5 px-4 text-sm',
              'font-semibold text-gray-700 dark:text-gray-300 text-left',
              header.column.getCanSort() ? 'cursor-pointer select-none' : '',
              header.column.columnDef.meta?.headerClass,
            ]"
            @click="header.column.getToggleSortingHandler()?.($event)"
          >
            <div
              v-if="!header.isPlaceholder"
              :class="[
                'flex items-center',
                header.column.columnDef.meta?.headerContainerClass
              ]"
            >
              <FlexRender
                :render="header.column.columnDef.header"
                :props="header.getContext()"
              />

              <component
                v-if="header.column.getIsSorted()"
                :is="header.column.getIsSorted() === 'asc' ? ArrowSmallUpIcon : ArrowSmallDownIcon"
                class="w-5 h-5 inline-block text-gray-500 dark:text-gray-400"
              />
            </div>
          </th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
        <template v-if="table.getRowModel().rows.length > 0">
          <tr
            v-for="row in table.getRowModel().rows"
            :key="row.id"
            class="group"
          >
            <td
              v-for="cell in row.getVisibleCells()"
              :key="cell.id"
              :class="[
                'px-4 py-2 text-sm',
                cell.column.columnDef.meta?.tabular ? 'tabular-nums' : '',
                cell.column.columnDef.meta?.cellClass,
                row.getIsSelected()
                  ? 'bg-primary-50 dark:bg-gray-700/80 text-primary-800 dark:text-gray-100 group-hover:bg-primary-100/70 dark:group-hover:bg-gray-700'
                  : 'dark:text-gray-300 dark:bg-gray-900 group-hover:bg-gray-50 dark:group-hover:bg-gray-800/80'
              ]"
            >
              <FlexRender
                :render="cell.column.columnDef.cell"
                :props="cell.getContext()"
              />
            </td>
          </tr>
        </template>
        <tr v-else-if="$slots.empty">
          <td :colspan="table.getAllColumns().length">
            <slot name="empty" />
          </td>
        </tr>
      </tbody>
    </table>
    <div
      v-if="showFooter"
      :class="[
        'bg-gray-50 dark:bg-gray-900',
        'border-t border-gray-200 dark:border-gray-800',
        'px-4 py-2 flex justify-between items-center'
      ]"
    >
      <div>
        <label for="items-per-page" class="sr-only">
          {{ $t('pagination.items-per-page') }}
        </label>
        <BasicSelect
          id="items-per-page"
          size="small"
          :model-value="table.getState().pagination.pageSize"
          :options="[10, 20, 30, 40, 50]"
          :option-text="(v) => $t('pagination.show-n-items', v)"
          @update:model-value="table.setPageSize($event)"
        />
      </div>
      <i18n-t
        keypath="pagination.page"
        tag="p"
        class="text-sm text-gray-600 dark:text-gray-300"
      >
        <span class="font-semibold dark:text-gray-100">
          {{ pagination!.pageIndex + 1 }}
        </span>
        <span class="font-semibold dark:text-gray-100">
          {{ pageCount }}
        </span>
        <span class="font-semibold dark:text-gray-100">
          {{ itemsCount }}
        </span>
      </i18n-t>
      <Paginator
        :has-previous-page="table.getCanPreviousPage()"
        :has-next-page="table.getCanNextPage()"
        @click:first-page="table.setPageIndex(0)"
        @click:previous-page="table.previousPage()"
        @click:next-page="table.nextPage()"
        @click:last-page="table.setPageIndex(table.getPageCount())"
      />
    </div>

    <LoadingIndicator :loading="loading" />
  </section>
</template>
