<script lang="ts" setup>
import { UserEntity, UserSort } from '@/types/tankobon-user'
import {
  createColumnHelper,
  type SortingState,
  type ColumnSort,
  type PaginationState,
} from '@tanstack/vue-table'
import type { Sort } from '@/types/tankobon-api'
import { getFullImageUrl } from '@/modules/api'
import { EllipsisHorizontalIcon } from '@heroicons/vue/20/solid'
import Avatar from '@/components/Avatar.vue'
import Badge from '@/components/Badge.vue'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import { getRelationship } from '@/utils/api'

const notificator = useNotificator()

const defaultSorting: ColumnSort = { id: 'createdAt', desc: true }
const pagination = ref<PaginationState>({ pageIndex: 0, pageSize: 20 })
const sorting = ref<SortingState>([defaultSorting])
const rowSelection = ref<Record<string, boolean>>({})

const { data: users, isLoading } = useUsersQuery({
  includes: ['avatar'],
  page: computed(() => pagination.value.pageIndex),
  size: computed(() => pagination.value.pageSize),
  sort: computed<Sort<UserSort>[]>(() => {
    return sorting.value.map((sort) => ({
      property: sort.id as UserSort,
      direction: sort.desc ? 'desc' : 'asc',
    }))
  }),
  onError: async (error) => {
    await notificator.failure({
      title: t('users.fetch-failure'),
      body: error.message,
    })
  }
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
    cell: ({ row }) => h('div', { class: 'flex items-center w-full h-full' }, [
      h(BasicCheckbox, {
        checked: row.getIsSelected(),
        disabled: !row.getCanSelect(),
        indeterminate: row.getIsSomeSelected(),
        onChange: row.getToggleSelectedHandler(),
      })
    ]),
    meta: {
      headerClass: 'w-12',
      cellClass: 'align-middle',
    }
  }),
  columnHelper.accessor(
    (user) => ({
      name: user.attributes.name,
      avatar: getRelationship(user, 'AVATAR'),
    }),
    {
      id: 'name',
      header: () => t('common-fields.name'),
      cell: (info) => {
        const { name, avatar } = info.getValue()

        return h('div', { class: 'flex items-center space-x-3' }, [
          h(Avatar, {
            pictureUrl: getFullImageUrl({
              collection: 'avatars',
              fileName: avatar?.attributes?.versions?.['64'],
              timeHex: avatar?.attributes?.timeHex,
            }),
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
      class: [
        'text-primary-600 dark:text-gray-100',
        'underline hover:no-underline',
      ],
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
        ]
      }
    ),
    meta: {
      headerClass: 'w-12',
    }
  }),
]
</script>

<template>
  <Table
    :data="users?.data"
    :columns="columns"
    :page-count="users?.pagination?.totalPages"
    :items-count="users?.pagination?.totalElements"
    :loading="isLoading"
    v-model:pagination="pagination"
    v-model:row-selection="rowSelection"
    v-model:sorting="sorting"
  />
</template>
