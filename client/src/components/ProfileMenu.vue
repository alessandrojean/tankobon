<script lang="ts" setup>
import { API_BASE_URL } from '@/modules/api';
import {
  Cog8ToothIcon,
  ChevronDownIcon,
  ArrowLeftOnRectangleIcon,
} from '@heroicons/vue/20/solid'

export interface ProfileMenuProps {
  light?: boolean
  transparent?: boolean
}

const props = withDefaults(defineProps<ProfileMenuProps>(), {
  light: false,
  transparent: false
})

const { light, transparent } = toRefs(props)

const { t } = useI18n({ useScope: 'global' })
const userStore = useUserStore()

const avatarUrl = computed(() => {
  return userStore.avatarUrls ? 
    `${API_BASE_URL}/images/avatars/${userStore.avatarUrls['64']}`
    : null
})

async function signOut() {
  await userStore.signOut()
}
</script>

<template>
  <Menu as="div" class="ml-3 relative inline-block" v-slot="{ open }">
    <div>
      <MenuButton
        class="max-w-xs flex items-center text-sm focus:outline-none group"
      >
        <span class="sr-only">{{ t('common-actions.open') }}</span>
        <div
          :class="[
            'w-8 h-8 rounded-full bg-cover shadow-avatar transition-shadow motion-reduce:transition-none group-focus-visible:ring-2 group-focus-visible:ring-offset-2 group-focus-visible:ring-primary-500',
            light
              ? 'group-focus-visible:ring-offset-white dark:group-focus-visible:ring-offset-gray-900'
              : 'group-focus-visible:ring-offset-gray-700'
          ]"
          :style="{ backgroundImage: avatarUrl ? `url('${avatarUrl}')` : undefined }"
        />
        <span aria-hidden="true">
          <ChevronDownIcon
            :class="[
              open ? '-scale-y-100' : '',
              'w-5 h-5 ml-1 motion-safe:transition-transform',
              light && !transparent
                ? 'text-gray-400 dark:text-gray-300 group-hover:text-gray-500 dark:group-hover:text-gray-100 group-focus-visible:text-gray-500 dark:group-focus-visible:text-gray-100'
                : '',
              !light && !transparent
                ? 'text-gray-300 group-hover:text-gray-100 group-focus-visible:text-gray-100'
                : '',
              transparent
                ? 'text-white/80 group-hover:text-white/95 group-focus-visible:text-white/95'
                : ''
            ]"
          />
        </span>
      </MenuButton>
    </div>
    <ScaleTransition>
      <MenuItems
        as="ul"
        class="absolute right-6 md:right-0 z-40 w-48 mt-2 py-1 origin-top-right bg-white dark:bg-gray-700 divide-y divide-gray-100 dark:divide-gray-600 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none"
      >
        <div class="pb-1">
          <MenuItem v-slot="{ active }">
            <RouterLink
              :to="{ name: 'welcome' }"
              :class="[
                active
                  ? 'bg-gray-100 dark:bg-gray-600 md:dark:bg-gray-600/50'
                  : '',
                'group flex items-center w-full px-4 py-2.5 text-sm text-gray-700 dark:text-gray-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-primary-500 focus-visible:ring-offset-gray-700'
              ]"
            >
              <span aria-hidden="true">
                <Cog8ToothIcon
                  class="w-5 h-5 mr-3 text-gray-500 dark:text-gray-400"
                />
              </span>
              {{ t('settings.title') }}
            </RouterLink>
          </MenuItem>
        </div>
        <div class="pt-1">
          <MenuItem v-slot="{ active }">
            <button
              type="button"
              :class="[
                active ? 'bg-gray-100 dark:bg-gray-600' : '',
                'group flex items-start w-full px-4 py-2.5 text-sm text-gray-700 dark:text-gray-300 dark:hover:bg-gray-600 dark:md:hover:bg-gray-600/50 dark:hover:text-gray-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-primary-500 focus-visible:ring-offset-gray-700'
              ]"
              @click.stop="signOut"
            >
              <span aria-hidden="true">
                <ArrowLeftOnRectangleIcon
                  class="w-5 h-5 mr-3 text-red-500 group-hover:text-red-600 dark:text-red-400 dark:group-hover:text-red-500"
                />
              </span>
              {{ t('common-actions.sign-out') }}
            </button>
          </MenuItem>
        </div>
      </MenuItems>
    </ScaleTransition>
  </Menu>
</template>