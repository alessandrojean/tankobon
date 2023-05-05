<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import ContributorRoleForm from '@/components/contributor-roles/ContributorRoleForm.vue'
import type { ContributorRoleCreation } from '@/types/tankobon-contributor-role'

export interface ContributorRoleCreateDialogProps {
  libraryId: string
  isOpen: boolean
}

export interface ContributorRoleCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', contributorRole: ContributorRoleCreation): void
}

const props = defineProps<ContributorRoleCreateDialogProps>()
const emit = defineEmits<ContributorRoleCreateDialogEmits>()

const { isOpen, libraryId } = toRefs(props)

const contributorRole = reactive<ContributorRoleCreation>({
  name: '',
  description: '',
  library: '',
})

const contributorRoleForm = ref<InstanceType<typeof ContributorRoleForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(contributorRole, {
    name: '',
    description: '',
    library: libraryId.value,
  })

  contributorRoleForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await contributorRoleForm.value!.v$.$validate()

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', toRaw(contributorRole))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-lg"
    novalidate
    :is-open="isOpen"
    :title="$t('contributor-roles.create-header')"
    :description="$t('contributor-roles.create-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <ContributorRoleForm
        ref="contributorRoleForm"
        v-model:name="contributorRole.name"
        v-model:description="contributorRole.description"
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
