<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { Square2StackIcon } from '@heroicons/vue/24/outline'
import { getRelationship } from '@/utils/api'
import { BookOpenIcon, InformationCircleIcon } from '@heroicons/vue/20/solid'
import Button from '@/components/form/Button.vue'
import { PillTab } from '@/components/PillTabsList.vue'
import { Sort } from '@/types/tankobon-api'
import { BookSort } from '@/types/tankobon-book'
import { safeNumber } from '@/utils/route'

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

const sort = ref<Sort<BookSort> | null>({ property: 'number', direction: 'asc' })

const { data: books, isLoading: isLoadingBooks } = useSeriesBooksQuery({
  seriesId: seriesId as Ref<string>,
  includes: ['cover_art', 'collection', 'series', 'publisher'],
  unpaged: true,
  sort: computed(() => sort.value ? [sort.value] : undefined),
  enabled: computed(() => queryEnabled.value && !!series.value?.id),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('books.fetch-failure'),
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

const tabs: PillTab[] = [
  { key: '0', text: 'series.information', icon: InformationCircleIcon },
  { key: '1', text: 'series.volumes', icon: BookOpenIcon },
]

const activeTabHash = useRouteQuery('tab', '0', { transform: v => safeNumber(v, 0, { min: 0, max: tabs.length - 1 })})
const activeTab = computed({
  get: () => {
    const index = Number(activeTabHash.value)
    return tabs[index] ?? tabs[0]
  },
  set: (newTab) => activeTabHash.value = Number(newTab.key)
})
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
      <TabGroup
        as="div"
        class="series-grid"
        :selected-index="Number(activeTab.key)"
        @change="activeTab = tabs[$event]"
      >
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

        <div class="series-buttons pt-1.5 flex items-center justify-between">
          <PillTabsList
            v-model="activeTab"
            :tabs="tabs"
            :disabled="isLoading"
          />
          
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

        <TabPanels class="series-tabs">
          <TabPanel class="series-information-grid -mb-4 sm:mb-0">
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
          </TabPanel>

          <TabPanel :unmount="false">
            <BooksListViewer
              v-model:sort="sort"
              column-order-key="series_books_column_order"
              column-visibility-key="series_books_column_visibility"
              view-mode-key="series_books_view_mode"
              unpaged
              :books="books"
              :default-column-order="['title', 'boughtAt', 'number']"
              :default-column-visibility="{
                collection: false,
                series: false,
                createdAt: false,
                modifiedAt: false,
                boughtAt: true,
                billedAt: false,
                arrivedAt: false,
                weightKg: false,
                publishers: false,
                title: true,
                number: true,
                pageCount: false,
              }"
              :loading="isLoadingBooks || isLoading"
              :with-search="false"
            />
          </TabPanel>
        </TabPanels>

        <div class="series-attributes">
          <SeriesAttributes
            class="sm:sticky sm:top-24"
            :loading="isLoading"
            :series="series"
          />
        </div>
      </TabGroup>
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
    'tabs tabs'
    'attributes attributes';
  grid-template-columns: 6rem 1fr;

  @media (min-width: theme('screens.sm')) {
    gap: 1.5rem;
    grid-template-areas:
      'cover title'
      'cover buttons'
      'cover padding'
      'attributes tabs';
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

  .series-tabs {
    grid-area: tabs / tabs / tabs / tabs;
  }

  .series-attributes {
    grid-area: attributes / attributes / attributes / attributes;
  }
}

.series-information-grid {
  display: grid;
  gap: 1rem;
  grid-template-areas:
    'description description'
    'names names';
  grid-template-columns: 6rem 1fr;

  @media (min-width: theme('screens.sm')) {
    gap: 1.5rem;
    grid-template-columns: 14rem 1fr;
  }

  .series-description {
    grid-area: description / description / description / description;
  }

  .series-names {
    grid-area: names / names / names / names;
  }
}
</style>
