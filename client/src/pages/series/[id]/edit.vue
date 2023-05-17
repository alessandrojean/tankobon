<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import { v4 as uuid } from 'uuid'
import { getRelationship } from '@/utils/api'
import { createImageUrl } from '@/modules/api'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { SeriesUpdate } from '@/types/tankobon-series'
import type { Cover } from '@/components/series/SeriesCoverForm.vue'
import SeriesCoverForm from '@/components/series/SeriesCoverForm.vue'
import SeriesMetadataForm from '@/components/series/SeriesMetadataForm.vue'
import SeriesAlternativeNamesForm from '@/components/series/SeriesAlternativeNamesForm.vue'
import type { FormAlternativeName } from '@/types/tankobon-alternative-name'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const seriesId = computed(() => route.params.id as string)

const { mutateAsync: editSeries, isLoading: isEditingSeries } = useUpdateSeriesMutation()
const { mutateAsync: uploadCover, isLoading: isUploadingCover } = useUploadSeriesCoverMutation()
const { mutateAsync: deleteCover, isLoading: isDeletingCover } = useDeleteSeriesCoverMutation()

const isEditing = logicOr(isEditingSeries, isUploadingCover, isDeletingCover)

const { data: series, isLoading } = useSeriesQuery({
  seriesId,
  includes: ['series_cover'],
  enabled: computed(() => !!seriesId.value),
  refetchOnWindowFocus: false,
  refetchOnReconnect: false,
  staleTime: Infinity,
  onError: async (error) => {
    await notificator.failure({
      title: t('series.fetch-one-failure'),
      body: error.message,
    })
  },
})

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

interface CustomSeriesUpdate extends Omit<SeriesUpdate, 'alternativeNames'> {
  alternativeNames: FormAlternativeName[]
}

const updatedSeries = reactive<CustomSeriesUpdate>({
  id: '',
  name: '',
  description: '',
  type: null,
  alternativeNames: [],
  lastNumber: null,
  originalLanguage: null,
})

const initialSeriesToEdit = ref('')

whenever(series, (loadedSeries) => {
  Object.assign(updatedSeries, {
    id: loadedSeries.id,
    name: loadedSeries.attributes.name,
    description: loadedSeries.attributes.description,
    type: loadedSeries.attributes.type,
    alternativeNames: loadedSeries.attributes.alternativeNames.map(an => ({
      id: uuid(),
      name: an.name,
      language: an.language,
    })),
    lastNumber: loadedSeries.attributes.lastNumber,
    originalLanguage: loadedSeries.attributes.originalLanguage,
  } satisfies CustomSeriesUpdate)

  initialSeriesToEdit.value = JSON.stringify(toRaw(updatedSeries))
}, { immediate: true })

const cover = ref<Cover>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  if (isLoading.value || !series.value) {
    return ''
  }

  return updatedSeries.name.length > 0 ? updatedSeries.name : series.value.attributes.name
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

  const editedSeries: SeriesUpdate = {
    ...toRaw(updatedSeries),
    lastNumber: nullOrNotBlank(updatedSeries.lastNumber),
    originalLanguage: nullOrNotBlank(updatedSeries.originalLanguage),
    alternativeNames: updatedSeries.alternativeNames
      .filter(fan => fan.language !== 'null' && fan.name.length > 0)
      .map(fan => ({
        name: fan.name,
        language: fan.language,
      })),
  }

  try {
    await editSeries(editedSeries)

    if (cover.value.file) {
      await uploadCover({ seriesId: updatedSeries.id, cover: cover.value.file })
    } else if (cover.value.removeExisting) {
      await deleteCover(updatedSeries.id)
    }

    await router.replace({ name: 'series-id', params: { id: updatedSeries.id } })
    await notificator.success({ title: t('series.edited-with-success') })
  } catch (error) {
    await notificator.failure({
      title: t('series.edited-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

const seriesWasModified = ref(false)

watch(updatedSeries, (newUpdatedSeries) => {
  seriesWasModified.value = initialSeriesToEdit.value !== JSON.stringify(toRaw(newUpdatedSeries))
})

useBeforeUnload({ enabled: seriesWasModified })

const seriesCover = computed(() => getRelationship(series.value, 'SERIES_COVER'))
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
            v-model="activeTab"
            class="md:hidden mb-4"
            :disabled="isLoading || isEditing"
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
              v-model:name="updatedSeries.name"
              v-model:description="updatedSeries.description"
              v-model:type="updatedSeries.type"
              v-model:original-language="updatedSeries.originalLanguage"
              v-model:alternative-names="updatedSeries.alternativeNames"
              v-model:last-number="updatedSeries.lastNumber"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <SeriesAlternativeNamesForm
              ref="alternativeNamesForm"
              v-model:alternative-names="updatedSeries.alternativeNames"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <SeriesCoverForm
              ref="coverForm"
              v-model:cover="cover"
              :current-image-url="
                createImageUrl({
                  fileName: seriesCover?.attributes?.versions?.['256'],
                  timeHex: seriesCover?.attributes?.timeHex,
                })
              "
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
