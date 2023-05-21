<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import { getRelationship } from '@/utils/api'
import { createImageUrl } from '@/modules/api'
import type { TankobonApiError } from '@/types/tankobon-response'
import PublisherPictureForm, { Picture } from '@/components/publishers/PublisherPictureForm.vue'
import PublisherMetadataForm from '@/components/publishers/PublisherMetadataForm.vue'
import { FormExternalLink } from '@/types/tankobon-external-link'
import EntityExternalLinksForm from '@/components/entity/EntityExternalLinksForm.vue'
import { PublisherUpdate, PublisherLinks } from '@/types/tankobon-publisher'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const publisherId = computed(() => route.params.id as string)

const { mutateAsync: editPublisher, isLoading: isEditingPublisher } = useUpdatePublisherMutation()
const { mutateAsync: uploadPicture, isLoading: isUploadingPicture } = useUploadPublisherPictureMutation()
const { mutateAsync: deletePicture, isLoading: isDeletingPicture } = useDeletePublisherPictureMutation()

const isEditing = logicOr(isEditingPublisher, isUploadingPicture, isDeletingPicture)

const { data: publisher, isLoading } = usePublisherQuery({
  publisherId,
  includes: ['publisher_picture'],
  enabled: computed(() => !!publisherId.value),
  refetchOnWindowFocus: false,
  refetchOnReconnect: false,
  staleTime: Infinity,
  onError: async (error) => {
    await notificator.failure({
      title: t('publishers.fetch-one-failure'),
      body: error.message,
    })
  },
})

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

interface CustomPublisherUpdate extends Omit<PublisherUpdate, 'links'> {
  links: FormExternalLink[]
}

const updatedPublisher = reactive<CustomPublisherUpdate>({
  id: '',
  name: '',
  description: '',
  links: [],
  legalName: '',
  location: null,
})

const initialPublisherToEdit = ref('')

whenever(publisher, (loadedPublisher) => {
  Object.assign(updatedPublisher, {
    id: loadedPublisher.id,
    name: loadedPublisher.attributes.name,
    description: loadedPublisher.attributes.description,
    links: Object.entries(loadedPublisher.attributes.links)
      .filter(([_, url]) => url !== null && url.length > 0)
      .map(([type, url]) => ({ type, url })),
    legalName: loadedPublisher.attributes.legalName,
    location: loadedPublisher.attributes.location,
  } satisfies CustomPublisherUpdate)

  initialPublisherToEdit.value = JSON.stringify(toRaw(updatedPublisher))
}, { immediate: true })

const picture = ref<Picture>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  if (isLoading.value || !publisher.value) {
    return ''
  }

  return updatedPublisher.name.length > 0 ? updatedPublisher.name : publisher.value.attributes.name
})

function nullOrNotBlank(value: string | null | undefined): string | null {
  return (value && value.length > 0) ? value : null
}

async function handleSubmit() {
  const isValidMetadata = await metadataForm.value!.v$.$validate()
  const isValidExternalLinks = await externalLinksForm.value!.v$.$validate()
  const isValidPicture = await pictureForm.value!.v$.$validate()

  if (!isValidMetadata || !isValidExternalLinks || !isValidPicture) {
    return
  }

  const editedPublisher: PublisherUpdate = {
    ...toRaw(updatedPublisher),
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
        updatedPublisher.links.map(l => [l.type, nullOrNotBlank(l.url)])
      )
    ),
  }

  try {
    await editPublisher(editedPublisher)

    if (picture.value.file) {
      await uploadPicture({ publisherId: updatedPublisher.id, picture: picture.value.file })
    } else if (picture.value.removeExisting) {
      await deletePicture(updatedPublisher.id)
    }

    await router.replace({ name: 'publishers-id', params: { id: updatedPublisher.id } })
    await notificator.success({ title: t('publishers.edited-with-success') })
  } catch (error) {
    await notificator.failure({
      title: t('publishers.edited-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

const publisherWasModified = ref(false)

watch(updatedPublisher, (newUpdatedPublisher) => {
  publisherWasModified.value = initialPublisherToEdit.value !== JSON.stringify(toRaw(newUpdatedPublisher))
})

useBeforeUnload({ enabled: publisherWasModified })

const publisherPicture = computed(() => getRelationship(publisher.value, 'PUBLISHER_PICTURE'))
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
            id="tabs"
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
            <PublisherMetadataForm
              ref="metadataForm"
              v-model:name="updatedPublisher.name"
              v-model:description="updatedPublisher.description"
              v-model:legal-name="updatedPublisher.legalName"
              v-model:location="updatedPublisher.location"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <EntityExternalLinksForm
              ref="externalLinksForm"
              v-model:external-links="updatedPublisher.links"
              :types="['website', 'store', 'twitter', 'instagram', 'facebook', 'youTube']"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <PublisherPictureForm
              ref="pictureForm"
              v-model:picture="picture"
              :current-image-url="
                createImageUrl({
                  fileName: publisherPicture?.attributes?.versions?.['256'],
                  timeHex: publisherPicture?.attributes?.timeHex,
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
