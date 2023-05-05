<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import type { ContributorRoleEntity, ContributorRoleUpdate } from '@/types/tankobon-contributor-role'
import ContributorRoleForm from '@/components/contributor-roles/ContributorRoleForm.vue'

export interface ContributorRoleEditDialogProps {
  isOpen: boolean
  contributorRoleEntity: ContributorRoleEntity
}

export interface ContributorRoleEditDialogEmits {
  (e: 'close'): void
  (e: 'submit', contributorRole: ContributorRoleUpdate): void
}

const props = defineProps<ContributorRoleEditDialogProps>()
const emit = defineEmits<ContributorRoleEditDialogEmits>()

const { isOpen, contributorRoleEntity } = toRefs(props)

const contributorRole = reactive<ContributorRoleUpdate>({
  id: '',
  name: '',
  description: '',
})

const contributorRoleForm = ref<InstanceType<typeof ContributorRoleForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(contributorRole, {
    id: contributorRoleEntity.value.id,
    name: contributorRoleEntity.value.attributes.name,
    description: contributorRoleEntity.value.attributes.description,
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
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('contributor-roles.update-header')"
    :description="$t('contributor-roles.update-description', [contributorRoleEntity.attributes.name])"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <ContributorRoleForm
        ref="contributorRoleForm"
        v-model:name="contributorRole.name"
        v-model:description="contributorRole.description"
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
