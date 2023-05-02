<script setup lang="ts">
import { MagnifyingGlassIcon } from '@heroicons/vue/20/solid'

const searchButton = cva(
  'flex items-center motion-safe:transition-colors rounded-lg',
  {
    variants: {
      transparent: {
        false: [
          'bg-gray-700/70 text-gray-300/80',
          'hover:bg-gray-700 focus-within:bg-gray-700',
          'hover:text-gray-300 focus-within:text-gray-300'
        ],
        true: [
          'bg-white/80 supports-backdrop-blur:bg-white/70',
          'dark:bg-gray-900/70 dark:supports-backdrop-blur:bg-gray-900/60',
          'backdrop-blur text-gray-700 dark:text-gray-400',
          'hover:bg-white/90 hover:supports-backdrop-blur:bg-white/80',
          'hover:dark:bg-gray-900/80 hover:dark:supports-backdrop-blur:bg-gray-900/70',
          'hover:text-gray-800 hover:dark:text-gray-300'
        ],
      }
    },
    defaultVariants: {
      transparent: false,
    }
  }
)

const kbd = cva(
  [
    'font-sans-safe block rounded px-1.5 py-px text-xs border',
    'motion-safe:transition-colors shadow-sm',
  ],
  {
    variants: {
      transparent: {
        false: [
          'border-gray-500 text-gray-200'
        ],
        true: [
          'border-transparent bg-white/80 text-gray-950',
          'dark:bg-transparent dark:text-gray-100',
          'dark:border-gray-500/50 group-hover:dark:border-gray-500/70'
        ]
      }
    },
    defaultVariants: {
      transparent: false
    }
  }
)

type SearchButtonCvaProps = Required<VariantProps<typeof searchButton>>

interface SearchButtonProps {
  transparent?: SearchButtonCvaProps['transparent'],
}

const props = withDefaults(defineProps<SearchButtonProps>(), {
  transparent: undefined,
})

const isMac = ref(
  // @ts-ignore
  navigator.userAgentData
    ? // @ts-ignore
      navigator.userAgentData.platform.toLowerCase().indexOf('mac') > -1
    : navigator.platform.toLowerCase().indexOf('mac') > -1
)
</script>

<template>
  <div :class="searchButton({ transparent })">
    <button
      type="button"
      :class="[
        'flex items-center px-3 py-2 group rounded-lg',
        'focus:outline-none focus-visible:ring-2',
        'focus-visible:ring-white/90',
      ]"
    >
      <MagnifyingGlassIcon class="w-5 h-5" />
      <span class="ml-3 text-sm w-56 text-left">
        {{ $t('common-actions.search-collection') }}
      </span>
      <div
        aria-hidden="true"
        class="space-x-1 flex"
      >
        <kbd :class="kbd({ transparent })">
          <abbr title="Control" class="no-underline" v-if="!isMac">Ctrl</abbr>
          <abbr title="Command" class="no-underline" v-else>⌘</abbr>
        </kbd>
        <kbd :class="kbd({ transparent })">K</kbd>
      </div>
    </button>
  </div>
</template>