<script lang="ts" setup>
import { UserEntity, UserSort } from '@/types/tankobon-user'
import {
  FlexRender,
  createColumnHelper,
  getCoreRowModel,
  useVueTable,
  SortingState,
  ColumnSort,
  PaginationState,
} from '@tanstack/vue-table'
import { ArrowSmallDownIcon, ArrowSmallUpIcon } from '@heroicons/vue/20/solid'
import { Sort } from '@/types/tankobon-api'
import { API_BASE_URL } from '@/modules/api'
import Avatar from '@/components/Avatar.vue'
import Badge from '@/components/Badge.vue'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'

const defaultSorting: ColumnSort = { id: 'createdAt', desc: true }
const pagination = ref<PaginationState>({ pageIndex: 0, pageSize: 20 })
const sorting = ref<SortingState>([defaultSorting])
const rowSelection = ref<Record<string, boolean>>({})

const { data: users } = useUsersQuery({
  includes: ['avatar'],
  page: computed(() => pagination.value.pageIndex),
  size: computed(() => pagination.value.pageSize),
  sort: computed<Sort<UserSort>[]>(() => {
    return sorting.value.map((sort) => ({
      property: sort.id as UserSort,
      direction: sort.desc ? 'desc' : 'asc',
    }))
  })
})
const { t, locale } = useI18n()
const columnHelper = createColumnHelper<UserEntity>()
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
  columnHelper.display({
    id: 'select',
    header: ({ table }) => h(BasicCheckbox, {
      checked: table.getIsAllRowsSelected(),
      indeterminate: table.getIsSomeRowsSelected(),
      onChange: table.getToggleAllRowsSelectedHandler(),
    }),
    cell: ({ row }) => h(BasicCheckbox, {
      checked: row.getIsSelected(),
      disabled: !row.getCanSelect(),
      indeterminate: row.getIsSomeSelected(),
      onChange: row.getToggleSelectedHandler(),
    }),
    meta: {
      headerClass: 'pr-0',
      cellClass: 'pr-0',
    }
  }),
  columnHelper.accessor(
    (user) => [
      user.attributes.name,
      user.relationships?.find((r) => r.type === 'AVATAR')
        ?.attributes?.versions?.['64']
    ],
    {
      id: 'name',
      header: () => t('common-fields.name'),
      cell: (info) => {
        const [name, avatarUrl] = info.getValue()

        return h('div', { class: 'flex items-center space-x-3' }, [
          h(Avatar, {
            pictureUrl: `${API_BASE_URL}/images/avatars/${avatarUrl}`,
            small: true
          }),
          h('span', { innerText: name })
        ])
      },
      meta: {
        headerClass: 'pl-0',
        cellClass: 'pl-0',
      },
    }
  ),
  columnHelper.accessor('attributes.email', {
    id: 'email',
    enableSorting: false,
    header: () => t('common-fields.email'),
    cell: (info) => h('a', {
      href: `mailto:${info.getValue()}`,
      innerText: info.getValue()
    })
  }),
  columnHelper.accessor((user) => user.attributes.roles.includes('ROLE_ADMIN'), {
    id: 'isAdmin',
    enableSorting: false,
    header: () => t('common-fields.role'),
    cell: (info) => h(
      Badge,
      { color: info.getValue() ? 'blue' : 'gray', class: '!font-medium' },
      { default: () => info.getValue() ? t('user.role-admin') : t('user.role-user') }
    ),
    meta: {
      cellClass: 'text-right',
      headerContainerClass: 'justify-end',
    },
  }),
  columnHelper.accessor('attributes.createdAt', {
    id: 'createdAt',
    header: () => t('common-fields.created-at'),
    cell: (info) => dateFormatter.value.format(new Date(info.getValue())),
    meta: { tabular: true },
  })
]

const table = useVueTable({
  get data () {
    return users.value?.data ?? []
  },
  columns,
  get pageCount() {
    return users.value?.pagination?.totalPages ?? -1
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
    pagination.value = typeof updaterOrValue === 'function'
      ? updaterOrValue(pagination.value)
      : updaterOrValue
  },
  onRowSelectionChange: (updaterOrValue) => {
    rowSelection.value = typeof updaterOrValue === 'function'
      ? updaterOrValue(rowSelection.value)
      : updaterOrValue
  },
  onSortingChange: (updaterOrValue) => {
    sorting.value = typeof updaterOrValue === 'function'
      ? updaterOrValue(sorting.value)
      : updaterOrValue
  },
  getCoreRowModel: getCoreRowModel(),
  manualPagination: true,
  manualSorting: true,
})

const hasPagination = computed(() => {
  return table.getCanNextPage() || table.getCanPreviousPage()
})
</script>

<template>
  <section class="overflow-hidden sm:rounded-lg border border-gray-200 dark:border-gray-700">
    <table class="w-full">
      <thead>
        <tr
          v-for="headerGroup in table.getHeaderGroups()"
          :key="headerGroup.id"
          class="border-b border-gray-200 dark:border-gray-700"
        >
          <th
            v-for="header in headerGroup.headers"
            :key="header.id"
            :colspan="header.colSpan"
            :class="[
              'bg-gray-50 dark:bg-gray-800 py-2.5 px-4 text-sm',
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
      <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
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
              '[&>a]:text-primary-600 [&>a]:underline [&>a:hover]:no-underline',
              cell.column.columnDef.meta?.cellClass,
              row.getIsSelected()
                ? 'bg-primary-50 dark:bg-gray-700/80 text-primary-800 dark:text-gray-100 group-hover:bg-primary-100/70 dark:group-hover:bg-gray-700'
                : 'dark:bg-gray-800 group-hover:bg-gray-50 dark:group-hover:bg-gray-700/80'
            ]"
          >
            <FlexRender
              :render="cell.column.columnDef.cell"
              :props="cell.getContext()"
            />
          </td>
        </tr>
      </tbody>
    </table>
    <div
      v-if="hasPagination"
      :class="[
        'bg-gray-50 dark:bg-gray-800',
        'border-t border-gray-200 dark:border-gray-700',
        'px-4 py-2 flex justify-between'
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
    </div>
  </section>
</template>
