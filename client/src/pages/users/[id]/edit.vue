<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import { getRelationship } from '@/utils/api'
import { createImageUrl } from '@/modules/api'
import type { TankobonApiError } from '@/types/tankobon-response'
import UserMetadataForm from '@/components/users/UserMetadataForm.vue'
import type { Avatar } from '@/components/users/UserAvatarForm.vue'
import UserAvatarForm from '@/components/users/UserAvatarForm.vue'
import type { UserUpdate } from '@/types/tankobon-user'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const userId = computed(() => route.params.id as string)

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

const { mutateAsync: editUser, isLoading: isEditingUser } = useUpdateUserMutation()
const { mutateAsync: uploadAvatar, isLoading: isUploadingAvatar } = useUploadUserAvatarMutation()
const { mutateAsync: deleteAvatar, isLoading: isDeletingAvatar } = useDeleteUserAvatarMutation()

const isEditing = logicOr(isEditingUser, isUploadingAvatar, isDeletingAvatar)

const { data: user, isLoading } = useUserQuery({
  userId,
  enabled: computed(() => !!userId.value),
  refetchOnWindowFocus: false,
  refetchOnReconnect: false,
  staleTime: Infinity,
  onError: async (error) => {
    await notificator.failure({
      title: t('users.fetch-one-failure'),
      body: error.message,
    })
  },
})

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

const updatedUser = reactive<UserUpdate>({
  id: '',
  name: '',
  email: '',
  biography: '',
  roles: ['ROLE_USER'],
})

const initialUserToEdit = ref('')

whenever(user, (loadedUser) => {
  Object.assign(updatedUser, {
    id: loadedUser.id,
    name: loadedUser.attributes.name,
    email: loadedUser.attributes.email,
    biography: loadedUser.attributes.biography,
    roles: [...loadedUser.attributes.roles],
  } satisfies UserUpdate)

  initialUserToEdit.value = JSON.stringify(toRaw(updatedUser))
}, { immediate: true })

const avatar = ref<Avatar>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  if (isLoading.value || !user.value) {
    return ''
  }

  return updatedUser.name.length > 0 ? updatedUser.name : user.value.attributes.name
})

async function handleSubmit() {
  const isValidMetadata = await metadataForm.value!.v$.$validate()
  const isValidAvatar = await avatarForm.value!.v$.$validate()

  if (!isValidMetadata || !isValidAvatar) {
    return
  }

  try {
    await editUser(toRaw(updatedUser))

    if (avatar.value.file) {
      await uploadAvatar({ userId: updatedUser.id, avatar: avatar.value.file })
    } else if (avatar.value.removeExisting) {
      await deleteAvatar(updatedUser.id)
    }

    await router.replace({ name: 'users-id', params: { id: updatedUser.id } })
    await notificator.success({ title: t('users.edited-with-success') })
  } catch (error) {
    await notificator.failure({
      title: t('users.edited-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

const userWasModified = ref(false)

watch(updatedUser, (newUpdatedUser) => {
  userWasModified.value = initialUserToEdit.value !== JSON.stringify(toRaw(newUpdatedUser))
})

useBeforeUnload({ enabled: userWasModified })

const userAvatar = computed(() => getRelationship(user.value, 'AVATAR'))
</script>

<template>
  <form autocomplete="off" novalidate @submit.prevent="handleSubmit">
    <TabGroup :selected-index="Number(activeTab.key)" @change="activeTab = tabs[$event]">
      <Header
        :title="headerTitle"
        :loading="isLoading"
      >
        <template #avatar>
          <Button
            class="aspect-1 w-10 h-10 -ml-2"
            size="mini"
            kind="ghost"
            rounded="full"
            :title="$t('common-actions.back')"
            :disabled="isEditing"
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
            :disabled="isLoading || isEditing"
            :loading="isEditing"
          >
            <CheckIcon class="w-5 h-5" />
            <span>{{ $t('common-actions.save') }}</span>
          </Button>
        </template>
        <template #tabs>
          <TabList class="hidden md:flex gap-3 -mb-px">
            <Tab
              v-for="tab in tabs"
              :key="tab.key"
              v-slot="{ selected }"
              :disabled="isLoading || isEditing"
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
            :disabled="isLoading || isEditing"
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
              v-model:name="updatedUser.name"
              v-model:email="updatedUser.email"
              v-model:roles="updatedUser.roles"
              v-model:biography="updatedUser.biography"
              hide-password
              :hide-roles="!isAdmin"
              mode="update"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <UserAvatarForm
              ref="avatarForm"
              v-model:avatar="avatar"
              :current-image-url="
                createImageUrl({
                  fileName: userAvatar?.attributes?.versions?.['256'],
                  timeHex: userAvatar?.attributes?.timeHex,
                })
              "
              :disabled="isLoading || isEditing"
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
