<script lang="ts" setup>
import * as password from 'secure-random-password'
import { CheckIcon } from '@heroicons/vue/20/solid'
import type { UserCreation } from '@/types/tankobon-user'
import UserForm from '@/components/users/UserForm.vue'

export interface UserCreateDialogProps {
  isOpen: boolean
}

export interface UserCreateDialogEmits {
  (e: 'close'): void
  (e: 'submit', user: UserCreation): void
}

const props = defineProps<UserCreateDialogProps>()
const emit = defineEmits<UserCreateDialogEmits>()

const { isOpen } = toRefs(props)

const user = reactive<UserCreation>({
  name: '',
  email: '',
  password: '',
  roles: ['ROLE_USER'],
})

const userForm = ref<InstanceType<typeof UserForm> | null>(null)

function generatePassword() {
  return password.randomPassword({
    length: 12,
    characters: [
      password.lower,
      password.upper,
      password.digits,
      { characters: '#?!@$%^&*-' },
    ],
  })
}

whenever(isOpen, () => {
  Object.assign(user, {
    name: '',
    email: '',
    password: generatePassword(),
    roles: ['ROLE_USER'],
  })

  userForm.value?.v$.$reset()
  userForm.value?.v$.password.$touch()
})

async function handleSubmit() {
  const isValid = await userForm.value!.v$.$validate()

  if (!isValid) {
    return
  }

  emit('close')
  emit('submit', toRaw(user))
}

function handleRegeneratePassword() {
  user.password = generatePassword()
  userForm.value?.v$.password.$touch()
}
</script>

<template>
  <Dialog
    as="form"
    autocomplete="off"
    dialog-class="max-w-md"
    novalidate
    :is-open="isOpen"
    :title="$t('users.create-header')"
    :description="$t('users.create-description')"
    :full-height="false"
    @close="$emit('close')"
    @submit.prevent="handleSubmit"
  >
    <template #default>
      <UserForm
        ref="userForm"
        v-model:name="user.name"
        v-model:email="user.email"
        v-model:password="user.password"
        v-model:roles="user.roles"
        hide-biography
        @click:regenerate-password="handleRegeneratePassword"
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
