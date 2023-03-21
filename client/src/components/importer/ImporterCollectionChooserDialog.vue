<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import { ExternalBookEntity } from '@/types/tankobon-external-book';
import { ImportOneBook } from '@/types/tankobon-importer-source';
import { getRelationship } from '@/utils/api';

export interface PersonCreateDialogProps {
  externalBook: ExternalBookEntity,
  isOpen: boolean,
}

export type PersonCreateDialogEmits = {
  (e: 'close'): void,
  (e: 'submit', book: ImportOneBook): void,
}

type CustomImport = Omit<ImportOneBook, 'collection'> & { collection?: string }

const props = defineProps<PersonCreateDialogProps>()
const emit = defineEmits<PersonCreateDialogEmits>()

const { isOpen, externalBook } = toRefs(props)
const source = computed(() => getRelationship(externalBook.value, 'IMPORTER_SOURCE'))

const importData = reactive<CustomImport>({
  id: '',
  isbn: '',
  collection: undefined,
  source: 'OPEN_LIBRARY',
})

whenever(isOpen, async () => {
  Object.assign(importData, <CustomImport> {
    id: externalBook.value.id,
    isbn: externalBook.value.attributes.isbn,
    collection: undefined,
    source: source.value!.id,
  })
}, { immediate: true })

async function handleSubmit() {
  const isValid = importData.collection !== undefined

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', toRaw(importData) as ImportOneBook)
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('importer.collection-chooser-header')"
    :description="$t('importer.collection-chooser-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <ImporterCollectionForm
        v-model:collection-id="importData.collection"
      />
    </template>
    <template #footer>
      <Button
        type="submit"
        class="ml-auto"
        kind="primary"
      >
        <CheckIcon class="w-5 h-5" />
        <span>{{ $t('common-actions.import') }}</span>
      </Button>
    </template>
  </Dialog>
</template>
