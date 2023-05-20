<script lang="ts" setup>
import { BookmarkIcon, InformationCircleIcon, PlusIcon } from '@heroicons/vue/20/solid'
import Button from '@/components/form/Button.vue'
import { isIsbnCode } from '@/types/tankobon-book'
import { createEmptyCollectionResponse, getRelationship, getRelationships } from '@/utils/api'
import type { ReadProgressCreation, ReadProgressEntity, ReadProgressUpdate } from '@/types/tankobon-read-progress'
import { PillTab } from '@/components/PillTabsList.vue'
import { safeNumber } from '@/utils/route'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const bookId = useRouteParams<string | undefined>('id', undefined)
const notificator = useToaster()

const { mutate: deleteBook, isLoading: isDeleting, isSuccess: isDeleted } = useDeleteBookMutation()

const queryEnabled = computed(() => {
  return !!bookId.value && !isDeleting.value && !isDeleted.value && route.name === 'books-id'
})

const { data: book, isLoading } = useBookQuery({
  bookId: bookId as Ref<string>,
  includes: [
    'publisher',
    'collection',
    'contributor',
    'series',
    'store',
    'tag',
    'library',
    'cover_art',
    'previous_book',
    'next_book',
  ],
  enabled: queryEnabled,
  onError: async (error) => {
    await notificator.failure({
      title: t('books.fetch-one-failure'),
      body: error.message,
    })
  },
})

const { data: contributors, isLoading: isLoadingContributors } = useBookContributorsQuery({
  bookId: bookId as Ref<string>,
  includes: ['person_picture'],
  select: response => response.data,
  enabled: computed(() => queryEnabled.value && !isLoading.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('book-contributors.fetch-failure'),
      body: error.message,
    })
  },
})

const { data: readProgresses, isLoading: isLoadingReadProgresses } = useBookReadProgressesQuery({
  bookId: bookId as Ref<string>,
  sort: [
    { property: 'startedAt', direction: 'desc' },
    { property: 'finishedAt', direction: 'desc' },
  ],
  enabled: computed(() => queryEnabled.value && !isLoading.value),
  select: response => response.data,
  initialData: createEmptyCollectionResponse(),
  onError: async (error) => {
    await notificator.failure({
      title: t('read-progresses.fetch-failure'),
      body: error.message,
    })
  },
})

const showBookInfo = computed(() => {
  return !isLoading.value && !!book.value
})

function handleDelete() {
  deleteBook(bookId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('books.deleted-with-success') })
      await router.replace({ name: 'books' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('books.deleted-with-failure'),
        body: error.message,
      })
    },
  })
}

const regionCode = computed(() => {
  const code = book.value?.attributes?.code
  return isIsbnCode(code) ? code.region : null
})

useHead({ title: () => book.value?.attributes?.title ?? '' })

const tabs: PillTab[] = [
  { key: '0', text: 'books.information', icon: InformationCircleIcon },
  { key: '1', text: 'books.reading', icon: BookmarkIcon },
]

const activeTabHash = useRouteQuery('tab', '0', { transform: v => safeNumber(v, 0, { min: 0, max: tabs.length - 1 })})
const activeTab = computed({
  get: () => {
    const index = Number(activeTabHash.value)
    return tabs[index] ?? tabs[0]
  },
  set: (newTab) => activeTabHash.value = Number(newTab.key)
})

const showCreateReadProgressDialog = ref(false)
const { mutate: createReadProgress, isLoading: isCreatingReadProgress } = useCreateReadProgressMutation()

