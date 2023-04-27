<script lang="ts" setup>
export interface AvatarProps {
  alt?: string,
  dark?: boolean,
  kind?: 'primary' | 'gray',
  pictureUrl: string | null | undefined,
  size?: 'mini' | 'small' | 'normal',
}

const props = withDefaults(defineProps<AvatarProps>(), {
  alt: undefined,
  dark: false,
  kind: 'primary',
  size: 'normal',
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
        'is-mini': size === 'mini',
        'is-small': size === 'small',
        'is-dark': dark,
        'is-empty': isEmpty,
        'is-gray': kind === 'gray',
        'is-primary': kind === 'primary',
      }
    ]"
  >
    <div
      v-if="isEmpty"
      :class="[
        'w-full h-full overflow-hidden rounded-full',
        'motion-safe:transition-colors',
        { 'motion-safe:animate-pulse': imageLoading },  
      ]"
    >
      <svg
        class="h-full w-full motion-safe:transition-colors"
        fill="currentColor"
        viewBox="0 0 24 24"
      >
        <path d="M24 20.993V24H0v-2.996A14.977 14.977 0 0112.004 15c4.904 0 9.26 2.354 11.996 5.993zM16.002 8.999a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
    </div>
    <img v-else class="avatar-img" :alt="alt" :src="pictureUrl ?? undefined" />
  </div>
</template>

<style lang="postcss" scoped>
.avatar {
  @apply w-12 h-12 rounded-full relative shrink-0
    ring-1 ring-black/5
    motion-safe:transition-colors;
}

.avatar.is-empty.is-primary {
  @apply border-2 border-primary-500 bg-primary-500;

  svg {
    @apply text-primary-200 dark:text-primary-300;
  }
}

.avatar.is-empty.is-gray {
  @apply border-2 border-gray-500 bg-gray-500;

  svg {
    @apply text-gray-400 dark:text-gray-400;
  }
}

.avatar:not(.is-dark) {
  @apply dark:border-gray-700 dark:bg-gray-700;
}

.avatar.is-empty:not(.is-dark).is-primary {
  @apply border-primary-500 bg-primary-500
    dark:border-primary-700 dark:bg-primary-700;
}

.avatar.is-empty:not(.is-dark).is-gray {
  @apply border-gray-200 bg-gray-200
    dark:border-gray-700 dark:bg-gray-700;
}

.avatar.is-mini {
  @apply w-8 h-8;
}

.avatar.is-small {
  @apply w-10 h-10;
}

.avatar-img {
  @apply w-full h-full rounded-full object-contain;
}
</style>