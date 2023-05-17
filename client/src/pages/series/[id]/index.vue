<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { Square2StackIcon } from '@heroicons/vue/24/outline'
import { getRelationship } from '@/utils/api'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const seriesId = useRouteParams<string | undefined>('id', undefined)
const notificator = useToaster()

const { mutate: deleteSeries, isLoading: isDeleting, isSuccess: isDeleted } = useDeleteSeriesMutation()

const queryEnabled = computed(() => {
  return !!seriesId.value && !isDeleting.value && !isDeleted.value && route.name === 'series-id'
})

const { data: series, isLoading } = useSeriesQuery({
  seriesId: seriesId as Ref<string>,
  includes: ['library', 'series_cover'],
  enabled: queryEnabled,
  onError: async (error) => {
    await notificator.failure({
      title: t('series.fetch-one-failure'),
      body: error.message,
    })
  },
})

function handleDelete() {
  deleteSeries(seriesId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('series.deleted-with-success') })
      await router.replace({ name: 'series' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('series.deleted-with-failure'),
        body: error.message,
      })
    },
  })
}

const regionCode = computed(() => {
  const originalLanguage = series.value?.attributes.originalLanguage

  if (!originalLanguage) {
    return undefined
  }

  const languageParts = originalLanguage.split('-')
  return languageParts[languageParts.length - 1]
})

useHead({ title: () => series.value?.attributes.name ?? '' })
</script>

<template>
  <div
    :class="[
      'bg-white dark:bg-gray-950 motion-safe:transition-colors',
      'duration-300 ease-in-out -mt-16 relative',
    ]"
  >
    <div class="absolute inset-x-0 top-0">
      <ImageBanner
        :alt="series?.attributes.name ?? ''"
        :loading="isLoading"
        :image="getRelationship(series, 'SERIES_COVER')?.attributes"
      />
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 z-10 pt-20 pb-6 relative">
      <div class="series-grid">
        <ImageCover
          class="series-cover"
          version="256"
          :icon="Square2StackIcon"
          :loading="isLoading"
          :image="getRelationship(series, 'SERIES_COVER')?.attributes"
          :alt="series?.attributes.name ?? ''"
        >
          <Flag
            v-if="!isLoading"
            :region="regionCode"
            :class="[
              'inline-block z-10',
              'absolute right-1.5 sm:right-3 bottom-1.5 sm:bottom-3',
              'pointer-events-none',
            ]"
          />
        </ImageCover>

        <SeriesName
          class="series-title"
          :loading="isLoading"
          :series="series"
        />

        <div class="series-buttons pt-1.5">
          <div
            v-if="isLoading"
            class="flex justify-center sm:justify-start items-center gap-2"
          >
            <div class="skeleton w-12 h-12" />
            <div class="skeleton w-12 h-12" />
          </div>
          <Toolbar v-else class="flex justify-center sm:justify-start items-center gap-2">
            <Button
              class="aspect-1"
              size="small"
              is-router-link
              :to="{ name: 'series-id-edit', params: { id: series?.id } }"
              :disabled="isDeleting"
              :title="$t('common-actions.edit')"
            >
              <span class="sr-only">{{ $t('common-actions.edit') }}</span>
              <PencilIcon class="w-5 h-5" />
            </Button>

            <Button
              class="aspect-1"
              size="small"
              :loading="isDeleting"
              :title="$t('common-actions.delete')"
              @click="handleDelete"
            >
              <span class="sr-only">{{ $t('common-actions.delete') }}</span>
              <TrashIcon class="w-5 h-5" />
            </Button>
          </Toolbar>
        </div>

        <div class="series-description">
          <BlockMarkdown
            :loading="isLoading"
            :markdown="series?.attributes?.description"
            :title="$t('common-fields.description')"
          />
        </div>

        <SeriesAlternativeNames
          class="series-names"
          :alternative-names="series?.attributes?.alternativeNames"
          :loading="isLoading"
        />
      </div>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: dashboard
  transparentNavbar: true
</route>

<style lang="postcss">
.series-grid {
  display: grid;
  gap: 1rem;
  grid-template-areas:
    'cover title'
    'buttons buttons'
    'description description'
    'names names'
    'statistics statistics';
  grid-template-columns: 6rem 1fr;

  @media (min-width: theme('screens.sm')) {
    gap: 1.5rem;
    grid-template-areas:
      'cover title'
      'cover buttons'
      'cover padding'
      'statistics description'
      'statistics names';
    grid-template-columns: 14rem 1fr;
  }

  .series-cover {
    grid-area: cover / cover / cover / cover;
  }

  .series-buttons {
    grid-area: buttons / buttons / buttons / buttons;
  }

  .series-title {
    grid-area: title / title / title / title;
  }

  .series-description {
    grid-area: description / description / description / description;
  }

  .series-names {
    grid-area: names / names / names / names;
  }

  .series-statistics {
    grid-area: statistics / statistics / statistics / statistics;
  }
}
</style>
