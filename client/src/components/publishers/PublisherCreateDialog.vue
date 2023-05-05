<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import PublisherForm from '@/components/publishers/PublisherForm.vue'
import type { PublisherCreation } from '@/types/tankobon-publisher'

export interface PublisherCreateDialogProps {
  libraryId: string
  isOpen: boolean
}

export interface PublisherCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', publisher: PublisherCreation): void
}

const props = defineProps<PublisherCreateDialogProps>()
const emit = defineEmits<PublisherCreateDialogEmits>()

const { isOpen, libraryId } = toRefs(props)

const publisher = reactive<PublisherCreation>({
  name: '',
  description: '',
  library: '',
})

const publisherForm = ref<InstanceType<typeof PublisherForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(publisher, {
    name: '',
    description: '',
    library: libraryId.value,
  })

  publisherForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await publisherForm.value!.v$.$validate()

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', toRaw(publisher))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-lg"
    novalidate
    :is-open="isOpen"
    :title="$t('publishers.create-header')"
    :description="$t('publishers.create-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <PublisherForm
        ref="publisherForm"
        v-model:name="publisher.name"
        v-model:description="publisher.description"
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
