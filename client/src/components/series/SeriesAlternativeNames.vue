<script setup lang="ts">
import type { AlternativeName } from '@/types/tankobon-alternative-name'
import { getFlagScriptCode, getRegionCode } from '@/utils/language'

export interface SeriesAlternativeNamesProps {
  alternativeNames?: AlternativeName[] | undefined | null
  loading?: boolean
}

withDefaults(defineProps<SeriesAlternativeNamesProps>(), {
  loading: false,
})
</script>

<template>
  <Block
    v-if="loading || (alternativeNames && alternativeNames.length > 0)"
    :title="$t('alternative-names.title')"
  >
    <div v-if="loading" class="flex flex-col gap-1.5 divide-y">
      <div
        v-for="i in 3"
        :key="i"
        class="flex items-center gap-4 pt-1.5 first:pt-0"
      >
        <Flag loading />
        <div class="skeleton w-36 h-6" />
      </div>
    </div>
    <ul v-else class="flex flex-col gap-1.5 divide-y">
      <li
        v-for="(alternativeName, i) in alternativeNames"
        :key="i"
        class="flex items-center gap-4 pt-1.5 first:pt-0"
      >
        <Flag
          :region="getRegionCode(alternativeName.language)"
          :script="getFlagScriptCode(alternativeName.language)"
        />
        <span :lang="alternativeName.language">
          {{ alternativeName.name }}
        </span>
      </li>
    </ul>
  </Block>
</template>
