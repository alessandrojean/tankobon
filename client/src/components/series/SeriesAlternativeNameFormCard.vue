<script setup lang="ts">
import { XMarkIcon } from '@heroicons/vue/20/solid'
import { helpers, required } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'

export interface BookContributorFormCardProps {
  disabled?: boolean
  draggable: boolean
  index: number
  invalid?: boolean
  language: string
  languages: string[]
  name: string
}

const props = withDefaults(defineProps<BookContributorFormCardProps>(), {
  disabled: false,
  invalid: false,
})

defineEmits<{
  (e: 'update:language', language: string): void
  (e: 'update:name', name: string): void
  (e: 'click:remove'): void
}>()

const { language, name } = toRefs(props)
const { t } = useI18n()

function notNullValue(value: string) {
  return value !== 'null'
}

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)
  const messageNotNull = helpers.withMessage(t('validation.not-unknown'), notNullValue)

  return {
    language: { messageRequired, messageNotNull },
    name: { messageRequired },
  }
})

const v$ = useVuelidate(rules, { language, name })

defineExpose({ v$ })
</script>

<template>
  <fieldset
    :disabled="disabled"
    :class="[
      'py-4 md:py-3 px-4 rounded-xl',
      'flex items-start gap-4',
      'motion-safe:transition',
      'bg-gray-100 dark:bg-gray-900',
      {
        'ring-2 ring-red-600 dark:ring-red-500/60': invalid,
      },
    ]"
  >
    <span
      :class="[
        'grabber shrink-0 text-gray-500 dark:text-gray-300 mt-2.5',
        {
          'cursor-grab': draggable,
          'opacity-50': !draggable,
        },
      ]"
    >
      <svg aria-hidden="true" viewBox="0 0 16 16" version="1.1" class="w-5 h-5" fill="currentColor">
        <path d="M10 13a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm0-4a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm-4 4a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm5-9a1 1 0 1 1-2 0 1 1 0 0 1 2 0ZM7 8a1 1 0 1 1-2 0 1 1 0 0 1 2 0ZM6 5a1 1 0 1 1 0-2 1 1 0 0 1 0 2Z" />
      </svg>
    </span>

    <AlternativeNameInput
      class="grow -mr-2"
      kind="basic"
      :index="index"
      :name="name"
      :language="language"
      :languages="languages"
      :placeholder="$t('common-placeholders.series-name')"
      :invalid-language="v$.language.$error"
      :invalid-name="v$.name.$error"
      :errors-language="v$.language.$errors"
      :errors-name="v$.name.$errors"
      @blur:name="v$.name.$touch()"
      @blur:language="v$.language.$touch()"
      @update:name="$emit('update:name', $event)"
      @update:language="$emit('update:language', $event)"
    />

    <Button
      class="shrink-0 w-10 h-10 -mr-2 mt-px"
      kind="ghost-alt"
      size="small"
      :title="$t('common-actions.remove')"
      @click="$emit('click:remove')"
    >
      <span class="sr-only">{{ $t('common-actions.remove') }}</span>
      <XMarkIcon class="w-5 h-5" />
    </Button>
  </fieldset>
</template>
