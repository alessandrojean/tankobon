<script lang="ts" setup>
import type { UserEntity } from '@/types/tankobon-user'

export interface UserNameProps {
  user?: UserEntity | null
  loading?: boolean
}

const props = withDefaults(defineProps<UserNameProps>(), {
  user: undefined,
  loading: false,
})

const { user, loading } = toRefs(props)
</script>

<template>
  <div class="flex flex-col justify-end sm:h-28 text-gray-700 sm:text-white/80 2xl:pr-52">
    <h2
      v-if="!loading"
      class="pt-3 text-gray-900 sm:text-white/95 dark:text-white/95 text-xl sm:text-2xl md:text-3xl font-display-safe font-semibold"
    >
      {{ user!.attributes.name }}
    </h2>
    <div
      v-else
      aria-hidden="true"
      class="mt-3 skeleton w-44 sm:w-72 h-8 mb-2 bg-white/50 dark:bg-white/30"
    />

    <a
      v-if="!loading"
      :class="[
        'w-fit font-display-safe text-md sm:text-lg sm:text-white/80',
        'dark:text-white/80 md:text-xl mb-2',
        'hocus:text-white relative rounded-sm',
        'hocus:underline hocus:underline-offset-2 hocus:decoration-2',
        'hocus:decoration-white/70 focus:outline-none focus-visible:outline',
        'focus-visible:outline-white/80 focus-visible:outline-2',
      ]"
      :href="`mailto:${user!.attributes.email}`"
    >
      {{ user!.attributes.email }}
    </a>
  </div>
</template>
