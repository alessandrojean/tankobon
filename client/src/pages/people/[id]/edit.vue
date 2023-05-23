<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import { getRelationship } from '@/utils/api'
import { createImageUrl } from '@/modules/api'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { Picture } from '@/components/people/PersonPictureForm.vue'
import PersonPictureForm from '@/components/people/PersonPictureForm.vue'
import PersonMetadataForm from '@/components/people/PersonMetadataForm.vue'
import type { FormExternalLink } from '@/types/tankobon-external-link'
import EntityExternalLinksForm from '@/components/entity/EntityExternalLinksForm.vue'
import type { PersonLinks, PersonUpdate } from '@/types/tankobon-person'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const personId = computed(() => route.params.id as string)

const { mutateAsync: editPerson, isLoading: isEditingPerson } = useUpdatePersonMutation()
const { mutateAsync: uploadPicture, isLoading: isUploadingPicture } = useUploadPersonPictureMutation()
const { mutateAsync: deletePicture, isLoading: isDeletingPicture } = useDeletePersonPictureMutation()

const isEditing = logicOr(isEditingPerson, isUploadingPicture, isDeletingPicture)

const { data: person, isLoading } = usePersonQuery({
  personId,
  includes: ['person_picture'],
  enabled: computed(() => !!personId.value),
  refetchOnWindowFocus: false,
  refetchOnReconnect: false,
  staleTime: Infinity,
  onError: async (error) => {
    await notificator.failure({
      title: t('people.fetch-one-failure'),
      body: error.message,
    })
  },
})

const metadataForm = ref<InstanceType<typeof PersonMetadataForm>>()
const externalLinksForm = ref<InstanceType<typeof EntityExternalLinksForm>>()
const pictureForm = ref<InstanceType<typeof PersonPictureForm>>()

const metadataInvalid = computed(() => metadataForm.value?.v$.$error ?? false)
const externalLinksInvalid = computed(() => externalLinksForm.value?.v$.$error ?? false)
const pictureInvalid = computed(() => pictureForm.value?.v$.$error ?? false)

const tabs = [
  { key: '0', text: 'people.metadata' },
  { key: '1', text: 'external-links.title' },
  { key: '2', text: 'people.picture' },
]

const invalidTabs = computed(() => [
  metadataInvalid.value,
  externalLinksInvalid.value,
  pictureInvalid.value,
])

interface CustomPersonUpdate extends Omit<PersonUpdate, 'links'> {
  links: FormExternalLink[]
}

const updatedPerson = reactive<CustomPersonUpdate>({
  id: '',
  name: '',
  description: '',
  links: [],
  nationality: null,
  bornAt: null,
  diedAt: null,
  nativeName: {
    name: '',
    language: null,
  },
})

const initialPersonToEdit = ref('')

whenever(person, (loadedPerson) => {
  Object.assign(updatedPerson, {
    id: loadedPerson.id,
    name: loadedPerson.attributes.name,
    description: loadedPerson.attributes.description,
    links: Object.entries(loadedPerson.attributes.links)
      .filter(([_, url]) => url !== null && url.length > 0)
      .map(([type, url]) => ({ type, url })),
    nationality: loadedPerson.attributes.nationality,
    bornAt: loadedPerson.attributes.bornAt,
    diedAt: loadedPerson.attributes.diedAt,
    nativeName: {
      name: loadedPerson.attributes.nativeName.name,
      language: loadedPerson.attributes.nativeName.language,
    },
  } satisfies CustomPersonUpdate)

  initialPersonToEdit.value = JSON.stringify(toRaw(updatedPerson))
}, { immediate: true })

const picture = ref<Picture>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  if (isLoading.value || !person.value) {
    return ''
  }

  return updatedPerson.name.length > 0 ? updatedPerson.name : person.value.attributes.name
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

  const editedPerson: PersonUpdate = {
    ...toRaw(updatedPerson),
    bornAt: nullOrNotBlank(updatedPerson.bornAt),
    diedAt: nullOrNotBlank(updatedPerson.diedAt),
    links: Object.assign(
      {
        website: null,
        twitter: null,
        instagram: null,
        facebook: null,
        pixiv: null,
        deviantArt: null,
        youTube: null,
      } satisfies PersonLinks,
      Object.fromEntries(
        updatedPerson.links.map(l => [l.type, nullOrNotBlank(l.url)]),
      ),
    ),
  }

  try {
    await editPerson(editedPerson)

    if (picture.value.file) {
      await uploadPicture({ personId: updatedPerson.id, picture: picture.value.file })
    } else if (picture.value.removeExisting) {
      await deletePicture(updatedPerson.id)
    }

    await router.replace({ name: 'people-id', params: { id: updatedPerson.id } })
    await notificator.success({ title: t('people.edited-with-success') })
  } catch (error) {
    await notificator.failure({
      title: t('people.edited-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

const personWasModified = ref(false)

watch(updatedPerson, (newUpdatedPerson) => {
  personWasModified.value = initialPersonToEdit.value !== JSON.stringify(toRaw(newUpdatedPerson))
})

useBeforeUnload({ enabled: personWasModified })

const personPicture = computed(() => getRelationship(person.value, 'PERSON_PICTURE'))
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
            <PersonMetadataForm
              ref="metadataForm"
              v-model:name="updatedPerson.name"
              v-model:description="updatedPerson.description"
              v-model:nationality="updatedPerson.nationality"
              v-model:born-at="updatedPerson.bornAt"
              v-model:died-at="updatedPerson.diedAt"
              v-model:native-name="updatedPerson.nativeName.name"
              v-model:native-name-language="updatedPerson.nativeName.language"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <EntityExternalLinksForm
              ref="externalLinksForm"
              v-model:external-links="updatedPerson.links"
              :types="['website', 'twitter', 'instagram', 'facebook', 'pixiv', 'deviantArt', 'youTube']"
              :disabled="isLoading || isEditing"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <PersonPictureForm
              ref="pictureForm"
              v-model:picture="picture"
              :current-image-url="
                createImageUrl({
                  fileName: personPicture?.attributes?.versions?.['256'],
                  timeHex: personPicture?.attributes?.timeHex,
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
