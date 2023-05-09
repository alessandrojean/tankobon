<script lang="ts" setup>
import {
  PencilIcon,
  TrashIcon,
} from '@heroicons/vue/24/solid'

import type { BookEntity } from '@/types/tankobon-book'

export interface BookButtonsProps {
  book?: BookEntity | null
  canEdit?: boolean
  deleting?: boolean
  loading?: boolean
}

const props = withDefaults(defineProps<BookButtonsProps>(), {
  book: undefined,
  canEdit: true,
  deleting: false,
  loading: false,
})

defineEmits<{
  (e: 'click:edit', event: MouseEvent): void
  (e: 'click:delete', event: MouseEvent): void
}>()

const { loading, deleting } = toRefs(props)

const disabled = computed(() => loading.value || deleting.value)
</script>

<template>
  <div
    v-if="loading"
    class="flex justify-center sm:justify-start items-center gap-2"
  >
    <div class="skeleton w-12 h-12" />
    <div class="skeleton w-12 h-12" />
  </div>
  <Toolbar
    v-else-if="canEdit"
    class="flex justify-center sm:justify-start items-center gap-2"
  >
    <Button
      v-if="!loading"
      class="aspect-1"
      size="small"
      is-router-link
      :to="{ name: 'books-id-edit', params: { id: book?.id } }"
      :disabled="disabled"
      :title="$t('common-actions.edit')"
    >
      <span class="sr-only">{{ $t('common-actions.edit') }}</span>
      <PencilIcon class="w-5 h-5" />
    </Button>

    <Button
      v-if="!loading"
      class="aspect-1"
      size="small"
      :disabled="disabled"
      :loading="deleting"
      :title="$t('common-actions.delete')"
      @click="$emit('click:delete', $event)"
    >
      <span class="sr-only">{{ $t('common-actions.delete') }}</span>
      <TrashIcon class="w-5 h-5" />
    </Button>
  </Toolbar>
</template>
