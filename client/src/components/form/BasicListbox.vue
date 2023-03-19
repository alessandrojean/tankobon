<script lang="ts" setup>
import type { HTMLAttributes } from 'vue'
import type { ErrorObject } from '@vuelidate/core'
import { CheckIcon, ChevronUpDownIcon } from '@heroicons/vue/20/solid';

export interface BasicListboxProps {
  errors?: ErrorObject[],
  invalid?: boolean,
  modelValue: any,
  options: any[],
  optionText?: (value: any) => string,
  optionValue?: (value: any) => string,
  placeholder?: string,
  size?: 'normal' | 'small',
}

const props = withDefaults(defineProps<BasicListboxProps>(), {
  errors: undefined,
  invalid: false,
  size: 'normal',
  optionText: (value: any) => String(value),
  optionValue: (value: any) => String(value),
})

const emit = defineEmits<{
  (e: 'update:modelValue', modelValue: any): void
}>()

const {  optionValue, options } = toRefs(props)

function handleChange(event: Event) {
  const newValue = (event.target! as HTMLSelectElement).value
  const toEmit = options.value.find((option) => optionValue.value(option) === newValue)

  emit('update:modelValue', toEmit)
}
</script>

<script lang="ts">
export default { inheritAttrs: false, components: { ChevronUpDownIcon, CheckIcon } }
</script>

<template>
  <Listbox
    :model-value="optionValue ? optionValue(modelValue) : modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    as="div"
    :class="['relative', $attrs.class]"
  >
    <ListboxButton
      :class="[
        'relative w-full cursor-default rounded-md',
        'bg-white dark:bg-gray-900 py-2 pl-3 pr-10 text-left',
        'shadow-sm focus:outline-none focus:ring ui-open:ring border',
        'border-gray-300 dark:border-gray-700',
        'focus:border-primary-500 dark:focus:border-primary-400',
        'focus:ring-primary-200 dark:focus:ring-primary-200/30',
        'ui-open:border-primary-500 dark:ui-open:border-primary-400',
        'ui-open:ring-primary-200 dark:ui-open:ring-primary-200/30'
      ]"
    >
      <slot name="button">
        <span class="block truncate">
          {{ optionText(modelValue) }}
        </span>
      </slot>
      <span
        :class="[
          'pointer-events-none absolute inset-y-0 right-0',
          'flex items-center pr-2',
        ]"
      >
        <ChevronUpDownIcon
          class="w-5 h-5 text-gray-400"
          aria-hidden="true"
        />
      </span>
    </ListboxButton>

    <transition
      leave-active-class="transition duration-100 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <ListboxOptions
        :class="[
          'absolute mt-1 max-h-60 w-full overflow-auto rounded-md',
          'bg-white py-1 text-base shadow-lg ring-1 ring-black/5',
          'focus:outline-none dark:bg-gray-800 dark:ring-gray-700'
        ]"
      >
        <ListboxOption
          v-slot="{ active, selected }"
          v-for="option of options"
          :key="optionValue ? optionValue(option) : option"
          :value="optionValue ? optionValue(option) : option"
          as="template"
        >
          <li
            :class="[
              'relative cursor-default select-none py-2 pl-12 pr-4',
              {
                'bg-primary-100 text-primary-800': active,
                'dark:bg-primary-600 dark:text-primary-100': active,
                'text-gray-900 dark:text-gray-300': !active,
              }
            ]"
          >
            <slot
              name="option"
              :active="active"
              :selected="selected"
              :option="option"
            >
              <span
                :class="[
                  selected ? 'font-medium' : 'font-normal',
                  'block truncate'
                ]"
              >
                {{ optionText(option) }}
              </span>
            </slot>
            <span
              v-if="selected"
              :class="[
                'absolute inset-y-0 left-0.5 flex items-center pl-3',
                'text-primary-600',
                active ? 'dark:text-primary-200' : 'dark:text-primary-400',
              ]"
            >
              <CheckIcon class="h-5 w-5" aria-hidden="true" />
            </span>
          </li>
        </ListboxOption>
      </ListboxOptions>
    </transition>
  </Listbox>
</template>
