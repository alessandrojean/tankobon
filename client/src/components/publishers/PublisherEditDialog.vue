<script lang="ts" setup>
import type { PublisherEntity, PublisherUpdate } from '@/types/tankobon-publisher'
import { CheckIcon } from '@heroicons/vue/20/solid'
import PublisherForm from '@/components/publishers/PublisherForm.vue'

export interface PublisherEditDialogProps {
  isOpen: boolean,
  publisherEntity: PublisherEntity,
}

export type PublisherEditDialogEmits = {
  (e: 'close'): void,
  (e: 'submit', publisher: PublisherUpdate): void,
}

const props = defineProps<PublisherEditDialogProps>()
const emit = defineEmits<PublisherEditDialogEmits>()

const { isOpen, publisherEntity } = toRefs(props)

const publisher = reactive<PublisherUpdate>({
  id: '',
  name: '',
  description: '',
})

const publisherForm = ref<InstanceType<typeof PublisherForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(publisher, {
    id: publisherEntity.value.id,
    name: publisherEntity.value.attributes.name,
    description: publisherEntity.value.attributes.description,
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
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('publishers.update-header')"
    :description="$t('publishers.update-description', [publisherEntity.attributes.name])"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <PublisherForm
        ref="publisherForm"
        v-model:name="publisher.name"
        v-model:description="publisher.description"
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