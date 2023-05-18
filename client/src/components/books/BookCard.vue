<script setup lang="ts">
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

const coverArt = computed(() => getRelationship(book.value, 'COVER_ART')?.attributes)

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
  <div v-else class="relative group/book-card">
    <ImageCover
      v-slot="{ imageHasError }"
      :class="[
        'motion-safe:transition ring-1 ring-black/5',
        'will-change-[transform,box-shadow]',
        'group-hover/book-card:-translate-y-1',
        'group-hover/book-card:shadow-lg',
      ]"
      aspect-ratio="2/3"
      version="256"
      :zoomable="false"
      :loading="loading"
      :image="coverArt"
      :alt="book?.attributes?.title ?? ''"
    >
      <FadeTransition>
        <div
          v-if="(mode === 'compact' && !imageOnly) || (imageOnly && imageHasError)"
          :class="[
            'absolute top-0 left-0 w-full h-full flex justify-end',
            'py-2 px-2 lg:pb-3 lg:px-3',
            'bg-gradient-to-t from-black/70 via-60%',
            imageOnly && imageHasError ? 'items-start' : 'items-end',
          ]"
        >
          <div class="flex text-white w-full items-center">
            <div class="flex flex-col grow min-w-0">
              <RouterLink
                :to="{ name: 'books-id', params: { id: book?.id } }"
                :class="[
                  'font-semibold font-sans-safe text-[0.8rem] rounded',
                  'sm:text-sm truncate max-w-full focus:outline-none',
                  'motion-safe:transition',
                  'focus-visible:ring-2 focus-visible:ring-black dark:focus-visible:ring-white/90'
                ]"
              >
                <span class="absolute inset-0 z-20 rounded-xl" />
                <span>{{ book?.attributes.title }}</span>
              </RouterLink>
              <span class="font-medium text-xxs sm:text-xs">
                {{ volume }}
              </span>
            </div>
          </div>
        </div>
      </FadeTransition>
    </ImageCover>

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
      <RouterLink
        :to="{ name: 'books-id', params: { id: book?.id } }"
        :class="[
          'text-[0.8rem] sm:text-sm font-sans-safe font-semibold rounded',
          'truncate text-gray-900 dark:text-gray-200 block focus:outline-none',
          'motion-safe:transition',
          'focus-visible:ring-2 focus-visible:ring-black dark:focus-visible:ring-white/90'
        ]"
      >
        <span class="absolute inset-0 z-20 rounded-xl" />
        <span>{{ book?.attributes.title }}</span>
      </RouterLink>
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
