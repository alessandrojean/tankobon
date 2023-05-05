<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import type { StoreEntity, StoreUpdate } from '@/types/tankobon-store'
import StoreForm from '@/components/stores/StoreForm.vue'

export interface StoreEditDialogProps {
  isOpen: boolean
  storeEntity: StoreEntity
}

export interface StoreEditDialogEmits {
  (e: 'close'): void
  (e: 'submit', store: StoreUpdate): void
}

const props = defineProps<StoreEditDialogProps>()
const emit = defineEmits<StoreEditDialogEmits>()

const { isOpen, storeEntity } = toRefs(props)

const store = reactive<StoreUpdate>({
  id: '',
  name: '',
  description: '',
})

const storeForm = ref<InstanceType<typeof StoreForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(store, {
    id: storeEntity.value.id,
    name: storeEntity.value.attributes.name,
    description: storeEntity.value.attributes.description,
  })

  storeForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await storeForm.value!.v$.$validate()

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', toRaw(store))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('stores.update-header')"
    :description="$t('stores.update-description', [storeEntity.attributes.name])"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <StoreForm
        ref="storeForm"
        v-model:name="store.name"
        v-model:description="store.description"
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
