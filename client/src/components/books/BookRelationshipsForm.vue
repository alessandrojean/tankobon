<script setup lang="ts">
import Draggable from 'vuedraggable'
import { PlusIcon } from '@heroicons/vue/20/solid'
import { helpers } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import type { SeriesEntity } from '@/types/tankobon-series'
import { createEmptyPaginatedResponse } from '@/utils/api'

export interface BookRelationshipsFormProps {
  disabled?: boolean
  loading?: boolean
  series: string | null | undefined
  publishers: string[]
}

const props = withDefaults(defineProps<BookRelationshipsFormProps>(), {
  disabled: false,
  loading: false,
})

const emit = defineEmits<{
  (e: 'update:series', series: string | null): void
  (e: 'update:publishers', publishers: string[]): void
}>()

const { series, publishers } = toRefs(props)

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

const { data: libraryPublishers } = useLibraryPublishersQuery({
  libraryId,
  includes: ['publisher_picture'],
  sort: [{ property: 'name', direction: 'asc' }],
  unpaged: true,
  select: response => response.data,
  initialData: () => createEmptyPaginatedResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('publishers.fetch-failure'),
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

const seriesValue = computed(() => {
  return librarySeries.value!.find(s => s.id === series.value) ?? nullSeries.value
})

const seriesOptions = computed(() => {
  return [nullSeries.value, ...librarySeries.value!]
})

const publisherMap = computed(() => {
  return Object.fromEntries(
    libraryPublishers.value!.map(p => [p.id, p]),
  )
})

function unique(publishers: string[]) {
  return [...new Set(publishers)].length === publishers.length
}

const rules = computed(() => {
  const messageUnique = helpers.withMessage(t('validation.unique'), unique)

  return {
    publishers: { messageUnique },
  }
})

const v$ = useVuelidate(rules, { publishers })

defineExpose({ v$ })

function handlePublisherPicked(publisherId: string, i: number) {
  const copy = structuredClone(toRaw(publishers.value))
  copy[i] = publisherId

  emit('update:publishers', copy)
  v$.value.publishers.$touch()
}

function handleDragAndDrop(newOrder: string[]) {
  const copy: string[] = JSON.parse(JSON.stringify(newOrder))
  emit('update:publishers', copy)
  v$.value.publishers.$touch()
}

function handleRemovePublisher(i: number) {
  const copy = structuredClone(toRaw(publishers.value))
  copy.splice(i, 1)

  emit('update:publishers', copy)
  v$.value.publishers.$touch()
}

const container = ref<HTMLFieldSetElement>()

async function addPublisher() {
  const copy = structuredClone(toRaw(publishers.value))
  copy.push('')

  emit('update:publishers', copy)
  await nextTick()

  const publisherIndex = copy.length - 1
  const inputToFocus = container.value
    ?.querySelector<HTMLInputElement>(`#publisher-input-${publisherIndex} input`)

  inputToFocus?.focus()
}
</script>

<template>
  <fieldset ref="container" :disabled="disabled || loading" class="space-y-6">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-2">
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
    </div>

    <fieldset class="flex flex-col gap-6">
      <div class="flex flex-row justify-between items-center">
        <legend class="block text-lg font-medium font-display-safe">
          {{ $t('entities.publishers') }}
        </legend>

        <div>
          <Button
            size="small"
            :disabled="loading"
            @click="addPublisher"
          >
            <PlusIcon class="w-5 h-5" />
            <span>{{ $t('common-actions.add') }}</span>
          </Button>
        </div>
      </div>

      <Draggable
        v-if="!loading && libraryPublishers!.length > 0"
        class="flex flex-col gap-4"
        ghost-class="opacity-50"
        drag-class="cursor-grabbing"
        handle=".grabber"
        :model-value="publishers"
        :item-key="(p: string) => p"
        :disabled="libraryPublishers!.length === 1"
        @update:model-value="handleDragAndDrop"
      >
        <template #item="{ element: publisher, index: i }">
          <BookPublisherFormCard
            :draggable="libraryPublishers!.length > 1"
            :index="i"
            :publisher="publisherMap[publisher]"
            :publishers="libraryPublishers ?? []"
            :book-publishers="publishers"
            @update:publisher="handlePublisherPicked($event, i)"
            @click:remove="handleRemovePublisher(i)"
          />
        </template>
      </Draggable>
    </fieldset>
  </fieldset>
</template>
