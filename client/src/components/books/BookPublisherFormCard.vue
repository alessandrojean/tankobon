<script setup lang="ts">
import { BuildingOffice2Icon, XMarkIcon } from '@heroicons/vue/20/solid'
import { helpers, required } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import { getFullImageUrl } from '@/modules/api'
import { getRelationship } from '@/utils/api'
import type { PublisherEntity } from '@/types/tankobon-publisher'

export interface BookPublisherFormCardProps {
  bookPublishers: string[]
  draggable: boolean
  index: number
  invalid?: boolean
  publishers: PublisherEntity[]
  publisher: PublisherEntity | undefined
}

const props = withDefaults(defineProps<BookPublisherFormCardProps>(), {
  invalid: false,
})

defineEmits<{
  (e: 'update:publisher', publisherId: string): void
  (e: 'click:remove'): void
}>()

const { publisher, publishers, bookPublishers } = toRefs(props)
const { t } = useI18n()

const rules = computed(() => {
  const messageRequired = helpers.withMessage(t('validation.required'), required)

  return {
    publisher: { messageRequired },
  }
})

const v$ = useVuelidate(rules, { publisher })

defineExpose({ v$ })

function getPublisherPicture(publisher: PublisherEntity) {
  return getRelationship(publisher, 'PUBLISHER_PICTURE')
}

const disabledIndexes = computed(() => {
  const currentIndex = publishers.value.findIndex(p => p.id === publisher.value?.id)

  return bookPublishers.value
    .map(id => publishers.value.findIndex(p => p.id === id))
    .filter(i => i >= 0 && i !== currentIndex)
})

const publisherPicture = computed(() => {
  return getRelationship(publisher.value, 'PUBLISHER_PICTURE')?.attributes
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
      :id="`publisher-input-${index}`"
      class="grow"
      :placeholder="$t('common-placeholders.book-publisher')"
      :label-text="$t('common-fields.publisher')"
      :model-value="publisher"
      :options="publishers ?? []"
      :option-text="(p: PublisherEntity) => p?.attributes?.name"
      :option-value="(p: PublisherEntity) => p"
      :option-value-select="(p: PublisherEntity) => p?.id"
      :invalid="v$.publisher.$error"
      :errors="v$.publisher.$errors"
      :disabled-options="disabledIndexes"
      @blur="v$.publisher.$touch()"
      @update:model-value="$emit('update:publisher', $event.id)"
      @update:model-value-select="$emit('update:publisher', $event)"
    >
      <template #left-icon>
        <div class="absolute left-2 top-1/2 -translate-y-1/2">
          <Avatar
            square
            size="extra-mini"
            :empty-icon="BuildingOffice2Icon"
            :picture-url="
              getFullImageUrl({
                collection: 'publishers',
                fileName: publisherPicture?.versions['128'],
                timeHex: publisherPicture?.timeHex,
              })
            "
          />
        </div>
      </template>
      <template #option="{ option }">
        <div class="flex items-center gap-3 w-full">
          <Avatar
            square
            class="-ml-0.5 shrink-0"
            size="extra-mini"
            :empty-icon="BuildingOffice2Icon"
            :picture-url="
              getFullImageUrl({
                collection: 'publishers',
                fileName: getPublisherPicture(option)?.attributes?.versions['128'],
                timeHex: getPublisherPicture(option)?.attributes?.timeHex,
              })
            "
          />
          <div class="grow">
            {{ (option as PublisherEntity).attributes!.name }}
          </div>
        </div>
      </template>
    </SearchableCombobox>
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
