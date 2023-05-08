<script setup lang="ts">
import { ArrowLeftIcon, ArrowRightIcon } from '@heroicons/vue/24/outline'
import type { BookAttributes } from '@/types/tankobon-book'
import type { Relationship } from '@/types/tankobon-entity'

export interface BookNavigatorProps {
  loading?: boolean
  next: Relationship<BookAttributes> | null | undefined
  previous: Relationship<BookAttributes> | null | undefined
}

withDefaults(defineProps<BookNavigatorProps>(), { loading: false })
</script>

<template>
  <nav v-if="!loading && (previous || next)">
    <ul class="flex flex-col sm:flex-row gap-2">
      <li v-if="previous" class="flex-1 shrink-0 min-w-0">
        <RouterLink
          :class="[
            'flex gap-2 items-center p-4 border rounded-xl group/previous',
            'dark:border-gray-700 dark:hover:border-gray-600',
            'dark:hover:bg-gray-900 hover:shadow-md hover:-translate-y-0.5',
            'motion-safe:transition will-change-transform',
            'focus:outline-none focus-visible:ring-2',
            'focus-visible:ring-black dark:focus-visible:ring-white/90',
          ]"
          :to="{ name: 'books-id', params: { id: previous.id } }"
          :title="$t('common-actions.go-to-page', [previous.attributes!.title])"
        >
          <ArrowLeftIcon
            :class="[
              'shrink-0 w-6 h-6 mr-2 text-gray-400',
              'group-hover/previous:text-primary-600 dark:group-hover/previous:text-gray-200',
              'motion-safe:transition-colors',
            ]"
          />
          <div class="flex-1 shrink-0 min-w-0">
            <span
              :class="[
                'block text-xs text-right text-gray-500',
                'dark:text-gray-400 dark:group-hover/previous:text-gray-300',
                'motion-safe:transition-colors',
              ]"
            >
              {{ $t('pagination.previous') }}
            </span>
            <span
              :class="[
                'block font-medium text-right truncate',
                'dark:text-gray-200 dark:group-hover/previous:text-gray-50',
                'motion-safe:transition-colors',
              ]"
            >
              {{ $t('books.volume', [previous.attributes!.number]) }}
            </span>
          </div>
        </RouterLink>
      </li>
      <li v-if="next" class="flex-1 shrink-0 min-w-0">
        <RouterLink
          :class="[
            'flex gap-2 items-center p-4 border rounded-xl group/next',
            'dark:border-gray-700 dark:hover:border-gray-600',
            'dark:hover:bg-gray-900 hover:shadow-md hover:-translate-y-0.5',
            'motion-safe:transition will-change-transform',
            'focus:outline-none focus-visible:ring-2',
            'focus-visible:ring-black dark:focus-visible:ring-white/90',
          ]"
          :to="{ name: 'books-id', params: { id: next.id } }"
          :title="$t('common-actions.go-to-page', [next.attributes!.title])"
        >
          <div class="flex-1 shrink-0 min-w-0">
            <span
              :class="[
                'block text-xs text-gray-500',
                'dark:text-gray-400 dark:group-hover/next:text-gray-300',
                'motion-safe:transition-colors',
              ]"
            >
              {{ $t('pagination.next') }}
            </span>
            <span
              :class="[
                'block font-medium truncate',
                'dark:text-gray-200 dark:group-hover/next:text-gray-50',
                'motion-safe:transition-colors',
              ]"
            >
              {{ $t('books.volume', [next.attributes!.number]) }}
            </span>
          </div>
          <ArrowRightIcon
            :class="[
              'shrink-0 w-6 h-6 ml-2 text-gray-400',
              'group-hover/next:text-primary-600 dark:group-hover/next:text-gray-200',
              'motion-safe:transition-colors',
            ]"
          />
        </RouterLink>
      </li>
    </ul>
  </nav>
  <div v-else-if="loading" aria-hidden="true">
    <div class="flex flex-col sm:flex-row gap-2">
      <div
        class="grow shrink-0 flex items-center p-4 border dark:border-gray-700 rounded-xl"
      >
        <div aria-hidden="true" class="shrink-0">
          <ArrowLeftIcon class="w-6 h-6 mr-2 text-gray-400" />
        </div>
        <div class="grow shrink-0">
          <div class="ml-auto skeleton w-16 h-4" />
          <div class="ml-auto skeleton w-28 h-5 mt-1" />
        </div>
      </div>
      <div
        class="grow shrink-0 flex items-center p-4 border dark:border-gray-700 rounded-xl"
      >
        <div class="grow shrink-0">
          <div class="skeleton w-16 h-4" />
          <div class="skeleton w-28 h-5 mt-1" />
        </div>
        <div aria-hidden="true" class="shrink-0">
          <ArrowRightIcon class="w-6 h-6 text-gray-400" />
        </div>
      </div>
    </div>
  </div>
</template>
