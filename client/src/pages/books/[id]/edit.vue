<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import type { BookLinks, BookUpdate } from '@/types/tankobon-book'
import type { DimensionsString } from '@/types/tankobon-dimensions'
import type { MonetaryAmountString } from '@/types/tankobon-monetary'
import BookMetadataForm from '@/components/books/BookMetadataForm.vue'
import BookOrganizationForm from '@/components/books/BookOrganizationForm.vue'
import { getRelationship, getRelationships } from '@/utils/api'
import BookContributorsForm from '@/components/books/BookContributorsForm.vue'
import type { CoverArt } from '@/components/books/BookCoverArtForm.vue'
import { createImageUrl } from '@/modules/api'
import BookCoverArtForm from '@/components/books/BookCoverArtForm.vue'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { FormExternalLink } from '@/types/tankobon-external-link'
import EntityExternalLinksForm from '@/components/entity/EntityExternalLinksForm.vue'

const { t, n } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const bookId = computed(() => route.params.id as string)

const { mutateAsync: editBook, isLoading: isEditingBook } = useUpdateBookMutation()
const { mutateAsync: uploadCover, isLoading: isUploadingCover } = useUploadBookCoverMutation()
const { mutateAsync: deleteCover, isLoading: isDeletingCover } = useDeleteBookCoverMutation()

const isEditing = logicOr(isEditingBook, isUploadingCover, isDeletingCover)

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
  enabled: computed(() => !!bookId.value),
  refetchOnWindowFocus: false,
  refetchOnReconnect: false,
  staleTime: Infinity,
  onError: async (error) => {
    await notificator.failure({
      title: t('books.fetch-one-failure'),
      body: error.message,
    })
  },
})

const metadataForm = ref<InstanceType<typeof BookMetadataForm>>()
const contributorsForm = ref<InstanceType<typeof BookContributorsForm>>()
const coverArtForm = ref<InstanceType<typeof BookCoverArtForm>>()
const organizationForm = ref<InstanceType<typeof BookOrganizationForm>>()
const externalLinksForm = ref<InstanceType<typeof EntityExternalLinksForm>>()

const metadataInvalid = computed(() => metadataForm.value?.v$.$error ?? false)
const contributorsInvalid = computed(() => contributorsForm.value?.v$.$error ?? false)
const coverArtInvalid = computed(() => coverArtForm.value?.v$.$error ?? false)
const organizationInvalid = computed(() => organizationForm.value?.v$.$error ?? false)
const externalLinksInvalid = computed(() => externalLinksForm.value?.v$.$error ?? false)

const tabs = [
  { key: '0', text: 'books.metadata' },
  { key: '1', text: 'entities.book-contributors' },
  { key: '2', text: 'books.cover-art' },
  { key: '3', text: 'books.organization' },
  { key: '4', text: 'external-links.title' },
]

const invalidTabs = computed(() => [
  metadataInvalid.value,
  contributorsInvalid.value,
  coverArtInvalid.value,
  organizationInvalid.value,
  externalLinksInvalid.value,
])

interface CustomBookUpdate extends Omit<BookUpdate, 'links' | 'dimensions' | 'pageCount' | 'labelPrice' | 'paidPrice' | 'weightKg'> {
  dimensions: DimensionsString
  labelPrice: MonetaryAmountString
  paidPrice: MonetaryAmountString
  pageCount: string
  weightKg: string
  links: FormExternalLink[]
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
  weightKg: '0',
  links: [],
})

const initialBookToEdit = ref('')

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
    collection: getRelationship(loadedBook, 'COLLECTION')!.id,
    contributors: (getRelationships(loadedBook, 'CONTRIBUTOR') ?? [])
      .map(c => ({
        person: c.attributes!.person.id,
        role: c.attributes!.role.id,
      })),
    isInLibrary: loadedBook.attributes.isInLibrary,
    publishers: (getRelationships(loadedBook, 'PUBLISHER') ?? []).map(p => p.id),
    tags: (getRelationships(loadedBook, 'TAG') ?? []).map(t => t.id),
    series: getRelationship(loadedBook, 'SERIES')?.id ?? null,
    store: getRelationship(loadedBook, 'STORE')?.id ?? null,
    weightKg: String(loadedBook.attributes.weightKg),
    links: Object.entries(loadedBook.attributes.links)
      .filter(([_, url]) => url !== null && url.length > 0)
      .map(([type, url]) => ({ type, url })),
  } satisfies CustomBookUpdate)

  initialBookToEdit.value = JSON.stringify(toRaw(updatedBook))
}, { immediate: true })

const coverArt = ref<CoverArt>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  if (isLoading.value || !book.value) {
    return ''
  }

  return updatedBook.title.length > 0 ? updatedBook.title : book.value.attributes.title
})

function nullOrNotBlank(value: string | null | undefined): string | null {
  return (value && value.length > 0) ? value : null
}

function validNumber(valueStr: string): number {
  const value = Number(valueStr.replace(',', '.'))
  return isNaN(value) ? 0 : value
}

