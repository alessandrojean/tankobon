<script lang="ts" setup>
import { MagnifyingGlassIcon, PlusIcon } from '@heroicons/vue/20/solid'
import { MagnifyingGlassIcon as MagnifyingGlassIconOutline, Square2StackIcon } from '@heroicons/vue/24/outline'

const showCreateDialog = ref(false)
const search = ref('')
const searchTerm = refDebounced(search, 500)

const libraryStore = useLibraryStore()
const library = computed(() => libraryStore.library!)
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
          is-router-link
          :to="{ name: 'series-new' }"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('series.new') }}</span>
        </Button>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <TableControls v-if="library">
        <div>
          <label class="sr-only" for="search-series">
            {{ $t('series.search') }}
          </label>
          <BasicTextInput
            id="search-series"
            v-model="search"
            class="w-48"
            size="small"
            type="search"
            :placeholder="$t('common-placeholders.search')"
          >
            <template #left-icon>
              <MagnifyingGlassIcon class="w-4 h-4" />
            </template>
          </BasicTextInput>
        </div>
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
            <template v-if="searchTerm.length === 0" #actions>
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
  </div>
</template>
