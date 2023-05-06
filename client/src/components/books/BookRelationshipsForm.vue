<script setup lang="ts">
import type { CollectionEntity } from '@/types/tankobon-collection'
import type { SeriesEntity } from '@/types/tankobon-series'
import type { StoreEntity } from '@/types/tankobon-store'
import { createEmptyPaginatedResponse } from '@/utils/api'

export interface BookRelationshipsFormProps {
  collection: string
  disabled?: boolean
  loading?: boolean
  series: string | null | undefined
  store: string | null | undefined
}

const props = withDefaults(defineProps<BookRelationshipsFormProps>(), {
  disabled: false,
  loading: false,
})

defineEmits<{
  (e: 'update:collection', collection: string): void
  (e: 'update:series', series: string | null): void
  (e: 'update:store', store: string | null): void
}>()

const { series, collection, store } = toRefs(props)

const { t } = useI18n()
const notificator = useToaster()
const libraryStore = useLibraryStore()
const libraryId = computed(() => libraryStore.library!.id)

const { data: librarySeries } = useLibrarySeriesQuery({
  libraryId,
  sort: [{ property: 'name', direction: 'asc' }],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('series.fetch-failure'),
      body: error.message,
    })
  },
})

const { data: collections } = useLibraryCollectionsQuery({
  libraryId,
  sort: [{ property: 'name', direction: 'asc' }],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('collections.fetch-failure'),
      body: error.message,
    })
  },
})

const { data: stores } = useLibraryStoresQuery({
  libraryId,
  sort: [{ property: 'name', direction: 'asc' }],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('stores.fetch-failure'),
      body: error.message,
    })
  },
})

const nullSeries = computed<SeriesEntity>(() => ({
  type: 'SERIES',
  id: 'null',
  attributes: {
    name: t('series.none'),
    description: '',
  },
  relationships: [],
}))

const nullStore = computed<StoreEntity>(() => ({
  type: 'STORE',
  id: 'null',
  attributes: {
    name: t('stores.unknown'),
    description: '',
  },
  relationships: [],
}))

const seriesValue = computed(() => {
  return librarySeries.value!.find(s => s.id === series.value) ?? nullSeries.value
})

const collectionValue = computed(() => {
  return collections.value!.find(c => c.id === collection.value)
})

const storeValue = computed(() => {
  return stores.value!.find(s => s.id === store.value) ?? nullStore.value
})

const seriesOptions = computed(() => {
  return [nullSeries.value, ...librarySeries.value!]
})

const storeOptions = computed(() => {
  return [nullStore.value, ...stores.value!]
})
</script>

<template>
  <fieldset :disabled="disabled || loading">
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-2">
      <SearchableCombobox
        kind="fancy"
        :placeholder="$t('common-placeholders.book-series')"
        :label-text="$t('common-fields.series')"
        :model-value="seriesValue"
        :options="seriesOptions ?? []"
        :option-text="(r: SeriesEntity) => r?.attributes?.name"
        :option-value="(r: SeriesEntity) => r"
        :option-value-select="(r: SeriesEntity) => r?.id"
        @update:model-value="$emit('update:series', $event?.id === 'null' ? null : $event?.id)"
        @update:model-value-select="$emit('update:series', $event === 'null' ? null : $event)"
      />

      <SearchableCombobox
        kind="fancy"
        :placeholder="$t('common-placeholders.book-collection')"
        :label-text="$t('common-fields.collection')"
        :model-value="collectionValue"
        :options="collections ?? []"
        :option-text="(r: CollectionEntity) => r?.attributes?.name"
        :option-value="(r: CollectionEntity) => r"
        :option-value-select="(r: CollectionEntity) => r?.id"
        @update:model-value="$emit('update:collection', $event?.id)"
        @update:model-value-select="$emit('update:collection', $event)"
      />

      <SearchableCombobox
        kind="fancy"
        align="end"
        :placeholder="$t('common-placeholders.book-store')"
        :label-text="$t('common-fields.store')"
        :model-value="storeValue"
        :options="storeOptions ?? []"
        :option-text="(r: StoreEntity) => r?.attributes?.name"
        :option-value="(r: StoreEntity) => r"
        :option-value-select="(r: StoreEntity) => r?.id"
        @update:model-value="$emit('update:store', $event?.id === 'null' ? null : $event?.id)"
        @update:model-value-select="$emit('update:store', $event === 'null' ? null : $event)"
      />
    </div>
  </fieldset>
</template>
