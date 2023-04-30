<script lang="ts" setup>
import { BookEntity } from '@/types/tankobon-book'
import {
  BookmarkIcon as BookmarkSolidIcon,
  PencilIcon,
  TrashIcon,
} from '@heroicons/vue/24/solid'
import {
  BookmarkIcon as BookmarkOutlineIcon,
} from '@heroicons/vue/24/outline'
import { breakpointsTailwind } from '@vueuse/core'

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
  loading: false
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

const breakpoints = useBreakpoints(breakpointsTailwind)
const iconOnly = breakpoints.smaller('2xl')
</script>

<template>
  <div
    class="flex w-full justify-center sm:justify-start items-center gap-2"
    v-if="canEdit"
  >
    <Button
      v-if="!loading"
      class="aspect-1"
      size="small"
      @click="$emit('click:edit', $event)"
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
  </div>
</template>