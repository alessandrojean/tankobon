<script lang="ts" setup>
import type { UserCreation } from '@/types/tankobon-user'
import { PlusIcon } from '@heroicons/vue/20/solid'

const { t } = useI18n()
const router = useRouter()
const showCreateDialog = ref(false)
const { mutate } = useCreateUserMutation()
const notificator = useToaster()

function handleCreateUser(user: UserCreation) {
  mutate(user, {
    onSuccess: async ({ id }) => {
      notificator.success({ title: t('users.created-with-success') })
      await router.push({ name: 'users-id', params: { id } })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('users.created-with-failure'),
        body: error.message,
      })
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
