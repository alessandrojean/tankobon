import { computed, onUnmounted, readonly, ref, watch } from 'vue'
import type { ComponentPublicInstance, Ref } from 'vue'
import type { _RouterLinkI } from 'vue-router'

export default function useImageLazyLoader(
  imageUrl: Ref<string | undefined>,
  elRef: Ref<
    ComponentPublicInstance | InstanceType<_RouterLinkI> | Element | undefined
  >,
) {
  const imageHasError = ref(false)
  const imageLoading = ref(true)
  const intersected = ref(false)
  const observer = ref<IntersectionObserver>()

  const observerCreated = computed(() => observer.value !== null)

  const image = new Image()

  image.onload = () => {
    if (image.naturalWidth === 1 && image.naturalHeight === 1) {
      imageHasError.value = true
    } else {
      imageHasError.value = false
    }

    imageLoading.value = false
  }

  image.onerror = () => {
    imageHasError.value = true
    imageLoading.value = false
  }

  function loadImage() {
    if (imageUrl.value && imageUrl.value.length > 0) {
      image.src = imageUrl.value
    } else {
      imageLoading.value = false
      imageHasError.value = true
    }
  }

  function setupObserver() {
    if (!elRef.value) {
      return
    }

    observer.value = new IntersectionObserver((entries) => {
      const el = entries[0]
      if (el.isIntersecting) {
        intersected.value = true
        observer.value?.disconnect()
      }
    })

    const element: Element
      = '$el' in elRef.value ? elRef.value.$el : elRef.value

    if (element.tagName) {
      observer.value.observe(element)
    }
  }

  function disconnectObserver() {
    observer.value?.disconnect()
  }

  onUnmounted(() => {
    if (observerCreated.value) {
      disconnectObserver()
    }
  })

  watch(intersected, (newValue) => {
    if (newValue) {
      loadImage()
    }
  })

  return {
    imageHasError: readonly(imageHasError),
    imageLoading: readonly(imageLoading),
    setupObserver,
    observerCreated: readonly(observerCreated),
  }
}
