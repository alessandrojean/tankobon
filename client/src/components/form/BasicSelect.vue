<script lang="ts" setup>
import type { ErrorObject } from '@vuelidate/core'

export interface BasicSelectProps {
  disabledOptions?: number[]
  errors?: ErrorObject[]
  invalid?: boolean
  modelValue: any
  options: any[]
  optionText?: (value: any) => string
  optionValue?: (value: any) => string
  placeholder?: string
  required?: boolean
  size?: 'normal' | 'small'
}

const props = withDefaults(defineProps<BasicSelectProps>(), {
  disabledOptions: () => [],
  errors: undefined,
  invalid: false,
  required: false,
  size: 'normal',
  optionText: (value: any) => String(value),
  optionValue: (value: any) => String(value),
})

const emit = defineEmits<{
  (e: 'update:modelValue', modelValue: any): void
}>()

const { optionValue, options } = toRefs(props)

function handleChange(event: Event) {
  const newValue = (event.target! as HTMLSelectElement).value
  const toEmit = options.value.find(option => optionValue.value(option) === newValue)

  emit('update:modelValue', toEmit)
}
</script>

<script lang="ts">
export default { inheritAttrs: false }
</script>

<template>
  <div class="relative">
    <select
      class="w-full bg-white dark:bg-gray-800 shadow-sm rounded-md dark:text-gray-200 focus:ring focus:ring-opacity-50 motion-safe:transition-shadow placeholder:text-gray-500" :class="[
        $slots['left-icon'] ? 'pl-16' : '',
        size === 'small' ? 'text-sm py-1.5' : '',
        invalid
          ? 'border-red-500 dark:border-red-500/95 focus:border-red-500 dark:focus:border-red-500/95 focus:ring-red-200 dark:focus:ring-red-200/30'
          : 'border-gray-300 dark:border-gray-800 focus:border-primary-500 dark:focus:border-primary-400 focus:ring-primary-200 dark:focus:ring-primary-200/30',
      ]"
      v-bind="$attrs"
      :placeholder="placeholder"
      :required="required"
      @change="handleChange"
    >
      <option
        v-for="(option, i) of options"
        :key="optionValue ? optionValue(option) : option"
        :value="optionValue ? optionValue(option) : option"
        :selected="optionValue(modelValue) === optionValue(option)"
        :disabled="disabledOptions.includes(i)"
      >
        {{ optionText(option) }}
      </option>
    </select>
    <div
      v-if="$slots['left-icon']"
      class="absolute left-[1.125rem] inset-y-0 flex items-center justify-center motion-safe:transition-colors" :class="[
        invalid
          ? 'text-red-600 peer-focus:text-red-600'
          : 'text-gray-500 peer-focus:text-primary-600 dark:peer-focus:text-primary-500',
      ]"
    >
      <slot name="left-icon" />
    </div>
  </div>
</template>
