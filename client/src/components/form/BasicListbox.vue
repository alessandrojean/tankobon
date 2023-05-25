<script lang="ts" setup generic="TValue extends string | number | boolean | object | null | undefined, TItem">
import type { ErrorObject } from '@vuelidate/core'
import { CheckIcon, ChevronUpDownIcon } from '@heroicons/vue/20/solid'
import { Listbox, ListboxOption, ListboxOptions } from '@headlessui/vue'
import type { AnchorAlignment } from '@primer/behaviors'

const props = withDefaults(defineProps<{
  align?: AnchorAlignment
  checkIcon?: boolean
  disabledOptions?: number[]
  errors?: ErrorObject[]
  fitWidth?: boolean
  invalid?: boolean
  labelText?: string
  modelValue: TValue
  options: TItem[]
  optionText?: (value: TItem | undefined, index: number) => string
  optionValue?: (value: TItem) => TValue
  placeholder?: string
  size?: 'normal' | 'small'
}>(), {
  align: 'start',
  checkIcon: true,
  disabledOptions: () => [] as number[],
  errors: undefined,
  fitWidth: false,
  invalid: false,
  labelText: undefined,
  size: 'normal',
  optionText: (value: TItem | undefined) => String(value),
  optionValue: (value: TItem) => String(value) as TValue,
})

const emit = defineEmits<{
  (e: 'update:modelValue', modelValue: TValue): void
}>()

const { optionValue, options, align } = toRefs(props)

const listboxButton = ref<HTMLElement>()
const listboxOptions = ref<InstanceType<typeof ListboxOptions>>()

const { position } = useAnchoredPosition({
  anchorElementRef: listboxButton,
  floatingElementRef: computed(() => listboxOptions.value?.$el),
  side: 'outside-bottom',
  align,
  allowOutOfBounds: false,
})

const selected = computed<{ index: number; option: TItem }>(() => {
  const index = props.options.findIndex(v => optionValue.value(v) === props.modelValue)

  return { index, option: options.value[index] }
})
</script>

<template>
  <Listbox
    :model-value="modelValue as TValue"
    as="div"
    class="relative"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <ListboxLabel v-if="labelText" class="sr-only">
      {{ labelText }}
    </ListboxLabel>
    <div ref="listboxButton">
      <slot
        name="listbox-button"
        :value="modelValue"
        :selected="selected"
        :text="optionText(selected.option, selected.index)"
      >
        <ListboxButton
          class="relative w-full cursor-default rounded-md bg-white dark:bg-gray-800 pl-3 pr-10 text-left shadow-sm focus:outline-none focus:ring ui-open:ring border border-gray-300 dark:border-gray-800 focus:border-primary-500 dark:focus:border-primary-400 focus:ring-primary-200 dark:focus:ring-primary-200/30 ui-open:border-primary-500 dark:ui-open:border-primary-400 ui-open:ring-primary-200 dark:ui-open:ring-primary-200/30"
          :class="[
            size === 'small' ? 'text-sm py-1.5' : 'py-2',
          ]"
        >
          <slot
            name="button"
            :selected="selected"
            :text="optionText(selected.option, selected.index)"
          >
            <span class="block truncate">
              {{ optionText(selected.option, selected.index) }}
            </span>
          </slot>
          <span
            class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2"
          >
            <ChevronUpDownIcon
              class="text-gray-400"
              :class="[size === 'small' ? 'w-4 h-4' : 'w-5 h-5']"
              aria-hidden="true"
            />
          </span>
        </ListboxButton>
      </slot>
    </div>

    <MenuTransition>
      <ListboxOptions
        ref="listboxOptions"
        as="div"
        :class="[
          'absolute top-[--top] left-[--left] z-10 p-2',
          'space-y-1 bg-white dark:bg-gray-800',
          'shadow-primer-overlay dark:shadow-primer-overlay-dark',
          'rounded-xl mt-0.5 origin-top-right ring-1',
          'ring-black/5 dark:ring-gray-700 focus:outline-none',
          fitWidth ? 'w-fit' : 'min-w-[20rem] max-w-[25rem]',
        ]"
        :style="{
          '--top': position?.top !== undefined ? `${position.top}px` : '100%',
          '--left': position?.left !== undefined ? `${position.left}px` : '0px',
        }"
      >
        <ul class="max-h-[18rem] overflow-y-auto rsounded-lg">
          <ListboxOption
            v-for="(option, i) of options"
            v-slot="{ active, selected: optionSelected }"
            :key="String(optionValue(option))"
            :value="optionValue(option)"
            :disabled="disabledOptions.includes(i)"
            :class="[
              'relative cursor-pointer select-none py-1.5 rounded-lg text-sm',
              'ui-disabled:opacity-50 ui-disabled:cursor-not-allowed',
              'ui-active:bg-primary-100 dark:ui-active:bg-primary-600',
              'dark:text-gray-300 ui-active:text-primary-700',
              'dark:ui-active:dark:text-primary-100',
              {
                'pl-10 pr-2': checkIcon,
                'px-2': !checkIcon,
              },
            ]"
          >
            <slot
              name="option"
              :active="active"
              :selected="optionSelected"
              :option="option"
              :text="optionText(option, i)"
            >
              <span
                class="block truncate"
                :class="[
                  optionSelected ? 'font-medium' : 'font-normal',
                ]"
              >
                {{ optionText(option, i) }}
              </span>
            </slot>
            <span
              v-if="optionSelected && checkIcon"
              class="absolute inset-y-0 left-2 flex items-center text-primary-600"
              :class="[
                active ? 'dark:text-primary-200' : 'dark:text-primary-400',
              ]"
            >
              <CheckIcon class="h-5 w-5" aria-hidden="true" />
            </span>
          </ListboxOption>
        </ul>
      </ListboxOptions>
    </MenuTransition>
  </Listbox>
</template>
