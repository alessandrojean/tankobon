<script lang="ts" setup>
import type { Component } from 'vue'

export interface EmptyStateProps {
  title?: string
  description?: string
  icon?: Component
}

defineProps<EmptyStateProps>()

const slots = useSlots()

const hasActions = computed(() => slots.actions && slots.actions() && slots.actions()[0]?.children !== 'v-if')
</script>

<template>
  <div class="flex flex-col items-center justify-center p-4 md:p-6 lg:p-10 gap-2">
    <slot name="icon">
      <Component
        :is="icon"
        v-if="icon"
        class="w-12 h-12 text-gray-400 dark:text-gray-500 stroke-1"
      />
    </slot>

    <div class="flex flex-col items-center mt-2">
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

    <div v-if="hasActions" class="mt-4">
      <slot name="actions" />
    </div>
  </div>
</template>
