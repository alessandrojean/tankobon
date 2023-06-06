<script setup lang="ts">
import { ComboboxInput, Dialog as HeadlessUiDialog } from '@headlessui/vue'
import { ArchiveBoxIcon, BookOpenIcon, BuildingOffice2Icon, BuildingStorefrontIcon, MagnifyingGlassIcon, PaintBrushIcon, Square2StackIcon, TagIcon } from '@heroicons/vue/24/outline'
import { XMarkIcon } from '@heroicons/vue/20/solid'
import type { SearchObject } from '@/types/tankobon-search'
import type { Entity, EntityType } from '@/types/tankobon-entity'
import useLuceneQuery from '@/composables/useLuceneQuery'

export interface SearchPaletteProps {
  open: boolean
}

const props = defineProps<SearchPaletteProps>()

const emit = defineEmits<{
  (e: 'update:open', open: boolean): void
}>()

const router = useRouter()

const { open } = toRefs(props)

const search = ref('')
const searchTerm = refDebounced(search, 500)

const { highlighted } = useLuceneQuery(search)

const libraryStore = useLibraryStore()
const libraryId = computed(() => libraryStore.library?.id)

const { data: results } = useSearchQuery({
  libraryId: libraryId as ComputedRef<string>,
  search: searchTerm,
  enabled: computed(() => libraryId.value !== undefined && searchTerm.value.length > 0),
})

const hasResults = computed(() => {
  return results.value && Object.values(results.value).some((r: any[]) => r.length > 0)
})

const showResults = computed(() => {
  return search.value.length > 0 && hasResults.value
})

const showEmptyState = computed(() => {
  return search.value.length > 0 && results.value && !hasResults.value
})

const groups = computed(() => {
  return Object.entries(results.value ?? {})
    .filter(([_, entities]) => entities.length > 0)
    .map(([group]) => group as keyof SearchObject)
})

const entityRouteMap: Partial<Record<EntityType, string>> = {
  PUBLISHER: 'publishers-id',
  STORE: 'stores-id',
  PERSON: 'people-id',
  SERIES: 'series-id',
  TAG: 'tags-id',
  COLLECTION: 'collections-id',
  BOOK: 'books-id',
}

const entityIconMap: Partial<Record<EntityType, Component>> = {
  PUBLISHER: BuildingOffice2Icon,
  STORE: BuildingStorefrontIcon,
  PERSON: PaintBrushIcon,
  SERIES: Square2StackIcon,
  TAG: TagIcon,
  COLLECTION: ArchiveBoxIcon,
  BOOK: BookOpenIcon,
}

async function handleResultSelected(result: Entity<any> | null | undefined) {
  if (!result) {
    return
  }

  emit('update:open', false)
  await router.push({
    name: entityRouteMap[result.type],
    params: { id: result.id },
  })
}

function title(result: Entity<any>): string {
  switch (result?.type) {
    case 'SERIES':
    case 'STORE':
    case 'PUBLISHER':
    case 'PERSON':
    case 'TAG':
    case 'COLLECTION':
      return result.attributes.name
    case 'BOOK':
      return result.attributes.title
    default:
      return ''
  }
}

const inputRenderer = ref<HTMLDivElement>()

function syncScroll(event: Event) {
  const target = event.target! as HTMLInputElement

  inputRenderer.value!.scrollTop = target.scrollTop
  inputRenderer.value!.scrollLeft = target.scrollLeft
}

const input = ref<InstanceType<typeof ComboboxInput>>()

function clearSearch() {
  search.value = ''
  input.value?.$el.focus?.()
}

onBeforeRouteLeave(() => {
  if (!open.value) {
    return true
  }

  emit('update:open', false)
  return false
})
</script>

