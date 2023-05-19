<script lang="ts" setup>
import type { LibraryEntity } from '@/types/tankobon-library'
import { getRelationship } from '@/utils/api'

export interface LibrarySelectorProps {
  modelValue: LibraryEntity
  options: LibraryEntity[]
  size?: 'normal' | 'small'
}

export interface LibrarySelectorEmits {
  (e: 'update:model-value', library: LibraryEntity): void
}

const props = withDefaults(defineProps<LibrarySelectorProps>(), {
  size: 'normal',
})

defineEmits<LibrarySelectorEmits>()

const { modelValue: library, options: libraries } = toRefs(props)

const { t } = useI18n()
const userStore = useUserStore()

const hasShared = computed(() => {
  return libraries.value?.some((l) => {
    return getRelationship(l, 'OWNER')?.id !== userStore.me?.id
  })
})

function sharedText(library: LibraryEntity) {
  if (!hasShared.value) {
    return null
  }

  const owner = getRelationship(library, 'OWNER')!

  return owner.id === userStore.me?.id
    ? t('libraries.yours')
    : `${t('libraries.shared')} · ${owner.attributes!.name}`
}
</script>

<template>
  <BasicListbox
    v-if="libraries && libraries.length > 1"
    :size="size"
    :model-value="library"
    :options="libraries ?? []"
    :option-value="(library) => library"
    :option-text="(library) => library?.attributes?.name ?? ''"
    @update:model-value="$emit('update:model-value', $event)"
  >
    <template #button>
      <div>
        <span class="block truncate">
          {{ library?.attributes.name }}
        </span>
        <span
          v-if="library && sharedText(library)"
          class="block truncate text-xs opacity-80"
        >
          {{ library ? sharedText(library) : '' }}
        </span>
      </div>
    </template>
    <template #option="{ option, selected }">
      <div>
        <span
          class="block truncate text-sm"
          :class="[
            selected ? 'font-medium' : 'font-normal',
          ]"
        >
          {{ (option as LibraryEntity).attributes.name }}
        </span>
        <span
          v-if="library && sharedText(library)"
          class="block truncate text-xs opacity-80"
        >
          {{ sharedText(option) }}
        </span>
      </div>
    </template>
  </BasicListbox>
</template>
