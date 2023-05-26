<script setup lang="ts">
import type { ColumnOrderState, SortDirection } from '@tanstack/vue-table'
import type { SortPropertyOption } from '../SortControls.vue'
import type { TableColumn } from '../TableColumnsControls.vue'
import type { LibraryEntity, LibrarySort } from '@/types/tankobon-library'
import type { PaginatedResponse } from '@/types/tankobon-response'
import type { Sort } from '@/types/tankobon-api'

export interface LibrariesListViewerProps {
  libraries?: PaginatedResponse<LibraryEntity>
  columnOrderKey: string
  columnVisibilityKey: string
  defaultColumnOrder?: ColumnOrderState
  defaultColumnVisibility?: Record<string, boolean>
  loading: boolean
  unpaged?: boolean
  showNewButton?: boolean
}

const props = withDefaults(defineProps<LibrariesListViewerProps>(), {
  libraries: undefined,
  defaultColumnOrder: () => ['name', 'ownership', 'createdAt'],
  defaultColumnVisibility: () => ({
    modifiedAt: false,
    createdAt: true,
    ownership: true,
    name: true,
  }),
  unpaged: false,
  withSearch: true,
  showNewButton: true,
})

defineEmits<{
  (e: 'click:new'): void
}>()

const sort = defineModel<Sort<LibrarySort> | null>('sort', { required: true })
const page = defineModel<number>('page', { default: 0 })
const size = defineModel<number>('size', { default: 20 })

const { t, locale } = useI18n()

const sortProperties = computed(() => {
  const properties: SortPropertyOption[] = [
    { property: 'name', text: t('common-fields.name') },
    { property: 'createdAt', text: t('common-fields.created-at') },
    { property: 'modifiedAt', text: t('common-fields.modified-at') },
  ]

  return properties.sort((a, b) => a.text.localeCompare(b.text, locale.value))
})

const tableColumns = computed(() => {
  const columns: TableColumn[] = [
    { id: 'name', text: t('common-fields.name'), disabled: true },
    { id: 'ownership', text: t('common-fields.ownership') },
    { id: 'createdAt', text: t('common-fields.created-at') },
    { id: 'modifiedAt', text: t('common-fields.modified-at') },
  ]

  const disabled = columns.filter(c => c.disabled)
  const enabled = columns.filter(c => !c.disabled)

  return [
    ...disabled,
    ...enabled.sort((a, b) => a.text.localeCompare(b.text, locale.value)),
  ]
})

const { preference: columnVisibility } = useUserPreference<Record<string, boolean>>(props.columnVisibilityKey, props.defaultColumnVisibility)

const { preference: columnOrder } = useUserPreference<ColumnOrderState>(props.columnOrderKey, props.defaultColumnOrder)

function handleSortPropertyChange(newSortProperty: LibrarySort) {
  sort.value = {
    property: newSortProperty,
    direction: sort.value?.direction ?? 'asc',
  }
}

function handleSortDirectionChange(newSortDirection: SortDirection) {
  sort.value = {
    property: sort.value?.property ?? 'name',
    direction: newSortDirection,
  }
}

function handleSortChange(newSort: Sort<LibrarySort>[]) {
  sort.value = newSort[0] ?? null
}
</script>

<template>
  <div>
    <ViewControls>
      <div class="md:ml-auto flex flex-col-reverse sm:flex-row gap-4 items-center justify-center md:justify-normal">
        <TableColumnsControls
          v-model:column-visibility="columnVisibility"
          v-model:column-order="columnOrder"
          :columns="tableColumns"
        />

        <div class="flex gap-6 items-center justify-between md:justify-normal">
          <SortControls
            :properties="sortProperties"
            :property="sort?.property"
            :direction="sort?.direction"
            @update:property="handleSortPropertyChange"
            @update:direction="handleSortDirectionChange"
          />
        </div>
      </div>
    </ViewControls>

    <LibrariesTable
      v-model:page="page"
      v-model:size="size"
      class="mt-4 sm:mt-6"
      :libraries="libraries"
      :loading="loading"
      :sort="sort ? [sort] : []"
      :column-visibility="columnVisibility"
      :column-order="columnOrder"
      :show-new-button="showNewButton"
      @click:new="$emit('click:new')"
      @update:sort="handleSortChange"
    />
  </div>
</template>
