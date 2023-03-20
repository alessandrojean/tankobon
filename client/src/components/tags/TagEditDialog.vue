<script lang="ts" setup>
import type { TagEntity, TagUpdate } from '@/types/tankobon-tag'
import { CheckIcon } from '@heroicons/vue/20/solid'
import TagForm from '@/components/tags/TagForm.vue'

export interface TagEditDialogProps {
  isOpen: boolean,
  tagEntity: TagEntity,
}

export type TagEditDialogEmits = {
  (e: 'close'): void,
  (e: 'submit', tag: TagUpdate): void,
}

const props = defineProps<TagEditDialogProps>()
const emit = defineEmits<TagEditDialogEmits>()

const { isOpen, tagEntity } = toRefs(props)

const tag = reactive<TagUpdate>({
  id: '',
  name: '',
  description: '',
})

const tagForm = ref<InstanceType<typeof TagForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(tag, {
    id: tagEntity.value.id,
    name: tagEntity.value.attributes.name,
    description: tagEntity.value.attributes.description,
  })

  tagForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await tagForm.value!.v$.$validate()

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', toRaw(tag))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('tags.update-header')"
    :description="$t('tags.update-description', [tagEntity.attributes.name])"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <TagForm
        ref="tagForm"
        v-model:name="tag.name"
        v-model:description="tag.description"
        mode="update"
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