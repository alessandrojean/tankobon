<script setup lang="ts">
import { CheckIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import BookMetadataForm from '@/components/books/BookMetadataForm.vue'
import type { BookUpdate } from '@/types/tankobon-book'
import type { DimensionsString } from '@/types/tankobon-dimensions'
import type { MonetaryAmountString } from '@/types/tankobon-monetary'

const { t, n } = useI18n()
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
  enabled: computed(() => !!bookId.value), // && !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('books.fetch-one-failure'),
      body: error.message,
    })
  },
})

const tabs = [
  { key: '0', text: 'books.metadata' },
  { key: '1', text: 'books.relationships' },
  { key: '2', text: 'books.cover-art' },
  { key: '3', text: 'books.organization' },
]

interface CustomBookUpdate extends Omit<BookUpdate, 'dimensions' | 'pageCount' | 'labelPrice' | 'paidPrice'> {
  dimensions: DimensionsString
  labelPrice: MonetaryAmountString
  paidPrice: MonetaryAmountString
  pageCount: string
}

const updatedBook = reactive<CustomBookUpdate>({
  id: '',
  arrivedAt: null,
  barcode: null,
  billedAt: null,
  boughtAt: null,
  code: '',
  collection: '',
  contributors: [],
  isInLibrary: true,
  dimensions: {
    widthCm: '0',
    heightCm: '0',
  },
  labelPrice: {
    amount: '0',
    currency: 'USD',
  },
  notes: '',
  number: '',
  pageCount: '0',
  paidPrice: {
    amount: '0',
    currency: 'USD',
  },
  publishers: [],
  series: null,
  store: null,
  subtitle: '',
  synopsis: '',
  tags: [],
  title: '',
})

whenever(book, (loadedBook) => {
  Object.assign(updatedBook, {
    id: loadedBook.id,
    code: loadedBook.attributes.code.code,
    number: loadedBook.attributes.number,
    barcode: loadedBook.attributes.barcode,
    title: loadedBook.attributes.title,
    subtitle: loadedBook.attributes.subtitle,
    synopsis: loadedBook.attributes.synopsis,
    pageCount: String(loadedBook.attributes.pageCount),
    notes: loadedBook.attributes.notes,
    boughtAt: loadedBook.attributes.boughtAt,
    billedAt: loadedBook.attributes.billedAt,
    arrivedAt: loadedBook.attributes.arrivedAt,
    dimensions: {
      widthCm: n(loadedBook.attributes.dimensions.widthCm, 'decimal'),
      heightCm: n(loadedBook.attributes.dimensions.heightCm, 'decimal'),
    },
    labelPrice: {
      amount: String(loadedBook.attributes.labelPrice.amount),
      currency: loadedBook.attributes.labelPrice.currency,
    },
    paidPrice: {
      amount: String(loadedBook.attributes.paidPrice.amount),
      currency: loadedBook.attributes.paidPrice.currency,
    },
  } satisfies Partial<CustomBookUpdate>)
}, { immediate: true })

const activeTab = ref(tabs[0])
</script>

<template>
  <div>
    <TabGroup :selected-index="Number(activeTab.key)" @change="activeTab = tabs[$event]">
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
          <TabList class="hidden md:flex gap-3 -mb-px">
            <Tab
              v-for="tab in tabs"
              :key="tab"
              v-slot="{ selected }"
              as="template"
            >
              <Button
                kind="underline-tab"
                size="underline-tab"
                rounded="none"
                :data-headlessui-state="selected ? 'selected' : undefined"
              >
                {{ $t(tab.text) }}
              </Button>
            </Tab>
          </TabList>
          <BasicSelect
            v-model="activeTab"
            class="md:hidden mb-4"
            :options="tabs"
            :option-text="(tab: any) => $t(tab.text)"
            :option-value="(tab: any) => tab.key"
          />
        </template>
      </Header>
      <div class="max-w-7xl mx-auto p-4 sm:p-6">
        <TabPanels>
          <TabPanel>
            <BookMetadataForm
              v-model:code="updatedBook.code"
              v-model:barcode="updatedBook.barcode"
              v-model:number="updatedBook.number"
              v-model:title="updatedBook.title"
              v-model:subtitle="updatedBook.subtitle"
              v-model:synopsis="updatedBook.synopsis"
              v-model:page-count="updatedBook.pageCount"
              v-model:notes="updatedBook.notes"
              v-model:bought-at="updatedBook.boughtAt"
              v-model:billed-at="updatedBook.billedAt"
              v-model:arrived-at="updatedBook.arrivedAt"
              v-model:dimensions="updatedBook.dimensions"
            />
          </TabPanel>
          <TabPanel>Relationships</TabPanel>
          <TabPanel>Cover art</TabPanel>
          <TabPanel>
            <BookOrganizationForm
              v-model:notes="updatedBook.notes"
              v-model:bought-at="updatedBook.boughtAt"
              v-model:billed-at="updatedBook.billedAt"
              v-model:arrived-at="updatedBook.arrivedAt"
              v-model:label-price="updatedBook.labelPrice"
              v-model:paid-price="updatedBook.paidPrice"
            />
          </TabPanel>
        </TabPanels>
      </div>
    </TabGroup>
  </div>
</template>

<route lang="yaml">
meta:
  layout: dashboard
</route>
