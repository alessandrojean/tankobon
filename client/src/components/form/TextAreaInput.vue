<script lang="ts" setup>
import type { HTMLAttributes } from 'vue'
import type { ErrorObject } from '@vuelidate/core'

export interface TextInputProps {
  errors?: ErrorObject[],
  invalid?: boolean,
  modelValue: string,
  labelText: string,
  autoComplete?: HTMLTextAreaElement['autocomplete'],
  placeholder?: string,
  required?: boolean,
}

const props = withDefaults(defineProps<TextInputProps>(), {
  errors: undefined,
  invalid: false,
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
      <textarea
        :class="[
          'peer w-full bg-white dark:bg-gray-950 shadow-sm rounded-md pt-8',
          'dark:text-gray-200',
          'focus:ring focus:ring-opacity-50 motion-safe:transition-shadow',
          'placeholder:text-gray-500',
          invalid 
            ? 'border-red-500 dark:border-red-500/95 focus:border-red-500 dark:focus:border-red-500/95 focus:ring-red-200 dark:focus:ring-red-200/30' 
            : 'border-gray-300 dark:border-gray-700 focus:border-primary-500 dark:focus:border-primary-400 focus:ring-primary-200 dark:focus:ring-primary-200/30'
        ]"
        v-bind="$attrs"
        :placeholder="placeholder"
        :required="required"
        :value="modelValue"
        @input="$emit('update:modelValue', ($event.target! as HTMLTextAreaElement).value)"
      />
      <label
        :class="[
          'font-medium text-xs px-3 pt-3 absolute top-px inset-x-px rounded-t-md',
          'select-none cursor-text bg-white dark:bg-gray-950',
          invalid ? 'text-red-800 dark:text-red-600' : 'text-gray-700 dark:text-gray-300',
        ]"
        :for="($attrs.id as string | undefined)"
      >
        {{ labelText }}
      </label>
    </div>

    <p
      v-if="invalid && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>
  </div>
</template>
