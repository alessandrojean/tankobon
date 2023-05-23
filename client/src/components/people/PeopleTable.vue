<script lang="ts" setup>
import type { ColumnOrderState, PaginationState, SortingState } from '@tanstack/vue-table'
import { createColumnHelper } from '@tanstack/vue-table'
import { EllipsisHorizontalIcon, PlusIcon } from '@heroicons/vue/20/solid'
import { MagnifyingGlassIcon, PaintBrushIcon } from '@heroicons/vue/24/outline'
import { PaintBrushIcon as PaintBrushSolidIcon } from '@heroicons/vue/24/solid'
import { parseISO } from 'date-fns'
import Avatar from '../Avatar.vue'
import Flag from '../Flag.vue'
import BasicCheckbox from '@/components/form/BasicCheckbox.vue'
import Button from '@/components/form/Button.vue'
import type { Sort } from '@/types/tankobon-api'
import { getRelationship } from '@/utils/api'
import { createImageUrl } from '@/modules/api'
import type { PaginatedResponse } from '@/types/tankobon-response'
import type { PersonEntity, PersonSort } from '@/types/tankobon-person'

export interface PeopleTableProps {
  people?: PaginatedResponse<PersonEntity>
  columnOrder?: ColumnOrderState
  columnVisibility?: Record<string, boolean>
  loading?: boolean
  page: number
  search?: string
  size: number
  sort: Sort<PersonSort>[]
}

const props = withDefaults(defineProps<PeopleTableProps>(), {
  people: undefined,
  columnOrder: () => [],
  columnVisibility: () => ({}),
  loading: false,
  search: '',
})

const emit = defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
  (e: 'update:sort', sort: Sort<PersonSort>[]): void
}>()

const { page, search, size, sort, columnVisibility } = toRefs(props)
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

const columnHelper = createColumnHelper<PersonEntity>()

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
    person => ({
      name: person.attributes.name,
      nativeName: person.attributes.nativeName,
      picture: getRelationship(person, 'PERSON_PICTURE'),
    }),
    {
      id: 'name',
      header: () => t('common-fields.name'),
      cell: (info) => {
        const { name, nativeName, picture } = info.getValue()

        return h('div', { class: 'flex items-center space-x-3' }, [
          h(Avatar, {
            square: true,
            emptyIcon: PaintBrushSolidIcon,
            pictureUrl: createImageUrl({
              fileName: picture?.attributes?.versions?.['64'],
              timeHex: picture?.attributes?.timeHex,
            }),
          }),
          h('div', { class: 'flex flex-col' }, [
            h('span', { innerText: name, class: 'font-medium', title: name }),
            (nativeName.name.length && !columnVisibility.value.nativeName)
              ? h('span', {
                lang: nativeName.language ?? undefined,
                innerText: nativeName.name,
                class: 'text-xs text-gray-700 dark:text-gray-400',
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
  columnHelper.accessor('attributes.nativeName', {
    id: 'nativeName',
    header: () => t('common-fields.native-name'),
    cell: (info) => {
      const nativeName = info.getValue()
      return h('span', {
        lang: nativeName.language ?? undefined,
        innerText: nativeName.name,
      })
    },
  }),
  columnHelper.accessor('attributes.nationality', {
    id: 'nationality',
    header: () => t('common-fields.nationality'),
    cell: (info) => {
      const nationality = info.getValue()

      return h('div', { class: 'flex items-center gap-3' }, [
        h(Flag, { region: nationality }),
        h('span', {
          innerText: nationality
            ? (regionNames.value.of(nationality) ?? t('location.unknown'))
            : t('location.unknown'),
        }),
      ])
    },
  }),
  columnHelper.accessor('attributes.bornAt', {
    id: 'bornAt',
    header: () => t('common-fields.born-at'),
    cell: info => info.getValue() ? d(parseISO(info.getValue()!), 'short') : t('date.unknown'),
    meta: { tabular: true },
  }),
  columnHelper.accessor('attributes.diedAt', {
    id: 'diedAt',
    header: () => t('common-fields.died-at'),
    cell: info => info.getValue() ? d(parseISO(info.getValue()!), 'short') : '',
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
        to: { name: 'people-id', params: { id: row.original.id } },
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
  const sortToEmit = sorting.map<Sort<PersonSort>>(sort => ({
    property: sort.id as PersonSort,
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
    :data="people?.data"
    :columns="columns"
    :page-count="people?.pagination?.totalPages"
    :items-count="people?.pagination?.totalElements"
    :loading="loading"
    :column-order="['select', ...columnOrder, 'actions']"
    :column-visibility="columnVisibility"
    @update:pagination="handlePaginationChange"
    @update:sorting="handleSortingChange"
  >
    <template #empty>
      <slot name="empty">
        <EmptyState
          :icon="search?.length ? MagnifyingGlassIcon : PaintBrushIcon"
          :title="$t('people.empty-header')"
          :description="
            search?.length
              ? $t('people.empty-search-description', [search])
              : $t('people.empty-description')
          "
        >
          <template v-if="!search?.length" #actions>
            <Button
              kind="primary"
              is-router-link
              :to="{ name: 'publishers-new' }"
            >
              <PlusIcon class="w-5 h-5" />
              <span>{{ $t('people.new') }}</span>
            </Button>
          </template>
        </EmptyState>
      </slot>
    </template>
  </Table>
</template>
