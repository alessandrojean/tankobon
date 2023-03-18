<script lang="ts" setup>
import { getFullImageUrl } from '@/modules/api'
import { UserUpdate } from '@/types/tankobon-user';
import { EnvelopeIcon } from '@heroicons/vue/20/solid'
import { PencilIcon, PhotoIcon, TrashIcon } from '@heroicons/vue/24/solid'
import type { AvatarResult } from '@/components/users/UserAvatarDialog.vue'
import { BuildingLibraryIcon } from '@heroicons/vue/24/outline';

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userId = computed(() => route.params.id?.toString())
const userStore = useUserStore()
const notificator = useNotificator()

const { data: user, isLoading } = useUserQuery({
  userId,
  enabled: computed(() => route.params.id !== undefined),
  onError: async (error) => {
    await notificator.failure({
      title: t('users.fetch-one-failure'),
      body: error.message,
    })
  }
})
const { mutate: deleteUser, isLoading: isDeleting } = useDeleteUserMutation()
const { mutate: editUser, isLoading: isEditing } = useUpdateUserMutation()
const { mutate: uploadAvatar, isLoading: isUploading } = useUploadUserAvatarMutation()
const { mutate: deleteAvatar, isLoading: isDeletingAvatar } = useDeleteUserAvatarMutation()

const avatar = computed(() => user.value?.relationships?.find(r => r.type === 'AVATAR'))
const isAdmin = computed(() => user.value?.attributes.roles.includes('ROLE_ADMIN'))
const isMe = computed(() => user.value?.id === userStore.me!.id)

function handleDelete() {
  deleteUser(userId.value, {
    onSuccess: async () => {
      notificator.success({ title: t('users.deleted-with-success') })
      await router.replace({ name: 'users' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('users.deleted-with-failure'),
        body: error.message,
      })
    }
  })
}

const showEditDialog = ref(false)

function handleEditUser(user: UserUpdate) {
  editUser(user, {
    onSuccess: async () => {
      await notificator.success({ title: t('users.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('users.edited-with-failure'),
        body: error.message,
      })
    }
  })
}

const showAvatarDialog = ref(false)

function handleAvatar(avatar: AvatarResult) {
  if (avatar.file) {
    uploadAvatar({ userId: userId.value, avatar: avatar.file }, {
      onSuccess: async () => {
        await notificator.success({ title: t('users.avatar-uploaded-with-success') })
      },
      onError: async (error) => {
        await notificator.failure({
          title: t('users.avatar-uploaded-with-failure'),
          body: error.message,
        })
      }
    })
  } else if (avatar.removeExisting) {
    deleteAvatar(userId.value, {
      onSuccess: async () => {
        await notificator.success({ title: t('users.avatar-removed-with-success') })
      },
      onError: async (error) => {
        await notificator.failure({
          title: t('users.avatar-removed-with-failure'),
          body: error.message,
        })
      }
    })
  }
}
</script>

<template>
  <div>
    <Header
      :title="user?.attributes.name ?? ''"
      :loading="isLoading"
      class="mb-3 md:mb-0"
    >
      <template #avatar>
        <Avatar
          :picture-url="
            getFullImageUrl({
              collection: 'avatars',
              fileName: avatar?.attributes.versions['128'],
              timeHex: avatar?.attributes.timeHex,
            })
          "
        />
      </template>
      <template #title-badge>
        <Badge v-if="isMe" class="ml-2">{{ $t('user.you') }}</Badge>
      </template>
      <template #subtitle>
        <ul class="flex items-center space-x-2 mt-0.5">
          <li v-if="isAdmin">
            <Badge color="blue">
              {{ $t('user.role-admin') }}
            </Badge>
          </li>
          <li class="text-sm text-gray-800">
            <EnvelopeIcon class="w-4 h-4 text-gray-400 inline-block" />
            <a
              class="ml-1.5 dark:text-gray-300 hover:underline hover:text-primary-600 dark:hover:text-gray-100"
              :href="`mailto:${user?.attributes.email}`"
            >
              {{ user?.attributes.email }}
            </a>
          </li>
        </ul>
      </template>
      <template #actions>
        <div class="flex space-x-2">
          <Button
            class="w-11 h-11"
            :loading="isUploading || isDeletingAvatar"
            :disabled="isDeleting || isEditing"
            :title="$t('users.edit-avatar')"
            @click="showAvatarDialog = true"
          >
            <span class="sr-only">{{ $t('users.edit-avatar') }}</span>
            <PhotoIcon class="w-6 h-6" />
          </Button>

          <Button
            class="w-11 h-11"
            :loading="isEditing"
            :disabled="isDeleting || isUploading || isDeletingAvatar"
            :title="$t('common-actions.edit')"
            @click="showEditDialog = true"
          >
            <span class="sr-only">{{ $t('common-actions.edit') }}</span>
            <PencilIcon class="w-6 h-6" />
          </Button>

          <Button
            class="w-11 h-11"
            kind="danger"
            :disabled="isMe || isEditing || isUploading || isDeletingAvatar"
            :loading="isDeleting"
            :title="$t('common-actions.delete')"
            @click="handleDelete"
          >
            <span class="sr-only">{{ $t('common-actions.delete') }}</span>
            <TrashIcon class="w-6 h-6" />
          </Button>
        </div>
      </template>
    </Header>
    <div class="max-w-7xl mx-auto p-4 sm:p-6 space-y-10">
      <div>
        <h2 class="font-display font-medium text-xl">
          {{ $t('entities.libraries') }}
        </h2>
        <LibrariesTable
          class="mt-4"
          :user-id="userId"
        >
          <template #empty>
            <EmptyState
              :icon="BuildingLibraryIcon"
              :title="$t('libraries.empty-header')"
              :description="$t('libraries.empty-description')"
            />
          </template>
        </LibrariesTable>
      </div>

      <div>
        <h2 class="font-display font-medium text-xl">
          {{ $t('authentication-activity.header') }}
        </h2>
        <UserAuthenticationActivityTable class="mt-4" :user-id="userId" />
      </div>
    </div>

    <UserEditDialog
      v-if="user"
      :is-open="showEditDialog"
      :user-entity="user"
      :is-me="isMe"
      @submit="handleEditUser"
      @close="showEditDialog = false"
    />

    <UserAvatarDialog
      v-if="user"
      :is-open="showAvatarDialog"
      :user-entity="user"
      @submit="handleAvatar"
      @close="showAvatarDialog = false"
    />
  </div>
</template>

<route lang="yaml">
  meta:
    layout: dashboard
    isAdminOnly: true
</route>
