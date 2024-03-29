<script lang="ts" setup>
import type { ErrorObject } from '@vuelidate/core'

export interface TextInputProps {
  errors?: ErrorObject[]
  invalid?: boolean
  modelValue: any
  options: any[]
  optionText?: (value: any) => string
  optionValue?: (value: any) => string
  labelText: string
  placeholder?: string
  required?: boolean
}

const props = withDefaults(defineProps<TextInputProps>(), {
  errors: undefined,
  invalid: false,
  required: false,
  optionText: () => (value: any) => String(value),
  optionValue: () => (value: any) => String(value),
})

const emit = defineEmits<{
  (e: 'update:modelValue', modelValue: any): void
}>()

const { errors, optionValue, options } = toRefs(props)

const errorMessage = computed(() => errors.value?.[0]?.$message)

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
  <div>
    <div class="relative">
      <select
        class="peer w-full bg-white dark:bg-gray-800 shadow-sm rounded-md pt-8 dark:text-gray-200 focus:ring focus:ring-opacity-50 motion-safe:transition-shadow placeholder:text-gray-500"
        :class="[
          { 'pl-16': $slots['left-icon'] },
          invalid
            ? 'border-red-500 dark:border-red-500/95 focus:border-red-500 dark:focus:border-red-500/95 focus:ring-red-200 dark:focus:ring-red-200/30'
            : 'border-gray-300 dark:border-gray-700 focus:border-primary-500 dark:focus:border-primary-400 focus:ring-primary-200 dark:focus:ring-primary-200/30',
        ]"
        v-bind="$attrs"
        :placeholder="placeholder"
        :required="required"
        @change="handleChange"
      >
        <option
          v-if="placeholder"
          value=""
          disabled
          :selected="!modelValue"
        >
          {{ placeholder }}
        </option>
        <option
          v-for="option of options"
          :key="optionValue ? optionValue(option) : option"
          :value="optionValue ? optionValue(option) : option"
          :selected="optionValue(modelValue) === optionValue(option)"
        >
          {{ optionText(option) }}
        </option>
      </select>
      <label
        class="font-medium text-xs px-3 absolute top-3 inset-x-0 select-none"
        :class="[
          { 'pl-16': $slots['left-icon'] },
          invalid ? 'text-red-800 dark:text-red-600' : 'text-gray-700 dark:text-gray-300',
        ]"
        :for="($attrs.id as string | undefined)"
      >
        {{ labelText }}
      </label>
      <div
        v-if="$slots['left-icon']"
        class="absolute left-[1.125rem] inset-y-0 flex items-center justify-center motion-safe:transition-colors"
        :class="[
          invalid
            ? 'text-red-600 peer-focus:text-red-600'
            : 'text-gray-500 peer-focus:text-primary-600 dark:peer-focus:text-primary-500',
        ]"
      >
        <slot name="left-icon" />
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
