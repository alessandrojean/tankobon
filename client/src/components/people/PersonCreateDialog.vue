<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import PersonForm from '@/components/people/PersonForm.vue'
import type { PersonCreation } from '@/types/tankobon-person'

export interface PersonCreateDialogProps {
  libraryId: string
  isOpen: boolean
}

export interface PersonCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', person: PersonCreation): void
}

const props = defineProps<PersonCreateDialogProps>()
const emit = defineEmits<PersonCreateDialogEmits>()

const { isOpen, libraryId } = toRefs(props)

const person = reactive<PersonCreation>({
  name: '',
  description: '',
  library: '',
})

const personForm = ref<InstanceType<typeof PersonForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(person, {
    name: '',
    description: '',
    library: libraryId.value,
  })

  personForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await personForm.value!.v$.$validate()

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', toRaw(person))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-lg"
    novalidate
    :is-open="isOpen"
    :title="$t('people.create-header')"
    :description="$t('people.create-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <PersonForm
        ref="personForm"
        v-model:name="person.name"
        v-model:description="person.description"
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
