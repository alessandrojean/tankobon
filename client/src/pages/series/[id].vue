<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { SeriesUpdate } from '@/types/tankobon-series'
import { getRelationship } from '@/utils/api'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const seriesId = computed(() => route.params.id?.toString())
const notificator = useNotificator()

const { data: series, isLoading } = useSeriesQuery({
  seriesId,
  includes: ['library'],
  enabled: computed(() => route.params.id !== undefined),
  onError: async (error) => {
    await notificator.failure({
      title: t('series.fetch-one-failure'),
      body: error.message,
    })
  }
})
const { mutate: deleteSeries, isLoading: isDeleting } = useDeleteSeriesMutation()
const { mutate: editSeries, isLoading: isEditing } = useUpdateSeriesMutation()

const library = computed(() => getRelationship(series.value, 'LIBRARY'))

function handleDelete() {
  deleteSeries(seriesId.value, {
    onSuccess: async () => {
      notificator.success({ title: t('series.deleted-with-success') })
      await router.replace({ name: 'series' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('series.deleted-with-failure'),
        body: error.message,
      })
    }
  })
}

const showEditDialog = ref(false)

function handleEditSeries(series: SeriesUpdate) {
  editSeries(series, {
    onSuccess: async () => {
      await notificator.success({ title: t('series.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('series.edited-with-failure'),
        body: error.message,
      })
    }
  })
}
</script>

<template>
  <div>
    <Header
      :title="series?.attributes.name ?? ''"
      :subtitle="series?.attributes.description"
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

    <SeriesEditDialog
      v-if="series"
      :is-open="showEditDialog"
      :series-entity="series"
      @submit="handleEditSeries"
      @close="showEditDialog = false"
    />
  </div>
</template>

<route lang="yaml">
  meta:
    layout: dashboard
    isAdminOnly: true
</route>
