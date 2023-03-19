<script lang="ts" setup>
import type { LibraryEntity } from '@/types/tankobon-library'

export interface LibrarySelectorProps {
  modelValue: LibraryEntity,
  options: LibraryEntity[],
}

export type LibrarySelectorEmits = {
  (e: 'update:model-value', library: LibraryEntity): void
}

const props = defineProps<LibrarySelectorProps>()
defineEmits<LibrarySelectorEmits>()

const { modelValue: library, options: libraries } = toRefs(props)

const { t } = useI18n()
const userStore = useUserStore()

const hasShared = computed(() => {
  return libraries.value?.some((l) => {
    return l.relationships?.find((r) => r.type === 'OWNER')?.id !== userStore.me!.id
  })
})

function sharedText(library: LibraryEntity) {
  if (!hasShared.value) {
    return null
  }

  const owner = library.relationships!.find((r) => r.type === 'OWNER')!

  return owner.id === userStore.me!.id
    ? t('libraries.yours')
    : `${t('libraries.shared')} · ${owner.attributes.name}`
}
</script>

<template>
  <BasicListbox
    v-if="libraries && libraries.length > 1"
    :model-value="library"
    :options="libraries ?? []"
    :option-value="(library: LibraryEntity) => library.id"
    :option-text="(library: LibraryEntity) => library.attributes?.name ?? ''"
    @update:model-value="userStore.changeSelectedLibrary($event)"
  >
    <template #button>
      <div>
        <span class="block truncate">
          {{ library?.attributes.name }}
        </span>
        <span
          class="block truncate text-xs opacity-80"
          v-if="library && sharedText(library)"
        >
          {{ library ? sharedText(library) : '' }}
        </span>
      </div>
    </template>
    <template #option="{ option, selected }">
      <div>
        <span
          :class="[
            selected ? 'font-medium' : 'font-normal',
            'block truncate text-sm'
          ]"
        >
          {{ (option as LibraryEntity).attributes.name }}
        </span>
        <span
          class="block truncate text-xs opacity-80"
          v-if="library && sharedText(library)"
        >
          {{ sharedText(option) }}
        </span>
      </div>
    </template>
  </BasicListbox>
</template>
