<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import ReadProgressForm from './ReadProgressForm.vue'
import type { ReadProgressEntity, ReadProgressUpdate } from '@/types/tankobon-read-progress'

export interface ReadProgressEditDialogProps {
  isOpen: boolean
  pageCount: number
  readProgressEntity: ReadProgressEntity
}

export interface ReadProgressEditDialogEmits {
  (e: 'close'): void
  (e: 'submit', readProgress: ReadProgressUpdate): void
}

const props = defineProps<ReadProgressEditDialogProps>()
const emit = defineEmits<ReadProgressEditDialogEmits>()

const { isOpen, readProgressEntity } = toRefs(props)

const readProgress = reactive<ReadProgressUpdate>({
  id: '',
  startedAt: null,
  finishedAt: null,
  page: 0,
  isCompleted: false,
})

const readProgressForm = ref<InstanceType<typeof ReadProgressForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(readProgress, {
    id: readProgressEntity.value.id,
    startedAt: readProgressEntity.value.attributes.startedAt,
    finishedAt: readProgressEntity.value.attributes.finishedAt,
    page: readProgressEntity.value.attributes.page,
    isCompleted: readProgressEntity.value.attributes.isCompleted,
  } satisfies ReadProgressUpdate)

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
    :title="$t('read-progresses.update-header')"
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
        <span>{{ $t('common-actions.save') }}</span>
      </Button>
    </template>
  </Dialog>
</template>