function handleCreateReadProgress(readProgress: ReadProgressCreation) {
  createReadProgress(readProgress, {
    onSuccess: async () => {
      await notificator.success({ title: t('read-progresses.created-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('read-progresses.created-with-failure'),
        body: error.message,
      })
    },
  })
}

const showEditReadProgressDialog = ref(false)
const readProgressToEdit = ref<ReadProgressEntity>()
const { mutate: updateReadProgress, isLoading: isUpdatingReadProgress } = useUpdateReadProgressMutation()

function openEditReadProgressDialog(readProgress: ReadProgressEntity) {
  readProgressToEdit.value = readProgress
  nextTick(() => showEditReadProgressDialog.value = true)
}

function handleEditReadProgress(readProgress: ReadProgressUpdate) {
  updateReadProgress(readProgress, {
    onSuccess: async () => {
      readProgressToEdit.value = undefined
      await notificator.success({ title: t('read-progresses.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('read-progresses.edited-with-failure'),
        body: error.message,
      })
    },
  })
}

const readProgressToDelete = ref<ReadProgressEntity>()
const { mutate: deleteReadProgress, isLoading: isDeletingReadProgress } = useDeleteReadProgressMutation()

const isEditingReadProgress = logicOr(
  isCreatingReadProgress,
  isUpdatingReadProgress,
  isDeletingReadProgress,
)

function handleDeleteReadProgress(readProgress: ReadProgressEntity) {
  readProgressToDelete.value = readProgress

  deleteReadProgress(readProgress.id, {
    onSuccess: async () => {
      readProgressToDelete.value = undefined
      await notificator.success({ title: t('read-progresses.deleted-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('read-progresses.deleted-with-failure'),
        body: error.message,
      })
    },
  })
}
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
        :alt="book?.attributes.title ?? ''"
        :loading="!showBookInfo"
        :image="getRelationship(book, 'COVER_ART')?.attributes"
      />
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 z-10 pt-20 pb-6 relative">
      <TabGroup
        as="div"
        class="book-grid"
        :selected-index="Number(activeTab.key)"
        @change="activeTab = tabs[$event]"
      >
        <ImageCover
          class="book-cover"
          version="256"
          :loading="!showBookInfo"
          :image="getRelationship(book, 'COVER_ART')?.attributes"
          :alt="book?.attributes.title ?? ''"
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

        <BookTitle
          class="book-title"
          :loading="!showBookInfo"
          :book="book"
        />

        <div class="book-buttons pt-1.5 flex items-center justify-between">
          <PillTabsList
            v-model="activeTab"
            :tabs="tabs"
            :disabled="!showBookInfo"
          />

          <BookButtons
            v-if="activeTab.key === '0'"
            :loading="!showBookInfo"
            :book="book"
            :editing="isDeleting || isEditingReadProgress"
            @click:delete="handleDelete"
          />
          <Button
            v-else
            class="aspect-1"
            size="small"
            :title="$t('common-actions.add')"
            :disabled="isEditingReadProgress || isLoadingReadProgresses || book?.attributes.pageCount === 0"
            :loading="isCreatingReadProgress"
            @click="showCreateReadProgressDialog = true"
          >
            <span class="sr-only">{{ $t('common-actions.add') }}</span>
            <PlusIcon class="w-5 h-5" />
          </Button>
        </div>

        <TabPanels class="book-tabs">
          <TabPanel class="book-information-grid -mb-4 sm:mb-0" :unmount="false">
            <div class="book-synopsis flex flex-col gap-4 sm:gap-6">
              <BookNavigator
                :loading="!showBookInfo"
                :previous="getRelationship(book, 'PREVIOUS_BOOK')"
                :next="getRelationship(book, 'NEXT_BOOK')"
              />

              <BlockMarkdown
                :title="$t('common-fields.synopsis')"
                :empty-message="$t('books.empty-synopsis')"
                :loading="!showBookInfo"
                :markdown="book?.attributes?.synopsis ?? undefined"
                :blur="false"
              />

              <BookContributors
                :loading="isLoadingContributors"
                :contributors="contributors"
              />

              <EntityExternalLinks
                :links="(book?.attributes?.links as Record<string, string | null> | undefined)"
                :loading="!showBookInfo"
              />

              <BookTags
                class="2xl:hidden"
                :tags="getRelationships(book, 'TAG')"
                :loading="!showBookInfo"
              />

              <BlockMarkdown
                :title="$t('common-fields.notes')"
                :loading="!showBookInfo"
                :markdown="book?.attributes?.notes ?? undefined"
                :blur="false"
              />
            </div>

            <aside class="book-right hidden 2xl:block">
              <div class="sticky top-24 flex flex-col gap-6">
                <BookTags
                  group
                  :tags="getRelationships(book, 'TAG')"
                  :loading="!showBookInfo"
                />
              </div>
            </aside>
          </TabPanel>

          <TabPanel>
            <BookReadProgresses
              :book="book"
              :read-progresses="readProgresses ?? []"
              :loading="!showBookInfo || isLoadingReadProgresses"
              :creating="isCreatingReadProgress"
              :deleting="isDeletingReadProgress"
              :deleting-id="readProgressToDelete?.id"
              :editing="isUpdatingReadProgress"
              :editing-id="readProgressToEdit?.id"
              @click:new="showCreateReadProgressDialog = true"
              @click:edit="openEditReadProgressDialog($event)"
              @click:delete="handleDeleteReadProgress($event)"
            />
          </TabPanel>
        </TabPanels>

        <div class="book-attributes">
          <BookAttributes
            class="sm:sticky sm:top-24"
            :loading="!showBookInfo"
            :book="book"
          />
        </div>
      </TabGroup>
    </div>

    <ReadProgressCreateDialog
      v-if="showBookInfo"
      :book-id="book?.id ?? ''"
      :page-count="book?.attributes.pageCount ?? 0"
      :is-open="showCreateReadProgressDialog"
      @submit="handleCreateReadProgress"
      @close="showCreateReadProgressDialog = false"
    />

    <ReadProgressEditDialog
      v-if="showBookInfo && readProgressToEdit"
      :is-open="showEditReadProgressDialog"
      :read-progress-entity="readProgressToEdit"
      :page-count="book?.attributes.pageCount ?? 0"
      @submit="handleEditReadProgress"
      @close="showEditReadProgressDialog = false"
    />
  </div>
</template>

<route lang="yaml">
meta:
  layout: dashboard
  transparentNavbar: true
</route>

<style lang="postcss">
.book-information-grid {
  display: grid;
  gap: 1rem;
  grid-template-areas:
    'synopsis synopsis'
    'notes notes';
  grid-template-columns: 6rem 1fr;

  @media (min-width: theme('screens.sm')) {
    gap: 1.5rem;
  }

  @media (min-width: theme('screens.2xl')) {
    grid-template-areas:
      'synopsis right'
      'tags right'
      'notes right';
    grid-template-columns: 1fr 22rem;
  }

  .book-synopsis {
    grid-area: synopsis / synopsis / synopsis / synopsis;
  }

  .book-right {
    grid-area: right / right / right / right;
  }
}

.book-grid {
  display: grid;
  gap: 1rem;
  grid-template-areas:
    'art title'
    'buttons buttons'
    'tabs tabs'
    'attributes attributes';
  grid-template-columns: 6rem 1fr;

  @media (min-width: theme('screens.sm')) {
    gap: 1.5rem;
    grid-template-areas:
      'art title'
      'art buttons'
      'art padding'
      'attributes tabs';
    grid-template-columns: 14rem 1fr;
  }

  .book-cover {
    grid-area: art / art / art / art;
  }

  .book-buttons {
    grid-area: buttons / buttons / buttons / buttons;
  }

  .book-title {
    grid-area: title / title / title / title;
  }

  .book-tabs {
    grid-area: tabs / tabs / tabs / tabs;
  }

  .book-attributes {
    grid-area: attributes / attributes / attributes / attributes;
  }
}
</style>
