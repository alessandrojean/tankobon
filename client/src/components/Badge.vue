<script lang="ts" setup>
withDefaults(defineProps<BadgeProps>(), {
  color: undefined,
  rounded: false,
})

const badge = cva(
  [
    'px-2 py-1 inline-flex items-center gap-1 text-xs',
    'font-medium ring-1 ring-inset font-sans-safe',
  ],
  {
    variants: {
      color: {
        gray: [
          'bg-gray-50 dark:bg-gray-400/10',
          'text-gray-600 dark:text-gray-400',
          'ring-gray-500/10 dark:ring-gray-400/20',
        ],
        green: [
          'bg-green-50 dark:bg-green-500/10',
          'text-green-700 dark:text-green-400',
          'ring-green-600/20 dark:ring-green-500/20',
        ],
        red: [
          'bg-red-100 dark:bg-red-400/10',
          'text-red-700 dark:text-red-400',
          'ring-red-600/10 dark:ring-red-400/20',
        ],
        blue: [
          'bg-blue-50 dark:bg-blue-400/10',
          'text-blue-700 dark:text-blue-400',
          'ring-blue-700/10 dark:ring-blue-400/30',
        ],
        purple: [
          'bg-purple-50 dark:bg-purple-400/10',
          'text-purple-700 dark:text-purple-400',
          'ring-purple-700/10 dark:ring-purple-400/30',
        ],
      },
      rounded: {
        true: 'rounded-full',
        false: 'rounded-md',
      },
    },
    defaultVariants: {
      color: 'gray',
      rounded: false,
    },
  },
)

type BadgeCvaProps = Required<VariantProps<typeof badge>>

export type BadgeColor = BadgeCvaProps['color']

interface BadgeProps {
  color?: BadgeCvaProps['color']
  rounded?: boolean
}
</script>

<template>
  <span :class="badge({ color, rounded })">
    <slot />
  </span>
</template>
