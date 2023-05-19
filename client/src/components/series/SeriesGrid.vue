<script lang="ts" setup>
import { PaginatedResponse } from '@/types/tankobon-response'
import { SeriesEntity } from '@/types/tankobon-series'
import { PlusIcon } from '@heroicons/vue/20/solid'
import { BookOpenIcon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import { FocusKeys } from '@primer/behaviors'

export interface BooksGridProps {
  series?: PaginatedResponse<SeriesEntity>
  loading?: boolean
  mode?: 'comfortable' | 'compact'
  page: number
  search?: string
  size: number
}

const props = withDefaults(defineProps<BooksGridProps>(), {
  series: undefined,
  loading: false,
  mode: 'comfortable',
  search: '',
})

defineEmits<{
  (e: 'update:page', page: number): void
  (e: 'update:size', size: number): void
}>()

const { series, loading } = toRefs(props)

const showFooter = computed(() => {
  return series.value && series.value.pagination.totalElements >= 10 && series.value.data.length > 0
})

const container = ref<HTMLUListElement>()

useFocusZone({
  containerRef: container,
  bindKeys: FocusKeys.ArrowAll | FocusKeys.HomeAndEnd,
  focusInStrategy: 'closest',
  focusOutBehavior: 'wrap',
  disabled: loading
})
</script>

<template>
  <Block class="@container">
    <div
      v-if="loading"
      class="grid gap-2.5 sm:gap-4 grid-cols-2 @md:grid-cols-3 @xl:grid-cols-5 @4xl:grid-cols-6"
    >
      <SeriesCard
        v-for="s in 30"
        :key="s"
        :mode="mode"
        loading
      />
    </div>
    <ul
      v-else-if="series?.data?.length"
      ref="container"
      class="grid gap-2.5 sm:gap-4 grid-cols-2 @md:grid-cols-3 @xl:grid-cols-5 @4xl:grid-cols-6"
    >
      <li v-for="seriesItem in series?.data" :key="seriesItem.id">
        <SeriesCard
          class="scroll-mt-20"
          :series="seriesItem"
          :loading="loading"
          :mode="mode"
        />
      </li>
    </ul>

    <slot v-else name="empty">
      <EmptyState
        :icon="search.length > 0 ? MagnifyingGlassIcon : BookOpenIcon"
        :title="$t('series.empty-header')"
        :description="
          search.length > 0
            ? $t('series.empty-search-description', [search])
            : $t('series.empty-description')
        "
      >
        <template v-if="search.length === 0" #actions>
          <Button
            kind="primary"
            is-router-link
            :to="{ name: 'series-new' }"
          >
            <PlusIcon class="w-5 h-5" />
            <span>{{ $t('series.new') }}</span>
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
          {{ series!.pagination.totalPages }}
        </span>
        <span class="font-semibold dark:text-gray-100">
          {{ series!.pagination.totalElements }}
        </span>
      </i18n-t>
      <Paginator
        :has-previous-page="series!.pagination.currentPage > 0"
        :has-next-page="series!.pagination.currentPage + 1 < series!.pagination.totalPages"
        @click:first-page="$emit('update:page', 0)"
        @click:previous-page="$emit('update:page', page - 1)"
        @click:next-page="$emit('update:page', page + 1)"
        @click:last-page="$emit('update:page', series!.pagination.totalPages - 1)"
      />
    </div>
  </Block>
</template>
