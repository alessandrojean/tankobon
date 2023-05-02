<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import CollectionForm from '@/components/collections/CollectionForm.vue'
import type { CollectionCreation } from '@/types/tankobon-collection'

export interface CollectionCreateDialogProps {
  libraryId: string
  isOpen: boolean
}

export interface CollectionCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', collection: CollectionCreation): void
}

const props = defineProps<CollectionCreateDialogProps>()
const emit = defineEmits<CollectionCreateDialogEmits>()

const { isOpen, libraryId } = toRefs(props)

const collection = reactive<CollectionCreation>({
  name: '',
  description: '',
  library: '',
})

const collectionForm = ref<InstanceType<typeof CollectionForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(collection, {
    name: '',
    description: '',
    library: libraryId.value,
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
    dialog-class="max-w-lg"
    novalidate
    :is-open="isOpen"
    :title="$t('collections.create-header')"
    :description="$t('collections.create-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <CollectionForm
        ref="collectionForm"
        v-model:name="collection.name"
        v-model:description="collection.description"
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
