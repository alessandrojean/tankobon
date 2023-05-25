<script setup lang="ts" generic="Unit extends LengthUnit | MassUnit">
import Button from './Button.vue'
import type { LengthUnit, MassUnit } from '@/types/tankobon-unit'
import { isMassUnit, unitAbbreviation } from '@/utils/unit'

const props = defineProps<{
  modelValue: Unit
  units: Unit[]
}>()

defineEmits<{
  (e: 'update:modelValue', modelValue: Unit): void
}>()

const { t, locale } = useI18n()
const { units } = toRefs(props)

function getUnitName(unit: Unit) {
  const type = isMassUnit(unit) ? 'mass' : 'length'
  const key = unit.toLowerCase().replace(/_/g, '-')

  return t(`units.${type}.${key}`)
}

interface UnitOption {
  unit: Unit
  name: string
  abbreviation: string
}

const options = computed(() => {
  return (units.value as Unit[])
    .map<UnitOption>(unit => ({
      unit,
      name: getUnitName(unit),
      abbreviation: unitAbbreviation[unit],
    }))
    .sort((a, b) => a.name.localeCompare(b.name, locale.value))
})
</script>

<template>
  <BasicListbox
    size="small"
    align="end"
    fit-width
    :check-icon="false"
    :label-text="$t('common-fields.unit')"
    :model-value="(modelValue as Unit)"
    :options="options"
    :option-text="(unit) => unit!!.name"
    :option-value="(unit) => unit.unit"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <template #listbox-button="{ text, selected: { option } }">
      <ListboxButton
        :as="Button"
        class="w-9 h-6 !px-0 !items-center"
        kind="ghost"
        size="mini"
      >
        <span aria-hidden="true">{{ (option as UnitOption).abbreviation }}</span>
        <span class="sr-only">{{ text }}</span>
      </ListboxButton>
    </template>
    <template #option="{ text, option }">
      <div class="flex gap-6 items-center">
        <span
          :class="[
            'block truncate grow',
            'text-black dark:text-gray-300 ui-active:text-primary-700',
            'dark:ui-active:dark:text-primary-100',
          ]"
        >
          {{ text }}
        </span>
        <span
          :class="[
            'block shrink-0 text-xs',
            'text-gray-600 ui-active:text-primary-600',
            'dark:text-gray-500 dark:ui-active:text-primary-200',
          ]"
        >
          {{ option.abbreviation }}
        </span>
      </div>
    </template>
  </BasicListbox>

  <!--
    <span
          class="shrink-0 text-center text-sm/none select-none text-gray-500"
        >
          cm
        </span>
  -->
</template>
