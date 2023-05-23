<script setup lang="ts">
import { CheckIcon, ExclamationCircleIcon } from '@heroicons/vue/20/solid'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { Picture } from '@/components/people/PersonPictureForm.vue'
import PersonPictureForm from '@/components/people/PersonPictureForm.vue'
import PersonMetadataForm from '@/components/people/PersonMetadataForm.vue'
import type { FormExternalLink } from '@/types/tankobon-external-link'
import EntityExternalLinksForm from '@/components/entity/EntityExternalLinksForm.vue'
import type { PersonCreation, PersonLinks } from '@/types/tankobon-person'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const notificator = useToaster()
const libraryStory = useLibraryStore()
const libraryId = computed(() => libraryStory.library!.id)

const { mutateAsync: createPerson, isLoading: isCreatingPerson } = useCreatePersonMutation()
const { mutateAsync: uploadPicture, isLoading: isUploadingPicture } = useUploadPersonPictureMutation()

const isCreating = logicOr(isCreatingPerson, isUploadingPicture)

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

interface CustomPersonUpdate extends Omit<PersonCreation, 'links'> {
  links: FormExternalLink[]
}

const newPerson = reactive<CustomPersonUpdate>({
  library: libraryId.value,
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

const picture = ref<Picture>({
  removeExisting: false,
  file: null,
})

const activeTab = ref(tabs[0])

const headerTitle = computed(() => {
  return newPerson.name.length > 0 ? newPerson.name : t('people.new')
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

  const personToCreate: PersonCreation = {
    ...toRaw(newPerson),
    bornAt: nullOrNotBlank(newPerson.bornAt),
    diedAt: nullOrNotBlank(newPerson.diedAt),
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
        newPerson.links.map(l => [l.type, nullOrNotBlank(l.url)]),
      ),
    ),
  }

  try {
    const { id } = await createPerson(personToCreate)

    if (picture.value.file) {
      await uploadPicture({ personId: id, picture: picture.value.file })
    }

    await router.replace({ name: 'people-id', params: { id } })
    await notificator.success({ title: t('people.created-with-success') })
  } catch (error) {
    await notificator.failure({
      title: t('people.created-with-failure'),
      body: (error as TankobonApiError | Error).message,
    })
  }
}

useBeforeUnload({
  enabled: computed(() => route.name === 'people-new'),
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
            <span>{{ $t('common-actions.save') }}</span>
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
            <PersonMetadataForm
              ref="metadataForm"
              v-model:name="newPerson.name"
              v-model:description="newPerson.description"
              v-model:nationality="newPerson.nationality"
              v-model:born-at="newPerson.bornAt"
              v-model:died-at="newPerson.diedAt"
              v-model:native-name="newPerson.nativeName.name"
              v-model:native-name-language="newPerson.nativeName.language"
              :disabled="isCreating"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <EntityExternalLinksForm
              ref="externalLinksForm"
              v-model:external-links="newPerson.links"
              :types="['website', 'twitter', 'instagram', 'facebook', 'pixiv', 'deviantArt', 'youTube']"
              :disabled="isCreating"
            />
          </TabPanel>
          <TabPanel :unmount="false">
            <PersonPictureForm
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
