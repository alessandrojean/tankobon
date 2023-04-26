<script lang="ts" setup>
import { cva, type VariantProps } from 'cva'

const badge = cva(
  [
    'px-2.5 py-0.5 inline-flex text-xs leading-5',
    'font-medium rounded dark:border font-sans-safe',
  ],
  {
    variants: {
      color: {
        gray: [
          'bg-gray-200 dark:bg-gray-800 text-gray-800',
          'dark:text-gray-300 dark:border-gray-700',
        ],
        green: [
          'bg-emerald-100 dark:bg-emerald-900/50',
          'text-emerald-800 dark:text-emerald-300 dark:border-emerald-500/40',
        ],
        red: [
          'bg-red-100 dark:bg-red-900/40',
          'text-red-800 dark:text-red-300 dark:border-red-400/25',
        ],
        blue: [
          'bg-blue-100 dark:bg-blue-800/40',
          'text-blue-700 dark:text-blue-300 dark:border-blue-400/40',
        ]
      }
    },
    defaultVariants: {
      color: 'gray',
    },
  }
)

type BadgeCvaProps = Required<VariantProps<typeof badge>>

interface BadgeProps {
  color?: BadgeCvaProps['color'],
}

withDefaults(defineProps<BadgeProps>(), {
  color: undefined
})
</script>

<template>
  <span :class="badge({ color })">
    <slot />
  </span>
</template>