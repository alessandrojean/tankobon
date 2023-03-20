<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { StoreUpdate } from '@/types/tankobon-store'
import { getRelationship } from '@/utils/api';

const { t } = useI18n()
const router = useRouter()
const storeId = useRouteParams<string | undefined>('id', undefined)
const notificator = useNotificator()

const { mutate: deleteStore, isLoading: isDeleting, isSuccess: isDeleted } = useDeleteStoreMutation()
const { mutate: editStore, isLoading: isEditing } = useUpdateStoreMutation()

const { data: store, isLoading } = useStoreQuery({
  storeId: storeId as Ref<string>,
  includes: ['library'],
  enabled: computed(() => !!storeId.value && !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('stores.fetch-one-failure'),
      body: error.message,
    })
  }
})

const library = computed(() => getRelationship(store.value, 'LIBRARY'))

function handleDelete() {
  deleteStore(storeId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('stores.deleted-with-success') })
      await router.replace({ name: 'stores' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('stores.deleted-with-failure'),
        body: error.message,
      })
    }
  })
}

const showEditDialog = ref(false)

function handleEditStore(store: StoreUpdate) {
  editStore(store, {
    onSuccess: async () => {
      await notificator.success({ title: t('stores.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('stores.edited-with-failure'),
        body: error.message,
      })
    }
  })
}
</script>

<template>
  <div>
    <Header
      :title="store?.attributes.name ?? ''"
      :subtitle="store?.attributes.description"
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

    <StoreEditDialog
      v-if="store"
      :is-open="showEditDialog"
      :store-entity="store"
      @submit="handleEditStore"
      @close="showEditDialog = false"
    />
  </div>
</template>

<route lang="yaml">
  meta:
    layout: dashboard
    isAdminOnly: true
</route>
