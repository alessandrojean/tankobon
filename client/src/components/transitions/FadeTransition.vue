<script lang="ts" setup>
export interface FadeTransitionProps {
  mode?: 'in-out' | 'out-in' | null
  simultaneous?: boolean
}
withDefaults(defineProps<FadeTransitionProps>(), {
  mode: 'out-in',
  simultaneous: false
})
</script>

<template>
  <Transition
    :mode="!simultaneous ? mode : null"
    leave-active-class="transition motion-reduce:transition-none duration-200 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
    enter-active-class="transition motion-reduce:transition-none duration-200 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
  >
    <slot></slot>
  </Transition>
</template>
