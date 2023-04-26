<script lang="ts" setup>
export interface LoadingIndicatorProps {
  blur?: boolean
  loading?: boolean
  position?: 'absolute' | 'fixed'
  small?: boolean
  zIndex?: number
}

withDefaults(defineProps<LoadingIndicatorProps>(), {
  blur: true,
  position: 'absolute',
  small: false,
  zIndex: 1
})
</script>

<template>
  <FadeTransition>
    <div
      aria-hidden="true"
      :class="[
        position,
        'flex justify-center items-center inset-0 bg-white/60 dark:bg-gray-900/40',
        'z-[--z]',
        blur ? 'backdrop-blur-sm' : ''
      ]"
      :style="{ '--z': zIndex }"
      v-if="loading"
    >
      <LoadingSpinIcon
        :class="[
          `motion-safe:animate-spin`,
          small ? 'h-8 w-8' : 'h-10 w-18',
          'mx-auto text-primary-500'
        ]"
      />
    </div>
  </FadeTransition>
</template>