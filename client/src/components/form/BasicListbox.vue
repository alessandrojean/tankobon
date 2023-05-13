<script lang="ts" setup>
import type { ErrorObject } from '@vuelidate/core'
import { CheckIcon, ChevronUpDownIcon } from '@heroicons/vue/20/solid'
import { Listbox, ListboxOption, ListboxOptions } from '@headlessui/vue'

export interface BasicListboxProps {
  checkIcon?: boolean
  disabledOptions?: number[]
  errors?: ErrorObject[]
  invalid?: boolean
  modelValue: any
  options: any[]
  optionText?: (value: any) => string
  optionValue?: (value: any) => string
  placeholder?: string
  size?: 'normal' | 'small'
}

const props = withDefaults(defineProps<BasicListboxProps>(), {
  checkIcon: true,
  disabledOptions: () => [],
  errors: undefined,
  invalid: false,
  size: 'normal',
  optionText: (value: any) => String(value),
  optionValue: (value: any) => String(value),
})

const emit = defineEmits<{
  (e: 'update:modelValue', modelValue: any): void
}>()

const { optionValue, options } = toRefs(props)

const listboxButton = ref<HTMLElement>()
const listboxOptions = ref<InstanceType<typeof ListboxOptions>>()

const { position } = useAnchoredPosition({
  anchorElementRef: listboxButton,
  floatingElementRef: computed(() => listboxOptions.value?.$el),
  side: 'outside-bottom',
  align: 'start',
  allowOutOfBounds: false,
})
</script>

<script lang="ts">
export default { components: { ChevronUpDownIcon, CheckIcon }, inheritAttrs: false }
</script>

<template>
  <Listbox
    :model-value="optionValue ? optionValue(modelValue) : modelValue"
    as="div"
    class="relative"
    :class="[$attrs.class]" @update:model-value="emit('update:modelValue', $event)"
  >
    <div ref="listboxButton">
      <slot
        name="listbox-button"
        :value="optionValue(modelValue)"
        :text="optionText(modelValue)"
      >
        <ListboxButton
          class="relative w-full cursor-default rounded-md bg-white dark:bg-gray-800 pl-3 pr-10 text-left shadow-sm focus:outline-none focus:ring ui-open:ring border border-gray-300 dark:border-gray-800 focus:border-primary-500 dark:focus:border-primary-400 focus:ring-primary-200 dark:focus:ring-primary-200/30 ui-open:border-primary-500 dark:ui-open:border-primary-400 ui-open:ring-primary-200 dark:ui-open:ring-primary-200/30"
          :class="[
            size === 'small' ? 'text-sm py-1.5' : 'py-2',
          ]"
        >
          <slot name="button">
            <span class="block truncate">
              {{ optionText(modelValue) }}
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
        <ul class="max-h-[18rem] overflow-y-auto rsounded-lg">
          <ListboxOption
            v-for="(option, i) of options"
            v-slot="{ active, selected }"
            :key="optionValue ? optionValue(option) : option"
            :value="optionValue ? optionValue(option) : option"
            :disabled="disabledOptions.includes(i)"
            :class="[
              'relative cursor-pointer select-none py-1.5 rounded-lg',
              'ui-disabled:opacity-50 ui-disabled:cursor-not-allowed',
              'ui-active:bg-primary-100 dark:ui-active:bg-primary-600',
              'dark:text-gray-300 ui-active:text-primary-700 dark:ui-active:dark:text-primary-100',
              {
                'pl-12 pr-2': checkIcon,
                'px-2': !checkIcon,
              },
            ]"
          >
            <slot
              name="option"
              :active="active"
              :selected="selected"
              :option="option"
            >
              <span
                class="block truncate"
                :class="[
                  selected ? 'font-medium' : 'font-normal',
                ]"
              >
                {{ optionText(option) }}
              </span>
            </slot>
            <span
              v-if="selected && checkIcon"
              class="absolute inset-y-0 left-0.5 flex items-center pl-3 text-primary-600"
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
