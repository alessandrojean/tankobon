<script lang="ts" setup>
import type { ButtonHTMLAttributes } from 'vue'
import type { RouterLinkProps } from 'vue-router'

withDefaults(defineProps<ButtonProps>(), {
  disabled: false,
  isLink: false,
  isRouterLink: false,
  loading: false,
  kind: 'normal',
  rounded: 'normal',
  size: 'normal',
  to: () => ({}),
  type: 'button',
})

const button = cva(
  [
    'font-medium motion-safe:transition-colors group/button',
    'border border-inset flex items-center justify-center relative',
    'focus:outline-none focus-visible:ring-2',
  ],
  {
    variants: {
      kind: {
        'primary': [
          'shadow-sm border-primary-500 bg-primary-500 text-primary-50',
          'not-disabled:hover:bg-primary-600 not-disabled:hover:border-primary-600',
          'focus-visible:ring-black dark:focus-visible:ring-white/90',
        ],
        'normal': [
          'shadow-sm border-gray-300 bg-white text-gray-700 not-disabled:hover:bg-gray-50',
          'dark:bg-gray-800 dark:text-gray-100 dark:border-gray-800',
          'dark:not-disabled:hover:bg-gray-700 dark:not-disabled:hover:border-gray-700',
          '[&_svg]:text-gray-600 [&_svg]:hover:text-gray-700',
          '[&_svg]:dark:text-gray-300 [&_svg]:dark:hover:text-gray-200',
          'focus-visible:ring-black dark:focus-visible:ring-white/90',
        ],
        'ghost': [
          'border-transparent bg-transparent text-primary-600 dark:text-primary-500',
          'not-disabled:hover:bg-gray-700/10 dark:not-disabled:hover:bg-gray-300/10',
          'not-disabled:hover:text-primary-800 dark:not-disabled:hover:text-primary-400',
          'focus-visible:ring-black dark:focus-visible:ring-white/90',
        ],
        'ghost-alt': [
          'border-transparent bg-transparent text-gray-500 dark:text-gray-300',
          'not-disabled:hocus:bg-gray-700/10 dark:not-disabled:hocus:bg-gray-300/10',
          'not-disabled:hocus:text-gray-600 dark:not-disabled:hocus:text-gray-100',
          'focus-visible:ring-black dark:focus-visible:ring-white/90',
        ],
        'ghost-danger': [
          'border-transparent bg-transparent text-gray-500 dark:text-gray-300',
          'not-disabled:hover:bg-gray-700/10 dark:not-disabled:hover:bg-gray-300/10',
          'not-disabled:hover:text-red-600 dark:not-disabled:hover:text-red-400',
          'focus-visible:ring-black dark:focus-visible:ring-white/90',
        ],
        'navbar-dark': [
          'border-transparent bg-transparent text-gray-300',
          'not-disabled:hocus:bg-gray-700 not-disabled:hocus:text-gray-200',
          'focus-visible:ring-white/90',
        ],
        'navbar-dark-elevated': [
          'border-gray-600 bg-gray-800 text-gray-300',
          'not-disabled:hocus:bg-gray-700 not-disabled:hocus:border-gray-500',
          'not-disabled:hocus:text-gray-200',
          'focus-visible:ring-white/90',
        ],
        'navbar-light': [
          'border-white/30 bg-transparent text-white/80',
          'not-disabled:hocus:bg-white/20 not-disabled:hocus:text-white/95',
          'not-disabled:hocus:border-white/50',
          'focus-visible:ring-white/90',
        ],
        'danger': [
          'shadow-sm border-red-600 text-white bg-red-600',
          'not-disabled:hover:bg-red-700 nod-disabled:hover:border-red-700',
          'focus-visible:ring-black dark:focus-visible:ring-white/90',
        ],
        'pill-tab': [
          'border-transparent bg-transparent',
          'text-gray-500 not-disabled:hocus:text-gray-800',
          'aria-selected:text-primary-600',
          'aria-checked:text-primary-600',
          'not-disabled:hocus:aria-selected:text-primary-600',
          'not-disabled:hocus:aria-checked:text-primary-600',
          'dark:text-gray-400 dark:not-disabled:hocus:text-gray-100',
          'dark:aria-selected:text-primary-50',
          'dark:aria-checked:text-primary-50',
          'focus-visible:ring-black dark:focus-visible:ring-white/90',
        ],
        'underline-tab': [
          'border-x-0 border-t-0 border-b-2 border-b-transparent',
          'text-gray-500 not-disabled:hocus:text-gray-800 not-disabled:hocus:border-gray-300',
          'ui-selected:text-primary-600 ui-selected:border-primary-600',
          'not-disabled:hocus:ui-selected:text-primary-600 not-disabled:hocus:ui-selected:border-primary-600',
          'dark:text-gray-400 dark:not-disabled:hocus:text-gray-100 dark:not-disabled:hocus:border-gray-600',
          'dark:ui-selected:text-primary-500 dark:ui-selected:border-primary-500',
          'dark:not-disabled:hocus:ui-selected:text-primary-500 dark:not-disabled:hocus:ui-selected:border-primary-500',
          'focus-visible:ring-black dark:focus-visible:ring-white/90',
        ],
      },
      size: {
        'mini': 'text-sm px-3',
        'small': 'text-sm px-3 py-2',
        'normal': 'px-4 py-2',
        'large': 'text-lg px-5 py-2',
        'underline-tab': 'text-sm py-3.5 px-0.5',
        'pill-tab': 'text-sm py-1.5 px-2.5',
      },
      loading: {
        true: 'disabled:opacity-80',
        false: 'disabled:opacity-50',
      },
      rounded: {
        full: 'rounded-full',
        normal: 'rounded-md',
        none: 'rounded-0',
      },
    },
    defaultVariants: {
      kind: 'normal',
      size: 'normal',
      loading: false,
      rounded: 'normal',
    },
  },
)

type ButtonCvaProps = VariantProps<typeof button>

interface ButtonProps {
  disabled?: boolean
  isLink?: boolean
  isRouterLink?: boolean
  loading?: boolean
  kind?: ButtonCvaProps['kind']
  rounded?: ButtonCvaProps['rounded']
  size?: ButtonCvaProps['size']
  to?: RouterLinkProps['to']
  type?: ButtonHTMLAttributes['type']
}
</script>

<template>
  <button
    v-if="!isLink && !isRouterLink"
    :class="button({ kind, size, loading, rounded })"
    :type="type"
    :disabled="disabled || loading"
  >
    <div
      class="flex items-center justify-center [&>svg:first-child]:-ml-1 [&>svg:first-child]:mr-2 transition-colors"
      :class="[
        { 'text-transparent [&_svg]:!text-transparent': loading },
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
          class="animate-spin"
          :class="[
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
      class="flex items-center justify-center [&>svg:first-child]:-ml-1 [&>svg:first-child]:mr-2"
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
      class="flex items-center justify-center [&>svg:first-child]:-ml-1 [&>svg:first-child]:mr-2"
    >
      <slot />
    </div>
  </RouterLink>
</template>
