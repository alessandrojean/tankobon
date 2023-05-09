<script lang="ts" setup>
import { decode as decodeBlurHash } from 'blurhash'
import {
  BookOpenIcon,
  ExclamationCircleIcon,
  MagnifyingGlassPlusIcon,
} from '@heroicons/vue/24/outline'
import { getFullImageUrl } from '@/modules/api'
import type { BookEntity } from '@/types/tankobon-book'
import { getRelationship } from '@/utils/api'

export interface BookCoverProps {
  book: BookEntity | null | undefined
  loading?: boolean
}

const props = withDefaults(defineProps<BookCoverProps>(), {
  book: undefined,
  loading: false,
})

const { book, loading } = toRefs(props)
const coverArt = computed(() => getRelationship(book.value, 'COVER_ART'))

function getBookCoverUrl(version: string) {
  if (!book.value) {
    return ''
  }

  return getFullImageUrl({
    collection: 'covers',
    fileName: version === 'original'
      ? coverArt.value?.attributes?.fileName
      : coverArt.value?.attributes?.versions?.[version],
    timeHex: coverArt.value?.attributes?.timeHex,
  }) ?? ''
}

const coverUrl = computed(() => getBookCoverUrl('256'))
const coverOriginalUrl = computed(() => getBookCoverUrl('original'))

const { imageHasError, imageLoading, loadImage }
  = useImageLoader(coverUrl)

const showBookInfo = computed(() => {
  return !loading.value && book.value !== null
})

const showBookCover = computed(() => {
  return !imageHasError.value && !imageLoading.value && showBookInfo.value
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

const figure = ref<HTMLElement>()
const canvas = ref<HTMLCanvasElement>()

const imageAspectRatio = computed(() => {
  return coverArt.value ? coverArt.value.attributes!.aspectRatio : '2 / 3'
})

whenever(coverArt, async (coverArt) => {
  if (!canvas.value || !figure.value) {
    return
  }

  const { width, height, blurHash } = coverArt.attributes!

  const figureWidth = figure.value.clientWidth
  const figureHeight = Math.floor((figureWidth * height) / width)

  const pixels = decodeBlurHash(blurHash, figureHeight, figureHeight)
  const context = canvas.value.getContext('2d')!
  const imageData = context.createImageData(figureHeight, figureHeight)
  imageData.data.set(pixels)
  context.putImageData(imageData, 0, 0)
})
</script>

<template>
  <figure
    ref="figure"
    class="group aspect-[--aspect] rounded-xl overflow-hidden bg-gray-200 dark:bg-gray-800 relative shadow-md"
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
      >
      <div v-else-if="loading || coverUrl.length === 0 || imageHasError" class="w-full h-full flex items-center justify-center">
        <BookOpenIcon
          v-if="loading || coverUrl.length === 0"
          class="w-10 h-10 text-gray-500 dark:text-gray-600"
          :class="[
            loading ? 'motion-safe:animate-pulse' : '',
          ]"
        />
        <ExclamationCircleIcon
          v-else
          class="w-10 h-10 text-gray-500 dark:text-gray-600"
        />
      </div>
    </FadeTransition>

    <canvas
      ref="canvas" class="w-full h-full motion-safe:transition-opacity motion-safe:duration-500 motion-safe:animate-pulse"
      :class="[
        { 'opacity-0': showBookCover || loading || coverUrl.length === 0 },
      ]"
    />

    <button
      v-if="showBookCover"
      :class="[
        'z-10 bg-gray-900/60 flex items-center justify-center',
        'absolute inset-0 w-full h-full opacity-0 rounded-xl',
        'group-hover:opacity-100 focus-visible:opacity-100 focus:outline-none',
        'motion-safe:transition-opacity cursor-zoom-in',
        'focus-visible:ring-2 focus-visible:ring-inset',
        'focus-visible:ring-white/90',
      ]"
      type="button"
      :title="$t('common-actions.zoom')"
      @click="openDialog"
    >
      <MagnifyingGlassPlusIcon class="w-8 h-8 text-white" />
    </button>

    <slot />

    <BookCoverDialog
      v-if="showBookCover && showBookInfo"
      :cover-url="coverOriginalUrl"
      :aspect-ratio="coverArt?.attributes?.aspectRatio"
      :open="dialogOpen"
      @close="closeDialog"
    />
  </figure>
</template>
