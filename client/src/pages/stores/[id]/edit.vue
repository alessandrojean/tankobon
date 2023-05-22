<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import { getRelationship } from '@/utils/api'
import { createImageUrl } from '@/modules/api'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { FormExternalLink } from '@/types/tankobon-external-link'
import EntityExternalLinksForm from '@/components/entity/EntityExternalLinksForm.vue'
import StoreMetadataForm from '@/components/stores/StoreMetadataForm.vue'
import StorePictureForm from '@/components/stores/StorePictureForm.vue'
import type { Picture } from '@/components/stores/StorePictureForm.vue'
import type { StoreLinks, StoreUpdate } from '@/types/tankobon-store'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const storeId = computed(() => route.params.id as string)

const { mutateAsync: editStore, isLoading: isEditingStore } = useUpdateStoreMutation()
const { mutateAsync: uploadPicture, isLoading: isUploadingPicture } = useUploadStorePictureMutation()
const { mutateAsync: deletePicture, isLoading: isDeletingPicture } = useDeleteStorePictureMutation()

const isEditing = logicOr(isEditingStore, isUploadingPicture, isDeletingPicture)

const { data: store, isLoading } = useStoreQuery({
  storeId,
  includes: ['store_picture'],
  enabled: computed(() => !!storeId.value),
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

const metadataForm = ref<InstanceType<typeof StoreMetadataForm>>()
const externalLinksForm = ref<InstanceType<typeof EntityExternalLinksForm>>()
const pictureForm = ref<InstanceType<typeof StorePictureForm>>()

const metadataInvalid = computed(() => metadataForm.value?.v$.$error ?? false)
const externalLinksInvalid = computed(() => externalLinksForm.value?.v$.$error ?? false)
const pictureInvalid = computed(() => pictureForm.value?.v$.$error ?? false)

const tabs = [
  { key: '0', text: 'stores.metadata' },
  { key: '1', text: 'external-links.title' },
  { key: '2', text: 'stores.picture' },
]

const invalidTabs = computed(() => [
  metadataInvalid.value,
  externalLinksInvalid.value,
  pictureInvalid.value,
])

interface CustomStoreUpdate extends Omit<StoreUpdate, 'links'> {
  links: FormExternalLink[]
}

const updatedStore = reactive<CustomStoreUpdate>({
  id: '',
  name: '',
  description: '',
  links: [],
  legalName: '',
  location: null,
  type: null,
})

const initialStoreToEdit = ref('')

whenever(store, (loadedStore) => {
  Object.assign(updatedStore, {
    id: loadedStore.id,
    name: loadedStore.attributes.name,
    description: loadedStore.attributes.description,
    links: Object.entries(loadedStore.attributes.links)
      .filter(([_, url]) => url !== null && url.length > 0)
      .map(([type, url]) => ({ type, url })),
    legalName: loadedStore.attributes.legalName,
    location: loadedStore.attributes.location,
    type: loadedStore.attributes.type,
  } satisfies CustomStoreUpdate)

  initialStoreToEdit.value = JSON.stringify(toRaw(updatedStore))
}, { immediate: true })

const picture = ref<Picture>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  if (isLoading.value || !store.value) {
    return ''
  }

  return updatedStore.name.length > 0 ? updatedStore.name : store.value.attributes.name
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

  const editedStore: StoreUpdate = {
    ...toRaw(updatedStore),
    links: Object.assign(
      {
        website: null,
        twitter: null,
        instagram: null,
        facebook: null,
        youTube: null,
      } satisfies StoreLinks,
      Object.fromEntries(
        updatedStore.links.map(l => [l.type, nullOrNotBlank(l.url)]),
      ),
    ),
  }

  try {
    await editStore(editedStore)

    if (picture.value.file) {
      await uploadPicture({ storeId: updatedStore.id, picture: picture.value.file })
    } else if (picture.value.removeExisting) {
      await deletePicture(updatedStore.id)
    }

    await router.replace({ name: 'stores-id', params: { id: updatedStore.id } })
    await notificator.success({ title: t('stores.edited-with-success') })
  } catch (error) {
    await notificator.failure({
      title: t('stores.edited-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

const storeWasModified = ref(false)

watch(updatedStore, (newUpdatedStore) => {
  storeWasModified.value = initialStoreToEdit.value !== JSON.stringify(toRaw(newUpdatedStore))
})

useBeforeUnload({ enabled: storeWasModified })

const storePicture = computed(() => getRelationship(store.value, 'STORE_PICTURE'))
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
            <StoreMetadataForm
              ref="metadataForm"
              v-model:name="updatedStore.name"
              v-model:description="updatedStore.description"
              v-model:legal-name="updatedStore.legalName"
              v-model:location="updatedStore.location"
              v-model:type="updatedStore.type"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <EntityExternalLinksForm
              ref="externalLinksForm"
              v-model:external-links="updatedStore.links"
              :types="['website', 'twitter', 'instagram', 'facebook', 'youTube']"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <StorePictureForm
              ref="pictureForm"
              v-model:picture="picture"
              :current-image-url="
                createImageUrl({
                  fileName: storePicture?.attributes?.versions?.['256'],
                  timeHex: storePicture?.attributes?.timeHex,
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