<template>
  <TransitionRoot
    appear
    as="template"
    :show="open"
    @close="$emit('update:open', false)"
    @after-leave="search = ''"
  >
    <HeadlessUiDialog
      static
      :open="open"
      @close="$emit('update:open', false)"
    >
      <div
        :class="[
          'fixed z-30 inset-0 flex flex-col items-center px-2 py-3 sm:py-6',
          'sm:px-6 md:px-0 md:py-12 lg:py-16',
        ]"
      >
        <TransitionChild
          as="template"
          enter="motion-safe:transition-opacity duration-200 ease-in-out-primer"
          enter-from="opacity-0"
          enter-to="opacity-100"
          leave="motion-safe:transition-opacity duration-50 ease-in"
          leave-from="opacity-100"
          leave-to="opacity-0"
        >
          <div
            :class="[
              'fixed inset-0 bg-gray-700/75 dark:bg-gray-950/80',
              'motion-safe:transition-opacity',
            ]"
            @click="$emit('update:open', false)"
          />
        </TransitionChild>

        <TransitionChild
          as="div"
          :class="[
            'relative flex flex-col w-full will-change-transform',
            'overflow-hidden text-left bg-white dark:bg-gray-900',
            'shadow-xl rounded-xl ring-1 ring-black/5 max-w-2xl',
          ]"
          enter="motion-reduce:transition-none duration-150 ease-in-out-primer"
          enter-from="opacity-0 scale-95"
          enter-to="opacity-100 scale-100"
          leave="motion-reduce:transition-none duration-50 ease-in"
          leave-from="opacity-100"
          leave-to="opacity-0"
        >
          <Combobox
            nullable
            @update:model-value="handleResultSelected"
          >
            <DialogPanel
              :class="[
                'relative overflow-hidden bg-white dark:bg-gray-800',
                'shrink-0 flex flex-col max-h-full',
              ]"
            >
              <div class="p-2 grow min-h-0 flex flex-col h-full">
                <div class="relative shrink-0">
                  <ComboboxLabel class="sr-only" for="search-palette-input">
                    {{ $t('search-palette.label') }}
                  </ComboboxLabel>

                  <div class="relative w-full">
                    <div
                      v-if="search.length > 0"
                      ref="inputRenderer"
                      :class="[
                        'pl-9 pr-4 absolute inset-x-0 -inset-y-1 flex',
                        'items-center whitespace-pre break-words text-base',
                        'font-normal overflow-x-auto select-none pointer-events-none',
                        '[scrollbar-width:0] [&::-webkit-scrollbar]:hidden',
                      ]"
                      v-html="highlighted"
                    />
                    <ComboboxInput
                      id="search-palette-input"
                      ref="input"
                      type="text"
                      :class="[
                        'relative flex w-full rounded-md border-gray-300 dark:border-gray-600 shadow-none',
                        'focus:outline-none focus:border-primary-500 focus:ring',
                        'dark:focus:border-primary-400 focus:ring-primary-200',
                        'dark:focus:ring-primary-200/30',
                        'motion-safe:transition pl-9 pr-4 peer',
                        'text-transparent caret-gray-800 dark:caret-gray-100 bg-transparent',
                        'dark:placeholder:text-gray-400',
                      ]"
                      :placeholder="$t('common-actions.search-library')"
                      :display-value="(item) => title(item as Entity<any>)"
                      autocomplete="off"
                      spellcheck="false"
                      autocapitalize="off"
                      @change="search = $event.target.value"
                      @scroll="syncScroll"
                    />

                    <MagnifyingGlassIcon
                      :class="[
                        'w-5 h-5 absolute left-2 top-1/2 -translate-y-1/2',
                        'text-gray-500 dark:text-gray-400',
                        'peer-focus:text-primary-600 dark:peer-focus:text-primary-400',
                        'motion-safe:transition-colors',
                      ]"
                    />
                  </div>

                  <div v-if="search.length" class="absolute right-1 inset-y-0 flex items-center">
                    <Button
                      class="w-8 h-8"
                      kind="ghost-alt"
                      size="mini"
                      @click="clearSearch"
                    >
                      <span class="sr-only">{{ $t('common-actions.clear-field') }}</span>
                      <XMarkIcon class="w-5 h-5" />
                    </Button>
                  </div>
                </div>

                <ComboboxOptions
                  v-if="showResults"
                  static
                  as="ul"
                  class="mt-2 grow min-h-0 overflow-y-auto"
                >
                  <li
                    v-for="group in groups"
                    :key="group"
                  >
                    <span
                      :class="[
                        'block font-medium font-display-safe text-sm',
                        'px-2 py-1',
                      ]"
                    >
                      {{ $t(`entities.${group}`) }}
                    </span>
                    <ul>
                      <li
                        v-for="result in results![group]"
                        :key="result.id"
                      >
                        <ComboboxOption
                          as="span"
                          :class="[
                            'flex items-center gap-3 py-2 px-2 rounded group',
                            'cursor-pointer text-gray-800 dark:text-gray-300 relative',
                            'ui-active:bg-primary-100 ui-active:text-primary-700',
                            'dark:ui-active:text-primary-100 dark:ui-active:bg-primary-600',
                            'focus:outline-none motion-safe:transition-shadow',
                          ]"
                          :value="result"
                        >
                          <Component
                            :is="entityIconMap[result.type]"
                            :class="[
                              'w-6 h-6 shrink-0',
                              'text-gray-500 ui-active:text-primary-600',
                              'dark:ui-active:text-primary-200',
                            ]"
                          />
                          <span class="grow truncate">
                            {{ title(result) }}
                          </span>
                          <kbd
                            :class="[
                              'font-sans-safe shrink-0 font-semibold w-6 h-6 -mr-1',
                              'text-primary-600 dark:text-primary-200',
                              'hidden md:ui-active:block',
                            ]"
                          >
                            &crarr;
                          </kbd>
                        </ComboboxOption>
                      </li>
                    </ul>
                  </li>
                </ComboboxOptions>
                <EmptyState
                  v-else-if="showEmptyState"
                  :icon="MagnifyingGlassIcon"
                  :title="$t('search-palette.no-results-found')"
                  :description="$t('search-palette.no-results-found-tip')"
                />
              </div>

              <footer
                v-if="showResults"
                :class="[
                  'shrink-0 hidden md:flex justify-between items-center',
                  'bg-gray-50 dark:bg-gray-800 text-gray-700 dark:text-gray-400',
                  'border-t border-gray-200 dark:border-gray-700 px-3 py-3',
                  '[&_kbd]:font-sans-safe [&_kbd]:text-xs/4 [&_kbd]:shadow-sm',
                  '[&_kbd]:bg-white dark:[&_kbd]:bg-gray-900',
                  '[&_kbd]:border [&_kbd]:rounded-md',
                  '[&_kbd]:border-gray-400 dark:[&_kbd]:border-gray-500',
                  '[&_kbd]:text-gray-900 dark:[&_kbd]:text-gray-50',
                  '[&_kbd]:flex [&_kbd]:items-center [&_kbd]:justify-center',
                ]"
              >
                <ul class="flex items-center gap-4 text-sm">
                  <i18n-t
                    class="flex items-center gap-0.5"
                    keypath="search-palette.enter-to-select"
                    tag="li"
                  >
                    <template #enter>
                      <kbd class="w-5 h-5 mr-1">&crarr;</kbd>
                    </template>
                  </i18n-t>
                  <i18n-t
                    class="flex items-center gap-0.5"
                    keypath="search-palette.arrows-to-navigate"
                    tag="li"
                  >
                    <template #arrows>
                      <kbd class="w-5 h-5">&uarr;</kbd>
                      <kbd class="w-5 h-5 mr-1">&darr;</kbd>
                    </template>
                  </i18n-t>
                  <i18n-t
                    class="flex items-center gap-0.5"
                    keypath="search-palette.esc-to-close"
                    tag="li"
                  >
                    <template #esc>
                      <kbd class="h-5 px-1 mr-1">esc</kbd>
                    </template>
                  </i18n-t>
                </ul>

                <Button
                  class="h-8 -mr-2"
                  size="mini"
                  kind="ghost-alt"
                  is-link
                  href="https://lucene.apache.org"
                  target="_blank"
                  rel="noopener noreferer"
                  :title="$t('search-palette.search-by', { provider: 'Apache Lucene™' })"
                >
                  <i18n-t
                    :class="[
                      'flex items-center gap-2 font-normal',
                      'text-gray-700 dark:text-gray-400',
                      'group-hocus/button:text-gray-800',
                      'dark:group-hocus/button:text-gray-100',
                      'motion-safe:transition-colors',
                    ]"
                    tag="span"
                    keypath="search-palette.search-by"
                  >
                    <template #provider>
                      <span class="sr-only">Apache Lucene&trade;</span>
                      <ApacheLuceneIcon class="w-5 h-5 !text-[#019b8f] -mr-1" />
                    </template>
                  </i18n-t>
                </Button>
              </footer>
            </DialogPanel>
          </Combobox>
        </TransitionChild>
      </div>
    </HeadlessUiDialog>
  </TransitionRoot>
</template>

<style lang="postcss">
.tag-expression {
  @apply text-primary-700 bg-primary-100 rounded-sm
    dark:bg-primary-900/40 dark:text-primary-400;
}

.logic-operator {
  @apply text-purple-600 dark:text-purple-400;
}

/* .syntax-error {
  @apply text-red-700 bg-red-50 rounded-sm
    dark:bg-red-900/40 dark:text-red-400
    underline decoration-wavy underline-offset-4
    decoration-red-600 dark:decoration-red-400;
} */
</style>
