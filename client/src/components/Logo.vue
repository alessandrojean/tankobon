<script lang="ts" setup>
import { BookOpenIcon } from '@heroicons/vue/20/solid'

export interface LogoProps {
  dark?: boolean
  label: string
  iconOnly?: boolean
}

const props = withDefaults(defineProps<LogoProps>(), { dark: false })
const { dark, label, iconOnly } = toRefs(props)

const supText = computed(() => import.meta.env.DEV ? 'DEV' : null)
</script>

<template>
  <div class="shrink-0 flex items-center py-1 select-none">
    <span aria-hidden="true">
      <BookOpenIcon class="h-9 w-9 text-primary-500" />
    </span>
    <span class="sr-only">{{ label }}</span>
    <span
      v-if="!iconOnly"
      :class="[
        'font-display font-semibold text-xl ml-3',
        dark ? 'text-gray-50' : 'text-gray-800 dark:text-gray-50'
      ]"
      aria-hidden="true"
    >
      Tankobon
    </span>
    <sup
      v-if="!iconOnly && supText"
      :class="[
        'font-semibold text-[0.6rem] align-super ml-0.5',
        dark ? 'text-gray-400' : 'text-gray-500 dark:text-gray-400'
      ]"
      aria-hidden="true"
    >
      {{ supText }}
    </sup>
  </div>
</template>
