<script lang="ts" setup>
import {
  CheckCircleIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  XCircleIcon,
} from '@heroicons/vue/24/outline';

export interface NotificationProps {
  title: string,
  body?: string,
  type?: 'success' | 'info' | 'failure' | 'warning',
}

const props = withDefaults(defineProps<NotificationProps>(), {
  type: 'info',
})
</script>

<template>
  <output
    role="status"
    :class="[
      'bg-white dark:bg-gray-700 rounded-md shadow-xl ring-1 ring-black/10',
      'p-3 flex gap-3 min-w-[16rem] max-w-lg',
      { 'items-center': (body ?? '').length === 0 },
    ]"
  >
    <CheckCircleIcon
      v-if="type === 'success'"
      class="w-6 h-6 shrink-0 text-green-500"
    />
    <ExclamationTriangleIcon
      v-else-if="type === 'warning'"
      class="w-6 h-6 shrink-0 text-amber-600 dark:text-amber-500"
    />
    <InformationCircleIcon
      v-else-if="type === 'info'"
      class="w-6 h-6 shrink-0 text-blue-600 dark:text-blue-400"
    />
    <XCircleIcon
      v-else
      class="w-6 h-6 shrink-0 text-red-600 dark:text-red-500"
    />
    <div class="grow">
      <p class="font-medium text-sm">
        {{ title }}
      </p>
      <p v-if="body" class="mt-1 text-xs text-gray-500 dark:text-gray-300">
        {{ body }}
      </p>
    </div>
  </output>
</template>