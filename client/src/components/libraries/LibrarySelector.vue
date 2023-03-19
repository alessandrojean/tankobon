<script lang="ts" setup>
import type { LibraryEntity } from '@/types/tankobon-library'
import { getRelationship } from '@/utils/api';

export interface LibrarySelectorProps {
  modelValue: LibraryEntity,
  options: LibraryEntity[],
  size?: 'normal' | 'small',
}

export type LibrarySelectorEmits = {
  (e: 'update:model-value', library: LibraryEntity): void
}

const props = withDefaults(defineProps<LibrarySelectorProps>(), {
  size: 'normal',
})
const emit = defineEmits<LibrarySelectorEmits>()

const { modelValue: library, options: libraries } = toRefs(props)

const { t } = useI18n()
const userStore = useUserStore()

const hasShared = computed(() => {
  return libraries.value?.some((l) => {
    return getRelationship(l, 'OWNER')?.id !== userStore.me!.id
  })
})

function sharedText(library: LibraryEntity) {
  if (!hasShared.value) {
    return null
  }

  const owner = getRelationship(library, 'OWNER')!

  return owner.id === userStore.me!.id
    ? t('libraries.yours')
    : `${t('libraries.shared')} · ${owner.attributes!.name}`
}

function handleUpdate(libraryId: string) {
  const library = libraries.value.find((l) => l.id === libraryId)

  if (library) {
    emit('update:model-value', library)
  }
}
</script>

<template>
  <BasicListbox
    v-if="libraries && libraries.length > 1"
    :size="size"
    :model-value="library"
    :options="libraries ?? []"
    :option-value="(library: LibraryEntity) => library.id"
    :option-text="(library: LibraryEntity) => library.attributes?.name ?? ''"
    @update:model-value="handleUpdate"
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
