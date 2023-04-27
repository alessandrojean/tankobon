<script lang="ts" setup>
import { getFullImageUrl } from '@/modules/api';
import { ContributorEntity } from '@/types/tankobon-contributor';
import { getRelationships, getRelationship } from '@/utils/api';

export interface BookAttributesProps {
  contributors?: ContributorEntity[],
  loading?: boolean
}

const props = withDefaults(defineProps<BookAttributesProps>(), {
  contributors: () => [],
  loading: false,
})

const { contributors, loading } = toRefs(props)
const { t, d, n, locale } = useI18n()

const pictures = computed(() => {
  return contributors.value.map((c) => getRelationship(c, 'PERSON_PICTURE'))
})
</script>

<template>
  <Block as="section" :title="$t('entities.book-contributors')">
    <ul class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-2 gap-6">
      <li
        v-for="(contributor, idx) in contributors"
        :key="contributor.id"
        class="group relative flex gap-4 items-center"
      >
        <Avatar
          size="small"
          kind="gray"
          class="relative z-10"
          :picture-url="
            getFullImageUrl({
              collection: 'people',
              fileName: pictures[idx]?.attributes?.versions?.['128'],
              timeHex: pictures[idx]?.attributes?.timeHex,
            })
          "
          :alt="contributor.attributes!.person.name"
        />
        <div class="text-sm">
          <RouterLink
            class="block font-medium dark:text-gray-200"
            :to="{ name: 'people-id', params: { id: contributor.attributes.person.id } }"
            :title="$t('common-actions.go-to-page', [contributor.attributes.person.name])"
          >
            <span class="absolute -inset-2 z-20 rounded-xl"></span>
            <span class="relative z-10">{{ contributor.attributes.person.name }}</span>
          </RouterLink>
          <span class="relative z-10 block text-gray-800 dark:text-gray-400">
            {{ contributor.attributes.role.name }}
          </span>
        </div>
        <div
          :class="[
            'absolute -inset-2 scale-95 bg-gray-200/50 dark:bg-gray-800',
            'motion-safe:transition opacity-0 group-hover:opacity-100',
            'group-hover:scale-100 rounded-lg'
          ]"
        />
      </li>
    </ul>
  </Block>
</template>
