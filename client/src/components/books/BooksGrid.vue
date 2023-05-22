<script lang="ts" setup>
import { PlusIcon } from '@heroicons/vue/20/solid'
import { BookOpenIcon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import { FocusKeys } from '@primer/behaviors'
import type { PaginatedResponse } from '@/types/tankobon-response'
import type { BookEntity } from '@/types/tankobon-book'

export interface BooksGridProps {
  books?: PaginatedResponse<BookEntity>
  loading?: boolean
  mode?: 'comfortable' | 'compact'
  page: number
  search?: string
  size: number
  unpaged?: boolean
}

const props = withDefaults(defineProps<BooksGridProps>(), {
  books: undefined,
  loading: false,
  mode: 'comfortable',
  search: '',
  unpaged: false,
})

defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
}>()

const { books, loading, unpaged } = toRefs(props)

const showFooter = computed(() => {
  return books.value
    && books.value.pagination.totalElements >= 10
    && books.value.data.length > 0
    && !unpaged.value
})

const container = ref<HTMLUListElement>()

useFocusZone({
  containerRef: container,
  bindKeys: FocusKeys.ArrowAll | FocusKeys.HomeAndEnd,
  focusInStrategy: 'closest',
  focusOutBehavior: 'wrap',
  disabled: loading,
})
</script>

<template>
  <Block class="@container">
    <div
      v-if="loading"
      class="grid gap-2.5 sm:gap-4 grid-cols-2 @md:grid-cols-3 @xl:grid-cols-5 @4xl:grid-cols-6"
    >
      <BookCard
        v-for="b in 30"
        :key="b"
        :mode="mode"
        loading
      />
    </div>
    <ul
      v-else-if="books?.data?.length"
      ref="container"
      class="grid gap-2.5 sm:gap-4 grid-cols-2 @md:grid-cols-3 @xl:grid-cols-5 @4xl:grid-cols-6"
    >
      <li v-for="book in books?.data" :key="book.id">
        <BookCard
          class="scroll-mt-20"
          :book="book"
          :loading="loading"
          :mode="mode"
        />
      </li>
    </ul>

    <slot v-else name="empty">
      <EmptyState
        :icon="search.length > 0 ? MagnifyingGlassIcon : BookOpenIcon"
        :title="$t('books.empty-header')"
        :description="
          search.length > 0
            ? $t('books.empty-search-description', [search])
            : $t('books.empty-description')
        "
      >
        <template v-if="search.length === 0" #actions>
          <Button
            kind="primary"
            is-router-link
            :to="{ name: 'books-new' }"
          >
            <PlusIcon class="w-5 h-5" />
            <span>{{ $t('books.new') }}</span>
          </Button>
        </template>
      </EmptyState>
    </slot>

    <div
      v-if="showFooter"
      class="mt-1 pt-1 sm:pt-4 sm:mt-4 bg-gray-50 dark:bg-gray-900 border-t border-gray-200 dark:border-gray-800 flex justify-between items-center"
    >
      <div>
        <label for="items-per-page" class="sr-only">
          {{ $t('pagination.items-per-page') }}
        </label>
        <BasicSelect
          id="items-per-page"
          size="small"
          :model-value="size"
          :options="[10, 20, 30, 40, 50]"
          :option-text="(v) => $t('pagination.show-n-items', v)"
          @update:model-value="$emit('update:size', $event)"
        />
      </div>
      <i18n-t
        keypath="pagination.page"
        tag="p"
        class="text-sm text-gray-600 dark:text-gray-300"
      >
        <span class="font-semibold dark:text-gray-100">
          {{ page + 1 }}
        </span>
        <span class="font-semibold dark:text-gray-100">
          {{ books!.pagination.totalPages }}
        </span>
        <span class="font-semibold dark:text-gray-100">
          {{ books!.pagination.totalElements }}
        </span>
      </i18n-t>
      <Paginator
        :has-previous-page="books!.pagination.currentPage > 0"
        :has-next-page="books!.pagination.currentPage + 1 < books!.pagination.totalPages"
        @click:first-page="$emit('update:page', 0)"
        @click:previous-page="$emit('update:page', page - 1)"
        @click:next-page="$emit('update:page', page + 1)"
        @click:last-page="$emit('update:page', books!.pagination.totalPages - 1)"
      />
    </div>
  </Block>
</template>
