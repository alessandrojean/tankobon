<script lang="ts" setup>
import type { ButtonHTMLAttributes } from 'vue'

export interface ButtonProps {
  disabled?: boolean,
  loading?: boolean,
  kind?: 'primary' | 'normal' | 'ghost',
  type?: ButtonHTMLAttributes['type'],
}

const props = withDefaults(defineProps<ButtonProps>(), {
  disabled: false,
  loading: false,
  kind: 'normal',
  type: 'button',
})

const kindMap: Record<string, string> = {
  primary: 'shadow-sm border-primary-500 bg-primary-500 text-primary-50 enabled:hover:bg-primary-600 enabled:hover:border-primary-600',
  normal: 'shadow-sm border-gray-300 bg-white text-gray-700 enabled:hover:bg-gray-50',
  ghost: 'border-transparent bg-transparent text-primary-600 enabled:hover:bg-gray-700/10 dark:enabled:hover:bg-gray-300/10 enabled:hover:text-primary-800 dark:enabled:hover:text-primary-500',
}
</script>

<template>
  <button
    :class="[
      'rounded-md px-3 py-2 font-medium motion-safe:transition-all',
      'border flex items-center justify-center',
      loading ? 'disabled:opacity-80' : 'disabled:opacity-50',
      kindMap[kind],
    ]"
    :type="type"
    :disabled="disabled || loading"
  >
    <div :class="{ 'sr-only': loading }">
      <slot />
    </div>
    <LoadingSpinIcon
      v-if="loading"
      class="w-6 h-6 animate-spin"
    />
  </button>
</template>