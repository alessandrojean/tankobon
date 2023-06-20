<script lang="ts" setup>
export interface MenuTransitionProps {
  mode?: 'in-out' | 'out-in' | undefined
  simultaneous?: boolean
}

withDefaults(defineProps<MenuTransitionProps>(), {
  mode: 'out-in',
  simultaneous: false,
})
</script>

<template>
  <Transition
    :mode="!simultaneous ? mode : undefined"
    enter-active-class="motion-safe:transition motion-safe:duration-200 motion-safe:ease-in-out-primer"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
  >
    <slot />
  </Transition>
</template>
