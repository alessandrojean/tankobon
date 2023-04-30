<script lang="ts" setup>
import { cva, type VariantProps } from 'cva'
import type { ButtonHTMLAttributes } from 'vue'
import type { RouterLinkProps } from 'vue-router'

const button = cva(
  [
    'font-medium motion-safe:transition-all',
    'border border-inset flex items-center justify-center relative',
  ],
  {
    variants: {
      kind: {
        primary: [
          'shadow-sm border-primary-500 bg-primary-500 text-primary-50',
          'not-disabled:hover:bg-primary-600 not-disabled:hover:border-primary-600',
        ],
        normal: [
          'shadow-sm border-gray-300 bg-white text-gray-700 not-disabled:hover:bg-gray-50',
          'dark:bg-gray-800 dark:text-gray-100 dark:border-gray-800',
          'dark:not-disabled:hover:bg-gray-700 dark:not-disabled:hover:border-gray-700',
          '[&_svg]:text-gray-600 [&_svg]:hover:text-gray-700',
          '[&_svg]:dark:text-gray-300 [&_svg]:dark:hover:text-gray-200'
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
        'navbar-dark': [
          'border-transparent bg-transparent text-gray-300',
          'not-disabled:hover:bg-gray-700 not-disabled:hover:text-gray-200'
        ],
        'navbar-dark-elevated': [
          'border-gray-800 bg-gray-800 text-gray-300',
          'not-disabled:hover:bg-gray-700 not-disabled:hover:border-gray-700',
          'not-disabled:hover:text-gray-200'
        ],
        'navbar-light': [
          'border-transparent bg-transparent text-white/80',
          'not-disabled:hover:bg-white/20 not-disabled:hover:text-white/95'
        ],
        danger: [
          'shadow-sm border-red-600 text-white bg-red-600',
          'not-disabled:hover:bg-red-700 nod-disabled:hover:border-red-700',
        ]
      },
      size: {
        mini: 'text-sm px-3',
        small: 'text-sm px-3 py-2',
        normal: 'px-4 py-2',
        large: 'text-lg px-5 py-2',
      },
      loading: {
        true: 'disabled:opacity-80',
        false: 'disabled:opacity-50',
      },
      rounded: {
        true: 'rounded-full',
        false: 'rounded-md',
      }
    },
    defaultVariants: {
      kind: 'normal',
      size: 'normal',
      loading: false,
      rounded: false,
    }
  }
)

type ButtonCvaProps = VariantProps<typeof button>

interface ButtonProps {
  disabled?: boolean,
  isLink?: boolean,
  isRouterLink?: boolean,
  loading?: boolean,
  kind?: ButtonCvaProps['kind'],
  rounded?: boolean,
  size?: ButtonCvaProps['size'],
  to?: RouterLinkProps['to'],
  type?: ButtonHTMLAttributes['type'],
}

withDefaults(defineProps<ButtonProps>(), {
  disabled: false,
  isLink: false,
  isRouterLink: false,
  loading: false,
  kind: 'normal',
  rounded: false,
  size: 'normal',
  to: () => ({}),
  type: 'button',
})
</script>

<template>
  <button
    v-if="!isLink && !isRouterLink"
    :class="button({ kind, size, loading, rounded })"
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
    :class="button({ kind, size, loading })"
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
    :class="button({ kind, size, loading })"
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
