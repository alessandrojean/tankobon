<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { PublisherUpdate } from '@/types/tankobon-publisher'
import { getRelationship } from '@/utils/api';

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const publisherId = computed(() => route.params.id?.toString())
const notificator = useNotificator()

const { data: publisher, isLoading } = usePublisherQuery({
  publisherId,
  includes: ['library'],
  enabled: computed(() => route.params.id !== undefined),
  onError: async (error) => {
    await notificator.failure({
      title: t('publishers.fetch-one-failure'),
      body: error.message,
    })
  }
})
const { mutate: deletePublisher, isLoading: isDeleting } = useDeletePublisherMutation()
const { mutate: editPublisher, isLoading: isEditing } = useUpdatePublisherMutation()

const library = computed(() => getRelationship(publisher.value, 'LIBRARY'))

function handleDelete() {
  deletePublisher(publisherId.value, {
    onSuccess: async () => {
      notificator.success({ title: t('publishers.deleted-with-success') })
      await router.replace({ name: 'publishers' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('publishers.deleted-with-failure'),
        body: error.message,
      })
    }
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
    }
  })
}
</script>

<template>
  <div>
    <Header
      :title="publisher?.attributes.name ?? ''"
      :subtitle="publisher?.attributes.description"
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

    <PublisherEditDialog
      v-if="publisher"
      :is-open="showEditDialog"
      :publisher-entity="publisher"
      @submit="handleEditPublisher"
      @close="showEditDialog = false"
    />
  </div>
</template>

<route lang="yaml">
  meta:
    layout: dashboard
    isAdminOnly: true
</route>
