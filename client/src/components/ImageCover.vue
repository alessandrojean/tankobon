<script lang="ts" setup>
import { decode as decodeBlurHash } from 'blurhash'
import {
  BookOpenIcon,
  ExclamationCircleIcon,
  MagnifyingGlassPlusIcon,
} from '@heroicons/vue/24/outline'
import { createImageUrl } from '@/modules/api'
import type { ImageDetailsAttributes } from '@/types/tankobon-image-details'

export interface ImageCoverProps {
  alt: string
  aspectRatio?: string
  icon?: Component
  image: ImageDetailsAttributes | null | undefined
  loading?: boolean
  version?: string
  zoomable?: boolean
}

const props = withDefaults(defineProps<ImageCoverProps>(), {
  aspectRatio: undefined,
  icon: BookOpenIcon,
  image: undefined,
  loading: false,
  version: 'original',
  zoomable: true,
})

const { aspectRatio, image, loading, version } = toRefs(props)

function getCoverUrl(as: string) {
  if (!image.value || !image.value.versions[as]) {
    return ''
  }

  return createImageUrl({
    fileName: version.value === 'original'
      ? image.value.fileName
      : image.value.versions[as],
    timeHex: image.value.timeHex,
  }) ?? ''
}

const coverUrl = computed(() => getCoverUrl(version.value))
const coverOriginalUrl = computed(() => getCoverUrl('original'))

const { imageHasError, imageLoading, loadImage }
  = useImageLoader(coverUrl)

const showCover = computed(() => {
  return !loading.value && !!image.value
})

const showCoverImage = computed(() => {
  return !imageHasError.value && !imageLoading.value && showCover.value
})

whenever(image, () => loadImage())

onMounted(() => {
  if (image.value !== null) {
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

const figure = ref<HTMLElement>()
const canvas = ref<HTMLCanvasElement>()

const imageAspectRatio = computed(() => {
  if (aspectRatio.value) {
    return aspectRatio.value
  }

  return image.value ? image.value.aspectRatio : '2 / 3'
})

whenever(image, async (image) => {
  if (!canvas.value || !figure.value) {
    return
  }

  const { width, height, blurHash } = image

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
        v-if="showCoverImage"
        :src="coverUrl"
        :alt="alt ?? ''"
        class="w-full h-full"
      >
      <div v-else-if="loading || coverUrl.length === 0 || imageHasError" class="w-full h-full flex items-center justify-center">
        <component
          :is="icon"
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
      ref="canvas"
      :class="[
        'w-full h-full motion-safe:transition-opacity',
        'motion-safe:duration-500 motion-safe:animate-pulse',
        { 'opacity-0': showCoverImage || loading || coverUrl.length === 0 },
      ]"
    />

    <button
      v-if="showCoverImage && zoomable"
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

    <slot :image-has-error="imageHasError" :image-loading="imageLoading" />

    <BookCoverDialog
      v-if="showCoverImage && showCover && zoomable"
      :cover-url="coverOriginalUrl"
      :aspect-ratio="image?.aspectRatio"
      :open="dialogOpen"
      @close="closeDialog"
    />
  </figure>
</template>
