<script lang="ts" setup>
import type { CollectionEntity } from '@/types/tankobon-collection'
import type { LibraryEntity } from '@/types/tankobon-library'
import { createEmptyPaginatedResponse } from '@/utils/api'

export interface ImporterCollectionFormProps {
  collectionId?: string
}

export interface PersonFormEmits {
  (e: 'update:collectionId', collectionId: string): void
}

const props = withDefaults(defineProps<ImporterCollectionFormProps>(), {
  collectionId: undefined,
})
const emit = defineEmits<PersonFormEmits>()

const { collectionId } = toRefs(props)

const { t } = useI18n()
const notificator = useToaster()
const userStore = useUserStore()
const userId = computed(() => userStore.me?.id)
const library = ref<LibraryEntity | undefined>()

const { data: libraries, isLoading: loadingLibraries } = useUserLibrariesByUserQuery({
  enabled: computed(() => userStore.isAuthenticated),
  userId: userId as ComputedRef<string>,
  unpaged: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-failure'),
      body: error.message,
    })
  },
  initialData: createEmptyPaginatedResponse(),
  select: response => response.data,
})

const { data: collections, isLoading: loadingCollections } = useLibraryCollectionsUnpagedQuery({
  libraryId: computed(() => library.value?.id ?? ''),
  enabled: computed(() => library.value !== undefined),
  onError: async (error) => {
    await notificator.failure({
      title: t('collections.fetch-failure'),
      body: error.message,
    })
  },
})

const collection = computed({
  get: () => collections.value?.find(c => c.id === collectionId.value),
  set: (collection) => {
    if (collection) {
      emit('update:collectionId', collection.id)
    }
  },
})
</script>

<template>
  <div class="space-y-2">
    <Select
      id="library"
      v-model="library"
      :options="libraries ?? []"
      :label-text="$t('common-fields.library')"
      :option-text="(l?: LibraryEntity) => l?.attributes.name ?? ''"
      :option-value="(l?: LibraryEntity) => l?.id ?? ''"
      :invalid="false"
      :errors="[]"
      :disabled="loadingLibraries"
      :placeholder="$t('libraries.select')"
    />

    <Select
      v-if="collections"
      id="collection"
      v-model="collection"
      :options="collections"
      :label-text="$t('common-fields.collection')"
      :option-text="(c?: CollectionEntity) => c?.attributes.name ?? ''"
      :option-value="(c?: CollectionEntity) => c?.id ?? ''"
      :invalid="false"
      :errors="[]"
      :disabled="loadingCollections || loadingLibraries"
      :placeholder="$t('collections.select')"
    />
  </div>
</template>
