<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import type { LibraryEntity, LibraryUpdate } from '@/types/tankobon-library'
import LibraryForm from '@/components/libraries/LibraryForm.vue'

export interface LibraryEditDialogProps {
  isOpen: boolean
  libraryEntity: LibraryEntity
}

export interface LibraryEditDialogEmits {
  (e: 'close'): void
  (e: 'submit', library: LibraryUpdate): void
}

const props = defineProps<LibraryEditDialogProps>()
const emit = defineEmits<LibraryEditDialogEmits>()

const { isOpen, libraryEntity } = toRefs(props)

const library = reactive<LibraryUpdate>({
  id: '',
  name: '',
  description: '',
  sharedUsers: [],
})

const libraryForm = ref<InstanceType<typeof LibraryForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(library, {
    id: libraryEntity.value.id,
    name: libraryEntity.value.attributes.name,
    description: libraryEntity.value.attributes.description,
    sharedUsers: libraryEntity.value.relationships
      ?.filter(r => r.type === 'LIBRARY_SHARING')
      ?.map(r => r.id),
  })

  libraryForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await libraryForm.value!.v$.$validate()

  if (!isValid)
    return

  emit('close')
  emit('submit', toRaw(library))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('libraries.update-header')"
    :description="$t('libraries.update-description', [libraryEntity.attributes.name])"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <LibraryForm
        ref="libraryForm"
        v-model:name="library.name"
        v-model:description="library.description"
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
