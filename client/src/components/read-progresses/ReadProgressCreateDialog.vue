<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import ReadProgressForm from './ReadProgressForm.vue'
import type { ReadProgressCreation } from '@/types/tankobon-read-progress'

export interface ReadProgressCreateDialogProps {
  bookId: string
  isOpen: boolean
  pageCount: number
}

export interface ReadProgressCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', readProgress: ReadProgressCreation): void
}

const props = defineProps<ReadProgressCreateDialogProps>()
const emit = defineEmits<ReadProgressCreateDialogEmits>()

const { isOpen, bookId } = toRefs(props)

const readProgress = reactive<ReadProgressCreation>({
  book: '',
  startedAt: null,
  finishedAt: null,
  page: 0,
  isCompleted: false,
})

const readProgressForm = ref<InstanceType<typeof ReadProgressForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(readProgress, {
    startedAt: null,
    finishedAt: null,
    page: 0,
    isCompleted: false,
    book: bookId.value,
  } satisfies ReadProgressCreation)

  readProgressForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await readProgressForm.value!.v$.$validate()

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', {
    ...toRaw(readProgress),
    page: Number(readProgress.page),
  })
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-lg"
    novalidate
    :is-open="isOpen"
    :title="$t('read-progresses.create-header')"
    :description="$t('read-progresses.create-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <ReadProgressForm
        ref="readProgressForm"
        v-model:started-at="readProgress.startedAt"
        v-model:finished-at="readProgress.finishedAt"
        v-model:page="readProgress.page"
        v-model:is-completed="readProgress.isCompleted"
        :page-count="pageCount"
      />
    </template>
    <template #footer>
      <Button
        type="submit"
        class="ml-auto"
        kind="primary"
      >
        <CheckIcon class="w-5 h-5" />
        <span>{{ $t('common-actions.create') }}</span>
      </Button>
    </template>
  </Dialog>
</template>
