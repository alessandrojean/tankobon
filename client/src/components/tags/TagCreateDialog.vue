<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import TagForm from '@/components/tags/TagForm.vue'
import type { TagCreation } from '@/types/tankobon-tag'

export interface TagCreateDialogProps {
  libraryId: string
  isOpen: boolean
}

export interface TagCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', tag: TagCreation): void
}

const props = defineProps<TagCreateDialogProps>()
const emit = defineEmits<TagCreateDialogEmits>()

const { isOpen, libraryId } = toRefs(props)

const tag = reactive<TagCreation>({
  name: '',
  description: '',
  library: '',
})

const tagForm = ref<InstanceType<typeof TagForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(tag, {
    name: '',
    description: '',
    library: libraryId.value,
  })

  tagForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await tagForm.value!.v$.$validate()

  if (!isValid)
    return

  emit('close')
  emit('submit', toRaw(tag))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-lg"
    novalidate
    :is-open="isOpen"
    :title="$t('tags.create-header')"
    :description="$t('tags.create-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <TagForm
        ref="tagForm"
        v-model:name="tag.name"
        v-model:description="tag.description"
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
