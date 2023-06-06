<script lang="ts" setup>
import { FocusKeys } from '@primer/behaviors'
import type { Relationship } from '@/types/tankobon-entity'
import type { TagAttributes } from '@/types/tankobon-tag'

export interface BookTagsProps {
  group?: boolean
  loading?: boolean
  tags?: Relationship<TagAttributes>[] | null
}

const props = withDefaults(defineProps<BookTagsProps>(), {
  group: false,
  loading: false,
  tags: () => [],
})

const { group, loading, tags } = toRefs(props)
const { t } = useI18n()

const SKELETON_TAG_SIZES = ['w-16', 'w-20', 'w-12', 'w-14']

function randomSize() {
  const randomIdx = Math.floor(Math.random() * SKELETON_TAG_SIZES.length)

  return SKELETON_TAG_SIZES[randomIdx]
}

const groupedTags = computed(() => {
  const allTagsHaveGroups
    = group.value && tags.value?.every(tag => tag.attributes!.name.includes(':'))

  if (allTagsHaveGroups !== true) {
    return null
  }

  return tags.value!.reduce((record, tag) => {
    const [title] = tag.attributes!.name.split(': ')
    const lowercaseTitle = title.toLowerCase()

    if (record[lowercaseTitle]) {
      record[lowercaseTitle].push(tag)
    } else {
      record[lowercaseTitle] = [tag]
    }

    return record
  }, {} as Record<string, Relationship<TagAttributes>[]>)
})

const container = ref<HTMLElement>()

useFocusZone({
  containerRef: container,
  bindKeys: FocusKeys.ArrowAll | FocusKeys.HomeAndEnd,
  focusInStrategy: 'closest',
  focusOutBehavior: 'wrap',
})
</script>

<template>
  <Block :title="t('entities.tags')">
    <ul
      v-if="!loading && tags?.length && !groupedTags"
      ref="container"
      class="flex flex-wrap gap-2"
    >
      <li v-for="tag in tags" :key="tag.id" class="block">
        <RouterLink
          :to="{ name: 'tags-id', params: { id: tag.id } }"
          :class="[
            'block group/tag text-xs font-medium tracking-wide',
            'bg-primary-100 dark:bg-gray-700 rounded-md',
            'text-primary-700 dark:text-gray-300',
            'px-2.5 py-1 inline-flex items-center',
            'motion-safe:transition-colors',
            'hocus:bg-primary-200 dark:hocus:bg-gray-600',
            'hocus:text-primary-800 dark:hocus:text-gray-200',
            'focus:outline-none focus-visible:ring-2',
            'focus-visible:ring-black dark:focus-visible:ring-white/90',
          ]"
          :title="$t('common-actions.go-to-page', [tag.attributes!.name])"
        >
          <span
            aria-hidden="true"
            :class="[
              'bg-primary-200 dark:bg-gray-500',
              'w-1.5 h-1.5 mr-2 -ml-0.5',
              'inline-block rounded',
              'motion-safe:transition-colors',
              'group-hocus/tag:bg-primary-300 dark:group-hocus/tag:bg-gray-400',
            ]"
          />
          <span>{{ tag.attributes!.name }}</span>
        </RouterLink>
      </li>
    </ul>
    <div
      v-else-if="!loading && tags?.length && groupedTags"
      ref="container"
      class="space-y-4 mt-2"
    >
      <div v-for="(tagsFromGroup, groupKey) in groupedTags" :key="groupKey">
        <p
          class="text-sm font-medium text-gray-600 dark:text-gray-300 first-letter:capitalize"
        >
          {{ groupKey }}
        </p>

        <ul class="flex flex-wrap gap-2 mt-2">
          <li v-for="tag in tagsFromGroup" :key="tag.id" class="block">
            <RouterLink
              :to="{ name: 'tags-id', params: { id: tag.id } }"
              :class="[
                'block group/tag text-xs font-medium tracking-wide',
                'bg-primary-100 dark:bg-gray-700 rounded-md',
                'text-primary-700 dark:text-gray-300',
                'px-2.5 py-1 inline-flex items-center',
                'motion-safe:transition-colors',
                'hocus:bg-primary-200 dark:hocus:bg-gray-600',
                'hocus:text-primary-800 dark:hocus:text-gray-200',
                'focus:outline-none focus-visible:ring-2',
                'focus-visible:ring-black dark:focus-visible:ring-white/90',
              ]"
              :title="$t('common-actions.go-to-page', [tag.attributes!.name])"
            >
              <span
                aria-hidden="true"
                :class="[
                  'bg-primary-200 dark:bg-gray-500',
                  'w-1.5 h-1.5 mr-2 -ml-0.5',
                  'inline-block rounded',
                  'motion-safe:transition-colors',
                  'group-hocus/tag:bg-primary-300 dark:group-hocus/tag:bg-gray-400',
                ]"
              />
              <span>{{ tag.attributes!.name.substring(tag.attributes!.name.indexOf(': ')) }}</span>
            </RouterLink>
          </li>
        </ul>
      </div>
    </div>
    <p
      v-else-if="tags?.length === 0 && !loading"
      class="mt-4 italic text-gray-700 dark:text-gray-300"
    >
      {{ $t('books.empty-tags') }}
    </p>
    <div v-else-if="loading" aria-hidden="true">
      <div class="flex flex-wrap gap-2 mt-4">
        <div
          v-for="tag of 20"
          :key="tag"
          :class="['skeleton h-6', randomSize()]"
        />
      </div>
    </div>
  </Block>
</template>
