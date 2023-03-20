<script lang="ts" setup>
import type { PersonEntity, PersonUpdate } from '@/types/tankobon-person'
import { CheckIcon } from '@heroicons/vue/20/solid'
import PersonForm from '@/components/people/PersonForm.vue'

export interface PersonEditDialogProps {
  isOpen: boolean,
  personEntity: PersonEntity,
}

export type PersonEditDialogEmits = {
  (e: 'close'): void,
  (e: 'submit', person: PersonUpdate): void,
}

const props = defineProps<PersonEditDialogProps>()
const emit = defineEmits<PersonEditDialogEmits>()

const { isOpen, personEntity } = toRefs(props)

const person = reactive<PersonUpdate>({
  id: '',
  name: '',
  description: '',
})

const personForm = ref<InstanceType<typeof PersonForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(person, {
    id: personEntity.value.id,
    name: personEntity.value.attributes.name,
    description: personEntity.value.attributes.description,
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
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('people.update-header')"
    :description="$t('people.update-description', [personEntity.attributes.name])"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <PersonForm
        ref="personForm"
        v-model:name="person.name"
        v-model:description="person.description"
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