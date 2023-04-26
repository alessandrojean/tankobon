<script lang="ts" setup>
import { getFullImageUrl } from '@/modules/api';
import { BookEntity } from '@/types/tankobon-book'
import { getRelationship } from '@/utils/api';

import {
  ExclamationCircleIcon,
  EyeIcon,
  MagnifyingGlassPlusIcon,
  PhotoIcon
} from '@heroicons/vue/24/outline'

export interface BookCoverProps {
  book: BookEntity | null | undefined
  loading?: boolean
}

const props = withDefaults(defineProps<BookCoverProps>(), {
  book: undefined,
  collection: undefined,
  loading: false,
})

const { book, loading } = toRefs(props)
const coverArt = computed(() => getRelationship(book.value, 'COVER_ART'))

const coverUrl = computed(() => {
  if (!book.value) {
    return ''
  }

  return getFullImageUrl({
    collection: 'covers',
    fileName: coverArt.value?.attributes?.fileName,
    timeHex: coverArt.value?.attributes?.timeHex,
  }) ?? ''
})

const { imageHasError, imageLoading, imageAspectRatio, loadImage } =
  useImageLoader(coverUrl)

const showBookCover = computed(() => {
  return !imageHasError.value && !imageLoading.value && showBookInfo.value
})

const showBookInfo = computed(() => {
  return !loading.value && book.value !== null
})

watch(book, (newValue) => {
  if (newValue !== null) {
    loadImage()
  }
})

onMounted(() => {
  if (book.value !== null) {
    loadImage()
  }
})

const dialogOpen = ref(false)

function openDialog() {
  dialogOpen.value = true
}

function closeDialog() {
  dialogOpen.value = false
}

const { t } = useI18n({ useScope: 'global' })
</script>

<template>
  <figure
    :class="[
      'group aspect-[--aspect] rounded-xl overflow-hidden',
      'bg-gray-200 dark:bg-gray-800 relative shadow-md'
    ]"
    :style="{ '--aspect': imageAspectRatio }"
  >
    <FadeTransition>
      <img
        v-if="showBookCover"
        :src="coverUrl"
        :alt="
          book?.attributes?.title
            ? t('books.cover', { title: book.attributes.title })
            : undefined
        "
        class="w-full h-full"
      />
      <div v-else class="w-full h-full flex items-center justify-center">
        <PhotoIcon
          v-if="imageLoading || loading || coverUrl.length === 0"
          :class="[
            'w-10 h-10 text-gray-500 dark:text-gray-600',
            imageLoading || loading ? 'motion-safe:animate-pulse' : ''
          ]"
        />
        <ExclamationCircleIcon
          v-else
          class="w-10 h-10 text-gray-500 dark:text-gray-600"
        />
      </div>
    </FadeTransition>

    <button
      v-if="showBookCover"
      class="z-10 bg-gray-900/60 flex items-center justify-center absolute inset-0 w-full h-full opacity-0 group-hover:opacity-100 focus-visible:opacity-100 focus:outline-none motion-safe:transition-opacity"
      type="button"
      :title="$t('common-actions.zoom')"
      @click="openDialog"
    >
      <MagnifyingGlassPlusIcon class="w-8 h-8 text-white" />
    </button>

    <slot />

    <BookCoverDialog
      v-if="showBookCover && showBookInfo"
      :cover-url="coverUrl"
      :open="dialogOpen"
      @close="closeDialog"
    />
  </figure>
</template>