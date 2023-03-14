<script lang="ts" setup>
export interface CheckboxInputProps {
  modelValue: boolean,
  labelText: string,
}

const props = defineProps<CheckboxInputProps>()

defineEmits<{
  (e: 'update:modelValue', modelValue: boolean): void
}>()
</script>

<script lang="ts">
export default { inheritAttrs: false }
</script>

<template>
  <div class="flex items-center space-x-2">
    <input
      class="rounded w-4 h-4 border-gray-300 shadow-sm text-primary-500 focus:border-primary-500 focus:ring focus:ring-offset-0 focus:ring-primary-200 focus:ring-opacity-50 motion-safe:transition-shadow"
      v-bind="$attrs"
      type="checkbox"
      :checked="modelValue"
      @change="$emit('update:modelValue', ($event.target! as HTMLInputElement).checked)"
    >
    <label
      class="text-gray-700 font-medium select-none text-sm"
      :for="($attrs.id as string | undefined)"
    >
      {{ labelText }}
    </label>
  </div>
</template>
