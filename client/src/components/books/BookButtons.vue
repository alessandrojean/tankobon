<script lang="ts" setup>
import {
  PencilIcon,
  TrashIcon,
} from '@heroicons/vue/24/solid'

import type { BookEntity } from '@/types/tankobon-book'

export interface BookButtonsProps {
  book?: BookEntity | null
  canEdit?: boolean
  editing?: boolean
  loading?: boolean
}

const props = withDefaults(defineProps<BookButtonsProps>(), {
  book: undefined,
  canEdit: true,
  editing: false,
  loading: false,
})

defineEmits<{
  (e: 'click:edit', event: MouseEvent): void
  (e: 'click:delete', event: MouseEvent): void
  (e: 'click:toggleFavorite', event: MouseEvent): void
  (e: 'click:toggleStatus', event: MouseEvent): void
  (e: 'click:updateCover', event: MouseEvent): void
  (e: 'click:share', event: MouseEvent): void
}>()

const { editing, loading } = toRefs(props)

const disabled = computed(() => loading.value || editing.value)
</script>

<template>
  <div
    v-if="loading"
    class="flex w-full justify-center sm:justify-start items-center gap-2"
  >
    <div class="skeleton w-12 h-12" />
    <div class="skeleton w-12 h-12" />
  </div>
  <Toolbar
    v-else-if="canEdit"
    class="flex w-full justify-center sm:justify-start items-center gap-2"
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

    <!-- <Button
      v-if="!loading && book!.attributes.isInLibrary"
      class="ml-2"
      size="large"
      :icon-only="iconOnly"
      :disabled="disabled"
      :title="
          t('dashboard.details.header.options.markAs', {
            status: t(book!.isRead ? 'book.unread' : 'book.read').toLowerCase()
          })
        "
      @click="$emit('click:toggleStatus', $event)"
    >
      <template #left="{ iconClass }" v-if="!iconOnly">
        <BookmarkSolidIcon v-if="book!.isRead" :class="iconClass" />
        <BookmarkOutlineIcon v-else :class="iconClass" />
      </template>
      <template #default v-if="!iconOnly">
        <span>
          {{ book!.isRead ? t('book.read') : t('book.unread') }}
        </span>
      </template>
      <template #default="{ iconClass }" v-else>
        <BookmarkSolidIcon v-if="book!.isRead" :class="iconClass" />
        <BookmarkOutlineIcon v-else :class="iconClass" />
      </template>
    </Button> -->

    <Button
      v-if="!loading"
      class="aspect-1"
      size="small"
      :disabled="disabled"
      :title="$t('common-actions.delete')"
      @click="$emit('click:delete', $event)"
    >
      <span class="sr-only">{{ $t('common-actions.delete') }}</span>
      <TrashIcon class="w-5 h-5" />
    </Button>
  </Toolbar>
</template>
