<script lang="ts" setup>
import { EnvelopeIcon } from '@heroicons/vue/20/solid'
import { PencilIcon, PhotoIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { BuildingLibraryIcon } from '@heroicons/vue/24/outline'
import { getFullImageUrl } from '@/modules/api'
import type { UserUpdate } from '@/types/tankobon-user'
import type { ImageResult } from '@/components/entity/EntityImageDialog.vue'
import { getRelationship } from '@/utils/api'

const { t } = useI18n()
const router = useRouter()
const userIdRoute = useRouteParams<string | undefined>('id', undefined)
const userStore = useUserStore()
const notificator = useToaster()

const userId = computed(() => {
  return userIdRoute.value === 'me' ? userStore.me?.id : userIdRoute.value
})

const {
  mutate: deleteUser,
  isLoading: isDeleting,
  isSuccess: isDeleted,
} = useDeleteUserMutation()
const { mutate: editUser, isLoading: isEditing } = useUpdateUserMutation()
const { mutate: uploadAvatar, isLoading: isUploading } = useUploadUserAvatarMutation()
const { mutate: deleteAvatar, isLoading: isDeletingAvatar } = useDeleteUserAvatarMutation()

const { data: user, isLoading } = useUserQuery({
  userId: userId as Ref<string>,
  enabled: computed(() => !!userId.value && !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('users.fetch-one-failure'),
      body: error.message,
    })
  },
})

const avatar = computed(() => getRelationship(user.value, 'AVATAR'))
const isAdmin = computed(() => user.value?.attributes.roles.includes('ROLE_ADMIN'))
const isMe = computed(() => user.value?.id === userStore.me!.id)

function handleDelete() {
  deleteUser(userId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('users.deleted-with-success') })
      await router.replace({ name: 'users' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('users.deleted-with-failure'),
        body: error.message,
      })
    },
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
    },
  })
}

const showAvatarDialog = ref(false)

function handleAvatar(avatar: ImageResult) {
  if (avatar.file) {
    uploadAvatar({ userId: userId.value!, avatar: avatar.file }, {
      onSuccess: async () => {
        await notificator.success({ title: t('users.avatar-uploaded-with-success') })
      },
      onError: async (error) => {
        await notificator.failure({
          title: t('users.avatar-uploaded-with-failure'),
          body: error.message,
        })
      },
    })
  } else if (avatar.removeExisting) {
    deleteAvatar(userId.value!, {
      onSuccess: async () => {
        await notificator.success({ title: t('users.avatar-removed-with-success') })
      },
      onError: async (error) => {
        await notificator.failure({
          title: t('users.avatar-removed-with-failure'),
          body: error.message,
        })
      },
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
          :loading="isLoading"
          :picture-url="
            getFullImageUrl({
              collection: 'avatars',
              fileName: avatar?.attributes?.versions['128'],
              timeHex: avatar?.attributes?.timeHex,
            })
          "
        />
      </template>
      <template #title-badge>
        <Badge v-if="isMe" class="ml-2">
          {{ $t('user.you') }}
        </Badge>
      </template>
      <template #subtitle>
        <div v-if="isLoading" class="skeleton w-24 h-5 mt-1.5" />
        <ul v-else class="flex items-center space-x-2 mt-0.5">
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
        <Toolbar class="flex space-x-2">
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
        </Toolbar>
      </template>
    </Header>
    <div class="max-w-7xl mx-auto p-4 sm:p-6 space-y-10">
      <div v-if="userId">
        <h2 class="font-display-safe font-medium text-xl">
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

      <div v-if="userId">
        <h2 class="font-display-safe font-medium text-xl">
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

    <EntityImageDialog
      v-if="user"
      :title="$t('users.edit-avatar-header')"
      :description="$t('users.edit-avatar-description')"
      :is-open="showAvatarDialog"
      :current-image-url="
        getFullImageUrl({
          collection: 'avatars',
          fileName: avatar?.attributes?.versions['128'],
          timeHex: avatar?.attributes?.timeHex,
        })
      "
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
