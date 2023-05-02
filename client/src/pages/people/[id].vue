<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { UserCircleIcon } from '@heroicons/vue/20/solid'
import type { PersonUpdate } from '@/types/tankobon-person'
import { getRelationship } from '@/utils/api'
import { getFullImageUrl } from '@/modules/api'
import type { ImageResult } from '@/components/entity/EntityImageDialog.vue'

const { t } = useI18n()
const router = useRouter()
const personId = useRouteParams<string | undefined>('id', undefined)
const notificator = useToaster()

const { mutate: deletePerson, isLoading: isDeleting, isSuccess: isDeleted } = useDeletePersonMutation()
const { mutate: editPerson, isLoading: isEditing } = useUpdatePersonMutation()
const { mutate: uploadPicture, isLoading: isUploading } = useUploadPersonPictureMutation()
const { mutate: deletePicture, isLoading: isDeletingPicture } = useDeletePersonPictureMutation()

const { data: person, isLoading } = usePersonQuery({
  personId: personId as Ref<string>,
  includes: ['library', 'person_picture'],
  enabled: computed(() => !!personId.value && !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('people.fetch-one-failure'),
      body: error.message,
    })
  },
})

const picture = computed(() => getRelationship(person.value, 'PERSON_PICTURE'))

function handleDelete() {
  deletePerson(personId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('people.deleted-with-success') })
      await router.replace({ name: 'people' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('people.deleted-with-failure'),
        body: error.message,
      })
    },
  })
}

const showEditDialog = ref(false)

function handleEditPerson(person: PersonUpdate) {
  editPerson(person, {
    onSuccess: async () => {
      await notificator.success({ title: t('people.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('people.edited-with-failure'),
        body: error.message,
      })
    },
  })
}

const showImageDialog = ref(false)

function handleImage(image: ImageResult) {
  if (image.file) {
    uploadPicture({ personId: personId.value!, picture: image.file }, {
      onSuccess: async () => {
        await notificator.success({ title: t('picture-upload.uploaded-with-success') })
      },
      onError: async (error) => {
        await notificator.failure({
          title: t('picture-upload.uploaded-with-failure'),
          body: error.message,
        })
      },
    })
  }
  else if (image.removeExisting) {
    deletePicture(personId.value!, {
      onSuccess: async () => {
        await notificator.success({ title: t('picture-upload.removed-with-success') })
      },
      onError: async (error) => {
        await notificator.failure({
          title: t('picture-upload.uploaded-with-failure'),
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
      :title="person?.attributes.name ?? ''"
      :loading="isLoading"
      class="mb-3 md:mb-0"
    >
      <template #avatar>
        <Avatar
          :loading="isLoading"
          :picture-url="
            getFullImageUrl({
              collection: 'people',
              fileName: picture?.attributes?.versions['128'],
              timeHex: picture?.attributes?.timeHex,
            })
          "
        />
      </template>
      <template #actions>
        <Toolbar class="flex space-x-2">
          <Button
            class="w-11 h-11"
            :loading="isUploading || isDeletingPicture"
            :disabled="isDeleting || isEditing"
            :title="$t('common-actions.edit-picture')"
            @click="showImageDialog = true"
          >
            <span class="sr-only">{{ $t('common-actions.edit-picture') }}</span>
            <UserCircleIcon class="w-6 h-6" />
          </Button>

          <Button
            class="w-11 h-11"
            :loading="isEditing"
            :disabled="isDeleting || isUploading || isDeletingPicture"
            :title="$t('common-actions.edit')"
            @click="showEditDialog = true"
          >
            <span class="sr-only">{{ $t('common-actions.edit') }}</span>
            <PencilIcon class="w-6 h-6" />
          </Button>

          <Button
            class="w-11 h-11"
            kind="danger"
            :disabled="isEditing || isUploading || isDeletingPicture"
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
      <BlockMarkdown
        :loading="isLoading"
        :markdown="person?.attributes?.description"
        :title="$t('common-fields.description')"
      />
    </div>

    <PersonEditDialog
      v-if="person"
      :is-open="showEditDialog"
      :person-entity="person"
      @submit="handleEditPerson"
      @close="showEditDialog = false"
    />

    <EntityImageDialog
      v-if="person"
      :title="$t('picture-upload.header')"
      :description="$t('picture-upload.description')"
      :is-open="showImageDialog"
      :current-image-url="
        getFullImageUrl({
          collection: 'people',
          fileName: picture?.attributes?.versions['128'],
          timeHex: picture?.attributes?.timeHex,
        })
      "
      @submit="handleImage"
      @close="showImageDialog = false"
    />
  </div>
</template>

<route lang="yaml">
meta:
  layout: dashboard
</route>
