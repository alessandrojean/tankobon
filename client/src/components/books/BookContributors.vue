<script lang="ts" setup>
import { FocusKeys } from '@primer/behaviors'
import { createImageUrl } from '@/modules/api'
import type { ContributorEntity } from '@/types/tankobon-contributor'
import { getRelationship } from '@/utils/api'

export interface BookAttributesProps {
  contributors?: ContributorEntity[]
  loading?: boolean
}

const props = withDefaults(defineProps<BookAttributesProps>(), {
  contributors: () => [],
  loading: false,
})

const { contributors, loading } = toRefs(props)

const pictures = computed(() => {
  return contributors.value.map(c => getRelationship(c, 'PERSON_PICTURE'))
})

const container = ref<HTMLUListElement>()

useFocusZone({
  containerRef: container,
  bindKeys: FocusKeys.ArrowAll | FocusKeys.HomeAndEnd,
  focusInStrategy: 'closest',
  focusOutBehavior: 'wrap',
})
</script>

<template>
  <Block as="section" :title="$t('entities.book-contributors')" class="@container">
    <div v-if="loading" class="grid grid-cols-1 @md:grid-cols-2 @2xl:grid-cols-3 @3xl:grid-cols-4 gap-6">
      <div
        v-for="i in 5"
        :key="i"
        class="flex gap-4 items-center"
      >
        <Avatar loading />

        <div class="grow">
          <div class="skeleton w-3/4 h-5" />
          <div class="mt-1 skeleton w-24 h-4" />
        </div>
      </div>
    </div>

    <ul
      v-else
      ref="container"
      class="grid grid-cols-1 @md:grid-cols-2 @2xl:grid-cols-3 @3xl:grid-cols-4 gap-6"
    >
      <li
        v-for="(contributor, idx) in contributors"
        :key="contributor.id"
        class="group relative flex gap-4 items-center w-full"
      >
        <Avatar
          kind="gray"
          empty-style="letter"
          class="relative z-10 shrink-0"
          :picture-url="
            createImageUrl({
              fileName: pictures[idx]?.attributes?.versions?.['128'],
              timeHex: pictures[idx]?.attributes?.timeHex,
            })
          "
          :alt="contributor.attributes!.person.name"
          :letter="contributor.attributes!.person.name.charAt(0)"
          :letter-id="contributor.attributes!.person.id"
        />
        <div class="text-sm grow min-w-0">
          <RouterLink
            :class="[
              'block font-medium dark:text-gray-200 focus:outline-none truncate',
              'motion-safe:transition',
              'rounded focus-visible:ring-2 focus-visible:ring-black dark:focus-visible:ring-white/90'
            ]"
            :to="{ name: 'people-id', params: { id: contributor.attributes.person.id } }"
            :title="$t('common-actions.go-to-page', [contributor.attributes.person.name])"
          >
            <span class="absolute -inset-2 z-20 rounded-xl" />
            <span class="relative z-10">{{ contributor.attributes.person.name }}</span>
          </RouterLink>
          <span class="relative z-10 block text-gray-800 dark:text-gray-400 truncate">
            {{ contributor.attributes.role.name }}
          </span>
        </div>
        <div
          class="absolute -inset-2 scale-95 bg-gray-200/50 dark:bg-gray-800 motion-safe:transition opacity-0 group-hover:opacity-100 group-hover:scale-100 rounded-lg"
        />
      </li>
    </ul>
  </Block>
</template>
