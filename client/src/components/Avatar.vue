<script lang="ts" setup>
import { VariantProps } from 'cva'

const avatar = cva(
  [
    'rounded-full relative shrink-0 ring-1 ring-black/5',
    'motion-safe:transition-colors',
  ],
  {
    variants: {
      kind: {
        primary: [
          'bg-primary-500 text-primary-200 dark:text-primary-300'
        ],
        gray: [
          'bg-gray-200 dark:bg-gray-700 text-gray-500 dark:text-gray-400'
        ],
      },
      size: {
        mini: ['w-8 h-8'],
        small: ['w-10 h-10'],
        normal: ['w-12 h-12'],
      }
    }
  }
)

type AvatarCvaProps = VariantProps<typeof avatar>

export interface AvatarProps {
  alt?: string,
  emptyStyle?: 'icon' | 'letter',
  kind?: AvatarCvaProps['kind'],
  letter?: string,
  letterId?: string,
  loading?: boolean,
  pictureUrl: string | null | undefined,
  size?: AvatarCvaProps['size'],
}

const props = withDefaults(defineProps<AvatarProps>(), {
  alt: undefined,
  dark: false,
  emptyStyle: 'icon',
  kind: 'primary',
  letter: undefined,
  letterId: '',
  loading: false,
  size: 'normal',
})

const { alt, pictureUrl, letterId, loading } = toRefs(props)

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
  <div v-if="loading" :class="avatar({ size, class: 'skeleton' })" />
  <div v-else :class="avatar({ kind, size })">
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
          'w-full h-full flex items-center justify-center select-none',
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
    <img
      v-else
      class="w-full h-full rounded-full object-contain"
      :alt="alt"
      :src="pictureUrl ?? undefined"
    />
  </div>
</template>
