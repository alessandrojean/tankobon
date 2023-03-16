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

const { imageLoading, imageHasError, loadImage } = useImageLoader(pictureUrl)

onMounted(() => loadImage())

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
      :class="['empty-avatar', imageLoading ? 'motion-safe:animate-pulse' : '']"
    >
      <UserIcon class="user-icon" />
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
  @apply border-primary-600 bg-primary-600;
}

.avatar:not(.is-dark) {
  @apply dark:border-gray-700 dark:bg-gray-700;
}

.avatar.is-empty:not(.is-dark) {
  @apply border-primary-600 bg-primary-600
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