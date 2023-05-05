<script lang="ts" setup>
import { CheckIcon } from '@heroicons/vue/20/solid'
import type { UserEntity, UserUpdate } from '@/types/tankobon-user'
import UserForm from '@/components/users/UserForm.vue'

export interface UserCreateDialogProps {
  isMe?: boolean
  isOpen: boolean
  userEntity: UserEntity
}

export interface UserCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', user: UserUpdate): void
}

const props = withDefaults(defineProps<UserCreateDialogProps>(), {
  isMe: false,
})
const emit = defineEmits<UserCreateDialogEmits>()

const { isOpen, userEntity } = toRefs(props)

const user = reactive<UserUpdate>({
  id: '',
  name: '',
  email: '',
  biography: '',
  roles: ['ROLE_USER'],
})

const userForm = ref<InstanceType<typeof UserForm> | null>(null)

whenever(isOpen, () => {
  Object.assign(user, {
    id: userEntity.value.id,
    name: userEntity.value.attributes.name,
    email: userEntity.value.attributes.email,
    biography: userEntity.value.attributes.biography,
    roles: [...userEntity.value.attributes.roles],
  })

  userForm.value?.v$.$reset()
})

async function handleSubmit() {
  const isValid = await userForm.value!.v$.$validate()

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', toRaw(user))
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="isMe ? $t('users.update-me-header') : $t('users.update-header')"
    :description="isMe ? $t('users.update-me-description') : $t('users.update-description', [userEntity.attributes.name])"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <UserForm
        ref="userForm"
        v-model:name="user.name"
        v-model:email="user.email"
        v-model:biography="user.biography"
        v-model:roles="user.roles"
        hide-password
        :hide-roles="isMe"
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
