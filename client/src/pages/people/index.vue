<script lang="ts" setup>
import type { PersonCreation } from '@/types/tankobon-person'
import { LibraryEntity } from '@/types/tankobon-library'
import { MagnifyingGlassIcon, PlusIcon } from '@heroicons/vue/20/solid'
import { AcademicCapIcon } from '@heroicons/vue/24/outline'
import { MagnifyingGlassIcon as MagnifyingGlassIconOutline } from '@heroicons/vue/24/outline'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const notificator = useNotificator()

const showCreateDialog = ref(false)
const search = ref('')
const searchTerm = refDebounced(search, 500)

const { mutate } = useCreatePersonMutation()
const { data: libraries } = useUserLibrariesByUserQuery({
  userId: computed(() => userStore.me!.id),
  includeShared: true,
  includes: ['owner'],
  initialData: [],
  onSuccess: (libraries) => {
    if (!library.value || !libraries.find((l) => l.id === library.value!.id)) {
      library.value = libraries[0]
    }
  }
})

const library = ref<LibraryEntity>()

function handleCreatePerson(person: PersonCreation) {
  mutate(person, {
    onSuccess: async ({ id }) => {
      notificator.success({ title: t('people.created-with-success') })
      await router.push({ name: 'people-id', params: { id } })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('people.created-with-failure'),
        body: error.message,
      })
    }
  })
}
</script>

<route lang="yaml">
  meta:
    layout: dashboard
    isAdminOnly: true
</route>

<template>
  <div>
    <Header
      class="mb-3 md:mb-0"
      :title="$t('entities.people')"
    >
      <template #actions>
        <Button
          kind="primary"
          @click="showCreateDialog = true"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('people.new') }}</span>
        </Button>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <TableControls v-if="library && libraries">
        <div>
          <label class="sr-only" for="search-person">
            {{ $t('people.search') }}
          </label>
          <BasicTextInput
            id="search-person"
            class="w-48"
            v-model="search"
            size="small"
            type="search"
            :placeholder="$t('common-placeholders.search')"
          >
            <template #left-icon>
              <MagnifyingGlassIcon class="w-4 h-4" />
            </template>
          </BasicTextInput>
        </div>
        <LibrarySelector
          class="w-48"
          size="small"
          v-model="library"
          :options="libraries"
        />
      </TableControls>

      <PeopleTable
        v-if="library"
        class="mt-4 sm:mt-6"
        :library-id="library.id"
        :search="searchTerm"
      >
        <template #empty>
          <EmptyState
            :icon="searchTerm.length > 0 ? MagnifyingGlassIconOutline : AcademicCapIcon"
            :title="$t('people.empty-header')"
            :description="
              searchTerm.length > 0
                ? $t('people.empty-search-description', [searchTerm])
                : $t('people.empty-description')
            "
          >
            <template #actions v-if="searchTerm.length === 0">
              <Button
                kind="primary"
                @click="showCreateDialog = true"
              >
                <PlusIcon class="w-5 h-5" />
                <span>{{ $t('people.new') }}</span>
              </Button>
            </template>
          </EmptyState>
        </template>
      </PeopleTable>
    </div>

    <PersonCreateDialog
      v-if="library"
      :library-id="library.id"
      :is-open="showCreateDialog"
      @submit="handleCreatePerson"
      @close="showCreateDialog = false"
    />
  </div>
</template>