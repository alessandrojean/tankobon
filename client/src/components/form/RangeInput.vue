<script lang="ts" setup>
import InputMask from 'inputmask'
import type { ErrorObject } from '@vuelidate/core'

export interface RangeInputProps {
  errors?: ErrorObject[]
  id: string
  invalid?: boolean
  labelText: string
  min: number
  max: number
  modelValue: number
  placeholder?: string
  step?: number
}

const props = withDefaults(defineProps<RangeInputProps>(), {
  errors: undefined,
  invalid: false,
  placeholder: undefined,
  step: 1,
})

const emit = defineEmits<{
  (e: 'blur', event: Event): void
  (e: 'update:modelValue', modelValue: number): void
}>()

const { errors, modelValue } = toRefs(props)

const errorMessage = computed(() => errors.value?.[0]?.$message)

const manualInput = ref<HTMLInputElement>()
const inputMaskInstance = ref<InputMask.Instance>()

const inputMask: InputMask.Options = {
  regex: '\\d+',
  placeholder: ' ',
  showMaskOnHover: false,
  showMaskOnFocus: false,
}

watchEffect((onCleanup) => {
  if (manualInput.value) {
    inputMaskInstance.value = InputMask(inputMask).mask(manualInput.value)

    onCleanup(() => {
      inputMaskInstance.value?.remove()
    })
  } else {
    inputMaskInstance.value?.remove()
  }
})

function handleInput(event: Event) {
  const input = event.target as HTMLInputElement
  const value = Number(input.value)

  emit('update:modelValue', isNaN(value) ? 0 : value)
}
</script>

<template>
  <fieldset class="min-w-0 w-full disabled:opacity-60 motion-safe:transition">
    <div
      class="min-w-0 block border bg-white dark:bg-gray-950 shadow-sm rounded-md overflow-hidden border-gray-300 dark:border-gray-700"
    >
      <label
        class="font-medium text-xs px-3 pt-3 select-none cursor-text block text-gray-700 dark:text-gray-300"
        :for="id"
      >
        {{ labelText }}
      </label>

      <div class="flex gap-4 items-start w-full px-3 pb-2 pt-1.5">
        <div class="grow min-w-0">
          <input
            :id="id"
            type="range"
            :class="[
              'w-full',
              invalid ? 'accent-red-500' : 'accent-primary-500',
            ]"
            :min="min"
            :max="max"
            :step="step"
            :value="modelValue"
            @input="handleInput"
          >
          <div class="w-full flex justify-between text-xs">
            <span>{{ min }}</span>
            <span>{{ max }}</span>
          </div>
        </div>
        <label :for="`${id}-manual`" class="sr-only">
          {{ labelText }}
        </label>
        <input
          :id="`${id}-manual`"
          ref="manualInput"
          :class="[
            'w-14 bg-white dark:bg-gray-950 rounded-md dark:text-gray-200',
            'border-0 focus:outline-none focus:ring placeholder:text-gray-500',
            'h-6 px-1.5 text-right motion-safe:transition tabular-nums',
            invalid
              ? 'ring-1 focus:ring-1 ring-red-500 focus:ring-red-500 dark:ring-red-500/95 dark:focus:ring-red-500/95'
              : 'hover:ring-1 hover:ring-gray-300 dark:hover:ring-gray-700 focus:ring-1 focus:ring-primary-500 dark:focus:ring-primary-500',
          ]"
          type="text"
          inputmode="decimal"
          required
          :value="String(modelValue)"
          :placeholder="placeholder"
          @input="handleInput"
          @blur="$emit('blur')"
        >
      </div>
    </div>

    <slot name="footer" />

    <p
      v-if="invalid && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>
  </fieldset>
</template>
