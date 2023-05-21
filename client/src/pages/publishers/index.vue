<script lang="ts" setup>
import { MagnifyingGlassIcon, PlusIcon } from '@heroicons/vue/20/solid'
import { BuildingOffice2Icon, MagnifyingGlassIcon as MagnifyingGlassIconOutline } from '@heroicons/vue/24/outline'

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
      :title="$t('entities.publishers')"
    >
      <template #actions>
        <Button
          kind="primary"
          is-router-link
          :to="{ name: 'publishers-new' }"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('publishers.new') }}</span>
        </Button>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <ViewControls v-if="library">
        <div>
          <label class="sr-only" for="search-publisher">
            {{ $t('publishers.search') }}
          </label>
          <BasicTextInput
            id="search-publisher"
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
      </ViewControls>

      <PublishersTable
        v-if="library"
        class="mt-4 sm:mt-6"
        :library-id="library.id"
        :search="searchTerm"
      >
        <template #empty>
          <EmptyState
            :icon="searchTerm.length > 0 ? MagnifyingGlassIconOutline : BuildingOffice2Icon"
            :title="$t('publishers.empty-header')"
            :description="
              searchTerm.length > 0
                ? $t('publishers.empty-search-description', [searchTerm])
                : $t('publishers.empty-description')
            "
          >
            <template v-if="searchTerm.length === 0" #actions>
              <Button
                kind="primary"
                @click="showCreateDialog = true"
              >
                <PlusIcon class="w-5 h-5" />
                <span>{{ $t('publishers.new') }}</span>
              </Button>
            </template>
          </EmptyState>
        </template>
      </PublishersTable>
    </div>
  </div>
</template>
