<script setup lang="ts">
import { XMarkIcon } from '@heroicons/vue/20/solid'
import { helpers, required } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import type { TagEntity } from '@/types/tankobon-tag'

export interface BookTagFormCardProps {
  bookTags: string[]
  draggable: boolean
  index: number
  invalid?: boolean
  tags: TagEntity[]
  tag: TagEntity | undefined
}

const props = withDefaults(defineProps<BookTagFormCardProps>(), {
  invalid: false,
})

defineEmits<{
  (e: 'update:tag', tagId: string): void
  (e: 'click:remove'): void
}>()

const { tag, tags, bookTags } = toRefs(props)
const { t } = useI18n()

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)

  return {
    tag: { messageRequired },
  }
})

const v$ = useVuelidate(rules, { tag })

defineExpose({ v$ })

const disabledIndexes = computed(() => {
  const currentIndex = tags.value.findIndex(p => p.id === tag.value?.id)

  return bookTags.value
    .map(id => tags.value.findIndex(p => p.id === id))
    .filter(i => i >= 0 && i !== currentIndex)
})
</script>

<template>
  <div
    :class="[
      'py-4 md:py-3 px-4 rounded-xl',
      'flex flex-row items-start gap-4',
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
    <SearchableCombobox
      :id="`tag-input-${index}`"
      class="grow"
      :placeholder="$t('common-placeholders.book-tag')"
      :label-text="$t('common-fields.tag')"
      :model-value="tag"
      :options="tags ?? []"
      :option-text="(p: TagEntity) => p?.attributes?.name"
      :option-value="(p: TagEntity) => p"
      :option-value-select="(p: TagEntity) => p?.id"
      :invalid="v$.tag.$error"
      :errors="v$.tag.$errors"
      :disabled-options="disabledIndexes"
      @blur="v$.tag.$touch()"
      @update:model-value="$emit('update:tag', $event.id)"
      @update:model-value-select="$emit('update:tag', $event)"
    />
    <Button
      class="shrink-0 w-10 h-10 -mr-2 hidden md:flex mt-px"
      kind="ghost-alt"
      size="small"
      :title="$t('common-actions.remove')"
      @click="$emit('click:remove')"
    >
      <span class="sr-only">{{ $t('common-actions.remove') }}</span>
      <XMarkIcon class="w-5 h-5" />
    </Button>
  </div>
</template>
