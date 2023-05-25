<script lang="ts" setup>
import InputMask from 'inputmask'
import type { ErrorObject } from '@vuelidate/core'
import type { DimensionsString } from '@/types/tankobon-dimensions'
import type { LengthUnit } from '@/types/tankobon-unit'

export interface DimensionsInputProps {
  errorsWidth?: ErrorObject[]
  errorsHeight?: ErrorObject[]
  errorsDepth?: ErrorObject[]
  invalidWidth?: boolean
  invalidHeight?: boolean
  invalidDepth?: boolean
  modelValue: DimensionsString
  placeholderWidth?: string
  placeholderHeight?: string
  placeholderDepth?: string
  required?: boolean
}

const props = withDefaults(defineProps<DimensionsInputProps>(), {
  errorsWidth: undefined,
  errorsHeight: undefined,
  errorsDepth: undefined,
  invalidWidth: false,
  invalidHeight: false,
  invalidDepth: false,
  placeholderWidth: undefined,
  placeholderHeight: undefined,
  placeholderDepth: undefined,
  required: false,
})

const emit = defineEmits<{
  (e: 'blur:width', event: Event): void
  (e: 'blur:height', event: Event): void
  (e: 'blur:depth', event: Event): void
  (e: 'focus:width', event: Event): void
  (e: 'focus:height', event: Event): void
  (e: 'focus:depth', event: Event): void
  (e: 'update:modelValue', modelValue: DimensionsString): void
}>()

const { errorsWidth, errorsHeight, errorsDepth, modelValue } = toRefs(props)

const errorMessage = computed(() => {
  const errorWidth = unref(errorsWidth.value?.[0]?.$message)
  const errorHeight = unref(errorsHeight.value?.[0]?.$message)
  const errorDepth = unref(errorsDepth.value?.[0]?.$message)

  if (errorWidth === errorHeight && errorHeight === errorDepth) {
    return errorWidth
  } else if (errorWidth && errorWidth.length > 0) {
    return errorWidth
  } else if (errorHeight && errorHeight.length > 0) {
    return errorHeight
  } else if (errorDepth && errorDepth.length > 0) {
    return errorDepth
  } else {
    return null
  }
})

type Focused = 'width' | 'height' | 'depth'
const focused = ref<Focused | null>(null)

const labelText: Record<Focused, string> = {
  width: 'common-fields.width',
  height: 'common-fields.height',
  depth: 'common-fields.depth',
}

const widthInput = ref<HTMLInputElement>()
const heightInput = ref<HTMLInputElement>()
const depthInput = ref<HTMLInputElement>()
const inputMaskWidth = ref<InputMask.Instance>()
const inputMaskHeight = ref<InputMask.Instance>()
const inputMaskDepth = ref<InputMask.Instance>()
const inputMask: InputMask.Options = {
  regex: '\\d+([,.]\\d{1,2})?',
  placeholder: ' ',
  showMaskOnHover: false,
  showMaskOnFocus: false,
}

watchEffect((onCleanup) => {
  if (widthInput.value && heightInput.value && depthInput.value) {
    inputMaskWidth.value = InputMask(inputMask).mask(widthInput.value)
    inputMaskHeight.value = InputMask(inputMask).mask(heightInput.value)
    inputMaskDepth.value = InputMask(inputMask).mask(depthInput.value)

    onCleanup(() => {
      inputMaskWidth.value?.remove()
      inputMaskHeight.value?.remove()
      inputMaskDepth.value?.remove()
    })
  } else {
    inputMaskWidth.value?.remove()
    inputMaskHeight.value?.remove()
    inputMaskDepth.value?.remove()
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
    newDimensions.width = input.value
  } else if (type === 'height') {
    newDimensions.height = input.value
  } else {
    newDimensions.depth = input.value
  }

  emit('update:modelValue', newDimensions)
}

function handleUnitPicked(newUnit: LengthUnit) {
  const newDimensions: DimensionsString = {
    ...modelValue.value,
    unit: newUnit,
  }

  emit('update:modelValue', newDimensions)
}

function handleBlur({ event, type }: EventHandler) {
  focused.value = null

  if (type === 'width') {
    emit('blur:width', event)
  } else if (type === 'height') {
    emit('blur:height', event)
  } else {
    emit('blur:depth', event)
  }
}

function handleFocus({ event, type }: EventHandler) {
  focused.value = type

  if (type === 'width') {
    emit('focus:width', event)
  } else if (type === 'height') {
    emit('focus:height', event)
  } else {
    emit('focus:depth', event)
  }
}
</script>

<template>
  <fieldset class="min-w-0 w-full disabled:opacity-60 motion-safe:transition">
    <div
      class="min-w-0 block border bg-white dark:bg-gray-950 shadow-sm rounded-md border-gray-300 dark:border-gray-700"
    >
      <label
        class="font-medium text-xs px-3 pt-3 select-none cursor-text block text-gray-700 dark:text-gray-300"
        :for="($attrs.id as string | undefined)"
      >
        {{ focused ? $t(labelText[focused]) : $t('common-fields.dimensions') }}
      </label>

      <div class="flex gap-4 items-center w-full px-3 pb-2 pt-1 relative">
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
          :value="modelValue.width"
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
          :value="modelValue.height"
          :placeholder="placeholderHeight"
          @input="handleInput({ event: $event, type: 'height' })"
          @focus="handleFocus({ event: $event, type: 'height' })"
          @blur="handleBlur({ event: $event, type: 'height' })"
        >
        <span
          class="shrink-0 text-center select-none text-gray-500"
        >
          &times;
        </span>
        <label :for="`${$attrs.id}-depth`" class="sr-only">
          {{ $t('common-fields.depth') }}
        </label>
        <input
          :id="`${$attrs.id}-depth`"
          ref="depthInput"
          class="grow min-w-0 bg-white dark:bg-gray-950 rounded-md dark:text-gray-200 border-0 focus:outline-none focus:ring placeholder:text-gray-500 h-6 px-1.5 text-right motion-safe:transition tabular-nums"
          :class="[
            invalidDepth
              ? 'ring-1 focus:ring-1 ring-red-500 focus:ring-red-500 dark:ring-red-500/95 dark:focus:ring-red-500/95'
              : 'hover:ring-1 hover:ring-gray-300 dark:hover:ring-gray-700 focus:ring-1 focus:ring-primary-500 dark:focus:ring-primary-500',
          ]"
          type="text"
          inputmode="decimal"
          :required="required"
          :value="modelValue.depth"
          :placeholder="placeholderDepth"
          @input="handleInput({ event: $event, type: 'depth' })"
          @focus="handleFocus({ event: $event, type: 'depth' })"
          @blur="handleBlur({ event: $event, type: 'depth' })"
        >
        <UnitListbox
          class="shrink-0 -mx-2"
          :model-value="modelValue.unit"
          :units="['CENTIMETER', 'INCH', 'MILLIMETER']"
          @update:model-value="handleUnitPicked($event)"
        />
      </div>
    </div>

    <slot name="footer" />

    <p
      v-if="(invalidWidth || invalidHeight || invalidDepth) && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>
  </fieldset>
</template>
