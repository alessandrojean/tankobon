<script lang="ts" setup>
import type { HTMLAttributes } from 'vue'
import type { ErrorObject } from '@vuelidate/core'

export interface TextInputProps {
  errors?: ErrorObject[],
  invalid?: boolean,
  modelValue: string,
  labelText: string,
  autoComplete?: HTMLInputElement['autocomplete'],
  type?: HTMLInputElement['type'],
  inputMode?: HTMLAttributes['inputmode'],
  placeholder?: string,
  required?: boolean,
}

const props = withDefaults(defineProps<TextInputProps>(), {
  errors: undefined,
  invalid: false,
  type: 'text',
  required: false,
})

defineEmits<{
  (e: 'update:modelValue', modelValue: string): void
}>()

const { errors } = toRefs(props)

const errorMessage = computed(() => errors.value?.[0]?.$message)
</script>

<script lang="ts">
export default { inheritAttrs: false }
</script>

<template>
  <div>
    <div class="relative">
      <input
        :class="[
          'peer w-full bg-white dark:bg-gray-900 shadow-sm rounded-md pt-8',
          'dark:text-gray-200',
          'focus:ring focus:ring-opacity-50 motion-safe:transition-shadow',
          'placeholder:text-gray-500',
          $slots['left-icon'] ? 'pl-16' : '',
          invalid 
            ? 'border-red-500 dark:border-red-500/95 focus:border-red-500 dark:focus:border-red-500/95 focus:ring-red-200 dark:focus:ring-red-200/30' 
            : 'border-gray-300 dark:border-gray-700 focus:border-primary-500 dark:focus:border-primary-400 focus:ring-primary-200 dark:focus:ring-primary-200/30'
        ]"
        v-bind="$attrs"
        :type="type"
        :inputmode="inputMode"
        :placeholder="placeholder"
        :required="required"
        :value="modelValue"
        @input="$emit('update:modelValue', ($event.target! as HTMLInputElement).value)"
      >
      <label
        :class="[
          'font-medium text-xs px-3 absolute top-3 inset-x-0',
          'select-none cursor-text',
          $slots['left-icon'] ? 'pl-16' : '',
          invalid ? 'text-red-800 dark:text-red-600' : 'text-gray-700 dark:text-gray-300',
        ]"
        :for="($attrs.id as string | undefined)"
      >
        {{ labelText }}
      </label>
      <div
        v-if="$slots['left-icon']"
        :class="[
          'absolute left-[1.125rem] inset-y-0 flex items-center justify-center',
          'motion-safe:transition-colors',
          invalid 
            ? 'text-red-600 peer-focus:text-red-600'
            : 'text-gray-500 peer-focus:text-primary-600 dark:peer-focus:text-primary-500',
        ]"
      >
        <slot name="left-icon"></slot>
      </div>
    </div>

    <slot name="footer" />

    <p
      v-if="invalid && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>
  </div>
</template>