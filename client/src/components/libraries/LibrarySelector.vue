<script lang="ts" setup>
import { BuildingLibraryIcon } from '@heroicons/vue/24/outline'
import Button from '@/components/form/Button.vue'
import type { LibraryEntity } from '@/types/tankobon-library'
import { getRelationship } from '@/utils/api'

defineProps<{
  transparent: boolean
}>()

const { t } = useI18n()
const notificator = useToaster()
const userStore = useUserStore()
const userId = computed(() => userStore.me?.id)
const libraryStore = useLibraryStore()

const { data: libraries } = useUserLibrariesByUserQuery({
  userId: userId as ComputedRef<string>,
  includes: ['owner'],
  includeShared: true,
  enabled: computed(() => userStore.isAuthenticated),
  onError: async (error) => {
    await notificator.failure({
      title: t('libraries.fetch-failure'),
      body: error.message,
    })
  },
  initialData: [],
})

const userHasAtLeastTwoLibraries = computed(() => libraries.value && libraries.value.length > 1)

const hasShared = computed(() => {
  return libraries.value?.some((l) => {
    return getRelationship(l, 'OWNER')?.id !== userStore.me?.id
  })
})

function sharedText(library: LibraryEntity | null | undefined) {
  if (!hasShared.value) {
    return null
  }

  const owner = getRelationship(library, 'OWNER')!

  return owner.id === userStore.me?.id
    ? t('libraries.yours')
    : `${t('libraries.shared')} · ${owner.attributes!.name}`
}

const library = computed({
  get: () => libraryStore.library?.id,
  set: (newLibraryId) => {
    const selected = libraries.value?.find(l => l.id === newLibraryId) ?? null

    libraryStore.setLibrary(selected)
  },
})

const currentLibrary = computed(() => {
  return libraries.value?.find(l => l.id === library.value) ?? null
})
</script>

<template>
  <FadeTransition>
    <BasicListbox
      v-if="userHasAtLeastTwoLibraries"
      v-model="library"
      :options="libraries ?? []"
      :option-value="(library) => library.id"
      :option-text="(library) => library?.attributes?.name ?? ''"
    >
      <template #listbox-button>
        <ListboxButton
          class="h-8 -ml-2 hidden lg:flex"
          :as="Button"
          :kind="transparent ? 'navbar-light' : 'navbar-dark-elevated'"
          size="mini"
          :title="$t('libraries.select')"
        >
          <BuildingLibraryIcon class="w-5 h-5" />
          <div>
            <span class="sr-only">{{ $t('libraries.selected') }}</span>
            {{ currentLibrary?.attributes?.name }}
          </div>
        </ListboxButton>
      </template>
      <template #option="{ option }">
        <div>
          <span class="block truncate text-sm">
            {{ (option as LibraryEntity).attributes.name }}
          </span>
          <span
            v-if="option && sharedText(option)"
            class="block truncate text-xs opacity-80"
          >
            {{ sharedText(option) }}
          </span>
        </div>
      </template>
    </BasicListbox>
  </FadeTransition>
</template>
