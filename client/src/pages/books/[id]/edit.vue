<script setup lang="ts">
import BookMetadataForm from '@/components/books/BookMetadataForm.vue'
import { BookUpdate } from '@/types/tankobon-book'
import { CheckIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const bookId = computed(() => route.params.id as string)

const { data: book, isLoading } = useBookQuery({
  bookId,
  includes: [
    'publisher',
    'collection',
    'contributor',
    'series',
    'store',
    'tag',
    'library',
    'cover_art',
  ],
  enabled: computed(() => !!bookId.value), //&& !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('books.fetch-one-failure'),
      body: error.message,
    })
  }
})

const tabs = computed(() => [
  { key: 'metadata', text: t('books.metadata') },
  { key: 'relationships', text: t('books.relationships') },
  { key: 'cover-art', text: t('books.cover-art') },
  { key: 'organization', text: t('books.organization') },
])

const updatedBook = reactive<BookUpdate>({
  id: '',
  arrivedAt: null,
  barcode: null,
  billedAt: null,
  boughtAt: null,
  code: '',
  collection: '',
  contributors: [],
  dimensions: {
    widthCm: 0,
    heightCm: 0,
  },
  isInLibrary: true,
  labelPrice: {
    amount: 0,
    currency: 'USD',
  },
  notes: '',
  number: '',
  pageCount: 0,
  paidPrice: {
    amount: 0,
    currency: 'USD',
  },
  publishers: [],
  series: null,
  store: null,
  synopsis: '',
  tags: [],
  title: '',
})

whenever(book, (loadedBook) => {
  Object.assign(updatedBook, <Partial<BookUpdate>> {
    id: loadedBook.id,
    code: loadedBook.attributes.code.code,
    title: loadedBook.attributes.title,
    synopsis: loadedBook.attributes.synopsis,
    notes: loadedBook.attributes.notes,
  })
}, { immediate: true })
</script>

<template>
  <div>
    <TabGroup>
      <Header
        :title="book?.attributes.title ?? ''"
        :loading="isLoading"
      >
        <template #avatar>
          <Button
            class="aspect-1 w-10 h-10 -ml-2"
            size="mini"
            kind="ghost"
            rounded="full"
            :title="$t('common-actions.back')"
            @click="router.back"
          >
            <span class="sr-only">
              {{ $t('common-actions.back') }}
            </span>
            <ArrowLeftIcon class="w-5 h-5" />
          </Button>
        </template>
        <template #actions>
          <Button
            kind="primary"
          >
            <CheckIcon class="w-5 h-5" />
            <span>{{ $t('common-actions.save') }}</span>
          </Button>
        </template>
        <template #tabs>
          <TabList class="flex gap-3 -mb-px">
            <Tab
              v-for="tab in tabs"
              :key="tab"
              as="template"
              v-slot="{ selected }"
            >
              <Button
                kind="underline-tab"
                size="underline-tab"
                rounded="none"
                :data-headlessui-state="selected ? 'selected' : undefined"
              >
                {{ tab.text }}
              </Button>
            </Tab>
          </TabList>
        </template>
      </Header>
      <div class="max-w-7xl mx-auto p-4 sm:p-6">
        <TabPanels>
          <TabPanel>
            <BookMetadataForm
              v-model:code="updatedBook.code"
              v-model:title="updatedBook.title"
              v-model:synopsis="updatedBook.synopsis"
              v-model:notes="updatedBook.notes"
            />
          </TabPanel>
          <TabPanel>Relationships</TabPanel>
          <TabPanel>Cover art</TabPanel>
          <TabPanel>Organization</TabPanel>
        </TabPanels>
      </div>
    </TabGroup>
  </div>
</template>

<route lang="yaml">
  meta:
    layout: dashboard
</route>