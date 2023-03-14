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
          'peer w-full bg-white shadow-sm rounded-md pt-8',
          'focus:ring focus:ring-opacity-50 motion-safe:transition-shadow',
          'placeholder:text-gray-500',
          invalid 
            ? 'border-red-500 focus:border-red-500 focus:ring-red-200' 
            : 'border-gray-300 focus:border-primary-500 focus:ring-primary-200'
        ]"
        v-bind="$attrs"
        :placeholder="placeholder"
        :required="required"
        :value="modelValue"
        @input="$emit('update:modelValue', ($event.target! as HTMLTextAreaElement).value)"
      />
      <label
        :class="[
          'font-medium text-xs px-3 absolute top-3 inset-x-0',
          'select-none cursor-text',
          invalid ? 'text-red-800' : 'text-gray-700',
        ]"
        :for="($attrs.id as string | undefined)"
      >
        {{ labelText }}
      </label>
    </div>

    <p
      v-if="invalid && errorMessage"
      class="text-red-700 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>
  </div>
</template>
