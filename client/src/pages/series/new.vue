<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { SeriesCreation } from '@/types/tankobon-series'
import type { Cover } from '@/components/series/SeriesCoverForm.vue'
import SeriesCoverForm from '@/components/series/SeriesCoverForm.vue'
import SeriesMetadataForm from '@/components/series/SeriesMetadataForm.vue'
import SeriesAlternativeNamesForm from '@/components/series/SeriesAlternativeNamesForm.vue'
import type { FormAlternativeName } from '@/types/tankobon-alternative-name'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const libraryStory = useLibraryStore()
const libraryId = computed(() => libraryStory.library!.id)

const { mutateAsync: createSeries, isLoading: isCreatingSeries } = useCreateSeriesMutation()
const { mutateAsync: uploadCover, isLoading: isUploadingCover } = useUploadSeriesCoverMutation()

const isCreating = logicOr(isCreatingSeries, isUploadingCover)

const metadataForm = ref<InstanceType<typeof SeriesMetadataForm>>()
const alternativeNamesForm = ref<InstanceType<typeof SeriesAlternativeNamesForm>>()
const coverForm = ref<InstanceType<typeof SeriesCoverForm>>()

const metadataInvalid = computed(() => metadataForm.value?.v$.$error ?? false)
const alternativeNamesInvalid = computed(() => alternativeNamesForm.value?.v$.$error ?? false)
const coverInvalid = computed(() => coverForm.value?.v$.$error ?? false)

const tabs = [
  { key: '0', text: 'series.metadata' },
  { key: '1', text: 'alternative-names.title' },
  { key: '2', text: 'series.cover' },
]

const invalidTabs = computed(() => [
  metadataInvalid.value,
  alternativeNamesInvalid.value,
  coverInvalid.value,
])

interface CustomSeriesUpdate extends Omit<SeriesCreation, 'alternativeNames'> {
  alternativeNames: FormAlternativeName[]
}

const newSeries = reactive<CustomSeriesUpdate>({
  library: libraryId.value,
  name: '',
  description: '',
  type: null,
  alternativeNames: [],
  lastNumber: null,
  originalLanguage: null,
})

whenever(libraryId, libraryId => newSeries.library = libraryId)

const cover = ref<Cover>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  return newSeries.name.length > 0 ? newSeries.name : t('series.new')
})

function nullOrNotBlank(value: string | null | undefined): string | null {
  return (value && value.length > 0) ? value : null
}

async function handleSubmit() {
  const isValidMetadata = await metadataForm.value!.v$.$validate()
  const isValidAlternativeNames = await alternativeNamesForm.value!.v$.$validate()
  const isValidCover = await coverForm.value!.v$.$validate()

  if (!isValidMetadata || !isValidAlternativeNames || !isValidCover) {
    return
  }

  const seriesToCreate: SeriesCreation = {
    ...toRaw(newSeries),
    lastNumber: nullOrNotBlank(newSeries.lastNumber),
    originalLanguage: nullOrNotBlank(newSeries.originalLanguage),
    alternativeNames: newSeries.alternativeNames
      .filter(fan => fan.language !== 'null' && fan.name.length > 0)
      .map(fan => ({
        name: fan.name,
        language: fan.language,
      })),
  }

  try {
    const { id } = await createSeries(seriesToCreate)

    if (cover.value.file) {
      await uploadCover({ seriesId: id, cover: cover.value.file })
    }

    notificator.success({ title: t('series.created-with-success') })
    await router.replace({ name: 'series-id', params: { id } })
  } catch (error) {
    await notificator.failure({
      title: t('series.created-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

useBeforeUnload({
  enabled: computed(() => route.name === 'series-new'),
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
              :disabled="isCreating"
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
            <SeriesMetadataForm
              ref="metadataForm"
              v-model:name="newSeries.name"
              v-model:description="newSeries.description"
              v-model:type="newSeries.type"
              v-model:original-language="newSeries.originalLanguage"
              v-model:alternative-names="newSeries.alternativeNames"
              v-model:last-number="newSeries.lastNumber"
              :disabled="isCreating"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <SeriesAlternativeNamesForm
              ref="alternativeNamesForm"
              v-model:alternative-names="newSeries.alternativeNames"
              :disabled="isCreating"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <SeriesCoverForm
              ref="coverForm"
              v-model:cover="cover"
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
