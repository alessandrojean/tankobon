<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import type { BookUpdate } from '@/types/tankobon-book'
import type { DimensionsString } from '@/types/tankobon-dimensions'
import type { MonetaryAmountString } from '@/types/tankobon-monetary'
import BookMetadataForm from '@/components/books/BookMetadataForm.vue'
import BookOrganizationForm from '@/components/books/BookOrganizationForm.vue'
import { getRelationship, getRelationships } from '@/utils/api'
import BookContributorsForm from '@/components/books/BookContributorsForm.vue'
import BookRelationshipsForm from '@/components/books/BookRelationshipsForm.vue'

const { t, n } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const bookId = computed(() => route.params.id as string)

const { mutate: editBook, isLoading: isEditing } = useUpdateBookMutation()

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

const metadataForm = ref<InstanceType<typeof BookMetadataForm>>()
const contributorsForm = ref<InstanceType<typeof BookContributorsForm>>()
const relationshipsForm = ref<InstanceType<typeof BookRelationshipsForm>>()
const organizationForm = ref<InstanceType<typeof BookOrganizationForm>>()

const metadataInvalid = computed(() => metadataForm.value?.v$.$error ?? false)
const contributorsInvalid = computed(() => contributorsForm.value?.v$.$error ?? false)
const relationshipsInvalid = computed(() => relationshipsForm.value?.v$.$error ?? false)
const organizationInvalid = computed(() => organizationForm.value?.v$.$error ?? false)

const tabs = [
  { key: '0', text: 'books.metadata' },
  { key: '1', text: 'entities.book-contributors' },
  { key: '2', text: 'books.relationships' },
  { key: '3', text: 'books.cover-art' },
  { key: '4', text: 'books.organization' },
]

const invalidTabs = computed(() => [
  metadataInvalid.value,
  contributorsInvalid.value,
  relationshipsInvalid.value,
  false,
  organizationInvalid.value,
])

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
  } satisfies CustomBookUpdate)

  initialBookToEdit.value = JSON.stringify(toRaw(updatedBook))
}, { immediate: true })

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
  const isValidRelationships = await relationshipsForm.value!.v$.$validate()
  const isValidOrganization = await organizationForm.value!.v$.$validate()

  if (
    !isValidMetadata
    || !isValidContributors
    || !isValidRelationships
    || !isValidOrganization
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
  }

  editBook(editedBook, {
    onSuccess: async () => {
      await router.replace({ name: 'books-id', params: { id: updatedBook.id } })
      await notificator.success({ title: t('books.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('books.edited-with-failure'),
        body: error.message,
      })
    },
  })
}

const bookWasModified = ref(false)

watch(updatedBook, (newUpdatedBook) => {
  bookWasModified.value = initialBookToEdit.value !== JSON.stringify(toRaw(newUpdatedBook))
})

useBeforeUnload({ enabled: bookWasModified })
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
            <BookRelationshipsForm
              ref="relationshipsForm"
              v-model:publishers="updatedBook.publishers"
              v-model:tags="updatedBook.tags"
              :loading="isLoading"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel>Cover art</TabPanel>
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
