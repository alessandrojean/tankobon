<script setup lang="ts">
import { BookmarkSquareIcon } from '@heroicons/vue/24/outline'
import { PlusIcon } from '@heroicons/vue/20/solid'
import type { BookEntity } from '@/types/tankobon-book'
import type { ReadProgressEntity } from '@/types/tankobon-read-progress'

export interface BookReadProgressesProps {
  book: BookEntity | null | undefined
  creating: boolean
  deleting: boolean
  deletingId?: string
  editing: boolean
  editingId?: string
  loading: boolean
  readProgresses: ReadProgressEntity[]
}

const props = defineProps<BookReadProgressesProps>()

defineEmits<{
  (e: 'click:new'): void
  (e: 'click:edit', readProgress: ReadProgressEntity): void
  (e: 'click:delete', readProgress: ReadProgressEntity): void
}>()

const { readProgresses } = toRefs(props)

const largestReadPeriod = computed(() => {
  const period = readProgresses.value
    .filter(rp => rp.attributes.startedAt && rp.attributes.finishedAt)
    .map((rp) => {
      const startedAt = new Date(rp.attributes.startedAt!)
      const finishedAt = new Date(rp.attributes.finishedAt!)

      return (finishedAt.getTime() - startedAt.getTime()) / 1_000
    })

  return period.length > 0 ? Math.max(...period) : 0
})

const totalPagesRead = computed(() => {
  return readProgresses.value
    .reduce((sum, rp) => sum + rp.attributes.page, 0)
})
</script>

<template>
  <div>
    <div
      v-if="readProgresses.length === 0"
      class="flex flex-col gap-4 sm:gap-6"
    >
      <Alert
        type="warning"
        :show="book?.attributes.pageCount === 0"
      >
        {{ $t('read-progresses.zero-page-count-warning') }}
      </Alert>

      <EmptyState
        :icon="BookmarkSquareIcon"
        :title="$t('read-progresses.empty-header')"
        :description="$t('read-progresses.empty-description')"
      >
        <template #actions>
          <Button
            kind="primary"
            :disabled="creating || loading || book?.attributes.pageCount === 0"
            :loading="creating"
            @click="$emit('click:new')"
          >
            <PlusIcon class="w-5 h-5" />
            <span>{{ $t('read-progresses.new') }}</span>
          </Button>
        </template>
      </EmptyState>
    </div>

    <div
      v-else
      class="read-progress-grid"
    >
      <div class="read-progress-statistics">
        <div class="grid grid-cols-1 md:grid-cols-3 2xl:grid-cols-1 gap-4 sm:gap-6">
          <StatisticCard
            unit="count"
            :value="readProgresses.length"
            :title="$t('read-progresses.readings-count')"
          />

          <StatisticCard
            unit="count"
            :value="totalPagesRead"
            :title="$t('read-progresses.total-pages-read')"
          />

          <StatisticCard
            unit="seconds"
            :value="largestReadPeriod"
            :title="$t('read-progresses.largest-read')"
          />
        </div>
      </div>

      <div class="read-progress-cards flex flex-col gap-4 sm:gap-6">
        <Alert
          type="warning"
          :show="book?.attributes.pageCount === 0"
        >
          {{ $t('read-progresses.zero-page-count-warning') }}
        </Alert>

        <BookReadProgress
          v-for="readProgress in readProgresses"
          :key="readProgress.id"
          :page-count="book?.attributes.pageCount ?? 0"
          :read-progress="readProgress"
          :editing="editing && editingId === readProgress.id"
          :deleting="deleting && deletingId === readProgress.id"
          :disabled="editing || deleting || creating"
          @click:edit="$emit('click:edit', readProgress)"
          @click:delete="$emit('click:delete', readProgress)"
        />
      </div>
    </div>
  </div>
</template>

<style lang="postcss" scoped>
.read-progress-grid {
  display: grid;
  gap: 1rem;
  grid-template-areas:
    'statistics statistics'
    'cards cards';
  grid-template-columns: 1fr 1fr;

  @media (min-width: theme('screens.sm')) {
    gap: 1.5rem;
  }

  @media (min-width: theme('screens.2xl')) {
    grid-template-areas:
      'cards statistics';
    grid-template-columns: 1fr 14rem;
  }

  .read-progress-cards {
    grid-area: cards / cards / cards / cards;
  }

  .read-progress-statistics {
    grid-area: statistics / statistics / statistics / statistics;
  }
}
</style>
