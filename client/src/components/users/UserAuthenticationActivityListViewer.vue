<script setup lang="ts">
import type { ColumnOrderState, SortDirection } from '@tanstack/vue-table'
import type { SortPropertyOption } from '../SortControls.vue'
import type { TableColumn } from '../TableColumnsControls.vue'
import type { PaginatedResponse } from '@/types/tankobon-response'
import type { Sort } from '@/types/tankobon-api'
import type { AuthenticationActivityEntity, AuthenticationActivitySort } from '@/types/tankobon-authentication-activity'

export interface LibrariesListViewerProps {
  authenticationActivity?: PaginatedResponse<AuthenticationActivityEntity>
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
  defaultColumnOrder: () => ['ip', 'status', 'source', 'error', 'timestamp'],
  defaultColumnVisibility: () => ({
    ip: true,
    status: true,
    source: true,
    error: true,
    timestamp: true,
    userAgent: false,
  }),
  unpaged: false,
  showNewButton: true,
})

const sort = defineModel<Sort<AuthenticationActivitySort> | null>('sort', { required: true })
const page = defineModel<number>('page', { default: 0 })
const size = defineModel<number>('size', { default: 20 })

const { t, locale } = useI18n()

const sortProperties = computed(() => {
  const properties: SortPropertyOption[] = [
    { property: 'timestamp', text: t('common-fields.timestamp') },
    { property: 'email', text: t('common-fields.email') },
    { property: 'success', text: t('authentication-activity.status') },
    { property: 'ip', text: t('authentication-activity.ip') },
    { property: 'error', text: t('authentication-activity.error') },
    { property: 'userAgent', text: t('authentication-activity.user-agent') },
  ]

  return properties.sort((a, b) => a.text.localeCompare(b.text, locale.value))
})

const tableColumns = computed(() => {
  const columns: TableColumn[] = [
    { id: 'ip', text: t('authentication-activity.ip'), disabled: true },
    { id: 'status', text: t('authentication-activity.status') },
    { id: 'source', text: t('authentication-activity.source') },
    { id: 'error', text: t('authentication-activity.error') },
    { id: 'timestamp', text: t('common-fields.timestamp') },
    { id: 'userAgent', text: t('authentication-activity.user-agent') },
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

function handleSortPropertyChange(newSortProperty: AuthenticationActivitySort) {
  sort.value = {
    property: newSortProperty,
    direction: sort.value?.direction ?? 'asc',
  }
}

function handleSortDirectionChange(newSortDirection: SortDirection) {
  sort.value = {
    property: sort.value?.property ?? 'ip',
    direction: newSortDirection,
  }
}

function handleSortChange(newSort: Sort<AuthenticationActivitySort>[]) {
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

    <UserAuthenticationActivityTable
      v-model:page="page"
      v-model:size="size"
      class="mt-4 sm:mt-6"
      :authentication-activity="authenticationActivity"
      :loading="loading"
      :sort="sort ? [sort] : []"
      :column-visibility="columnVisibility"
      :column-order="columnOrder"
      @update:sort="handleSortChange"
    />
  </div>
</template>
