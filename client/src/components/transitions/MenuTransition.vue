<script lang="ts" setup>
export interface ScaleTransitionProps {
  mode?: 'in-out' | 'out-in' | undefined
  simultaneous?: boolean
}

withDefaults(defineProps<ScaleTransitionProps>(), {
  mode: 'out-in',
  simultaneous: false,
})
</script>

<template>
  <Transition
    :mode="!simultaneous ? mode : undefined"
    enter-active-class="motion-safe:transition motion-safe:duration-200 motion-safe:ease-in-out-primer"
    enter-from-class="opacity-0 -translate-y-1.5"
    enter-to-class="opacity-100 translate-y-0"
  >
    <slot />
  </Transition>
</template>
