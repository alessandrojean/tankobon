<script lang="ts" setup>
import { ExternalBookEntity } from '@/types/tankobon-external-book'
import { getRelationship } from '@/utils/api';
import { ArrowDownOnSquareIcon, ArrowTopRightOnSquareIcon } from '@heroicons/vue/20/solid'
import { BookOpenIcon, ExclamationCircleIcon } from '@heroicons/vue/24/outline';

export interface ImporterResultCardProps {
  result: ExternalBookEntity
}

export type ImporterResultCardEmits = {
  (e: 'click:import'): void
}

const props = defineProps<ImporterResultCardProps>()
defineEmits<ImporterResultCardEmits>()

const { result } = toRefs(props)
const { locale } = useI18n()

const listFormatter = computed(() => {
  return new Intl.ListFormat(locale.value, {
    style: 'long',
    type: 'conjunction'
  })
})

const contributors = computed(() => {
  return listFormatter.value.format(
    result.value.attributes.contributors.map((c) => c.name)
  )
})

const card = ref<HTMLDivElement>()
const source = computed(() => getRelationship(result.value, 'IMPORTER_SOURCE')!)
const coverUrl = computed(() => result.value.attributes.coverUrl)

const { imageHasError, imageLoading, setupObserver, observerCreated } =
  useImageLazyLoader(coverUrl, card)

onMounted(() => setupObserver())
</script>

<template>
  <div class="flex gap-4 sm:gap-6 group" ref="card">
    <div class="shrink-0">
      <FadeTransition>
        <div
          v-if="imageLoading || imageHasError"
          :class="[
            'bg-gray-50 dark:bg-gray-800 rounded-md shadow-lg w-32',
            'sm:w-36 lg:w-40 aspect-[2/3] flex items-center justify-center',
          ]"
        >
          <BookOpenIcon
            v-if="imageLoading || coverUrl?.length === 0"
            :class="[
              { 'motion-safe:animate-pulse': imageLoading },
              'w-10 h-10 text-gray-400 dark:text-gray-500'
            ]"
            aria-hidden="true"
          />
          <ExclamationCircleIcon
            v-else
            class="w-10 h-10 text-gray-400 dark:text-gray-500"
          />
        </div>
        <img
          v-else
          class="w-32 sm:w-36 lg:w-40 rounded-md shadow-lg ring-1 ring-black/5"
          :src="result.attributes.coverUrl"
          :alt="result.attributes.title"
        >
      </FadeTransition>
    </div>

    <div class="grow group-[:not(:last-child)]:border-b border-gray-200 dark:border-gray-700 pb-8">
      <p class="text-xl font-display font-semibold line-clamp-2">
        {{ result.attributes.title }}
      </p>
      <p class="text-gray-600 dark:text-gray-300 mt-0.5 line-clamp-1">
        {{ contributors }}
      </p>

      <ul
        :class="[
          'mt-4 text-sm font-medium flex space-x-2',
          `[&>:not(:first-child)]:before:content-['·']`,
          '[&>:not(:first-child)]:before:mr-2',
          '[&>:not(:first-child)]:before:text-gray-400',
          '[&>:not(:first-child)]:before:font-normal'
        ]"
      >
        <li>{{ result.attributes.publisher }}</li>
        <li v-if="result.attributes.pageCount > 0">
          {{ $t('books.page-count', result.attributes.pageCount) }}
        </li>
        <li>
          <a
            v-if="result.attributes.url"
            :href="result.attributes.url"
            target="_blank"
            rel="noreferer"
            class="text-primary-600 dark:text-primary-500 hover:underline"
            :title="$t('importer.view-page', [source.attributes!.name])"
          >
            <span>{{ source.attributes!.name }}</span>
            <ArrowTopRightOnSquareIcon class="w-3.5 h-3.5 inline-block ml-1" />
          </a>
          <span v-else>
            {{ source.attributes!.name }}
          </span>
        </li>
      </ul>

      <div class="prose prose-sm mt-4 dark:prose-invert dark:prose-p:text-gray-300">
        <p class="line-clamp-5">{{ result.attributes.synopsis }}</p>
      </div>

      <div class="mt-4">
        <Button @click="$emit('click:import')">
          <ArrowDownOnSquareIcon class="w-5 h-5" />
          <span>{{ $t('common-actions.import') }}</span>
        </Button>
      </div>
    </div>
  </div>
</template>