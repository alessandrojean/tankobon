<script lang="ts" setup>
import useCreateUserMutation from '@/mutations/useCreateUserMutation';
import type { UserCreation } from '@/types/tankobon-user'
import { PlusIcon } from '@heroicons/vue/20/solid'

const router = useRouter()
const showCreateDialog = ref(false)
const { mutate } = useCreateUserMutation()

function handleCreateUser(user: UserCreation) {
  mutate(user, {
    onSuccess: async ({ id }) => {
      await router.push({ name: 'users-id', params: { id } })
    }
  })
}
</script>

<template>
  <div>
    <Header
      :title="$t('users.header')"
      class="mb-3 md:mb-0"
    >
      <template #actions>
        <Button
          kind="primary"
          @click="showCreateDialog = true"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('users.new') }}</span>
        </Button>
      </template>
    </Header>
    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <UsersTable />
    </div>

    <UserCreateDialog
      :is-open="showCreateDialog"
      @submit="handleCreateUser"
      @close="showCreateDialog = false"
    />
  </div>
</template>

<route lang="yaml">
  meta:
    layout: dashboard
    isAdminOnly: true
</route>
