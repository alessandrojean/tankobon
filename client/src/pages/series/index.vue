<script lang="ts" setup>
import type { SeriesCreation } from '@/types/tankobon-series'
import { LibraryEntity } from '@/types/tankobon-library'
import { MagnifyingGlassIcon, PlusIcon } from '@heroicons/vue/20/solid'
import { Square2StackIcon } from '@heroicons/vue/24/outline'
import { MagnifyingGlassIcon as MagnifyingGlassIconOutline } from '@heroicons/vue/24/outline'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const notificator = useNotificator()

const showCreateDialog = ref(false)
const search = ref('')
const searchTerm = refDebounced(search, 500)

const { mutate } = useCreateSeriesMutation()
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

function handleCreateSeries(series: SeriesCreation) {
  mutate(series, {
    onSuccess: async ({ id }) => {
      notificator.success({ title: t('series.created-with-success') })
      await router.push({ name: 'series-id', params: { id } })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('series.created-with-failure'),
        body: error.message,
      })
    }
  })
}
</script>

<route lang="yaml">
  meta:
    layout: dashboard
</route>

<template>
  <div>
    <Header
      class="mb-3 md:mb-0"
      :title="$t('entities.series')"
    >
      <template #actions>
        <Button
          kind="primary"
          @click="showCreateDialog = true"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('series.new') }}</span>
        </Button>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <TableControls v-if="library && libraries">
        <div>
          <label class="sr-only" for="search-series">
            {{ $t('series.search') }}
          </label>
          <BasicTextInput
            id="search-series"
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

      <SeriesTable
        v-if="library"
        class="mt-4 sm:mt-6"
        :library-id="library.id"
        :search="searchTerm"
      >
        <template #empty>
          <EmptyState
            :icon="searchTerm.length > 0 ? MagnifyingGlassIconOutline : Square2StackIcon"
            :title="$t('series.empty-header')"
            :description="
              searchTerm.length > 0
                ? $t('series.empty-search-description', [searchTerm])
                : $t('series.empty-description')
            "
          >
            <template #actions v-if="searchTerm.length === 0">
              <Button
                kind="primary"
                @click="showCreateDialog = true"
              >
                <PlusIcon class="w-5 h-5" />
                <span>{{ $t('series.new') }}</span>
              </Button>
            </template>
          </EmptyState>
        </template>
      </SeriesTable>
    </div>

    <SeriesCreateDialog
      v-if="library"
      :library-id="library.id"
      :is-open="showCreateDialog"
      @submit="handleCreateSeries"
      @close="showCreateDialog = false"
    />
  </div>
</template>