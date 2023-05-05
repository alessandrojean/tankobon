<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import StoreForm from '@/components/stores/StoreForm.vue'
import type { StoreCreation } from '@/types/tankobon-store'

export interface StoreCreateDialogProps {
  libraryId: string
  isOpen: boolean
}

export interface StoreCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', store: StoreCreation): void
}

const props = defineProps<StoreCreateDialogProps>()
const emit = defineEmits<StoreCreateDialogEmits>()

const { isOpen, libraryId } = toRefs(props)

const store = reactive<StoreCreation>({
  name: '',
  description: '',
  library: '',
})

const storeForm = ref<InstanceType<typeof StoreForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(store, {
    name: '',
    description: '',
    library: libraryId.value,
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
    dialog-class="max-w-lg"
    novalidate
    :is-open="isOpen"
    :title="$t('stores.create-header')"
    :description="$t('stores.create-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <StoreForm
        ref="storeForm"
        v-model:name="store.name"
        v-model:description="store.description"
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
