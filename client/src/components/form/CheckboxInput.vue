<script lang="ts" setup>
export interface CheckboxInputProps {
  disabled?: boolean
  id?: string
  modelValue: boolean
  labelText?: string
}

defineProps<CheckboxInputProps>()

defineEmits<{
  (e: 'update:modelValue', modelValue: boolean): void
}>()
</script>

<template>
  <div class="flex items-center gap-2 min-w-0">
    <input
      :class="[
        'rounded w-4 h-4 dark:bg-gray-950 border-gray-300 dark:border-gray-600',
        'shadow-sm text-primary-500 focus:border-primary-500',
        'focus:ring dark:focus:border-primary-400',
        'dark:checked:bg-primary-500 dark:checked:border-primary-500',
        'dark:focus:ring-primary-200/30 focus:ring-offset-0',
        'focus:ring-primary-200 focus:ring-opacity-50',
        'disabled:opacity-50 disabled:checked:text-gray-400',
        'dark:disabled:checked:bg-gray-500 dark:disabled:checked:border-gray-500',
        'motion-safe:transition-shadow shrink-0 peer/checkbox',
      ]"
      type="checkbox"
      :checked="modelValue"
      :disabled="disabled"
      :id="String(id)"
      @change="$emit('update:modelValue', ($event.target! as HTMLInputElement).checked)"
    >
    <label
      class="text-gray-700 dark:text-gray-300 font-medium select-none text-sm grow min-w-0"
      :for="String(id)"
      v-if="labelText"
    >
      {{ labelText }}
    </label>

    <slot name="footer" />
  </div>
</template>
