<script setup lang="ts">
import Button from '@/components/form/Button.vue'
import { Squares2X2Icon, SquaresPlusIcon, TableCellsIcon } from '@heroicons/vue/20/solid'

export interface ViewModeSelectorProps {
  disabled?: boolean
  loading?: boolean,
  preferenceKey: string
}

export type ViewMode = 'table' | 'grid-comfortable' | 'grid-compact'

const props = withDefaults(defineProps<ViewModeSelectorProps>(), {
  disabled: false,
  loading: false,
})

const { t } = useI18n()
const notificator = useToaster()

const options = computed(() => [
  { key: 'table', label: t('view-mode.table'), icon: TableCellsIcon },
  { key: 'grid-comfortable', label: t('view-mode.grid-comfortable'), icon: Squares2X2Icon },
  { key: 'grid-compact', label: t('view-mode.grid-compact'), icon: SquaresPlusIcon },
])

const localViewMode = useLocalStorage<ViewMode>(props.preferenceKey, 'grid-comfortable')

const { data: viewMode } = useUserPreferencesQuery({
  select: (preferences) => {
    return preferences[props.preferenceKey]
      ? preferences[props.preferenceKey] as ViewMode
      : localViewMode.value
  },
  initialData: { [props.preferenceKey]: 'grid-comfortable' },
  onError: async (error) => {
    await notificator.failure({
      title: t('preferences.view-mode-failure'),
      body: error.message,
    })
  },
})

const selectedIndex = computed(() => options.value.findIndex(o => o.key === viewMode.value))

const { mutateAsync: setPreferences } = useSetPreferencesMutation()

async function setViewMode(viewMode: ViewMode) {
  await setPreferences({ [props.preferenceKey]: viewMode })
  localViewMode.value = viewMode
}
</script>

<template>
  <RadioGroup
    :model-value="viewMode"
    :disabled="loading || disabled"
    @update:model-value="setViewMode"
  >
    <RadioGroupLabel class="sr-only">
      {{ $t('view-mode.label') }}
    </RadioGroupLabel>

    <div class="flex relative gap-1">
      <div
        aria-hidden="true"
        :class="[
          'w-9 h-9 rounded-md bg-primary-100 dark:bg-primary-900',
          'absolute left-0 top-0 motion-safe:transition-transform',
          'translate-x-[--offset]'
        ]"
        :style="{ '--offset': `${2.5 * selectedIndex}rem`}"
      />
      <RadioGroupOption
        v-for="option in options"
        :key="option.key"
        :value="option.key"
        :as="Button"
        class="w-9 h-9"
        kind="pill-tab"
        :title="option.label"
      >
        <span class="sr-only">{{ option.label }}</span>
        <component :is="option.icon" class="w-5 h-5" />
      </RadioGroupOption>
    </div>
  </RadioGroup>
</template>
