<script lang="ts" setup>
import InputMask from 'inputmask'
import type { ErrorObject } from '@vuelidate/core'
import type { DimensionsString } from '@/types/tankobon-dimensions'

export interface DimensionsInputProps {
  errorsWidth?: ErrorObject[]
  errorsHeight?: ErrorObject[]
  invalidWidth?: boolean
  invalidHeight?: boolean
  modelValue: DimensionsString
  placeholderWidth?: string
  placeholderHeight?: string
  required?: boolean
}

const props = withDefaults(defineProps<DimensionsInputProps>(), {
  errorsWidth: undefined,
  errorsHeight: undefined,
  invalidWidth: false,
  invalidHeight: false,
  placeholderWidth: undefined,
  placeholderHeight: undefined,
  required: false,
})

const emit = defineEmits<{
  (e: 'blur:width', event: Event): void
  (e: 'blur:height', event: Event): void
  (e: 'focus:width', event: Event): void
  (e: 'focus:height', event: Event): void
  (e: 'update:modelValue', modelValue: DimensionsString): void
}>()

const { errorsWidth, errorsHeight, modelValue } = toRefs(props)

const errorMessage = computed(() => {
  const errorWidth = unref(errorsWidth.value?.[0]?.$message)
  const errorHeight = unref(errorsHeight.value?.[0]?.$message)

  if (errorWidth === errorHeight) {
    return errorWidth
  } else if (errorWidth && errorWidth.length > 0) {
    return errorWidth
  } else if (errorHeight && errorHeight.length > 0) {
    return errorHeight
  } else {
    return null
  }
})

type Focused = 'width' | 'height'
const focused = ref<Focused | null>(null)

const labelText: Record<Focused, string> = {
  width: 'common-fields.width',
  height: 'common-fields.height',
}

const widthInput = ref<HTMLInputElement>()
const heightInput = ref<HTMLInputElement>()
const inputMaskWidth = ref<InputMask.Instance>()
const inputMaskHeight = ref<InputMask.Instance>()
const inputMask: InputMask.Options = {
  regex: '\\d+([,.]\\d{1,2})?',
  placeholder: ' ',
  showMaskOnHover: false,
  showMaskOnFocus: false,
}

watchEffect((onCleanup) => {
  if (widthInput.value && heightInput.value) {
    inputMaskWidth.value = InputMask(inputMask).mask(widthInput.value)
    inputMaskHeight.value = InputMask(inputMask).mask(heightInput.value)

    onCleanup(() => {
      inputMaskWidth.value?.remove()
      inputMaskHeight.value?.remove()
    })
  } else {
    inputMaskWidth.value?.remove()
    inputMaskHeight.value?.remove()
  }
})

interface EventHandler {
  event: Event
  type: Focused
}

function handleInput({ event, type }: EventHandler) {
  const newDimensions: DimensionsString = { ...modelValue.value }
  const input = event.target as HTMLInputElement

  if (type === 'width') {
    newDimensions.widthCm = input.value
  } else {
    newDimensions.heightCm = input.value
  }

  emit('update:modelValue', newDimensions)
}

function handleBlur({ event, type }: EventHandler) {
  focused.value = null

  if (type === 'width') {
    emit('blur:width', event)
  } else {
    emit('blur:height', event)
  }
}

function handleFocus({ event, type }: EventHandler) {
  focused.value = type

  if (type === 'width') {
    emit('focus:width', event)
  } else {
    emit('focus:height', event)
  }
}
</script>

<template>
  <fieldset class="min-w-0 w-full disabled:opacity-60 motion-safe:transition">
    <div
      class="min-w-0 block border bg-white dark:bg-gray-950 shadow-sm rounded-md overflow-hidden border-gray-300 dark:border-gray-700"
    >
      <label
        class="font-medium text-xs px-3 pt-3 select-none cursor-text block text-gray-700 dark:text-gray-300"
        :for="($attrs.id as string | undefined)"
      >
        {{ focused ? $t(labelText[focused]) : $t('common-fields.dimensions') }}
      </label>

      <div class="flex gap-4 items-center w-full px-3 pb-2 pt-1">
        <label :for="`${$attrs.id}-width`" class="sr-only">
          {{ $t('common-fields.width') }}
        </label>
        <input
          :id="`${$attrs.id}-width`"
          ref="widthInput"
          class="grow min-w-0 bg-white dark:bg-gray-950 rounded-md dark:text-gray-200 border-0 focus:outline-none focus:ring placeholder:text-gray-500 h-6 px-1.5 text-right motion-safe:transition tabular-nums"
          :class="[
            invalidWidth
              ? 'ring-1 focus:ring-1 ring-red-500 focus:ring-red-500 dark:ring-red-500/95 dark:focus:ring-red-500/95'
              : 'hover:ring-1 hover:ring-gray-300 dark:hover:ring-gray-700 focus:ring-1 focus:ring-primary-500 dark:focus:ring-primary-500',
          ]"
          type="text"
          inputmode="decimal"
          :required="required"
          :value="modelValue.widthCm"
          :placeholder="placeholderWidth"
          @input="handleInput({ event: $event, type: 'width' })"
          @focus="handleFocus({ event: $event, type: 'width' })"
          @blur="handleBlur({ event: $event, type: 'width' })"
        >
        <span
          class="shrink-0 text-center select-none text-gray-500"
        >
          &times;
        </span>
        <label :for="`${$attrs.id}-height`" class="sr-only">
          {{ $t('common-fields.height') }}
        </label>
        <input
          :id="`${$attrs.id}-height`"
          ref="heightInput"
          class="grow min-w-0 bg-white dark:bg-gray-950 rounded-md dark:text-gray-200 border-0 focus:outline-none focus:ring placeholder:text-gray-500 h-6 px-1.5 text-right motion-safe:transition tabular-nums"
          :class="[
            invalidHeight
              ? 'ring-1 focus:ring-1 ring-red-500 focus:ring-red-500 dark:ring-red-500/95 dark:focus:ring-red-500/95'
              : 'hover:ring-1 hover:ring-gray-300 dark:hover:ring-gray-700 focus:ring-1 focus:ring-primary-500 dark:focus:ring-primary-500',
          ]"
          type="text"
          inputmode="decimal"
          :required="required"
          :value="modelValue.heightCm"
          :placeholder="placeholderHeight"
          @input="handleInput({ event: $event, type: 'height' })"
          @focus="handleFocus({ event: $event, type: 'height' })"
          @blur="handleBlur({ event: $event, type: 'height' })"
        >
        <span
          class="shrink-0 text-center text-sm/none select-none text-gray-500"
        >
          cm
        </span>
      </div>
    </div>

    <slot name="footer" />

    <p
      v-if="(invalidWidth || invalidHeight) && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>
  </fieldset>
</template>
