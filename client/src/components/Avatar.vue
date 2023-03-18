<script lang="ts" setup>
import { UserIcon } from '@heroicons/vue/20/solid'

export interface AvatarProps {
  alt?: string
  dark?: boolean
  pictureUrl: string | null | undefined
  small?: boolean
}

const props = withDefaults(defineProps<AvatarProps>(), {
  alt: undefined,
  dark: false,
  small: false
})

const { alt, pictureUrl } = toRefs(props)

const { imageLoading, imageHasError, loadImage, unloadImage } = useImageLoader(pictureUrl)

onMounted(() => loadImage())
onUnmounted(() => unloadImage())

const isEmpty = computed(() => {
  return !pictureUrl.value || imageHasError.value || imageLoading.value
})
</script>

<template>
  <div
    :class="[
      'avatar', 
      {
        'is-small': small,
        'is-dark': dark,
        'is-empty': isEmpty,
      }
    ]"
  >
    <div
      v-if="isEmpty"
      :class="[
        'w-full h-full overflow-hidden rounded-full',
        'bg-primary-500 dark:bg-primary-700',
        'motion-safe:transition-colors',
        { 'motion-safe:animate-pulse': imageLoading },  
      ]"
    >
      <svg class="h-full w-full text-primary-200 dark:text-primary-300 motion-safe:transition-colors" fill="currentColor" viewBox="0 0 24 24">
        <path d="M24 20.993V24H0v-2.996A14.977 14.977 0 0112.004 15c4.904 0 9.26 2.354 11.996 5.993zM16.002 8.999a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
    </div>
    <img v-else class="avatar-img" :alt="alt" :src="pictureUrl ?? undefined" />
  </div>
</template>

<style lang="postcss" scoped>
.avatar {
  @apply w-12 h-12 rounded-full relative shrink-0
    border-2 border-primary-50 bg-primary-50
    motion-safe:transition-colors;
}

.avatar.is-empty {
  @apply border-primary-500 bg-primary-500;
}

.avatar:not(.is-dark) {
  @apply dark:border-gray-700 dark:bg-gray-700;
}

.avatar.is-empty:not(.is-dark) {
  @apply border-primary-500 bg-primary-500
    dark:border-primary-700 dark:bg-primary-700;
}

.avatar.is-small {
  @apply w-8 h-8;
}

.empty-avatar {
  @apply w-full h-full rounded-full overflow-hidden
    flex items-start justify-center
    bg-primary-600 text-primary-100
    motion-safe:transition-colors;
}

.avatar:not(.is-dark) .empty-avatar {
  @apply dark:bg-primary-700 dark:text-primary-200;
}

.user-icon {
  @apply w-16 h-16 block pb-3;
}

.avatar.is-small .user-icon {
  @apply w-9 h-9 pb-0;
}

.avatar-img {
  @apply w-full h-full rounded-full object-contain;
}
</style>