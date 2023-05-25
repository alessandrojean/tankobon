<script setup lang="ts">
export interface PreferenceProps {
  alwaysHorizontal?: boolean
  description: string
  title: string
}

withDefaults(defineProps<PreferenceProps>(), {
  alwaysHorizontal: false,
})
</script>

<template>
  <div
    :class="[
      'flex gap-4 xl:gap-2 w-full py-4 lg:py-5',
      alwaysHorizontal
        ? 'flex-row items-center'
        : 'flex-col @xl:flex-row @xl:items-center',
    ]"
  >
    <div
      :class="[
        'gap-0.5',
        alwaysHorizontal
          ? 'flex flex-col grow'
          : '@xl:flex @xl:flex-col @xl:grow',
      ]"
    >
      <slot name="title" :title="title">
        <p class="w-full font-medium dark:text-gray-100 flex items-center mb-1">
          {{ title }}
        </p>
      </slot>
      <slot name="description" :description="description">
        <p class="w-full text-sm text-gray-600 dark:text-gray-400">
          {{ description }}
        </p>
      </slot>
    </div>
    <div class="shrink-0">
      <slot />
    </div>
  </div>
</template>
