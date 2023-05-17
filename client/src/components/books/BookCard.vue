<script setup lang="ts">
import { BookOpenIcon, ExclamationCircleIcon } from '@heroicons/vue/24/outline'
import { createImageUrl } from '@/modules/api'
import type { BookEntity } from '@/types/tankobon-book'
import { getRelationship } from '@/utils/api'

export interface BookCardProps {
  book?: BookEntity
  current?: boolean
  imageOnly?: boolean
  loading?: boolean
  mode?: 'comfortable' | 'compact'
}

const props = withDefaults(defineProps<BookCardProps>(), {
  book: undefined,
  current: false,
  imageOnly: false,
  loading: false,
  mode: 'comfortable',
})

const { book, loading, mode } = toRefs(props)
const { t } = useI18n()

const card = ref<HTMLDivElement>()

const coverArt = computed(() => getRelationship(book.value, 'COVER_ART')?.attributes)
const coverUrl = computed(() => {
  return createImageUrl({
    fileName: coverArt.value?.versions['256'],
    timeHex: coverArt.value?.timeHex,
  })
})

const {
  imageHasError,
  imageLoading,
  setupObserver,
  observerCreated,
} = useImageLazyLoader(coverUrl, card)

onMounted(() => {
  if (!loading.value && book.value) {
    setupObserver()
  }
})

watch(loading, (newValue) => {
  if (!newValue && !observerCreated.value) {
    setupObserver()
  }
})

const volume = computed(() => {
  if (!book.value) {
    return ''
  }

  const isSingle = book.value.attributes.number.length === 0

  return isSingle
    ? t('books.single')
    : t('books.volume', [book.value.attributes.number])
})
</script>

<template>
  <div v-if="loading">
    <div class="skeleton shadow rounded-xl aspect-w-2 aspect-h-3" />
    <div v-if="mode === 'comfortable' && !imageOnly" class="mt-3 space-y-1">
      <div class="skeleton h-4 w-3/4 rounded" />
      <div class="skeleton h-3 w-1/2 rounded" />
    </div>
  </div>
  <div
    v-else
    ref="card"
  >
    <div
      :class="[
        'relative shadow rounded-xl overflow-hidden block',
        'bg-gray-200 dark:bg-gray-800 aspect-[2/3]',
        'motion-safe:transition-all will-change-[transform,box-shadow]',
      ]"
    >
      <FadeTransition>
        <div
          v-if="imageLoading || imageHasError"
          class="w-full h-full flex justify-center items-center"
          aria-hidden="true"
        >
          <BookOpenIcon
            v-if="loading || imageLoading || coverUrl?.length === 0"
            :class="[
              { 'motion-safe:animate-pulse': imageLoading },
              'w-10 h-10 text-gray-400 dark:text-gray-500',
            ]"
          />
          <ExclamationCircleIcon
            v-else
            class="w-10 h-10 text-gray-400 dark:text-gray-500"
          />
        </div>
        <div
          v-else
          class="w-full h-full overflow-hidden"
        >
          <img
            class="w-full h-full object-cover"
            :src="coverUrl ?? undefined"
            :alt="book?.attributes.title ?? ''"
          >
        </div>
      </FadeTransition>

      <FadeTransition>
        <div
          v-if="(mode === 'compact' && !imageOnly) || (imageOnly && imageHasError)"
          :class="[
            'absolute top-0 left-0 w-full h-full flex justify-end',
            'py-2 px-2 lg:pb-3 lg:px-3',
            'bg-gradient-to-b from-black/70 via-60%',
            imageOnly && imageHasError ? 'items-start' : 'items-end',
          ]"
        >
          <div class="flex text-white w-full items-center">
            <div class="flex flex-col grow min-w-0">
              <span
                :class="[
                  'font-semibold font-sans-safe text-[0.8rem]',
                  'sm:text-sm truncate max-w-full',
                ]"
              >
                {{ book?.attributes.title }}
              </span>
              <span class="font-medium text-xxs sm:text-xs">
                {{ volume }}
              </span>
            </div>
          </div>
        </div>
      </FadeTransition>
    </div>

    <div
      v-if="$slots.actions"
      class="w-full relative -mt-4 flex justify-center motion-safe:transition"
    >
      <slot name="actions" />
    </div>

    <div
      v-if="mode === 'comfortable' && !imageOnly"
      class="mt-3"
    >
      <span
        :class="[
          'text-[0.8rem] sm:text-sm font-sans-safe font-semibold',
          'truncate text-gray-900 dark:text-gray-200 block',
        ]"
      >
        {{ book?.attributes.title }}
      </span>
      <span
        :class="[
          'block text-xxs sm:text-xs font-medium truncate',
          'text-gray-600 dark:text-gray-400',
        ]"
      >
        {{ volume }}
      </span>
    </div>
  </div>
</template>
