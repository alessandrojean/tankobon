<script lang="ts" setup>
import type { Component } from 'vue'

export interface EmptyStateProps {
  title?: string,
  description?: string,
  icon?: Component,
}

defineProps<EmptyStateProps>()
</script>

<template>
  <div class="flex flex-col items-center justify-center p-4 md:p-6 lg:p-10 gap-2">
    <slot name="icon">
      <component
        v-if="icon"
        :is="icon"
        class="w-12 h-12 text-gray-400 dark:text-gray-500 stroke-1"
      />
    </slot>

    <div class="flex flex-col items-center">
      <slot name="title">
        <p
          v-if="title"
          class="font-medium dark:text-gray-100"
        >
          {{ title }}
        </p>
      </slot>

      <slot name="description">
        <p
          v-if="description"
          class="text-sm text-gray-600 dark:text-gray-300"
        >
          {{ description }}
        </p>
      </slot>
    </div>

    <div v-if="$slots.actions" class="mt-4">
      <slot name="actions" />
    </div>
  </div>
</template>