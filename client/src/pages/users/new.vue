<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import * as password from 'secure-random-password'
import type { TankobonApiError } from '@/types/tankobon-response'
import UserMetadataForm from '@/components/users/UserMetadataForm.vue'
import type { Avatar } from '@/components/users/UserAvatarForm.vue'
import UserAvatarForm from '@/components/users/UserAvatarForm.vue'
import type { UserCreation } from '@/types/tankobon-user'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

const { mutateAsync: createUser, isLoading: isCreatingUser } = useCreateUserMutation()
const { mutateAsync: uploadAvatar, isLoading: isUploadingAvatar } = useUploadUserAvatarMutation()

const isCreating = logicOr(isCreatingUser, isUploadingAvatar)

const metadataForm = ref<InstanceType<typeof UserMetadataForm>>()
const avatarForm = ref<InstanceType<typeof UserAvatarForm>>()

const metadataInvalid = computed(() => metadataForm.value?.v$.$error ?? false)
const avatarInvalid = computed(() => avatarForm.value?.v$.$error ?? false)

const tabs = [
  { key: '0', text: 'users.metadata' },
  { key: '1', text: 'users.avatar' },
]

const invalidTabs = computed(() => [
  metadataInvalid.value,
  avatarInvalid.value,
])

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

const newUser = reactive<UserCreation>({
  name: '',
  email: '',
  biography: '',
  password: generatePassword(),
  roles: ['ROLE_USER'],
})

const avatar = ref<Avatar>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  return newUser.name.length > 0 ? newUser.name : t('users.new')
})

async function handleSubmit() {
  const isValidMetadata = await metadataForm.value!.v$.$validate()
  const isValidAvatar = await avatarForm.value!.v$.$validate()

  if (!isValidMetadata || !isValidAvatar) {
    return
  }

  try {
    const { id } = await createUser(toRaw(newUser))

    if (avatar.value.file) {
      await uploadAvatar({ userId: id, avatar: avatar.value.file })
    }

    await router.replace({ name: 'users-id', params: { id } })
    await notificator.success({ title: t('users.created-with-success') })
  } catch (error) {
    await notificator.failure({
      title: t('users.created-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

useBeforeUnload({
  enabled: computed(() => route.name === 'users-new'),
})

function handleRegeneratePassword() {
  newUser.password = generatePassword()
  metadataForm.value?.v$.password.$touch()
}
</script>

<template>
  <form autocomplete="off" novalidate @submit.prevent="handleSubmit">
    <TabGroup :selected-index="Number(activeTab.key)" @change="activeTab = tabs[$event]">
      <Header :title="headerTitle">
        <template #avatar>
          <Button
            class="aspect-1 w-10 h-10 -ml-2"
            size="mini"
            kind="ghost"
            rounded="full"
            :title="$t('common-actions.back')"
            :disabled="isCreating"
            @click="router.back"
          >
            <span class="sr-only">
              {{ $t('common-actions.back') }}
            </span>
            <ArrowLeftIcon class="w-5 h-5" />
          </Button>
        </template>
        <template #actions>
          <Button
            kind="primary"
            type="submit"
            :disabled="isCreating"
            :loading="isCreating"
          >
            <CheckIcon class="w-5 h-5" />
            <span>{{ $t('common-actions.create') }}</span>
          </Button>
        </template>
        <template #tabs>
          <TabList class="hidden md:flex gap-3 -mb-px">
            <Tab
              v-for="tab in tabs"
              :key="tab.key"
              v-slot="{ selected }"
              :disabled="isCreating"
              as="template"
            >
              <Button
                kind="underline-tab"
                size="underline-tab"
                rounded="none"
                :data-headlessui-state="selected ? 'selected' : undefined"
              >
                <span>{{ $t(tab.text) }}</span>
                <div
                  v-if="invalidTabs[Number(tab.key)]"
                  class="ml-2 mt-0.5 relative"
                >
                  <span class="absolute inset-1 rounded-full bg-white" />
                  <ExclamationCircleIcon
                    class="relative w-4 h-4 text-red-600 dark:text-red-500"
                  />
                </div>
              </Button>
            </Tab>
          </TabList>
          <BasicSelect
            id="tabs"
            v-model="activeTab"
            class="md:hidden mb-4"
            :disabled="isCreating"
            :options="tabs"
            :option-text="tab => $t(tab.text)"
            :option-value="tab => tab.key"
          />
        </template>
      </Header>
      <div class="max-w-7xl mx-auto p-4 sm:p-6">
        <TabPanels>
          <TabPanel :unmount="false">
            <UserMetadataForm
              ref="metadataForm"
              v-model:name="newUser.name"
              v-model:email="newUser.email"
              v-model:roles="newUser.roles"
              v-model:biography="newUser.biography"
              v-model:password="newUser.password"
              :hide-roles="!isAdmin"
              mode="creation"
              :disabled="isCreating"
              @click:regenerate-password="handleRegeneratePassword"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <UserAvatarForm
              ref="avatarForm"
              v-model:avatar="avatar"
              :disabled="isCreating"
            />
          </TabPanel>
        </TabPanels>
      </div>
    </TabGroup>
  </form>
</template>

<route lang="yaml">
meta:
  layout: dashboard
</route>
