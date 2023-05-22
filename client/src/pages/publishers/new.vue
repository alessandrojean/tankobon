<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { Picture } from '@/components/publishers/PublisherPictureForm.vue'
import PublisherPictureForm from '@/components/publishers/PublisherPictureForm.vue'
import PublisherMetadataForm from '@/components/publishers/PublisherMetadataForm.vue'
import type { FormExternalLink } from '@/types/tankobon-external-link'
import EntityExternalLinksForm from '@/components/entity/EntityExternalLinksForm.vue'
import type { PublisherCreation, PublisherLinks } from '@/types/tankobon-publisher'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const libraryStory = useLibraryStore()
const libraryId = computed(() => libraryStory.library!.id)

const { mutateAsync: createPublisher, isLoading: isCreatingPublisher } = useCreatePublisherMutation()
const { mutateAsync: uploadPicture, isLoading: isUploadingPicture } = useUploadPublisherPictureMutation()

const isCreating = logicOr(isCreatingPublisher, isUploadingPicture)

const metadataForm = ref<InstanceType<typeof PublisherMetadataForm>>()
const externalLinksForm = ref<InstanceType<typeof EntityExternalLinksForm>>()
const pictureForm = ref<InstanceType<typeof PublisherPictureForm>>()

const metadataInvalid = computed(() => metadataForm.value?.v$.$error ?? false)
const externalLinksInvalid = computed(() => externalLinksForm.value?.v$.$error ?? false)
const pictureInvalid = computed(() => pictureForm.value?.v$.$error ?? false)

const tabs = [
  { key: '0', text: 'series.metadata' },
  { key: '1', text: 'external-links.title' },
  { key: '2', text: 'publishers.picture' },
]

const invalidTabs = computed(() => [
  metadataInvalid.value,
  externalLinksInvalid.value,
  pictureInvalid.value,
])

interface CustomPublisherCreation extends Omit<PublisherCreation, 'links' | 'foundingYear' | 'dissolutionYear'> {
  links: FormExternalLink[]
  foundingYear: string | null
  dissolutionYear: string | null
}

const newPublisher = reactive<CustomPublisherCreation>({
  library: libraryId.value,
  name: '',
  description: '',
  links: [],
  legalName: '',
  location: null,
  foundingYear: null,
  dissolutionYear: null,
})

const picture = ref<Picture>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  return newPublisher.name.length > 0 ? newPublisher.name : t('publishers.new')
})

function nullOrNotBlank(value: string | null | undefined): string | null {
  return (value && value.length > 0) ? value : null
}

function validNumber(valueStr: string | null): number | null {
  const value = valueStr?.length ? Number(valueStr.replace(',', '.') ?? 'NaN') : NaN
  return isNaN(value) ? null : value
}

async function handleSubmit() {
  const isValidMetadata = await metadataForm.value!.v$.$validate()
  const isValidExternalLinks = await externalLinksForm.value!.v$.$validate()
  const isValidPicture = await pictureForm.value!.v$.$validate()

  if (!isValidMetadata || !isValidExternalLinks || !isValidPicture) {
    return
  }

  const publisherToCreate: PublisherCreation = {
    ...toRaw(newPublisher),
    foundingYear: validNumber(newPublisher.foundingYear),
    dissolutionYear: validNumber(newPublisher.dissolutionYear),
    links: Object.assign(
      {
        website: null,
        store: null,
        twitter: null,
        instagram: null,
        facebook: null,
        youTube: null,
      } satisfies PublisherLinks,
      Object.fromEntries(
        newPublisher.links.map(l => [l.type, nullOrNotBlank(l.url)]),
      ),
    ),
  }

  try {
    const { id } = await createPublisher(publisherToCreate)

    if (picture.value.file) {
      await uploadPicture({ publisherId: id, picture: picture.value.file })
    }

    await router.replace({ name: 'publishers-id', params: { id } })
    await notificator.success({ title: t('publishers.created-with-success') })
  } catch (error) {
    await notificator.failure({
      title: t('publishers.created-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

useBeforeUnload({
  enabled: computed(() => route.name === 'publishers-new'),
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
            id="tabs"
            v-model="activeTab"
            class="md:hidden mb-4"
            :disabled="isCreating"
            :options="tabs"
            :option-text="tab => $t(tab.text)"
            :option-value="tab => tab.key"
          />
        </template>
      </Header>
      <div class="max-w-7xl mx-auto p-4 sm:p-6">
        <TabPanels>
          <TabPanel :unmount="false">
            <PublisherMetadataForm
              ref="metadataForm"
              v-model:name="newPublisher.name"
              v-model:description="newPublisher.description"
              v-model:legal-name="newPublisher.legalName"
              v-model:location="newPublisher.location"
              v-model:founding-year="newPublisher.foundingYear"
              v-model:dissolution-year="newPublisher.dissolutionYear"
              :disabled="isCreating"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <EntityExternalLinksForm
              ref="externalLinksForm"
              v-model:external-links="newPublisher.links"
              :types="['website', 'store', 'twitter', 'instagram', 'facebook', 'youTube']"
              :disabled="isCreating"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <PublisherPictureForm
              ref="pictureForm"
              v-model:picture="picture"
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
