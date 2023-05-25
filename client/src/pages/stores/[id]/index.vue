<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { BuildingStorefrontIcon } from '@heroicons/vue/24/outline'
import { BookOpenIcon, InformationCircleIcon } from '@heroicons/vue/20/solid'
import { getRelationship } from '@/utils/api'
import type { Sort } from '@/types/tankobon-api'
import type { BookSort } from '@/types/tankobon-book'
import type { PillTab } from '@/components/PillTabsList.vue'
import { safeNumber } from '@/utils/route'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const storeId = useRouteParams<string | undefined>('id', undefined)
const notificator = useToaster()

const { mutate: deleteStore, isLoading: isDeleting, isSuccess: isDeleted } = useDeleteStoreMutation()

const queryEnabled = computed(() => {
  return !!storeId.value && !isDeleting.value && !isDeleted.value && route.name === 'stores-id'
})

const { data: store, isLoading } = useStoreQuery({
  storeId: storeId as Ref<string>,
  includes: ['library', 'store_picture'],
  enabled: queryEnabled,
  onError: async (error) => {
    await notificator.failure({
      title: t('stores.fetch-one-failure'),
      body: error.message,
    })
  },
})

const sort = ref<Sort<BookSort> | null>({ property: 'number', direction: 'asc' })

const { data: books, isLoading: isLoadingBooks } = useStoreBooksQuery({
  storeId: storeId as Ref<string>,
  includes: ['cover_art', 'collection', 'series', 'publisher'],
  sort: computed(() => sort.value ? [sort.value] : undefined),
  enabled: computed(() => queryEnabled.value && !!store.value?.id),
  keepPreviousData: true,
  onError: async (error) => {
    await notificator.failure({
      title: t('books.fetch-failure'),
      body: error.message,
    })
  },
})

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
    },
  })
}

useHead({ title: () => store.value?.attributes.name ?? '' })

const tabs: PillTab[] = [
  { key: '0', text: 'stores.information', icon: InformationCircleIcon },
  { key: '1', text: 'entities.books', icon: BookOpenIcon },
]

const activeTabHash = useRouteQuery('tab', '0', { transform: v => safeNumber(v, 0, { min: 0, max: tabs.length - 1 }) })
const activeTab = computed({
  get: () => {
    const index = Number(activeTabHash.value)
    return tabs[index] ?? tabs[0]
  },
  set: newTab => activeTabHash.value = Number(newTab.key),
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
        class="!h-52"
        :alt="store?.attributes.name ?? ''"
        :loading="isLoading"
        :image="getRelationship(store, 'STORE_PICTURE')?.attributes"
        size="64"
        kind="repeated"
      />
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 z-10 pt-20 pb-6 relative">
      <TabGroup
        as="div"
        class="store-grid"
        :selected-index="Number(activeTab.key)"
        @change="activeTab = tabs[$event]"
      >
        <ImageCover
          class="store-cover"
          version="256"
          aspect-ratio="1 / 1"
          :icon="BuildingStorefrontIcon"
          :loading="isLoading"
          :image="getRelationship(store, 'STORE_PICTURE')?.attributes"
          :alt="store?.attributes.name ?? ''"
        >
          <Flag
            v-if="!isLoading"
            :region="store?.attributes.location"
            :class="[
              'inline-block z-10',
              'absolute right-1.5 sm:right-3 bottom-1.5 sm:bottom-3',
              'pointer-events-none',
            ]"
          />
        </ImageCover>

        <StoreName
          class="store-name"
          :loading="isLoading"
          :store="store"
        />

        <div class="store-buttons pt-1.5 flex items-center justify-between">
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
              :to="{ name: 'stores-id-edit', params: { id: store?.id } }"
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

        <TabPanels class="store-tabs">
          <TabPanel class="flex flex-col gap-4 sm:gap-6 -mb-4 sm:mb-0">
            <BlockMarkdown
              :loading="isLoading"
              :markdown="store?.attributes?.description"
              :title="$t('common-fields.description')"
            />

            <EntityExternalLinks
              :links="(store?.attributes?.links as Record<string, string | null> | undefined)"
              :loading="isLoading"
            />
          </TabPanel>

          <TabPanel :unmount="false">
            <BooksListViewer
              v-model:sort="sort"
              column-order-key="store_books_column_order"
              column-visibility-key="store_books_column_visibility"
              view-mode-key="store_books_view_mode"
              :books="books"
              :default-column-order="['title', 'collection', 'boughtAt']"
              :default-column-visibility="{
                collection: true,
                series: false,
                createdAt: false,
                modifiedAt: false,
                boughtAt: true,
                billedAt: false,
                arrivedAt: false,
                weight: false,
                publishers: false,
                title: true,
                number: false,
                pageCount: false,
              }"
              :loading="isLoadingBooks || isLoading"
              :with-search="false"
            />
          </TabPanel>
        </TabPanels>

        <div class="store-attributes">
          <StoreAttributes
            class="sm:sticky sm:top-24"
            :loading="isLoading"
            :store="store"
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
.store-grid {
  display: grid;
  gap: 1rem;
  grid-template-areas:
    'cover name'
    'buttons buttons'
    'tabs tabs'
    'attributes attributes';
  grid-template-columns: 6rem 1fr;

  @media (min-width: theme('screens.sm')) {
    gap: 1.5rem;
    grid-template-areas:
      'cover name'
      'cover buttons'
      'cover padding'
      'attributes tabs';
    grid-template-columns: 12rem 1fr;
  }

  .store-cover {
    grid-area: cover / cover / cover / cover;
  }

  .store-buttons {
    grid-area: buttons / buttons / buttons / buttons;
  }

  .store-name {
    grid-area: name / name / name / name;
  }

  .store-tabs {
    grid-area: tabs / tabs / tabs / tabs;
  }

  .store-attributes {
    grid-area: attributes / attributes / attributes / attributes;
  }
}
</style>
