<script lang="ts" setup>
import {
  CheckCircleIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  XCircleIcon,
} from '@heroicons/vue/20/solid'

export interface NotificationProps {
  title: string
  body?: string
  type?: 'success' | 'info' | 'failure' | 'warning'
}

withDefaults(defineProps<NotificationProps>(), {
  type: 'info',
})
</script>

<template>
  <output
    role="status"
    class="bg-white dark:bg-gray-700 rounded-md shadow-xl ring-1 ring-black/10 max-w-xl flex overflow-hidden" :class="[
      { 'items-center': (body ?? '').length === 0 },
    ]"
  >
    <div
      class="h-full w-12 flex items-center justify-center shrink-0 [&>svg]:w-5 [&>svg]:h-5" :class="[
        {
          'bg-green-500 dark:bg-green-600': type === 'success',
          'bg-amber-600': type === 'warning',
          'bg-blue-600': type === 'info',
          'bg-red-600 dark:bg-red-700': type === 'failure',
        },
      ]"
    >
      <CheckCircleIcon
        v-if="type === 'success'"
        class="text-green-100"
      />
      <ExclamationTriangleIcon
        v-else-if="type === 'warning'"
        class="text-amber-100"
      />
      <InformationCircleIcon
        v-else-if="type === 'info'"
        class="text-blue-100"
      />
      <XCircleIcon
        v-else
        class="text-red-100"
      />
    </div>
    <div class="p-4">
      <p class="font-medium text-sm">
        {{ title }}
      </p>
      <p v-if="body" class="mt-1 text-xs text-gray-500 dark:text-gray-300">
        {{ body }}
      </p>
    </div>
  </output>
</template>
