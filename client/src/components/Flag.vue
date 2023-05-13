<script setup lang="ts">
import { createFlagUrl } from '@/utils/flags'

export interface FlagProps {
  loading?: boolean
  region?: string | null
  script?: 'romanized' | 'oriental'
}

const props = withDefaults(defineProps<FlagProps>(), {
  loading: false,
  region: undefined,
  script: undefined,
})

const { region } = toRefs(props)
const { locale } = useI18n()

const regionName = computed(() => {
  if (!region.value) {
    return null
  }

  const formatter = new Intl.DisplayNames(locale.value, { type: 'region' })

  return formatter.of(region.value)
})

const flagUrl = computed(() => {
  return region.value ? createFlagUrl(region.value, 'rectangle') : null
})

const showFlagImage = ref(true)
</script>

<template>
  <div
    v-if="loading"
    class="skeleton w-5 sm:w-6 aspect-[3/2] rounded-sm ring-1 ring-black/5 pointer-events-none"
  />
  <div v-else class="w-5 sm:w-6 aspect-[3/2] rounded-sm shadow ring-1 ring-black/5 pointer-events-none">
    <div v-if="flagUrl && showFlagImage" class="relative">
      <img
        :src="flagUrl"
        :alt="$t('common.flag', [regionName])"
        class="w-5 sm:w-6 aspect-[3/2] rounded-sm"
        @error="showFlagImage = false"
      >
      <img
        v-if="script"
        aria-hidden="true"
        :src="createFlagUrl(script, 'scripts')"
        class="w-2.5 h-2.5 absolute -right-1 -bottom-1 rounded-sm"
      >
    </div>
    <div
      v-else
      :class="[
        'w-5 sm:w-6 aspect-[3/2] rounded-sm',
        'flex items-center justify-center',
        'text-xs font-medium',
        'text-gray-800 dark:text-gray-200',
        'bg-gray-100 dark:bg-gray-700',
      ]"
    >
      <span class="sr-only">{{ $t('original-language.unknown') }}</span>
      <span>?</span>
    </div>
  </div>
</template>
