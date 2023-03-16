<script lang="ts" setup>
export interface ScaleTransitionProps {
  mode?: 'in-out' | 'out-in' | undefined
  simultaneous?: boolean
}

withDefaults(defineProps<ScaleTransitionProps>(), {
  mode: 'out-in',
  simultaneous: false
})
</script>

<template>
  <Transition
    :mode="!simultaneous ? mode : undefined"
    leave-active-class="transition motion-reduce:transition-none motion-reduce:transform-none duration-150 ease-in"
    leave-from-class="opacity-100 scale-100"
    leave-to-class="opacity-0 scale-95"
    enter-active-class="transition motion-reduce:transition-none motion-reduce:transform-none duration-150 ease-out"
    enter-from-class="opacity-0 scale-95"
    enter-to-class="opacity-100 scale-100"
  >
    <slot></slot>
  </Transition>
</template>
