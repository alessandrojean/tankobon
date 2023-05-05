<script lang="ts" setup>
import type { ErrorObject } from '@vuelidate/core'

export interface TextInputProps {
  errors?: ErrorObject[]
  invalid?: boolean
  modelValue: string
  labelText: string
  autoComplete?: HTMLTextAreaElement['autocomplete']
  placeholder?: string
  required?: boolean
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
    <div
      class="w-full border bg-white dark:bg-gray-950 shadow-sm rounded-md focus-within:ring focus-within:ring-opacity-50 motion-safe:transition-shadow"
      :class="[
        invalid
          ? 'border-red-500 dark:border-red-500/95 focus-within:border-red-500 dark:focus-within:border-red-500/95 focus-within:ring-red-200 dark:focus-within:ring-red-200/30'
          : 'border-gray-300 dark:border-gray-700 focus-within:border-primary-500 dark:focus-within:border-primary-400 focus-within:ring-primary-200 dark:focus-within:ring-primary-200/30',
      ]"
    >
      <label
        class="font-medium text-xs px-3 pt-3 pb-1 select-none block"
        :class="[
          invalid
            ? 'text-red-800 dark:text-red-600'
            : 'text-gray-700 dark:text-gray-300',
        ]"
        :for="($attrs.id as string | undefined)"
      >
        {{ labelText }}
      </label>
      <textarea
        class="w-full bg-white dark:bg-gray-950 rounded-md dark:text-gray-200 focus:outline-none border-0 focus:ring-0 placeholder:text-gray-500"
        v-bind="$attrs"
        :placeholder="placeholder"
        :required="required"
        :value="modelValue"
        spellcheck="false"
        data-gramm="false"
        @input="$emit('update:modelValue', ($event.target! as HTMLTextAreaElement).value)"
      />
    </div>

    <p
      v-if="invalid && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>
  </div>
</template>
