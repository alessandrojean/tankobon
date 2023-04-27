<script lang="ts" setup>
import { ArrowTopRightOnSquareIcon } from '@heroicons/vue/20/solid'

import type { Item } from '@/components/AsideMenu.vue'

export interface DashboardAsideButton {
  active?: boolean
  href: string
  item: Item
}

const props = withDefaults(defineProps<DashboardAsideButton>(), {
  active: false
})

const { active, item, href } = toRefs(props)
</script>

<template>
  <a
    :href="href"
    :target="item.external ? '_blank' : undefined"
    :class="[
      'group flex items-center flex-nowrap text-sm rounded-lg w-full has-ring-focus',
      'font-medium dark:focus-visible:ring-offset-gray-800',
      item.icon ? 'h-10' : 'h-9',
      active
        ? 'bg-primary-100 text-primary-900 dark:text-gray-100 dark:bg-gray-800'
        : 'text-gray-700 dark:text-gray-300 hocus:bg-gray-200 hocus:text-gray-800 dark:hocus:text-gray-100 dark:hocus:bg-gray-800'
    ]"
    :title="item.label"
  >
    <div
      v-if="item.icon"
      :class="[
        'shrink-0 w-10 h-10 flex items-center justify-center',
        'motion-safe:transition-colors'
      ]"
    >
      <component
        :is="item.icon"
        :class="[
          'w-6 h-6 motion-safe:transition-colors',
          active
            ? 'text-primary-600 dark:text-primary-500'
            : 'text-gray-500 dark:text-gray-400 group-hocus:text-gray-600 dark:group-hocus:text-gray-300'
        ]"
      />
    </div>
    <span
      :class="[
        'shrink-0 pl-3 box-border truncate motion-safe:transition-all',
        item.icon ? 'w-[11.5rem]' : 'w-full'
      ]"
    >
      {{ item.label }}
    </span>
    <div
      v-if="item.external"
      class="shrink-0 w-10 h-10 flex items-center justify-center"
    >
      <ArrowTopRightOnSquareIcon
        v-if="item.external"
        class="w-4 h-4 text-gray-500 dark:text-gray-400 group-hocus:text-gray-600 dark:group-hocus:text-gray-100 motion-safe:transition-color"
      />
    </div>
  </a>
</template>