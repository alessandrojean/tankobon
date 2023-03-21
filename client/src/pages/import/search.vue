<script lang="ts" setup>
import { isbn as isValidIsbn } from '@/utils/validation'
import { ImporterSources, ImportOneBook } from '@/types/tankobon-importer-source'
import { MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import useVuelidate from '@vuelidate/core'
import { helpers } from '@vuelidate/validators'
import { ExternalBookEntity } from '@/types/tankobon-external-book'
import { FunnelIcon } from '@heroicons/vue/20/solid'

const { t } = useI18n()
const router = useRouter()
const notificator = useNotificator()

const isbn = ref('')
const isbnDebounced = refDebounced(isbn, 1_000)
const sources = ref<ImporterSources[] | undefined>()

const rules = {
  isbn: {
    isbn: helpers.withMessage(t('validation.isbn'), isValidIsbn),
  }
}

const v$ = useVuelidate(rules, { isbn })

watch(isbn, (value) => {
  if (value.length < 10) {
    v$.value.$reset()
    bookToImport.value = undefined
  } else {
    v$.value.$touch()
  }
})

const { data: results, isFetching } = useImporterSearchQuery({
  isbn: isbnDebounced,
  sources,
  includes: ['importer_source'],
  enabled: computed(() => {
    return isbnDebounced.value.length > 0 && !v$.value.$invalid
  }),
  onError: async (error) => {
    await notificator.failure({
      title: t('importer.fetch-failure'),
      body: error.message,
    })
  }
})

const showCollectionChooserDialog = ref(false)
const bookToImport = ref<ExternalBookEntity | undefined>()

const { mutate: importBook, isLoading: isImporting } = useImportBookMutation()

function handleResultImportClick(book: ExternalBookEntity) {
  bookToImport.value = book
  showCollectionChooserDialog.value = true
}

function handleImportBook(book: ImportOneBook) {
  importBook(book, {
    onSuccess: async (created) => {
      notificator.success({ title: t('importer.imported-with-success') })
      await router.push({ name: 'books-id', params: { id: created.id } })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('importer.imported-with-failure'),
        body: error.message,
      })
    }
  })
}
</script>

<route lang="yaml">
  meta:
    layout: dashboard
</route>

<template>
  <div>
    <Header :title="$t('importer.header')" />

    <div class="max-w-7xl mx-auto p-4 sm:p-6 space-y-10">
      <div class="bg-gray-100 dark:bg-gray-800 rounded-lg p-4 flex gap-2">
        <div class="max-w-xs w-full">
          <label for="isbn" class="sr-only">
            {{ $t('common-fields.isbn') }}
          </label>
          <BasicTextInput
            class="text-lg tabular-nums"
            v-model="isbn"
            type="search"
            input-mode="numeric"
            id="isbn"
            :disabled="isFetching || isImporting"
            :placeholder="$t('common-placeholders.search-by-isbn')"
            :errors="v$.isbn.$errors"
            :invalid="v$.isbn.$error"
          >
            <template #left-icon>
              <MagnifyingGlassIcon class="w-5 h-5" />
            </template>

            <template #right-icon v-if="isFetching">
              <LoadingSpinIcon class="w-5 h-5 text-primary-500 animate-spin" />
            </template>
          </BasicTextInput>

          <p
            v-if="v$.isbn.$error && v$.isbn.$errors[0]?.$message"
            class="text-red-700 dark:text-red-500/95 text-sm font-medium ml-2 mt-1 mb-4"
          >
            {{ v$.isbn.$errors[0]?.$message }}
          </p>
        </div>

        <Button
          kind="ghost-alt"
          :title="$t('common-actions.filter')"
        >
          <span class="sr-only">{{ $t('common-actions.filter') }}</span>
          <FunnelIcon class="w-5 h-5" />
        </Button>
      </div>

      <div v-if="results && results.length > 0">
        <h2 class="font-display font-medium text-xl">
          {{ $t('importer.results') }}
        </h2>

        <div class="space-y-8 mt-4 sm:mt-6">
          <ImporterResultCard
            v-for="result in results"
            :key="result.id + result.attributes.isbn"
            :result="result"
            @click:import="handleResultImportClick(result)"
          />
        </div>
      </div>
    </div>

    <ImporterCollectionChooserDialog
      v-if="bookToImport"
      :is-open="showCollectionChooserDialog"
      :external-book="bookToImport"
      @close="showCollectionChooserDialog = false"
      @submit="handleImportBook"
    />
  </div>
</template>
