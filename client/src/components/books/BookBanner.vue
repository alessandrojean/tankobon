<script lang="ts" setup>
import { getFullImageUrl } from '@/modules/api';
import { BookEntity } from '@/types/tankobon-book'
import { getRelationship } from '@/utils/api';

export interface BookCoverProps {
  book: BookEntity | null | undefined
  loading?: boolean
}

const props = withDefaults(defineProps<BookCoverProps>(), {
  book: undefined,
  loading: false
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
    timeHex: coverArt.value?.attributes?.timeHex
  }) ?? ''
})

const { imageHasError, imageLoading, loadImage } = useImageLoader(coverUrl)

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
</script>

<template>
  <div
    aria-hidden="true"
    class="h-72 sm:h-80 w-full overflow-hidden relative bg-primary-300 dark:bg-gray-900"
  >
    <FadeTransition>
      <img
        v-if="showBookCover"
        :src="coverUrl"
        :alt="book?.attributes.title ?? undefined"
        class="w-full h-full scale-105 object-cover filter blur"
      />
      <div v-else class="relative w-full h-full">
        <img
          src="@/assets/library-unsplash.jpg"
          alt=""
          class="w-full h-full scale-105 object-cover filter blur"
        />
        <div
          aria-hidden="true"
          class="absolute inset-0 opacity-70 bg-gradient-to-br from-primary-900/80 to-primary-500/80"
        />
      </div>
    </FadeTransition>

    <div
      :class="[
        'absolute inset-0 bg-gradient-to-b sm:bg-gradient-to-r',
        'from-white/40 sm:from-gray-900/80 dark:from-gray-800 sm:dark:from-gray-900/80',
        'via-white/70 sm:via-gray-900/60 dark:via-gray-900/60 sm:dark:via-gray-900/60',
        'to-white sm:to-gray-900/20 dark:to-gray-900 sm:dark:to-gray-900/20'
      ]"
    />
  </div>
</template>