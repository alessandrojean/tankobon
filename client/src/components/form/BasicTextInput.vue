<script lang="ts" setup>
import type { HTMLAttributes } from 'vue'
import type { ErrorObject } from '@vuelidate/core'

export interface TextInputProps {
  errors?: ErrorObject[]
  invalid?: boolean
  modelValue: string
  autoComplete?: HTMLInputElement['autocomplete']
  type?: HTMLInputElement['type']
  inputMode?: HTMLAttributes['inputmode']
  placeholder?: string
  required?: boolean
  size?: 'normal' | 'small'
}

withDefaults(defineProps<TextInputProps>(), {
  errors: undefined,
  invalid: false,
  type: 'text',
  required: false,
  size: 'normal',
})

defineEmits<{
  (e: 'update:modelValue', modelValue: string): void
}>()
</script>

<script lang="ts">
export default { inheritAttrs: false }
</script>

<template>
  <div class="relative">
    <input
      class="peer w-full bg-white dark:bg-gray-950 shadow-sm rounded-md dark:text-gray-200 focus:ring focus:ring-opacity-50 motion-safe:transition-shadow placeholder:text-gray-500 disabled:opacity-50" :class="[
        {
          'pl-10': $slots['left-icon'] && size === 'normal',
          'pl-9': $slots['left-icon'] && size === 'small',
          'pr-10': $slots['right-icon'] && size === 'normal',
          'pr-9': $slots['right-icon'] && size === 'small',
        },
        size === 'small' ? 'text-sm py-1.5' : '',
        invalid
          ? 'border-red-500 dark:border-red-500/95 focus:border-red-500 dark:focus:border-red-500/95 focus:ring-red-200 dark:focus:ring-red-200/30'
          : 'border-gray-300 dark:border-gray-800 focus:border-primary-500 dark:focus:border-primary-400 focus:ring-primary-200 dark:focus:ring-primary-200/30',
      ]"
      v-bind="$attrs"
      :type="type"
      :inputmode="inputMode"
      :placeholder="placeholder"
      :required="required"
      :value="modelValue"
      @input="$emit('update:modelValue', ($event.target! as HTMLInputElement).value)"
    >
    <div
      v-if="$slots['left-icon']"
      class="absolute inset-y-0 flex items-center justify-center motion-safe:transition-colors" :class="[
        size === 'small' ? 'left-2.5' : 'left-3',
        invalid
          ? 'text-red-600 peer-focus:text-red-600'
          : 'text-gray-500 peer-focus:text-primary-600 dark:peer-focus:text-primary-500',
      ]"
    >
      <slot name="left-icon" />
    </div>
    <div
      v-if="$slots['right-icon']"
      class="absolute inset-y-0 flex items-center justify-center motion-safe:transition-colors" :class="[
        size === 'small' ? 'right-2.5' : 'right-3',
        invalid
          ? 'text-red-600 peer-focus:text-red-600'
          : 'text-gray-500 peer-focus:text-primary-600 dark:peer-focus:text-primary-500',
      ]"
    >
      <slot name="right-icon" />
    </div>
  </div>
</template>
