<script lang="ts" setup>
import { ArrowDownOnSquareIcon, MagnifyingGlassIcon } from '@heroicons/vue/20/solid'
import { BookOpenIcon, MagnifyingGlassIcon as MagnifyingGlassIconOutline } from '@heroicons/vue/24/outline'

const libraryStore = useLibraryStore()
const library = computed(() => libraryStore.library!)

const search = ref('')
const searchTerm = refDebounced(search, 500)
</script>

<route lang="yaml">
meta:
  layout: dashboard
</route>

<template>
  <div>
    <Header :title="$t('entities.books')">
      <template #actions>
        <Button
          kind="primary"
          is-router-link
          :to="{ name: 'import-search' }"
        >
          <ArrowDownOnSquareIcon class="w-5 h-5" />
          <span>{{ $t('common-actions.import') }}</span>
        </Button>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <TableControls v-if="library">
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
      </TableControls>

      <BooksTable
        v-if="library"
        class="mt-4 sm:mt-6"
        :library-id="library.id"
        :search="searchTerm"
      >
        <template #empty>
          <EmptyState
            :icon="searchTerm.length > 0 ? MagnifyingGlassIconOutline : BookOpenIcon"
            :title="$t('books.empty-header')"
            :description="
              searchTerm.length > 0
                ? $t('books.empty-search-description', [searchTerm])
                : $t('books.empty-description')
            "
          >
            <template v-if="searchTerm.length === 0" #actions>
              <Button
                kind="primary"
                is-router-link
                :to="{ name: 'import-search' }"
              >
                <ArrowDownOnSquareIcon class="w-5 h-5" />
                <span>{{ $t('common-actions.import') }}</span>
              </Button>
            </template>
          </EmptyState>
        </template>
      </BooksTable>
    </div>
  </div>
</template>
