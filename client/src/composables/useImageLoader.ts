import { computed, readonly, ref, watch } from 'vue'
import type { Ref } from 'vue'

export default function useImageLoader(
  imageUrl?: Ref<string | undefined | null>,
  aspectRatioDefault = '2 / 3',
) {
  const imageHasError = ref(false)
  const imageLoading = ref(true)
  const imageWidth = ref(0)
  const imageHeight = ref(0)
  const imageAspectRatio = computed(() => {
    return (imageWidth.value > 0 && imageHeight.value > 0 && !imageHasError.value)
      ? `${imageWidth.value} / ${imageHeight.value}`
      : aspectRatioDefault
  })

  const image = new Image()

  image.onload = () => {
    if (image.naturalWidth === 1 && image.naturalHeight === 1) {
      imageHasError.value = true
    } else {
      imageHasError.value = false
    }

    imageWidth.value = image.naturalWidth
    imageHeight.value = image.naturalHeight

    imageLoading.value = false
  }

  image.onerror = () => {
    imageHasError.value = true
    imageLoading.value = false
  }

  const loadImage = () => {
    if (imageUrl?.value && imageUrl.value.length > 0) {
      image.src = imageUrl.value
    } else {
      imageLoading.value = false
      imageHasError.value = true
    }
  }

  const unloadImage = (imageToUnload: string | null | undefined) => {
    if (imageToUnload?.indexOf('blob:') === 0) {
      URL.revokeObjectURL(imageToUnload)
    }
  }

  watch(
    () => imageUrl?.value,
    (current, old) => {
      if (current !== old) {
        unloadImage(old)
      }

      imageHasError.value = false
      imageLoading.value = true
      loadImage()
    },
  )

  return {
    imageHasError: readonly(imageHasError),
    imageLoading: readonly(imageLoading),
    imageWidth: readonly(imageWidth),
    imageHeight: readonly(imageHeight),
    imageAspectRatio,
    loadImage,
    unloadImage: () => unloadImage(imageUrl?.value),
  }
}
