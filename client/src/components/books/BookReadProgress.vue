<script setup lang="ts">
import { BookmarkIcon, CheckCircleIcon, PencilIcon, PlayIcon, TrashIcon } from '@heroicons/vue/20/solid'
import type { ReadProgressEntity } from '@/types/tankobon-read-progress'

export interface BookReadProgressProps {
  deleting: boolean
  disabled: boolean
  editing: boolean
  pageCount: number
  readProgress: ReadProgressEntity
}

defineProps<BookReadProgressProps>()

defineEmits<{
  (e: 'click:edit'): void
  (e: 'click:delete'): void
}>()

const { d, t } = useI18n()

function formatDate(date: string | null) {
  return date ? d(new Date(date), 'short') : t('date.unknown')
}
</script>

<template>
  <Block>
    <div class="flex items-center gap-4">
      <div class="flex flex-col grow min-w-0">
        <div class="flex items-center gap-2">
          <label :for="`progress-${readProgress.id}`" class="sr-only">
            {{ $t('read-progresses.progress') }}
          </label>
          <progress
            :id="`progress-${readProgress.id}`"
            :class="[
              'appearance-none w-full h-2.5 rounded-full',
              'bg-gray-200 dark:bg-gray-700',
              '[&::-webkit-progress-bar]:rounded-full',
              '[&::-webkit-progress-bar]:overflow-hidden',
              '[&::-webkit-progress-bar]:bg-gray-200',
              'dark:[&::-webkit-progress-bar]:bg-gray-700',
              '[&::-webkit-progress-value]:bg-primary-500',
              '[&::-moz-progress-bar]:bg-primary-500',
            ]"
            :value="readProgress.attributes.page"
            :max="pageCount"
          >
            {{ $n(readProgress.attributes.page / pageCount, 'percent-integer') }}
          </progress>
          <span class="w-8 text-xs text-right text-gray-700 dark:text-gray-300">
            {{ $n(readProgress.attributes.page / pageCount, 'percent-integer') }}
          </span>
        </div>
        <div class="w-full flex flex-col md:flex-row gap-1 md:items-center justify-between tabular-nums mt-1 text-xs md:text-sm">
          <div class="w-full md:w-32 inline-flex items-center gap-1.5 text-gray-800 dark:text-gray-200">
            <PlayIcon class="w-4 h-4 text-gray-500 dark:text-gray-400" />
            <time
              v-if="readProgress.attributes.startedAt"
              :datetime="readProgress.attributes.startedAt ?? undefined"
            >
              {{ formatDate(readProgress.attributes.startedAt) }}
            </time>
            <span v-else>
              {{ $t('date.unknown-short') }}
            </span>

            <span class="ml-auto md:hidden text-gray-600 dark:text-gray-300">
              {{ readProgress.attributes.page }}/{{ pageCount }}
            </span>
          </div>

          <span class="hidden md:block w-32 text-center text-gray-600 dark:text-gray-300 text-xs">
            {{ readProgress.attributes.page }}/{{ pageCount }}
          </span>

          <div class="md:w-32 flex items-center gap-1.5 md:justify-end text-gray-800 dark:text-gray-200">
            <div
              v-if="readProgress.attributes.isCompleted"
              class="relative"
            >
              <div aria-hidden="true" class="inset-1 absolute bg-white rounded-full" />
              <CheckCircleIcon class="w-4 h-4 text-primary-600 dark:text-primary-500 relative" />
            </div>
            <BookmarkIcon
              v-else
              class="w-4 h-4 text-secondary-500"
            />
            <time
              v-if="readProgress.attributes.finishedAt"
              :datetime="readProgress.attributes.finishedAt ?? undefined"
            >
              {{ formatDate(readProgress.attributes.finishedAt) }}
            </time>
            <span v-else>
              {{
                readProgress.attributes.isCompleted
                  ? $t('date.unknown-short')
                  : $t('read-progresses.reading')
              }}
            </span>
          </div>
        </div>
      </div>
      <Toolbar class="shrink-0 flex items-center gap-1 -mr-2">
        <Button
          kind="ghost-alt"
          size="small"
          rounded="full"
          class="w-9 h-9"
          :title="$t('common-actions.edit')"
          :loading="editing"
          :disabled="disabled"
          @click="$emit('click:edit')"
        >
          <span class="sr-only">{{ $t('common-actions.edit') }}</span>
          <PencilIcon class="w-5 h-5" />
        </Button>
        <Button
          kind="ghost-danger"
          size="small"
          rounded="full"
          class="w-9 h-9"
          :title="$t('common-actions.delete')"
          :loading="deleting"
          :disabled="disabled"
          @click="$emit('click:delete')"
        >
          <span class="sr-only">{{ $t('common-actions.delete') }}</span>
          <TrashIcon class="w-5 h-5" />
        </Button>
      </Toolbar>
    </div>
  </Block>
</template>
