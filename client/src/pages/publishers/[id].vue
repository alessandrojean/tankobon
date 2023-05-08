<script lang="ts" setup>
import { BuildingOffice2Icon, PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { PhotoIcon } from '@heroicons/vue/20/solid'
import type { PublisherUpdate } from '@/types/tankobon-publisher'
import { getFullImageUrl } from '@/modules/api'
import { getRelationship } from '@/utils/api'
import type { ImageResult } from '@/components/entity/EntityImageDialog.vue'

const { t } = useI18n()
const router = useRouter()
const publisherId = useRouteParams<string | undefined>('id', undefined)
const notificator = useToaster()

const { mutate: deletePublisher, isLoading: isDeleting, isSuccess: isDeleted } = useDeletePublisherMutation()
const { mutate: editPublisher, isLoading: isEditing } = useUpdatePublisherMutation()
const { mutate: uploadPicture, isLoading: isUploading } = useUploadPublisherPictureMutation()
const { mutate: deletePicture, isLoading: isDeletingPicture } = useDeletePublisherPictureMutation()

const { data: publisher, isLoading } = usePublisherQuery({
  publisherId: publisherId as Ref<string>,
  includes: ['library', 'publisher_picture'],
  enabled: computed(() => !!publisherId.value && !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('publishers.fetch-one-failure'),
      body: error.message,
    })
  },
})

const picture = computed(() => getRelationship(publisher.value, 'PUBLISHER_PICTURE'))

function handleDelete() {
  deletePublisher(publisherId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('publishers.deleted-with-success') })
      await router.replace({ name: 'publishers' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('publishers.deleted-with-failure'),
        body: error.message,
      })
    },
  })
}

const showEditDialog = ref(false)

function handleEditPublisher(publisher: PublisherUpdate) {
  editPublisher(publisher, {
    onSuccess: async () => {
      await notificator.success({ title: t('publishers.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('publishers.edited-with-failure'),
        body: error.message,
      })
    },
  })
}

const showImageDialog = ref(false)

function handleImage(image: ImageResult) {
  if (image.file) {
    uploadPicture({ publisherId: publisherId.value!, picture: image.file }, {
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
  } else if (image.removeExisting) {
    deletePicture(publisherId.value!, {
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
      :title="publisher?.attributes.name ?? ''"
      :loading="isLoading"
      class="mb-3 md:mb-0"
    >
      <template #avatar>
        <Avatar
          size="lg"
          square
          :empty-icon="BuildingOffice2Icon"
          :loading="isLoading"
          :picture-url="
            getFullImageUrl({
              collection: 'publishers',
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
            <PhotoIcon class="w-6 h-6" />
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
        :markdown="publisher?.attributes?.description"
        :title="$t('common-fields.description')"
      />
    </div>

    <PublisherEditDialog
      v-if="publisher"
      :is-open="showEditDialog"
      :publisher-entity="publisher"
      @submit="handleEditPublisher"
      @close="showEditDialog = false"
    />

    <EntityImageDialog
      v-if="publisher"
      :title="$t('picture-upload.header')"
      :description="$t('picture-upload.description')"
      :is-open="showImageDialog"
      :current-image-url="
        getFullImageUrl({
          collection: 'publishers',
          fileName: picture?.attributes?.versions['128'],
          timeHex: picture?.attributes?.timeHex,
        })
      "
      @submit="handleImage"
      @close="showImageDialog = false"
    >
      <template #preview="{ pictureUrl }">
        <Avatar
          size="lg"
          square
          :picture-url="pictureUrl"
          :empty-icon="BuildingOffice2Icon"
        />
      </template>
    </EntityImageDialog>
  </div>
</template>

<route lang="yaml">
meta:
  layout: dashboard
</route>
