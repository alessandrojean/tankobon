<script lang="ts" setup>
import {
  ExclamationTriangleIcon,
  InformationCircleIcon,
  XCircleIcon
} from '@heroicons/vue/20/solid'

export interface AlertProps {
  border?: boolean
  show?: boolean
  title?: string
  type: 'info' | 'error' | 'warning'
}

const props = withDefaults(defineProps<AlertProps>(), { border: true })

const { type, border } = toRefs(props)

const classes: Record<AlertProps['type'], string> = {
  info: 'bg-blue-50 text-blue-700 border-blue-500',
  error: 'bg-red-50 text-red-700 border-red-500',
  warning: 'bg-amber-50 text-amber-900 border-amber-400'
}

const titleClasses: Record<AlertProps['type'], string> = {
  info: 'text-blue-800',
  error: 'text-red-800',
  warning: 'text-amber-800'
}
</script>

<template>
  <FadeTransition>
    <div
      v-if="show"
      role="alert"
      :data-type="type"
      :class="[
        'dark:bg-gray-800 dark:border dark:rounded-xl dark:text-gray-100 px-3 py-5 text-sm flex space-x-3',
        border ? 'border-l-4' : '',
        classes[type]
      ]"
    >
      <div :class="['shrink-0', title ? 'pt-1' : '']">
        <XCircleIcon
          v-if="type === 'error'"
          class="h-5 w-5 text-red-500 dark:text-red-400"
          aria-hidden="true"
        />
        <InformationCircleIcon
          v-else-if="type === 'info'"
          class="h-5 w-5 text-blue-500 dark:text-blue-400"
          aria-hidden="true"
        />
        <ExclamationTriangleIcon
          v-else
          class="h-5 w-5 text-amber-400 dark:text-amber-500"
          aria-hidden="true"
        />
      </div>
      <div
        class="space-y-2 dark:text-gray-200 [&_a]:underline [&_a]:font-medium"
      >
        <p
          v-if="title && title.length > 0"
          :class="[
            titleClasses[type],
            'font-display font-medium text-md sm:text-lg dark:text-gray-100'
          ]"
        >
          {{ title }}
        </p>
        <slot></slot>
        <div
          v-if="$slots.actions"
          class="flex pt-3 space-x-3 alert-actions -ml-2.5"
        >
          <slot name="actions"></slot>
        </div>
      </div>
    </div>
  </FadeTransition>
</template>

<style lang="postcss">
.alert-actions > a,
.alert-actions > button {
  @apply font-semibold rounded-md px-2.5 py-1.5
    motion-safe:transition-shadow;
}

.alert-actions a:hover,
.alert-actions a:focus-visible,
.alert-actions button:hover,
.alert-actions button:focus-visible {
  @apply underline;
}

.alert-actions a:focus,
.alert-actions button:focus {
  @apply outline-none;
}

.alert-actions a:focus-visible,
.alert-actions button:focus-visible {
  @apply ring-2 ring-current;
}
</style>