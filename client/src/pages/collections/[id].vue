<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { CollectionUpdate } from '@/types/tankobon-collection'
import { getRelationship } from '@/utils/api';

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const collectionId = computed(() => route.params.id?.toString())
const notificator = useNotificator()

const { data: collection, isLoading } = useCollectionQuery({
  collectionId,
  includes: ['library'],
  enabled: computed(() => route.params.id !== undefined),
  onError: async (error) => {
    await notificator.failure({
      title: t('collections.fetch-one-failure'),
      body: error.message,
    })
  }
})
const { mutate: deleteCollection, isLoading: isDeleting } = useDeleteCollectionMutation()
const { mutate: editCollection, isLoading: isEditing } = useUpdateCollectionMutation()

const library = computed(() => getRelationship(collection.value, 'LIBRARY'))

function handleDelete() {
  deleteCollection(collectionId.value, {
    onSuccess: async () => {
      notificator.success({ title: t('collections.deleted-with-success') })
      await router.replace({ name: 'collections' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('collections.deleted-with-failure'),
        body: error.message,
      })
    }
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
    }
  })
}
</script>

<template>
  <div>
    <Header
      :title="collection?.attributes.name ?? ''"
      :subtitle="collection?.attributes.description"
      :loading="isLoading"
      class="mb-3 md:mb-0"
    >
      <template #title-badge v-if="library && library.attributes">
        <Badge class="ml-2">{{ library?.attributes?.name }}</Badge>
      </template>
      <template #actions>
        <div class="flex space-x-2">
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
        </div>
      </template>
    </Header>
    <div class="max-w-7xl mx-auto p-4 sm:p-6 space-y-10">
      
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
    isAdminOnly: true
</route>
