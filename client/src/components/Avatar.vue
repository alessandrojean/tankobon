<script lang="ts" setup>
export interface AvatarProps {
  alt?: string,
  emptyStyle?: 'icon' | 'letter',
  dark?: boolean,
  kind?: 'primary' | 'gray',
  letter?: string,
  letterId?: string,
  pictureUrl: string | null | undefined,
  size?: 'mini' | 'small' | 'normal',
}

const props = withDefaults(defineProps<AvatarProps>(), {
  alt: undefined,
  dark: false,
  emptyStyle: 'icon',
  kind: 'primary',
  letter: undefined,
  letterId: '',
  size: 'normal',
})

const { alt, pictureUrl, letterId } = toRefs(props)

const { imageLoading, imageHasError, loadImage, unloadImage } = useImageLoader(pictureUrl)

onMounted(() => loadImage())
onUnmounted(() => unloadImage())

const isEmpty = computed(() => {
  return !pictureUrl.value || imageHasError.value || imageLoading.value
})

const letterBackgroundColor = computed(() => {
  let hash = 0
  
  for (let i = 0; i < letterId.value.length; i++) {
    hash = letterId.value.charCodeAt(i) + ((hash << 5) - hash)
  }
  
  let color = '#'
  
  for (let i = 0; i < 3; i++) {
    const value = (hash >> (i * 8)) & 0xFF
    color += ('00' + value.toString(16)).slice(-2)
  }
  
  return color
})

const colorIsLight = computed(() => {
  const hex = letterBackgroundColor.value.replace('#', '')
  const red = parseInt(hex.substring(0, 0 + 2), 16)
  const green = parseInt(hex.substring(2, 2 + 2), 16)
  const blue = parseInt(hex.substring(4, 4 + 2), 16)
  const brightness = ((red * 299) + (green * 587) + (blue * 114)) / 1000

  return brightness > 180
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
        v-if="emptyStyle === 'icon'"
      >
        <path d="M24 20.993V24H0v-2.996A14.977 14.977 0 0112.004 15c4.904 0 9.26 2.354 11.996 5.993zM16.002 8.999a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
      <div
        v-else
        :class="[
          'w-full h-full flex items-center justify-center',
          'font-semibold bg-[--letter-bg-color] dark:opacity-80'
        ]"
        :style="{
          '--letter-bg-color': letterBackgroundColor,
        }"
      >
        <span
          :class="{
            'text-white/80 dark:text-white': !colorIsLight,
            'text-black/70 dark:text-black': colorIsLight,
          }"
        >
          {{ letter }}
        </span>
      </div>
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