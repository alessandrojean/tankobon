<script lang="ts" setup>
import { createImageUrl } from '@/modules/api'
import type { ImageDetailsAttributes } from '@/types/tankobon-image-details'

export interface ImageBannerProps {
  alt: string | undefined
  image: ImageDetailsAttributes | null | undefined
  loading?: boolean
  kind?: 'normal' | 'repeated'
  size?: string
}

const props = withDefaults(defineProps<ImageBannerProps>(), {
  image: undefined,
  loading: false,
  kind: 'normal',
  size: 'original',
})

const { image, loading, size } = toRefs(props)

const bannerUrl = computed(() => {
  if (!image.value) {
    return ''
  }

  return createImageUrl({
    fileName: image.value?.versions[size.value],
    timeHex: image.value?.timeHex,
  }) ?? ''
})

const { imageHasError, imageLoading, loadImage } = useImageLoader(bannerUrl)

const showBanner = computed(() => {
  return !loading.value && !!image.value
})

const showImage = computed(() => {
  return !imageHasError.value && !imageLoading.value && showBanner.value
})

whenever(image, () => loadImage())

onMounted(() => {
  if (image.value !== null) {
    loadImage()
  }
})
</script>

<template>
  <div
    aria-hidden="true"
    class="h-72 sm:h-80 w-full overflow-hidden relative bg-primary-300 dark:bg-gray-950"
  >
    <FadeTransition>
      <img
        v-if="showImage && kind === 'normal'"
        :src="bannerUrl"
        :alt="alt ?? ''"
        class="w-full h-full scale-105 object-cover blur"
      >
      <div
        v-else-if="showImage && kind === 'repeated'"
        :class="[
          'w-full h-full overflow-hidden relative',
          'before:content-[\'\'] before:absolute',
          'before:w-[110%] before:h-[110%] before:left-1/2 before:top-1/2',
          'before:bg-[image:--background] before:blur',
          'before:-translate-x-1/2 before:-translate-y-1/2',
        ]"
        :style="{ '--background': `url('${bannerUrl}')` }"
      />
      <div v-else class="relative w-full h-full">
        <img
          src="@/assets/library-unsplash.jpg"
          alt=""
          class="w-full h-full scale-105 object-cover filter blur"
        >
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
        'to-white sm:to-gray-900/20 dark:to-gray-950 sm:dark:to-gray-950/20',
      ]"
    />
  </div>
</template>
