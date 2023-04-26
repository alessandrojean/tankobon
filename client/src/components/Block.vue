<script lang="ts" setup>
export interface BlockProps {
  as?: keyof HTMLElementTagNameMap
  title?: string
}

const props = withDefaults(defineProps<BlockProps>(), {
  as: 'section'
})

const { as } = toRefs(props)
</script>

<template>
  <component
    :is="as"
    class="bg-block dark:bg-block-dark p-4 rounded-xl relative motion-safe:transition"
  >
    <slot
      name="title"
      titleClasses="text-md sm:text-lg font-medium font-display dark:text-gray-100"
    >
      <h2
        v-if="title"
        class="text-md sm:text-lg font-medium font-display dark:text-gray-100"
      >
        {{ title }}
      </h2>
    </slot>
    <div :class="{ 'mt-4': title }">
      <slot />
    </div>
  </component>
</template>