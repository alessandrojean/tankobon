<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import type { BookLinks, BookUpdate } from '@/types/tankobon-book'
import type { DimensionsString } from '@/types/tankobon-dimensions'
import type { MonetaryAmountString } from '@/types/tankobon-monetary'
import BookMetadataForm from '@/components/books/BookMetadataForm.vue'
import BookOrganizationForm from '@/components/books/BookOrganizationForm.vue'
import BookContributorsForm from '@/components/books/BookContributorsForm.vue'
import type { CoverArt } from '@/components/books/BookCoverArtForm.vue'
import BookCoverArtForm from '@/components/books/BookCoverArtForm.vue'
import type { TankobonApiError } from '@/types/tankobon-response'
import EntityExternalLinksForm from '@/components/entity/EntityExternalLinksForm.vue'
import { FormExternalLink } from '@/types/tankobon-external-link'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const notificator = useToaster()

const { mutateAsync: createBook, isLoading: isCreatingBook } = useCreateBookMutation()
const { mutateAsync: uploadCover, isLoading: isUploadingCover } = useUploadBookCoverMutation()

const isCreating = logicOr(isCreatingBook, isUploadingCover)

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

const newBook = reactive<CustomBookUpdate>({
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

const coverArt = ref<CoverArt>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  return newBook.title.length > 0 ? newBook.title : t('books.new')
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

  const bookToCreate: BookUpdate = {
    ...toRaw(newBook),
    barcode: nullOrNotBlank(newBook.barcode),
    boughtAt: nullOrNotBlank(newBook.boughtAt),
    billedAt: nullOrNotBlank(newBook.billedAt),
    arrivedAt: nullOrNotBlank(newBook.arrivedAt),
    pageCount: validNumber(newBook.pageCount),
    weightKg: validNumber(newBook.weightKg),
    labelPrice: {
      amount: validNumber(newBook.labelPrice.amount),
      currency: newBook.labelPrice.currency,
    },
    paidPrice: {
      amount: validNumber(newBook.paidPrice.amount),
      currency: newBook.paidPrice.currency,
    },
    dimensions: {
      widthCm: validNumber(newBook.dimensions.widthCm),
      heightCm: validNumber(newBook.dimensions.heightCm),
    },
    links: Object.assign(
      { 
        amazon: null, 
        openLibrary: null,
        skoob: null,
        goodreads: null,
      } satisfies BookLinks,
      Object.fromEntries(
        newBook.links.map(l => [l.type, nullOrNotBlank(l.url)])
      )
    ),
  }

  try {
    const { id } = await createBook(bookToCreate)

    if (coverArt.value.file) {
      await uploadCover({ bookId: id, cover: coverArt.value.file })
    }

    notificator.success({ title: t('books.created-with-success') })
    await router.replace({ name: 'books-id', params: { id } })
  } catch (error) {
    await notificator.failure({
      title: t('books.created-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

useBeforeUnload({
  enabled: computed(() => route.name === 'books-new'),
})
</script>

<template>
  <form autocomplete="off" novalidate @submit.prevent="handleSubmit">
    <TabGroup :selected-index="Number(activeTab.key)" @change="activeTab = tabs[$event]">
      <Header :title="headerTitle">
        <template #avatar>
          <Button
            class="aspect-1 w-10 h-10 -ml-2"
            size="mini"
            kind="ghost"
            rounded="full"
            :title="$t('common-actions.back')"
            :disabled="isCreating"
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
            :disabled="isCreating"
            :loading="isCreating"
          >
            <CheckIcon class="w-5 h-5" />
            <span>{{ $t('common-actions.create') }}</span>
          </Button>
        </template>
        <template #tabs>
          <TabList class="hidden md:flex gap-3 -mb-px">
            <Tab
              v-for="tab in tabs"
              :key="tab.key"
              v-slot="{ selected }"
              as="template"
              :disabled="isCreating"
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
            v-model="activeTab"
            class="md:hidden mb-4"
            :disabled="isCreating"
            :options="tabs"
            :option-text="(tab: any) => $t(tab.text)"
            :option-value="(tab: any) => tab.key"
          />
        </template>
      </Header>
      <div class="max-w-7xl mx-auto p-4 sm:p-6">
        <TabPanels>
          <TabPanel :unmount="false">
            <BookMetadataForm
              ref="metadataForm"
              v-model:code="newBook.code"
              v-model:barcode="newBook.barcode"
              v-model:number="newBook.number"
              v-model:title="newBook.title"
              v-model:subtitle="newBook.subtitle"
              v-model:synopsis="newBook.synopsis"
              v-model:page-count="newBook.pageCount"
              v-model:notes="newBook.notes"
              v-model:bought-at="newBook.boughtAt"
              v-model:billed-at="newBook.billedAt"
              v-model:arrived-at="newBook.arrivedAt"
              v-model:dimensions="newBook.dimensions"
              v-model:series="newBook.series"
              v-model:publishers="newBook.publishers"
              v-model:weight="newBook.weightKg"
              :disabled="isCreating"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <BookContributorsForm
              ref="contributorsForm"
              v-model:contributors="newBook.contributors"
              :disabled="isCreating"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <BookCoverArtForm
              ref="coverArtForm"
              v-model:cover-art="coverArt"
              :disabled="isCreating"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <BookOrganizationForm
              ref="organizationForm"
              v-model:notes="newBook.notes"
              v-model:bought-at="newBook.boughtAt"
              v-model:billed-at="newBook.billedAt"
              v-model:arrived-at="newBook.arrivedAt"
              v-model:label-price="newBook.labelPrice"
              v-model:paid-price="newBook.paidPrice"
              v-model:store="newBook.store"
              v-model:collection="newBook.collection"
              v-model:tags="newBook.tags"
              :disabled="isCreating"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <EntityExternalLinksForm
              ref="externalLinksForm"
              v-model:external-links="newBook.links"
              :types="['amazon', 'openLibrary', 'skoob', 'goodreads']"
              :disabled="isCreating"
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
