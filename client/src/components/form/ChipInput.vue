<script setup lang="ts">
import { ComboboxInput, ComboboxOptions } from '@headlessui/vue'
import { XMarkIcon } from '@heroicons/vue/20/solid'
import type { AnchorAlignment } from '@primer/behaviors'
import type { ErrorObject } from '@vuelidate/core'

export interface ChipInputProps {
  align?: AnchorAlignment
  errors?: ErrorObject[]
  filter?: (query: string, option: any) => boolean
  inputClass?: string
  invalid?: boolean
  labelText?: string
  modelText?: (value: any) => string
  modelValue: any[]
  options: any[]
  optionText?: (value: any) => string
  optionValue?: (value: any) => any
  placeholder?: string
}

const props = withDefaults(defineProps<ChipInputProps>(), {
  align: 'start',
  errors: undefined,
  filter: undefined,
  invalid: false,
  modelText: (value: any) => String(value),
  optionText: (value: any) => String(value),
  optionValue: (value: any) => String(value),
})

const emit = defineEmits<{
  (e: 'blur'): void
  (e: 'update:model-value', modelValue: any[]): void
}>()

const { align, options, modelValue, optionValue, filter, optionText, errors } = toRefs(props)

const comboboxInput = ref<InstanceType<typeof ComboboxInput>>()
const comboboxOptions = ref<InstanceType<typeof ComboboxOptions>>()

const { position } = useAnchoredPosition({
  anchorElementRef: computed(() => comboboxInput.value?.$el),
  floatingElementRef: computed(() => comboboxOptions.value?.$el),
  side: 'outside-bottom',
  align,
  allowOutOfBounds: false,
})

const query = ref('')

const filterFunction = computed(() => {
  return filter.value ?? ((query: string, option: any) => {
    return optionText.value(option).toLowerCase().includes(query)
  })
})

const availableOptions = computed(() => {
  return options.value.filter(o => !modelValue.value.includes(optionValue.value(o)))
})

const filteredOptions = computed(() => {
  if (query.value.length === 0) {
    return availableOptions.value
  }

  return availableOptions.value.filter((option) => {
    return filterFunction.value(query.value.toLowerCase(), option)
  })
})

function handleOptionPicked(option: any) {
  const newValues = structuredClone(toRaw(modelValue.value))
  newValues.push(option)

  emit('update:model-value', newValues)
  query.value = ''
}

const isOverlayVisible = computed(() => comboboxOptions.value?.$el !== null)

function removeLastChip(event: KeyboardEvent) {
  if (query.value.length > 0 || modelValue.value.length === 0 || isOverlayVisible.value) {
    return
  }

  event.preventDefault()

  const newValues = structuredClone(toRaw(modelValue.value))
  newValues.pop()

  emit('update:model-value', newValues)
}

function removeAtIndex(index: number) {
  const newValues = structuredClone(toRaw(modelValue.value))
  newValues.splice(index, 1)

  emit('update:model-value', newValues)
}

const errorMessage = computed(() => errors.value?.[0]?.$message)
</script>

<template>
  <fieldset class="min-w-0 w-full disabled:opacity-60 motion-safe:transition">
    <div
      :class="[
        'min-w-0 block border bg-white dark:bg-gray-950',
        'shadow-sm rounded-md overflow-hidden',
        invalid
          ? 'border-red-500 dark:border-red-500/95'
          : 'border-gray-300 dark:border-gray-700',
      ]"
    >
      <label
        :class="[
          'font-medium text-xs px-3 pt-3 select-none block',
          'text-gray-700 dark:text-gray-300',
        ]"
        :for="String($attrs.id)"
      >
        {{ labelText }}
      </label>
      <ul class="flex flex-wrap items-center gap-2 px-3 py-2">
        <template v-for="(chip, i) in modelValue">
          <slot name="chip" :option="chip" :remove="() => removeAtIndex(i)">
            <li
              :key="i"
              :class="[
                'flex items-center gap-2 select-none',
                'px-2 py-0.5 rounded-lg tracking-wide text-sm',
                'bg-primary-100 text-primary-700',
                'dark:bg-gray-800 dark:text-gray-300',
              ]"
            >
              <span>
                {{ modelText(chip) }}
              </span>
              <Button
                class="w-6 h-6 -mr-1.5"
                size="small"
                kind="ghost-alt"
                rounded="full"
                tabindex="-1"
                :title="$t('common-actions.remove')"
                @click="removeAtIndex(i)"
              >
                <span class="sr-only">{{ $t('common-actions.remove') }}</span>
                <XMarkIcon class="w-4 h-4 light:text-primary-600 light:group-hover/button:text-primary-700" />
              </Button>
            </li>
          </slot>
        </template>

        <Combobox
          as="li"
          :model-value="undefined"
          @update:model-value="handleOptionPicked"
        >
          <ComboboxInput
            :id="String($attrs.id)"
            ref="comboboxInput"
            :class="[
              'w-full bg-white dark:bg-gray-950 rounded-md',
              'py-1.5 px-2',
              'dark:text-gray-200 hocus:ring-1 focus:outline-none border-0',
              'motion-safe:transition-shadow pr-10',
              'placeholder:text-gray-500 disabled:opacity-50',
              'hover:ring-gray-300 dark:hover:ring-gray-700',
              'focus:ring-primary-500 dark:focus:ring-primary-500',
              inputClass,
            ]"
            :placeholder="placeholder"
            :display-value="optionText"
            @keydown.backspace="removeLastChip"
            @blur="$emit('blur')"
            @change="query = $event.target.value"
          />
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
                  v-for="option of filteredOptions"
                  v-slot="{ active, selected }"
                  :key="optionValue ? optionValue(option) : option"
                  :value="optionValue ? optionValue(option) : option"
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
      </ul>
    </div>

    <p
      v-if="invalid && errorMessage"
      class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
    >
      {{ errorMessage }}
    </p>
  </fieldset>
</template>
