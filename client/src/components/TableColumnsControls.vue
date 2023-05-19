<script setup lang="ts">
import Draggable from 'vuedraggable'
import Button from '@/components/form/Button.vue'
import { Popover, PopoverPanel } from '@headlessui/vue'
import { ViewColumnsIcon } from '@heroicons/vue/20/solid'
import { ColumnOrderState } from '@tanstack/vue-table'
import { FocusKeys } from '@primer/behaviors'

export interface TableColumn {
  id: string
  text: string
  disabled?: boolean
}

export interface TableColumnsControlsProps {
  columns: TableColumn[]
}

const props = defineProps<TableColumnsControlsProps>()

const { columns } = toRefs(props)

const columnVisibility = defineModel<Record<string, boolean>>('columnVisibility', { required: true })
const columnOrder = defineModel<ColumnOrderState>('columnOrder', { required: true })

const popover = ref<InstanceType<typeof Popover>>()
const popoverPanel = ref<InstanceType<typeof PopoverPanel>>()
const options = ref<InstanceType<typeof Draggable>>()

const { position } = useAnchoredPosition({
  anchorElementRef: computed(() => popover.value?.$el),
  floatingElementRef: computed(() => popoverPanel.value?.$el),
  side: 'outside-bottom',
  align: 'start',
  allowOutOfBounds: false,
})

function handleColumnVisibilityChange(column: TableColumn, visible: boolean) {
  const newColumnVisibility = structuredClone(toRaw(columnVisibility.value))
  newColumnVisibility[column.id] = visible

  columnVisibility.value = newColumnVisibility
}

const columnMap = computed(() => {
  return Object.fromEntries(columns.value.map(c => [c.id, c]))
})

const safeColumnOrder = computed(() => {
  if (columnOrder.value.length === 0) {
    return columns.value
      .filter(c => !c.disabled)
      .map(c => c.id)
  }

  const order = columnOrder.value.filter(c => !columnMap.value[c].disabled)
  const newItems = columns.value
    .filter(c => !c.disabled && !order.includes(c.id))
    .map(c => c.id)

  return [...order, ...newItems]
})

function handleDragAndDrop(newOrder: ColumnOrderState) {
  const copy: ColumnOrderState = JSON.parse(JSON.stringify(newOrder))
  const disabled = columns.value.filter(c => c.disabled).map(c => c.id)
  columnOrder.value = [...disabled, ...copy]
}

useFocusZone({
  containerRef: computed(() => options.value?.$el),
  bindKeys: FocusKeys.ArrowVertical | FocusKeys.HomeAndEnd,
  focusInStrategy: 'closest',
  // focusOutBehavior: 'wrap',
})
</script>

<template>
  <Popover class="relative" ref="popover">
    <PopoverButton
      :as="Button"
      class="h-9"
      kind="ghost-alt"
      size="small"
    >
      <ViewColumnsIcon class="w-5 h-5" />
      <span>{{ $t('common-actions.columns') }}</span>
    </PopoverButton>

    <MenuTransition>
      <PopoverPanel
        ref="popoverPanel"
        as="div"
        :class="[
          'absolute top-[--top] left-[--left] min-w-[12rem] max-w-[20rem] z-20 p-2',
          'space-y-1 bg-white dark:bg-gray-800',
          'shadow-primer-overlay dark:shadow-primer-overlay-dark',
          'rounded-xl mt-0.5 origin-top-right ring-1',
          'ring-black/5 dark:ring-gray-700',
        ]"
        :style="{
          '--top': position?.top !== undefined ? `${position.top}px` : '100%',
          '--left': position?.left !== undefined ? `${position.left}px` : '0px',
        }"
      >
        <Draggable
          ref="options"
          tag="ul"
          class="max-h-[18rem] w-full min-w-0 overflow-y-auto rounded-lg"
          ghost-class="opacity-50"
          drag-class="cursor-grabbing"
          handle=".grabber"
          :model-value="safeColumnOrder"
          :item-key="(id: string) => id"
          :disabled="columns.length === 1"
          @update:model-value="handleDragAndDrop"
        >
          <template #header>
            <li
              v-for="column in columns.filter(c => c.disabled)"
              :key="column.id"
              :class="[
                'select-none pl-2 pr-1 py-1.5 text-sm',
                'rounded-lg flex gap-1 items-center',
                'dark:text-gray-300 w-full min-w-0',
              ]"
            >
              <CheckboxInput
                class="!gap-3.5 grow min-w-0 [&_label]:truncate"
                disabled
                :id="`column-${column.id}`"
                :model-value="columnVisibility[column.id] ?? false"
                :label-text="column.text"
              />
            </li>
          </template>
          <template #item="{ element: columnId }">
            <li
              :class="[
                'select-none pl-2 pr-1 py-1.5 text-sm',
                'rounded-lg flex gap-3 items-center',
                'dark:text-gray-300 relative w-full min-w-0',
              ]"
            >
              <CheckboxInput
                class="!gap-3.5 grow min-w-0 [&_label]:truncate"
                :disabled="columnMap[columnId].disabled"
                :id="`column-${columnId}`"
                :model-value="columnVisibility[columnId] ?? false"
                :label-text="columnMap[columnId].text"
                @update:model-value="handleColumnVisibilityChange(columnMap[columnId], $event)"
              >
                <template #footer>
                  <div
                    :class="[
                      'hidden peer-focus-visible/checkbox:block',
                      'absolute inset-0 pointer-events-none',
                      'rounded-md ring-2 ring-inset ring-black dark:ring-white/90'
                    ]"
                  />
                </template>
              </CheckboxInput>
              <SixDotsVerticalIcon
                v-if="!columnMap[columnId].disabled"
                class="w-5 h-5 shrink-0 text-gray-500 dark:text-gray-300 cursor-grab grabber"
              />
            </li>
          </template>
        </Draggable>
      </PopoverPanel>
    </MenuTransition>
  </Popover>
</template>
