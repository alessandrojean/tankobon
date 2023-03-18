<script lang="ts" setup>
import { createColumnHelper } from '@tanstack/vue-table'
import { EllipsisHorizontalIcon } from '@heroicons/vue/20/solid'
import Badge from '@/components/Badge.vue'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import { LibraryEntity } from '@/types/tankobon-library'

export interface LibrariesTableProps {
  userId: string,
}

const props = defineProps<LibrariesTableProps>()
const { userId } = toRefs(props)
const notificator = useNotificator()

const rowSelection = ref<Record<string, boolean>>({})

const { data: libraries } = useUserLibrariesByUserQuery({
  userId,
  includeShared: true,
  includes: ['owner'],
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-failure'),
      body: error.message,
    })
  }
})
const { t, locale } = useI18n()
const columnHelper = createColumnHelper<LibraryEntity>()
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
      })
    ]),
    meta: {
      headerClass: 'w-12',
      cellClass: 'align-middle',
    }
  }),
  columnHelper.accessor('attributes.name', {
    id: 'name',
    enableSorting: false,
    header: () => t('common-fields.name'),
    cell: (info) => info.getValue(),
  }),
  columnHelper.accessor('attributes.description', {
    id: 'description',
    enableSorting: false,
    header: () => t('common-fields.description'),
    cell: (info) => info.getValue()
  }),
  columnHelper.accessor(
    (library) => library.relationships?.find(r => r.type === 'OWNER')?.id !== userId.value,
    {
      id: 'isShared',
      enableSorting: false,
      header: () => t('common-fields.ownership'),
      cell: (info) => h(
        Badge,
        { color: info.getValue() ? 'blue' : 'gray' },
        { default: () => info.getValue() ? t('libraries.owner-shared') : t('libraries.owner-self') }
      ),
      meta: {
        cellClass: 'text-right',
        headerContainerClass: 'justify-end',
      },
    }
  ),
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
    :data="libraries"
    :columns="columns"
    v-model:row-selection="rowSelection"
  >
    <template #empty>
      <slot name="empty" />
    </template>
  </Table>
</template>
