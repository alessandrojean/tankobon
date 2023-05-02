<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import type { CollectionUpdate } from '@/types/tankobon-collection'

const { t } = useI18n()
const router = useRouter()
const collectionId = useRouteParams<string | undefined>('id', undefined)
const notificator = useToaster()

const { mutate: deleteCollection, isLoading: isDeleting, isSuccess: isDeleted } = useDeleteCollectionMutation()
const { mutate: editCollection, isLoading: isEditing } = useUpdateCollectionMutation()

const { data: collection, isLoading } = useCollectionQuery({
  collectionId: collectionId as Ref<string>,
  includes: ['library'],
  enabled: computed(() => !!collectionId.value && !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('collections.fetch-one-failure'),
      body: error.message,
    })
  },
})

function handleDelete() {
  deleteCollection(collectionId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('collections.deleted-with-success') })
      await router.replace({ name: 'collections' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('collections.deleted-with-failure'),
        body: error.message,
      })
    },
  })
}

const showEditDialog = ref(false)

function handleEditCollection(collection: CollectionUpdate) {
  editCollection(collection, {
    onSuccess: async () => {
      await notificator.success({ title: t('collections.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('collections.edited-with-failure'),
        body: error.message,
      })
    },
  })
}
</script>

<template>
  <div>
    <Header
      :title="collection?.attributes.name ?? ''"
      :loading="isLoading"
      class="mb-3 md:mb-0"
    >
      <template #actions>
        <Toolbar class="flex space-x-2">
          <Button
            class="w-11 h-11"
            :loading="isEditing"
            :disabled="isDeleting"
            :title="$t('common-actions.edit')"
            @click="showEditDialog = true"
          >
            <span class="sr-only">{{ $t('common-actions.edit') }}</span>
            <PencilIcon class="w-6 h-6" />
          </Button>

          <Button
            class="w-11 h-11"
            kind="danger"
            :disabled="isEditing"
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
        :markdown="collection?.attributes?.description"
        :title="$t('common-fields.description')"
      />
    </div>

    <CollectionEditDialog
      v-if="collection"
      :is-open="showEditDialog"
      :collection-entity="collection"
      @submit="handleEditCollection"
      @close="showEditDialog = false"
    />
  </div>
</template>

<route lang="yaml">
meta:
  layout: dashboard
</route>
