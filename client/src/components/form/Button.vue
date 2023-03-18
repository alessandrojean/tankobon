<script lang="ts" setup>
import type { ButtonHTMLAttributes } from 'vue'
import type { RouterLinkProps } from 'vue-router'

export interface ButtonProps {
  disabled?: boolean,
  isLink?: boolean,
  isRouterLink?: boolean,
  loading?: boolean,
  kind?: 'primary' | 'normal' | 'ghost' | 'ghost-alt' | 'danger',
  size?: 'small' | 'normal' | 'large',
  to?: RouterLinkProps['to'],
  type?: ButtonHTMLAttributes['type'],
}

withDefaults(defineProps<ButtonProps>(), {
  disabled: false,
  isLink: false,
  isRouterLink: false,
  loading: false,
  kind: 'normal',
  size: 'normal',
  to: () => ({}),
  type: 'button',
})

const kindMap: Record<string, string[]> = {
  primary: [
    'shadow-sm border-primary-500 bg-primary-500 text-primary-50',
    'not-disabled:hover:bg-primary-600 not-disabled:hover:border-primary-600',
  ],
  normal: [
    'shadow-sm border-gray-300 bg-white text-gray-700 not-disabled:hover:bg-gray-50',
    'dark:bg-gray-700 dark:text-gray-100 dark:border-gray-600',
    'dark:not-disabled:hover:bg-gray-600',
  ],
  ghost: [
    'border-transparent bg-transparent text-primary-600 dark:text-primary-500',
    'not-disabled:hover:bg-gray-700/10 dark:not-disabled:hover:bg-gray-300/10',
    'not-disabled:hover:text-primary-800 dark:not-disabled:hover:text-primary-400',
  ],
  'ghost-alt': [
    'border-transparent bg-transparent text-gray-500 dark:text-gray-300',
    'not-disabled:hover:bg-gray-700/10 dark:not-disabled:hover:bg-gray-300/10',
    'not-disabled:hover:text-gray-600 dark:not-disabled:hover:text-gray-100',
  ],
  danger: [
    'shadow-sm border-red-600 text-white bg-red-600',
    'not-disabled:hover:bg-red-700 nod-disabled:hover:border-red-700',
  ],
}
</script>

<template>
  <button
    v-if="!isLink && !isRouterLink"
    :class="[
      'rounded-md px-3 py-2 font-medium motion-safe:transition-all',
      'border flex items-center justify-center relative',
      loading ? 'disabled:opacity-80' : 'disabled:opacity-50',
      { 'text-lg': size === 'large', 'text-sm': size === 'small' },
      ...kindMap[kind],
    ]"
    :type="type"
    :disabled="disabled || loading"
  >
    <div
      :class="[
        { 'text-transparent': loading },
        'flex items-center justify-center',
        '[&>svg:first-child]:-ml-1 [&>svg:first-child]:mr-2',
        'transition-colors',
      ]"
    >
      <slot />
    </div>
    <FadeTransition>
      <div
        v-if="loading"
        class="absolute inset-0 flex items-center justify-center"
        aria-hidden="true"
      >
        <LoadingSpinIcon
          :class="[
            'animate-spin',
            size === 'large' ? 'w-7 h-7' : 'w-6 h-6',
          ]"
        />
      </div>
    </FadeTransition>
  </button>
  <a
    v-else-if="!isRouterLink"
    :class="[
      'rounded-md px-3 py-2 font-medium motion-safe:transition-all',
      'border flex items-center justify-center',
      loading ? 'disabled:opacity-80' : 'disabled:opacity-50',
      { 'text-lg': size === 'large' },
      ...kindMap[kind],
    ]"
  >
    <div
      :class="[
        'flex items-center justify-center',
        '[&>svg:first-child]:-ml-1 [&>svg:first-child]:mr-2'
      ]"
    >
      <slot />
    </div>
  </a>
  <RouterLink
    v-else
    :to="to"
    :class="[
      'rounded-md px-3 py-2 font-medium motion-safe:transition-all',
      'border flex items-center justify-center',
      loading ? 'disabled:opacity-80' : 'disabled:opacity-50',
      { 'text-lg': size === 'large' },
      ...kindMap[kind],
    ]"
  >
    <div
      :class="[
        'flex items-center justify-center',
        '[&>svg:first-child]:-ml-1 [&>svg:first-child]:mr-2'
      ]"
    >
      <slot />
    </div>
  </RouterLink>
</template>
