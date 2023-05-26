<script lang="ts" setup>
import { createColumnHelper } from '@tanstack/vue-table'
import type { ColumnOrderState, PaginationState, SortingState } from '@tanstack/vue-table'
import { EllipsisHorizontalIcon } from '@heroicons/vue/20/solid'
import type { UserEntity, UserSort } from '@/types/tankobon-user'
import type { Sort } from '@/types/tankobon-api'
import { createImageUrl } from '@/modules/api'
import Avatar from '@/components/Avatar.vue'
import Badge from '@/components/Badge.vue'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import { getRelationship } from '@/utils/api'
import type { PaginatedResponse } from '@/types/tankobon-response'

export interface UsersTableProps {
  users?: PaginatedResponse<UserEntity>
  columnOrder?: ColumnOrderState
  columnVisibility?: Record<string, boolean>
  loading?: boolean
  page: number
  size: number
  sort: Sort<UserSort>[]
}

const props = withDefaults(defineProps<UsersTableProps>(), {
  users: undefined,
  columnOrder: () => [],
  columnVisibility: () => ({}),
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
  (e: 'update:sort', sort: Sort<UserSort>[]): void
}>()

const { page, size, sort, columnVisibility } = toRefs(props)
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

const columnHelper = createColumnHelper<UserEntity>()

const columns = [
  columnHelper.display({
    id: 'select',
    header: ({ table }) => h(BasicCheckbox, {
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
    user => ({
      name: user.attributes.name,
      email: user.attributes.email,
      avatar: getRelationship(user, 'AVATAR'),
    }),
    {
      id: 'name',
      header: () => t('common-fields.name'),
      cell: (info) => {
        const { name, email, avatar } = info.getValue()

        return h('div', { class: 'flex items-center space-x-3' }, [
          h(Avatar, {
            pictureUrl: createImageUrl({
              fileName: avatar?.attributes?.versions?.['64'],
              timeHex: avatar?.attributes?.timeHex,
            }),
          }),
          h('div', { class: 'flex flex-col' }, [
            h('span', { innerText: name, class: 'font-medium', title: name }),
            (!columnVisibility.value.email)
              ? h('a', {
                class: [
                  'text-xs text-primary-600 dark:text-gray-100',
                  'underline hover:no-underline',
                ],
                href: `mailto:${email}`,
                innerText: email,
              })
              : undefined,
          ]),
        ])
      },
      meta: {
        headerClass: 'pl-0',
        cellClass: 'pl-0',
      },
    },
  ),
  columnHelper.accessor('attributes.email', {
    id: 'email',
    enableSorting: false,
    header: () => t('common-fields.email'),
    cell: info => h('a', {
      class: [
        'text-primary-600 dark:text-gray-100',
        'underline hover:no-underline',
      ],
      href: `mailto:${info.getValue()}`,
      innerText: info.getValue(),
    }),
  }),
  columnHelper.accessor(user => user.attributes.roles.includes('ROLE_ADMIN'), {
    id: 'role',
    enableSorting: false,
    header: () => t('common-fields.role'),
    cell: info => h(
      Badge,
      { color: info.getValue() ? 'blue' : 'green' },
      { default: () => info.getValue() ? t('user.role-admin') : t('user.role-user') },
    ),
    meta: {
      cellClass: 'text-right',
      headerContainerClass: 'justify-end',
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
        to: { name: 'users-id', params: { id: row.original.id } },
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
  const sortToEmit = sorting.map<Sort<UserSort>>(sort => ({
    property: sort.id as UserSort,
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
    :data="users?.data"
    :columns="columns"
    :page-count="users?.pagination?.totalPages"
    :items-count="users?.pagination?.totalElements"
    :loading="loading"
    :column-order="['select', ...columnOrder, 'actions']"
    :column-visibility="columnVisibility"
    @update:pagination="handlePaginationChange"
    @update:sorting="handleSortingChange"
  />
</template>
