<script lang="ts" setup>
import { MoonIcon, SunIcon } from '@heroicons/vue/24/outline'

import {
  ComputerDesktopIcon as ComputerDesktopIconSolid,
  DevicePhoneMobileIcon as DevicePhoneMobileIconSolid,
  MoonIcon as MoonIconSolid,
  SunIcon as SunIconSolid,
} from '@heroicons/vue/20/solid'

import Button from './form/Button.vue'
import type { Theme } from '@/App.vue'

export interface ThemeToggleProps {
  light?: boolean
  bottom?: boolean
  transparent?: boolean
}

const props = withDefaults(defineProps<ThemeToggleProps>(), {
  light: false,
  bottom: false,
  transparent: false,
})

const { light, bottom, transparent } = toRefs(props)
const notificator = useToaster()

const { t } = useI18n({ useScope: 'global' })
const localTheme = useLocalStorage<Theme>('theme', 'system')
const { data: theme } = useUserPreferencesQuery({
  select: (preferences) => {
    return preferences.theme ? preferences.theme as Theme : localTheme.value
  },
  initialData: { theme: 'system' },
  onError: async (error) => {
    await notificator.failure({
      title: t('preferences.theme-failure'),
      body: error.message,
    })
  },
})

const options = computed(() => [
  { key: 'light', icon: SunIcon, menuIcon: SunIconSolid },
  { key: 'dark', icon: MoonIcon, menuIcon: MoonIconSolid },
  {
    key: 'system',
    responsive: true,
    menuIcon: [DevicePhoneMobileIconSolid, ComputerDesktopIconSolid],
  },
])

const currentOption = computed(() => {
  return options.value.find(o => o.key === theme.value)
})

const { mutateAsync: setPreferences } = useSetPreferencesMutation()

async function setTheme(theme: Theme) {
  await setPreferences({ theme })
  localTheme.value = theme
}
</script>

<template>
  <Listbox
    :model-value="theme"
    as="div"
    :class="[
      'relative flex items-center justify-center',
      { light, transparent },
    ]"
    @update:model-value="setTheme"
  >
    <ListboxButton
      :as="Button"
      :title="t('theme-toggle.label')"
      :kind="transparent ? 'navbar-light' : 'navbar-dark-elevated'"
      size="mini"
      class="w-8 h-8"
    >
      <span class="sr-only">
        {{ t('theme-toggle.label') }}
      </span>
      <template v-if="theme === 'system'">
        <!-- eslint-disable-next-line prettier/prettier -->
        <Component
          :is="options[0].icon"
          class="w-6 h-6 dark:hidden system"
        />
        <Component
          :is="options[1].icon"
          class="w-6 h-6 hidden dark:block system"
        />
      </template>
      <Component
        :is="currentOption!.icon"
        v-else
        class="w-6 h-6 not-system"
      />
    </ListboxButton>
    <MenuTransition>
      <ListboxOptions :class="bottom ? 'is-bottom' : ''" class="theme-options">
        <ListboxOption
          v-for="option in options"
          :key="option.key"
          v-slot="{ active }"
          :value="option.key"
          as="template"
        >
          <li :class="active ? 'active' : ''" class="theme">
            <span aria-hidden="true">
              <Component
                :is="option.menuIcon"
                v-if="!option.responsive"
                class="w-5 h-5 mr-3"
              />
              <template v-else>
                <Component
                  :is="option.menuIcon[0]"
                  :class="theme === 'system' ? 'system' : 'not-system'"
                  class="w-5 h-5 mr-3 lg:hidden"
                />
                <Component
                  :is="option.menuIcon[1]"
                  :class="theme === 'system' ? 'system' : 'not-system'"
                  class="w-5 h-5 mr-3 hidden lg:inline-block"
                />
              </template>
            </span>
            <span>
              {{ t(`theme-toggle.${option.key}`) }}
            </span>
          </li>
        </ListboxOption>
      </ListboxOptions>
    </MenuTransition>
  </Listbox>
</template>

<style lang="postcss" scoped>
.theme-chooser svg.not-system {
  @apply !text-blue-300;
}

.light .theme-chooser svg.not-system {
  @apply !text-blue-400;
}

.light .theme-chooser:hover svg.system {
  @apply text-gray-500 dark:text-gray-300;
}

.transparent .theme-chooser {
  svg.system,
  svg.not-system {
    @apply text-white/80;
  }

  &:where(:focus-visible, :hover) {
    svg.system,
    svg.not-system {
      @apply text-white/95;
    }
  }
}

.theme-options {
  @apply absolute top-full right-0 space-y-1 overflow-hidden
    bg-white dark:bg-gray-800 dark:ring-1 dark:ring-gray-700
    rounded-xl shadow-primer-overlay dark:shadow-primer-overlay-dark
    min-w-[9rem] p-2 mt-2 origin-top-right
    ring-1 ring-black ring-opacity-5;
}

.theme-options.is-bottom {
  @apply top-0 -translate-y-full origin-bottom-right -mt-2;
}

.theme-options:focus {
  @apply outline-none;
}

.theme {
  @apply flex items-center select-none rounded-lg
    w-full px-2 py-1.5 text-sm cursor-pointer
    text-gray-700 dark:text-gray-300;
}

.theme:hover,
.theme:focus-visible,
.theme.active {
  @apply bg-primary-100 text-primary-700
    dark:bg-primary-600 dark:text-primary-100;
}

.theme:focus {
  @apply outline-none;
}

.theme svg {
  @apply text-gray-500 dark:text-gray-400;
}

.theme:hover svg,
.theme:focus-visible svg,
.theme.active svg {
  @apply text-primary-600 dark:text-primary-200;
}
/*
.theme[aria-selected='true'] {
  @apply text-primary-500 dark:text-primary-300 font-semibold;
}

.theme[aria-selected='true'] svg {
  @apply text-primary-500 dark:text-primary-300;
} */
</style>
