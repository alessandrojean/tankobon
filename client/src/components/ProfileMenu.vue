<script lang="ts" setup>
import {
  ArrowLeftOnRectangleIcon,
  ChevronDownIcon,
  Cog8ToothIcon,
} from '@heroicons/vue/20/solid'
import { RouterLink } from 'vue-router'
import type { NavigationFailure } from 'vue-router'
import { createImageUrl } from '@/modules/api'

export interface ProfileMenuProps {
  light?: boolean
  transparent?: boolean
}

const props = withDefaults(defineProps<ProfileMenuProps>(), {
  light: false,
  transparent: false,
})

const { light, transparent } = toRefs(props)

const { t } = useI18n({ useScope: 'global' })
const userStore = useUserStore()
const avatar = computed(() => userStore.avatar)
const me = computed(() => userStore.me)

const avatarUrl = computed(() => {
  return createImageUrl({
    fileName: avatar.value?.attributes?.versions?.['64'],
    timeHex: avatar.value?.attributes?.timeHex,
  })
})

async function signOut() {
  await userStore.signOut()
}

const menuItems = computed(() => [
  {
    icon: Cog8ToothIcon,
    text: t('settings.title'),
    to: { name: 'settings' },
  },
  {
    icon: ArrowLeftOnRectangleIcon,
    text: t('common-actions.sign-out'),
    click: signOut,
  },
])

interface HandleNavigationProps {
  navigate: (e?: MouseEvent | undefined) => Promise<void | NavigationFailure>
  close: () => void
  event: MouseEvent
}

async function handleNavigation({ navigate, close, event }: HandleNavigationProps) {
  await navigate(event)
  close()
}
</script>

<template>
  <Menu v-slot="{ open }" as="div" class="ml-3 relative inline-block">
    <div>
      <MenuButton
        class="max-w-xs p-1 flex items-center text-sm group rounded-md focus:outline-none focus-visible:ring-2"
        :class="[
          light ? 'focus-visible:ring-black' : 'focus-visible:ring-white/90',
        ]"
      >
        <span class="sr-only">{{ t('common-actions.open') }}</span>
        <Avatar :picture-url="avatarUrl" size="sm" kind="gray" />
        <span aria-hidden="true">
          <ChevronDownIcon
            class="w-5 h-5 ml-1 motion-safe:transition-transform"
            :class="[
              open ? '-scale-y-100' : '',
              light && !transparent
                ? 'text-gray-400 dark:text-gray-300 group-hover:text-gray-500 dark:group-hover:text-gray-100 group-focus-visible:text-gray-500 dark:group-focus-visible:text-gray-100'
                : '',
              !light && !transparent
                ? 'text-gray-300 group-hover:text-gray-100 group-focus-visible:text-gray-100'
                : '',
              transparent
                ? 'text-white/80 group-hover:text-white/95 group-focus-visible:text-white/95'
                : '',
            ]"
          />
        </span>
      </MenuButton>
    </div>
    <MenuTransition>
      <MenuItems
        as="ul"
        class="absolute right-6 md:right-0 z-40 min-w-[15rem] mt-2 p-2 flex flex-col gap-1 origin-top-right bg-white dark:bg-gray-800 dark:ring-gray-700 rounded-xl shadow-primer-overlay dark:shadow-primer-overlay-dark ring-1 ring-black ring-opacity-5 focus:outline-none"
      >
        <MenuItem v-slot="{ active, close }" as="li">
          <RouterLink
            v-slot="{ href, navigate, isExactActive }"
            :to="{ name: 'users-id', params: { id: 'me' } }"
            custom
          >
            <a
              class="p-2 flex gap-3 text-sm select-none rounded-lg focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-primary-500 focus-visible:ring-offset-gray-700 ui-active:bg-primary-100 ui-active:text-primary-700 dark:ui-active:bg-primary-600 dark:ui-active:text-primary-100"
              :data-headlessui-state="active ? 'active' : undefined"
              :href="href"
              :aria-current="isExactActive ? 'page' : undefined"
              @click="handleNavigation({ navigate, close, event: $event })"
            >
              <Avatar class="shrink-0" :picture-url="avatarUrl" kind="gray" />
              <div class="min-w-0 grow">
                <p class="font-medium truncate">{{ me?.attributes.name ?? '' }}</p>
                <p
                  class="text-xs text-gray-600 dark:text-gray-400 truncate ui-active:text-primary-600 dark:ui-active:text-primary-200"
                >
                  {{ me?.attributes.email ?? '' }}
                </p>
              </div>
            </a>
          </RouterLink>
        </MenuItem>
        <hr class="my-1 dark:border-gray-700">
        <MenuItem
          v-for="(item, i) in menuItems"
          v-slot="{ active, close }"
          :key="i"
          as="li"
        >
          <Component
            :is="item.to ? RouterLink : 'button'"
            :to="item.to"
            class="group rounded-lg flex items-center w-full px-2 py-1.5 text-sm text-gray-700 dark:text-gray-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-primary-500 focus-visible:ring-offset-gray-700 ui-active:bg-primary-100 ui-active:text-primary-700 dark:ui-active:bg-primary-600 dark:ui-active:text-primary-100"
            :data-headlessui-state="active ? 'active' : undefined"
            @click.stop="item.click ? item.click() : close()"
          >
            <Component
              :is="item.icon"
              class="w-5 h-5 mr-3 ui-active:text-primary-600 dark:ui-active:text-primary-100 text-gray-500 dark:text-gray-400"
            />
            <span>{{ item.text }}</span>
          </component>
        </MenuItem>
      </MenuItems>
    </MenuTransition>
  </Menu>
</template>
