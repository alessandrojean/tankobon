<script lang="ts" setup>
import { API_BASE_URL, getFullImageUrl } from '@/modules/api';
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
const avatar = computed(() => userStore.avatar)

const avatarUrl = computed(() => {
  return getFullImageUrl({
    collection: 'avatars',
    fileName: avatar.value?.attributes?.versions?.['64'],
    timeHex: avatar.value?.attributes?.timeHex,
  })
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
        <Avatar :picture-url="avatarUrl" small />
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
        class="absolute right-6 md:right-0 z-40 min-w-[9rem] mt-2 p-1.5 space-y-1 origin-top-right bg-white dark:bg-gray-800 dark:ring-1 dark:ring-gray-700 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none"
      >
        <MenuItem v-slot="{ active }">
          <RouterLink
            :to="{ name: 'welcome' }"
            :class="[
              active
                ? 'bg-primary-100 text-primary-700 dark:bg-primary-600 dark:text-primary-100'
                : '',
              'group rounded flex items-center w-full px-3 py-2 text-sm text-gray-700 dark:text-gray-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-primary-500 focus-visible:ring-offset-gray-700'
            ]"
          >
            <span aria-hidden="true">
              <Cog8ToothIcon
                :class="[
                  'w-5 h-5 mr-3',
                  active
                    ? 'text-primary-600 dark:text-primary-100' 
                    : 'text-gray-500 dark:text-gray-400',
                ]"
              />
            </span>
            {{ t('settings.title') }}
          </RouterLink>
        </MenuItem>
        <MenuItem v-slot="{ active }">
          <button
            type="button"
            :class="[
              active
                ? 'bg-primary-100 text-primary-700 dark:bg-primary-600 dark:text-primary-100'
                : '',
              'group rounded flex items-center w-full px-3 py-2 text-sm text-gray-700 dark:text-gray-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-primary-500 focus-visible:ring-offset-gray-700'
            ]"
            @click.stop="signOut"
          >
            <span aria-hidden="true">
              <ArrowLeftOnRectangleIcon
                :class="[
                  'w-5 h-5 mr-3',
                  active
                    ? 'text-primary-600 dark:text-primary-100' 
                    : 'text-gray-500 dark:text-gray-400',
                ]"
              />
            </span>
            {{ t('common-actions.sign-out') }}
          </button>
        </MenuItem>
      </MenuItems>
    </ScaleTransition>
  </Menu>
</template>