<script lang="ts" setup>
import type { PersonEntity } from '@/types/tankobon-person'

export interface PersonNameProps {
  person?: PersonEntity | null
  loading?: boolean
}

const props = withDefaults(defineProps<PersonNameProps>(), {
  person: undefined,
  loading: false,
})

const { person, loading } = toRefs(props)
</script>

<template>
  <div class="flex flex-col justify-end sm:h-28 text-gray-700 sm:text-white/80 2xl:pr-52">
    <h2
      v-if="!loading"
      class="pt-3 text-gray-900 sm:text-white/95 dark:text-white/95 text-xl sm:text-2xl md:text-3xl font-display-safe font-semibold"
    >
      {{ person!.attributes.name }}
    </h2>
    <div
      v-else
      aria-hidden="true"
      class="mt-3 skeleton w-44 sm:w-72 h-8 mb-2 bg-white/50 dark:bg-white/30"
    />

    <p
      v-if="!loading && person?.attributes.nativeName.name.length"
      :lang="person.attributes.nativeName.language ?? undefined"
      class="font-display-safe text-md sm:text-lg sm:text-white/80 dark:text-white/80 md:text-xl mb-2"
    >
      {{ person.attributes.nativeName.name }}
    </p>
  </div>
</template>
