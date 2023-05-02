<script lang="ts" setup>
import {
  ChevronDoubleLeftIcon,
  ChevronDoubleRightIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
} from '@heroicons/vue/20/solid'

export interface PaginatorProps {
  hasPreviousPage: boolean
  hasNextPage: boolean
}

defineProps<PaginatorProps>()

defineEmits<{
  (e: 'click:first-page'): void
  (e: 'click:previous-page'): void
  (e: 'click:next-page'): void
  (e: 'click:last-page'): void
}>()
</script>

<template>
  <div class="flex -space-x-px">
    <Button
      kind="ghost-alt"
      class="rounded-r-none w-10"
      :disabled="!hasPreviousPage"
      @click="$emit('click:first-page')"
    >
      <span class="sr-only">{{ $t('pagination.first-page') }}</span>
      <ChevronDoubleLeftIcon class="w-5 h-5" />
    </Button>
    <Button
      kind="ghost-alt"
      class="rounded-none w-10"
      :disabled="!hasPreviousPage"
      @click="$emit('click:previous-page')"
    >
      <span class="sr-only">{{ $t('pagination.previous-page') }}</span>
      <ChevronLeftIcon class="w-5 h-5" />
    </Button>
    <Button
      kind="ghost-alt"
      class="rounded-none w-10"
      :disabled="!hasNextPage"
      @click="$emit('click:next-page')"
    >
      <span class="sr-only">{{ $t('pagination.next-page') }}</span>
      <ChevronRightIcon class="w-5 h-5" />
    </Button>
    <Button
      kind="ghost-alt"
      class="rounded-l-none w-10"
      :disabled="!hasNextPage"
      @click="$emit('click:last-page')"
    >
      <span class="sr-only">{{ $t('pagination.last-page') }}</span>
      <ChevronDoubleRightIcon class="w-5 h-5" />
    </Button>
  </div>
</template>
