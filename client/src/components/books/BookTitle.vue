<script lang="ts" setup>
import { BookEntity } from '@/types/tankobon-book'
import { getRelationship, getRelationships } from '@/utils/api';

export interface BookTitleProps {
  book?: BookEntity | null
  loading?: boolean
}

const props = withDefaults(defineProps<BookTitleProps>(), {
  book: undefined,
  loading: false
})

defineEmits<{
  (e: 'click:author', author: string): void
}>()

const { book, loading } = toRefs(props)
const series = computed(() => getRelationship(book.value, 'SERIES'))
const contributors = computed(() => getRelationships(book.value, 'CONTRIBUTOR'))
const { t, locale } = useI18n()

const listFormatter = computed(() => {
  return new Intl.ListFormat(locale.value, {
    style: 'long',
    type: 'conjunction',
  })
})

const contributorsList = computed(() => {
  const unique = Array.from(new Set(contributors.value?.map((c) => c.attributes!.person) ?? []))

  return listFormatter.value.formatToParts(unique)
})
</script>

<template>
  <div class="flex flex-col sm:h-56 text-gray-700 sm:text-white/80 2xl:pr-52">
    <h2
      v-if="!loading"
      class="pt-3 text-gray-900 sm:text-white/95 dark:text-white/95 text-xl sm:text-2xl md:text-3xl font-display font-medium"
    >
      {{ book!.attributes.title }}
    </h2>
    <div
      v-else
      aria-hidden="true"
      class="mt-3 skeleton w-44 sm:w-72 h-8 mb-2 bg-white/50 dark:bg-white/30"
    />

    <!-- <p
      v-if="!loading && book!.titleParts.subtitle"
      class="font-display text-md sm:text-lg sm:text-white/80 dark:text-white/80 md:text-xl -mt-1 mb-2"
    >
      {{ book!.titleParts.subtitle }}
    </p> -->

    <div class="flex-grow hidden sm:block" aria-hidden="true" />

    <p
      v-if="!loading"
      class="text-sm md:text-base sm:text-white/90 dark:text-white/90"
    >
      <template v-for="(part, idx) in contributorsList" :key="idx">
        <a
          v-if="part.type === 'element'"
          href="#"
          class="author"
          :title="t('common-actions.search-by', [part.value])"
        >
          {{ part.value }}
        </a>
        <span v-else>
          {{ part.value }}
        </span>
      </template>
    </p>
    <div
      v-else
      class="skeleton h-6 w-40 bg-white/50 dark:bg-white/30"
      aria-hidden="true"
    />
  </div>
</template>

<style lang="postcss" scoped>
.author {
  @apply font-medium hocus:text-white relative sm:text-white/95 rounded-sm
    hocus:underline hocus:underline-offset-2 hocus:decoration-2
    hocus:decoration-white/70 focus:outline-none focus-visible:outline
    focus-visible:outline-white/80 focus-visible:outline-2;
}
</style>