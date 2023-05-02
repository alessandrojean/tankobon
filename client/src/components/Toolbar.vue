<script setup lang="ts">
import { UseFocusZoneOptions } from '@/composables/useFocusZone'
import { FocusKeys } from '@primer/behaviors'

export interface ToolbarProps {
  as?: string,
  bindKeys?: UseFocusZoneOptions['bindKeys'],
  focusInStrategy?: UseFocusZoneOptions['focusInStrategy'],
  focusOutBehavior?:  UseFocusZoneOptions['focusOutBehavior'],
}

const props = withDefaults(defineProps<ToolbarProps>(), {
  as: 'div',
  bindKeys: FocusKeys.ArrowHorizontal | FocusKeys.HomeAndEnd,
  focusInStrategy: 'closest',
  focusOutBehavior: 'wrap',
})

const toolbar = ref<HTMLDivElement>()

useFocusZone({
  containerRef: toolbar,
  focusInStrategy: props.focusInStrategy,
  bindKeys: props.bindKeys,
  focusOutBehavior: props.focusOutBehavior,
})
</script>

<template>
  <component :is="as" role="toolbar" ref="toolbar">
    <slot />
  </component>
</template>