<script lang="ts" setup>
import { MagnifyingGlassIcon, PlusIcon } from '@heroicons/vue/20/solid'
import { MagnifyingGlassIcon as MagnifyingGlassIconOutline, TagIcon } from '@heroicons/vue/24/outline'
import type { TagCreation } from '@/types/tankobon-tag'

const { t } = useI18n()
const router = useRouter()
const notificator = useToaster()

const showCreateDialog = ref(false)
const search = ref('')
const searchTerm = refDebounced(search, 500)

const { mutate } = useCreateTagMutation()

const libraryStore = useLibraryStore()
const library = computed(() => libraryStore.library!)

function handleCreateTag(tag: TagCreation) {
  mutate(tag, {
    onSuccess: async ({ id }) => {
      notificator.success({ title: t('tags.created-with-success') })
      await router.push({ name: 'tags-id', params: { id } })
    },
    onError: async (error) => {
      await notificator.failure({
        title: t('tags.created-with-failure'),
        body: error.message,
      })
    },
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
      :title="$t('entities.tags')"
    >
      <template #actions>
        <Button
          kind="primary"
          @click="showCreateDialog = true"
        >
          <PlusIcon class="w-5 h-5" />
          <span>{{ $t('tags.new') }}</span>
        </Button>
      </template>
    </Header>

    <div class="max-w-7xl mx-auto p-4 sm:p-6">
      <ViewControls v-if="library">
        <div>
          <label class="sr-only" for="search-tag">
            {{ $t('tags.search') }}
          </label>
          <BasicTextInput
            id="search-tag"
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

      <TagsTable
        v-if="library"
        class="mt-4 sm:mt-6"
        :library-id="library.id"
        :search="searchTerm"
      >
        <template #empty>
          <EmptyState
            :icon="searchTerm.length > 0 ? MagnifyingGlassIconOutline : TagIcon"
            :title="$t('tags.empty-header')"
            :description="
              searchTerm.length > 0
                ? $t('tags.empty-search-description', [searchTerm])
                : $t('tags.empty-description')
            "
          >
            <template v-if="searchTerm.length === 0" #actions>
              <Button
                kind="primary"
                @click="showCreateDialog = true"
              >
                <PlusIcon class="w-5 h-5" />
                <span>{{ $t('tags.new') }}</span>
              </Button>
            </template>
          </EmptyState>
        </template>
      </TagsTable>
    </div>

    <TagCreateDialog
      v-if="library"
      :library-id="library.id"
      :is-open="showCreateDialog"
      @submit="handleCreateTag"
      @close="showCreateDialog = false"
    />
  </div>
</template>
