<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import type { CollectionEntity, CollectionUpdate } from '@/types/tankobon-collection'
import CollectionForm from '@/components/collections/CollectionForm.vue'

export interface CollectionEditDialogProps {
  isOpen: boolean
  collectionEntity: CollectionEntity
}

export interface CollectionEditDialogEmits {
  (e: 'close'): void
  (e: 'submit', collection: CollectionUpdate): void
}

const props = defineProps<CollectionEditDialogProps>()
const emit = defineEmits<CollectionEditDialogEmits>()

const { isOpen, collectionEntity } = toRefs(props)

const collection = reactive<CollectionUpdate>({
  id: '',
  name: '',
  description: '',
})

const collectionForm = ref<InstanceType<typeof CollectionForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(collection, {
    id: collectionEntity.value.id,
    name: collectionEntity.value.attributes.name,
    description: collectionEntity.value.attributes.description,
  })

  collectionForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await collectionForm.value!.v$.$validate()

  if (!isValid)
    return

  emit('close')
  emit('submit', toRaw(collection))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('collections.update-header')"
    :description="$t('collections.update-description', [collectionEntity.attributes.name])"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <CollectionForm
        ref="collectionForm"
        v-model:name="collection.name"
        v-model:description="collection.description"
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
