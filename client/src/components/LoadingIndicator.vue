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
  zIndex: 1,
})
</script>

<template>
  <FadeTransition>
    <div
      v-if="loading"
      aria-hidden="true" class="flex justify-center items-center inset-0 bg-white/60 dark:bg-gray-900/40 z-[--z]"
      :class="[
        position,
        blur ? 'backdrop-blur-sm' : '',
      ]"
      :style="{ '--z': zIndex }"
    >
      <LoadingSpinIcon
        class="motion-safe:animate-spin mx-auto text-primary-500"
        :class="[
          small ? 'h-8 w-8' : 'h-10 w-18',
        ]"
      />
    </div>
  </FadeTransition>
</template>