async function handleSubmit() {
  const isValidMetadata = await metadataForm.value!.v$.$validate()
  const isValidContributors = await contributorsForm.value!.v$.$validate()
  const isValidCoverArt = await coverArtForm.value!.v$.$validate()
  const isValidOrganization = await organizationForm.value!.v$.$validate()
  const isValidExternalLinks = await externalLinksForm.value!.v$.$validate()

  if (
    !isValidMetadata
    || !isValidContributors
    || !isValidOrganization
    || !isValidCoverArt
    || !isValidExternalLinks
  ) {
    return
  }

  const editedBook: BookUpdate = {
    ...toRaw(updatedBook),
    barcode: nullOrNotBlank(updatedBook.barcode),
    boughtAt: nullOrNotBlank(updatedBook.boughtAt),
    billedAt: nullOrNotBlank(updatedBook.billedAt),
    arrivedAt: nullOrNotBlank(updatedBook.arrivedAt),
    pageCount: validNumber(updatedBook.pageCount),
    weightKg: validNumber(updatedBook.weightKg),
    labelPrice: {
      amount: validNumber(updatedBook.labelPrice.amount),
      currency: updatedBook.labelPrice.currency,
    },
    paidPrice: {
      amount: validNumber(updatedBook.paidPrice.amount),
      currency: updatedBook.paidPrice.currency,
    },
    dimensions: {
      widthCm: validNumber(updatedBook.dimensions.widthCm),
      heightCm: validNumber(updatedBook.dimensions.heightCm),
    },
    links: Object.assign(
      {
        amazon: null,
        openLibrary: null,
        skoob: null,
        goodreads: null,
        guiaDosQuadrinhos: null,
      } satisfies BookLinks,
      Object.fromEntries(
        updatedBook.links.map(l => [l.type, nullOrNotBlank(l.url)]),
      ),
    ),
  }

  try {
    await editBook(editedBook)

    if (coverArt.value.file) {
      await uploadCover({ bookId: updatedBook.id, cover: coverArt.value.file })
    } else if (coverArt.value.removeExisting) {
      await deleteCover(updatedBook.id)
    }

    await router.replace({ name: 'books-id', params: { id: updatedBook.id } })
    await notificator.success({ title: t('books.edited-with-success') })
  } catch (error) {
    await notificator.failure({
      title: t('books.edited-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

const bookWasModified = ref(false)

watch(updatedBook, (newUpdatedBook) => {
  bookWasModified.value = initialBookToEdit.value !== JSON.stringify(toRaw(newUpdatedBook))
})

useBeforeUnload({ enabled: bookWasModified })

const bookCover = computed(() => getRelationship(book.value, 'COVER_ART'))
</script>

<template>
  <form autocomplete="off" novalidate @submit.prevent="handleSubmit">
    <TabGroup :selected-index="Number(activeTab.key)" @change="activeTab = tabs[$event]">
      <Header
        :title="headerTitle"
        :loading="isLoading"
      >
        <template #avatar>
          <Button
            class="aspect-1 w-10 h-10 -ml-2"
            size="mini"
            kind="ghost"
            rounded="full"
            :title="$t('common-actions.back')"
            :disabled="isEditing"
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
            type="submit"
            :disabled="isLoading || isEditing"
            :loading="isEditing"
          >
            <CheckIcon class="w-5 h-5" />
            <span>{{ $t('common-actions.save') }}</span>
          </Button>
        </template>
        <template #tabs>
          <TabList class="hidden md:flex gap-3 -mb-px">
            <Tab
              v-for="tab in tabs"
              :key="tab.key"
              v-slot="{ selected }"
              :disabled="isLoading || isEditing"
              as="template"
            >
              <Button
                kind="underline-tab"
                size="underline-tab"
                rounded="none"
                :data-headlessui-state="selected ? 'selected' : undefined"
              >
                <span>{{ $t(tab.text) }}</span>
                <div
                  v-if="invalidTabs[Number(tab.key)]"
                  class="ml-2 mt-0.5 relative"
                >
                  <span class="absolute inset-1 rounded-full bg-white" />
                  <ExclamationCircleIcon
                    class="relative w-4 h-4 text-red-600 dark:text-red-500"
                  />
                </div>
              </Button>
            </Tab>
          </TabList>
          <BasicSelect
            id="tabs"
            v-model="activeTab"
            class="md:hidden mb-4"
            :disabled="isLoading || isEditing"
            :options="tabs"
            :option-text="tab => $t(tab.text)"
            :option-value="tab => tab.key"
          />
        </template>
      </Header>
      <div class="max-w-7xl mx-auto p-4 sm:p-6">
        <TabPanels>
          <TabPanel :unmount="false">
            <BookMetadataForm
              ref="metadataForm"
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
              v-model:series="updatedBook.series"
              v-model:publishers="updatedBook.publishers"
              v-model:weight="updatedBook.weightKg"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <BookContributorsForm
              ref="contributorsForm"
              v-model:contributors="updatedBook.contributors"
              :loading="isLoading"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <BookCoverArtForm
              ref="coverArtForm"
              v-model:cover-art="coverArt"
              :current-image-url="
                createImageUrl({
                  fileName: bookCover?.attributes?.versions?.['256'],
                  timeHex: bookCover?.attributes?.timeHex,
                })
              "
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <BookOrganizationForm
              ref="organizationForm"
              v-model:notes="updatedBook.notes"
              v-model:bought-at="updatedBook.boughtAt"
              v-model:billed-at="updatedBook.billedAt"
              v-model:arrived-at="updatedBook.arrivedAt"
              v-model:label-price="updatedBook.labelPrice"
              v-model:paid-price="updatedBook.paidPrice"
              v-model:store="updatedBook.store"
              v-model:collection="updatedBook.collection"
              v-model:tags="updatedBook.tags"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <EntityExternalLinksForm
              ref="externalLinksForm"
              v-model:external-links="updatedBook.links"
              :types="['amazon', 'openLibrary', 'skoob', 'goodreads', 'guiaDosQuadrinhos']"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
        </TabPanels>
      </div>
    </TabGroup>
  </form>
</template>

<route lang="yaml">
meta:
  layout: dashboard
</route>
