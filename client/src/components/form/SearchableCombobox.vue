<script lang="ts" setup>
import { ChevronUpDownIcon } from '@heroicons/vue/20/solid'
import type { ErrorObject } from '@vuelidate/core'
import { Combobox, ComboboxInput, ComboboxOptions } from '@headlessui/vue'
import type { AnchorAlignment } from '@primer/behaviors'
import Button from './Button.vue'

export interface BasicListboxProps {
  align?: AnchorAlignment
  disabledOptions?: number[]
  errors?: ErrorObject[]
  filter?: (query: string, option: any) => boolean
  invalid?: boolean
  kind?: 'basic' | 'fancy'
  labelText?: string
  modelValue: any
  options: any[]
  optionText?: (value: any) => string
  optionValue?: (value: any) => any
  optionValueSelect?: (value: any) => any
  placeholder?: string
}

const props = withDefaults(defineProps<BasicListboxProps>(), {
  align: 'start',
  disabledOptions: () => [],
  errors: undefined,
  filter: undefined,
  invalid: false,
  kind: 'basic',
  size: 'normal',
  optionText: (value: any) => String(value),
  optionValue: (value: any) => String(value),
  optionValueSelect: (value: any) => String(value),
})

const emit = defineEmits<{
  (e: 'blur'): void
  (e: 'update:model-value', modelValue: any): void
  (e: 'update:model-value-select', modelValue: any): void
}>()

const { optionValue, optionText, options, filter, errors, align } = toRefs(props)

const query = ref('')

const filterFunction = computed(() => {
  return filter.value ?? ((query: string, option: any) => {
    return optionText.value(option).toLowerCase().includes(query)
  })
})

const filteredOptions = computed(() => {
  if (query.value.length === 0) {
    return options.value
  }

  return options.value.filter((option) => {
    return filterFunction.value(query.value.toLowerCase(), option)
  })
})

const errorMessage = computed(() => errors.value?.[0]?.$message)

const comboboxInput = ref<InstanceType<typeof ComboboxInput>>()
const comboboxOptions = ref<InstanceType<typeof ComboboxOptions>>()

const { position } = useAnchoredPosition({
  anchorElementRef: computed(() => comboboxInput.value?.$el),
  floatingElementRef: computed(() => comboboxOptions.value?.$el),
  side: 'outside-bottom',
  align,
  allowOutOfBounds: false,
})
</script>

<template>
  <fieldset>
    <BasicSelect
      class="md:hidden"
      :errors="errors"
      :invalid="invalid"
      :model-value="modelValue"
      :options="options"
      :option-text="optionText"
      :option-value="optionValueSelect"
      :placeholder="placeholder"
      :disabled-options="disabledOptions"
      @update:model-value="$emit('update:model-value-select', $event)"
    />
    <Combobox
      as="div"
      class="relative hidden md:block"
      :model-value="optionValue ? optionValue(modelValue) : modelValue"
      @update:model-value="emit('update:model-value', $event)"
    >
      <div class="relative">
        <ComboboxLabel
          v-if="labelText"
          :for="$attrs.id"
          :class="[
            'font-medium text-xs px-3 absolute top-3 inset-x-0',
            'select-none cursor-text',
            invalid ? 'text-red-800 dark:text-red-600' : 'text-gray-700 dark:text-gray-300',
            { 'sr-only': kind === 'basic' },
          ]"
        >
          {{ labelText }}
        </ComboboxLabel>
        <slot name="left-icon" />
        <ComboboxInput
          :id="String($attrs.id)"
          ref="comboboxInput"
          :class="[
            'w-full bg-white dark:bg-gray-950 shadow-sm rounded-md',
            'dark:text-gray-200 focus:ring focus:ring-opacity-50',
            'motion-safe:transition-shadow pr-10',
            'placeholder:text-gray-500 disabled:opacity-50',
            { 'pl-11': $slots['left-icon'], 'pt-8': kind === 'fancy' },
            invalid
              ? 'border-red-500 dark:border-red-500/95 focus:border-red-500 dark:focus:border-red-500/95 focus:ring-red-200 dark:focus:ring-red-200/30'
              : 'border-gray-300 dark:border-gray-800 focus:border-primary-500 dark:focus:border-primary-400 focus:ring-primary-200 dark:focus:ring-primary-200/30',
          ]"
          :placeholder="placeholder"
          :display-value="optionText"
          @blur="$emit('blur')"
          @change="query = $event.target.value"
        />
        <div class="absolute right-0.5 top-1/2 -translate-y-1/2">
          <ComboboxButton
            :as="Button"
            :class="['w-8 h-8', { 'mt-6': kind === 'fancy' }]"
            size="small"
            kind="ghost-alt"
            :title="$t('common-actions.show-options')"
          >
            <span class="sr-only">{{ $t('common-actions.show-options') }}</span>
            <ChevronUpDownIcon class="w-5 h-5" />
          </ComboboxButton>
        </div>
      </div>
      <MenuTransition>
        <ComboboxOptions
          ref="comboboxOptions"
          as="div"
          :class="[
            'absolute top-[--top] left-[--left] min-w-[20rem] max-w-[25rem] z-10 p-2',
            'space-y-1 bg-white dark:bg-gray-800',
            'shadow-primer-overlay dark:shadow-primer-overlay-dark',
            'rounded-xl mt-0.5 origin-top-right ring-1',
            'ring-black/5 dark:ring-gray-700',
          ]"
          :style="{
            '--top': position?.top !== undefined ? `${position.top}px` : '100%',
            '--left': position?.left !== undefined ? `${position.left}px` : '0px',
          }"
        >
          <div
            v-if="filteredOptions.length === 0"
            class="px-2 text-sm py-0.5"
          >
            {{ $t('searchable-combobox.no-results-found') }}
          </div>
          <ul v-else class="max-h-[18rem] overflow-y-auto rounded-lg">
            <ComboboxOption
              v-for="(option, i) of filteredOptions"
              v-slot="{ active, selected }"
              :key="optionValue ? optionValue(option) : option"
              :value="optionValue ? optionValue(option) : option"
              :disabled="disabledOptions.includes(i)"
              :class="[
                'select-none px-2 py-1.5 cursor-pointer text-sm',
                'rounded-lg flex gap-1 items-center',
                'ui-disabled:opacity-50 ui-disabled:cursor-not-allowed',
                'ui-active:bg-primary-100 dark:ui-active:bg-primary-600',
                'dark:text-gray-300 ui-active:text-primary-700 dark:ui-active:dark:text-primary-100',
              ]"
            >
              <slot name="option" :option="option" :active="active" :selected="selected">
                {{ optionText(option) }}
              </slot>
            </ComboboxOption>
          </ul>
        </ComboboxOptions>
      </MenuTransition>
    </Combobox>

    <p
      v-if="invalid && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1"
    >
      {{ errorMessage }}
    </p>
  </fieldset>
</template>
