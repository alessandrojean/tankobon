<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import LibraryForm from '@/components/libraries/LibraryForm.vue'
import type { LibraryCreation } from '@/types/tankobon-library'

export interface LibraryCreateDialogProps {
  isOpen: boolean
}

export interface UserCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', library: LibraryCreation): void
}

const props = defineProps<LibraryCreateDialogProps>()
const emit = defineEmits<UserCreateDialogEmits>()

const { isOpen } = toRefs(props)

const library = reactive<LibraryCreation>({
  name: '',
  description: '',
})

const libraryForm = ref<InstanceType<typeof LibraryForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(library, {
    name: '',
    description: '',
  })

  libraryForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await libraryForm.value!.v$.$validate()

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', toRaw(library))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-lg"
    novalidate
    :is-open="isOpen"
    :title="$t('libraries.create-header')"
    :description="$t('libraries.create-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <LibraryForm
        ref="libraryForm"
        v-model:name="library.name"
        v-model:description="library.description"
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
