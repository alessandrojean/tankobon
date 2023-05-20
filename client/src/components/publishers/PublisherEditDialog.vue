<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import type { PublisherEntity, PublisherUpdate } from '@/types/tankobon-publisher'
import PublisherForm from '@/components/publishers/PublisherForm.vue'

export interface PublisherEditDialogProps {
  isOpen: boolean
  publisherEntity: PublisherEntity
}

export interface PublisherEditDialogEmits {
  (e: 'close'): void
  (e: 'submit', publisher: PublisherUpdate): void
}

const props = defineProps<PublisherEditDialogProps>()
const emit = defineEmits<PublisherEditDialogEmits>()

const { isOpen, publisherEntity } = toRefs(props)

const publisher = reactive<PublisherUpdate>({
  id: '',
  name: '',
  description: '',
  links: {
    website: null,
    store: null,
    twitter: null,
    instagram: null,
    facebook: null,
    youTube: null,
  },
})

const publisherForm = ref<InstanceType<typeof PublisherForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(publisher, {
    id: publisherEntity.value.id,
    name: publisherEntity.value.attributes.name,
    description: publisherEntity.value.attributes.description,
    links: {
      website: publisherEntity.value.attributes.links.website,
      store: publisherEntity.value.attributes.links.store,
      twitter: publisherEntity.value.attributes.links.twitter,
      instagram: publisherEntity.value.attributes.links.instagram,
      facebook: publisherEntity.value.attributes.links.facebook,
      youTube: publisherEntity.value.attributes.links.youTube,
    },
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
