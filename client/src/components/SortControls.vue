<script setup lang="ts">
import { ArrowsUpDownIcon, Bars3BottomLeftIcon, BarsArrowDownIcon, BarsArrowUpIcon } from '@heroicons/vue/20/solid'
import Button from '@/components/form/Button.vue'
import type { SortDirection } from '@/types/tankobon-api'

export interface SortPropertyOption {
  property: string
  text: string
}

export interface SortControlsProps {
  properties: SortPropertyOption[]
}

defineProps<SortControlsProps>()

const direction = defineModel<SortDirection | null | undefined>('direction', { required: true })
const property = defineModel<string | null | undefined>('property', { required: true })

function toggleSortDirection() {
  direction.value = direction.value === 'asc' ? 'desc' : 'asc'
}
</script>

<template>
  <div class="flex">
    <BasicListbox
      v-model="property"
      size="small"
      fit-width
      unselect-on-click
      :label-text="$t('common-actions.sort')"
      :options="properties"
      :option-text="(property) => property?.text ?? $t('sort.none')"
      :option-value="(option) => option.property"
    >
      <template #listbox-button>
        <ListboxButton
          :as="Button"
          class="h-9"
          kind="ghost-alt"
          size="small"
        >
          <Bars3BottomLeftIcon class="w-5 h-5" />
          <span>{{ $t('common-actions.sort') }}</span>
        </ListboxButton>
      </template>
    </BasicListbox>

    <Button
      class="w-9 h-9"
      size="small"
      kind="ghost-alt"
      :disabled="!property"
      :title="!direction ? $t('sort.direction') : $t(`sort.${direction}`)"
      @click="toggleSortDirection"
    >
      <span class="sr-only">
        {{ !direction ? $t('sort.direction') : $t(`sort.${direction}`) }}
      </span>
      <BarsArrowUpIcon v-if="direction === 'asc'" class="w-5 h-5 -mb-1" />
      <BarsArrowDownIcon v-else-if="direction === 'desc'" class="w-5 h-5 -mb-1" />
      <ArrowsUpDownIcon v-else class="w-5 h-5" />
    </Button>
  </div>
</template>
