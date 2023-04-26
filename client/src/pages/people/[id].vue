<script lang="ts" setup>
import { PencilIcon, TrashIcon } from '@heroicons/vue/24/solid'
import { PersonUpdate } from '@/types/tankobon-person'
import { getRelationship } from '@/utils/api'

const { t } = useI18n()
const router = useRouter()
const personId = useRouteParams<string | undefined>('id', undefined)
const notificator = useToaster()

const { mutate: deletePerson, isLoading: isDeleting, isSuccess: isDeleted } = useDeletePersonMutation()
const { mutate: editPerson, isLoading: isEditing } = useUpdatePersonMutation()

const { data: person, isLoading } = usePersonQuery({
  personId: personId as Ref<string>,
  includes: ['library'],
  enabled: computed(() => !!personId.value && !isDeleting.value && !isDeleted.value),
  onError: async (error) => {
    await notificator.failure({
      title: t('people.fetch-one-failure'),
      body: error.message,
    })
  }
})

const library = computed(() => getRelationship(person.value, 'LIBRARY'))

function handleDelete() {
  deletePerson(personId.value!, {
    onSuccess: async () => {
      notificator.success({ title: t('people.deleted-with-success') })
      await router.replace({ name: 'people' })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('people.deleted-with-failure'),
        body: error.message,
      })
    }
  })
}

const showEditDialog = ref(false)

function handleEditPerson(person: PersonUpdate) {
  editPerson(person, {
    onSuccess: async () => {
      await notificator.success({ title: t('people.edited-with-success') })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('people.edited-with-failure'),
        body: error.message,
      })
    }
  })
}
</script>

<template>
  <div>
    <Header
      :title="person?.attributes.name ?? ''"
      :subtitle="person?.attributes.description"
      :loading="isLoading"
      class="mb-3 md:mb-0"
    >
      <template #title-badge v-if="library && library.attributes">
        <Badge class="ml-2">{{ library?.attributes?.name }}</Badge>
      </template>
      <template #actions>
        <div class="flex space-x-2">
          <Button
            class="w-11 h-11"
            :loading="isEditing"
            :disabled="isDeleting"
            :title="$t('common-actions.edit')"
            @click="showEditDialog = true"
          >
            <span class="sr-only">{{ $t('common-actions.edit') }}</span>
            <PencilIcon class="w-6 h-6" />
          </Button>

          <Button
            class="w-11 h-11"
            kind="danger"
            :disabled="isEditing"
            :loading="isDeleting"
            :title="$t('common-actions.delete')"
            @click="handleDelete"
          >
            <span class="sr-only">{{ $t('common-actions.delete') }}</span>
            <TrashIcon class="w-6 h-6" />
          </Button>
        </div>
      </template>
    </Header>
    <div class="max-w-7xl mx-auto p-4 sm:p-6 space-y-10">
      
    </div>

    <PersonEditDialog
      v-if="person"
      :is-open="showEditDialog"
      :person-entity="person"
      @submit="handleEditPerson"
      @close="showEditDialog = false"
    />
  </div>
</template>

<route lang="yaml">
  meta:
    layout: dashboard
</route>
