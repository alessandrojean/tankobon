<script lang="ts" setup>
import type { ComputedRef, FunctionalComponent } from 'vue'
import type { RouteLocation, RouteLocationRaw } from 'vue-router'

import {
  ArchiveBoxIcon,
  BookOpenIcon,
  BuildingLibraryIcon,
  BuildingOffice2Icon,
  BuildingStorefrontIcon,
  ChevronDoubleLeftIcon,
  HomeIcon,
  PaintBrushIcon,
  PresentationChartLineIcon,
  QueueListIcon,
  ServerStackIcon,
  Square2StackIcon,
  TagIcon,
  UsersIcon,
} from '@heroicons/vue/24/outline'
import { ArrowTopRightOnSquareIcon } from '@heroicons/vue/20/solid'
import { breakpointsTailwind } from '@vueuse/core'

export interface Item {
  key: string
  label: string
  icon?: FunctionalComponent
  to: RouteLocationRaw
  exact?: boolean
  external?: boolean
  active?: (() => boolean) | ComputedRef<boolean>
  isAdminOnly?: boolean
}

export interface AsideMenuProps {
  collapsible?: boolean
  dark?: boolean
  isAdmin?: boolean
}

const props = withDefaults(defineProps<AsideMenuProps>(), {
  collapsible: false,
  isAdmin: false,
})

const emit = defineEmits<{ (e: 'navigate', location: RouteLocation): void }>()

const { isAdmin } = toRefs(props)

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()

const items = computed<Item[]>(() => [
  {
    key: 'dashboard',
    label: t('dashboard.title'),
    icon: HomeIcon,
    to: { name: 'index' },
    exact: true,
  },
  {
    key: 'statistics',
    label: t('statistics.title'),
    icon: PresentationChartLineIcon,
    to: { name: 'statistics' },
  },
  {
    key: 'libraries',
    label: t('entities.libraries'),
    icon: BuildingLibraryIcon,
    to: { name: 'libraries' },
    active: computed(() => {
      return String(router.currentRoute.value.name).includes('libraries')
    }),
  },
  {
    key: 'books',
    label: t('entities.books'),
    icon: BookOpenIcon,
    to: { name: 'books' },
    active: computed(() => {
      const routeName = String(router.currentRoute.value.name)
      return routeName.includes('books') || routeName.includes('import')
    }),
  },
  {
    key: 'collections',
    label: t('entities.collections'),
    icon: ArchiveBoxIcon,
    to: { name: 'collections' },
    active: computed(() => {
      return String(router.currentRoute.value.name).includes('collections')
    }),
  },
  {
    key: 'series',
    label: t('entities.series'),
    icon: Square2StackIcon,
    to: { name: 'series' },
    active: computed(() => {
      return String(router.currentRoute.value.name).includes('series')
    }),
  },
  {
    key: 'publishers',
    label: t('entities.publishers'),
    icon: BuildingOffice2Icon,
    to: { name: 'publishers' },
    active: computed(() => {
      return String(router.currentRoute.value.name).includes('publishers')
    }),
  },
  {
    key: 'stores',
    label: t('entities.stores'),
    icon: BuildingStorefrontIcon,
    to: { name: 'stores' },
    active: computed(() => {
      return String(router.currentRoute.value.name).includes('stores')
    }),
  },
  {
    key: 'people',
    label: t('entities.people'),
    icon: PaintBrushIcon,
    to: { name: 'people' },
    active: computed(() => {
      return String(router.currentRoute.value.name).includes('people')
    }),
  },
  {
    key: 'contributor-roles',
    label: t('entities.contributor-roles'),
    icon: QueueListIcon,
    to: { name: 'contributor-roles' },
    active: computed(() => {
      return String(router.currentRoute.value.name).includes('contributor-roles')
    }),
  },
  {
    key: 'tags',
    label: t('entities.tags'),
    icon: TagIcon,
    to: { name: 'tags' },
    active: computed(() => {
      return String(router.currentRoute.value.name).includes('tags')
    }),
  },
  {
    key: 'users',
    label: t('entities.users'),
    icon: UsersIcon,
    to: { name: 'users' },
    isAdminOnly: true,
    active: computed(() => {
      return String(router.currentRoute.value.name).includes('users')
    }),
  },
  {
    key: 'metrics',
    label: t('metrics.header'),
    icon: ServerStackIcon,
    to: { name: 'metrics' },
    isAdminOnly: true,
  },
])

function active(
  active: undefined | (() => boolean) | ComputedRef<boolean>,
  exact: boolean | undefined,
  isExactActive: boolean,
  isActive: boolean,
): boolean {
  const activeResult
    = active && (typeof active === 'function' ? active() : active.value)

  return activeResult ?? ((exact && isExactActive) || (!exact && isActive))
}

async function handleNavigation(route: RouteLocation, event: MouseEvent) {
  event.preventDefault()
  await router.push(route)
  emit('navigate', route)
}

const collapsed = useLocalStorage('aside-collapsed', false)

const allowedItems = computed(() => {
  return items.value.filter(item => item.isAdminOnly ? isAdmin.value : true)
})

const appVersion = import.meta.env.APP_VERSION
const gitShortHash = import.meta.env.GIT_SHORT_HASH
const isDev = import.meta.env.DEV
const isNightly = import.meta.env.NIGHTLY

const versionString = (isDev || isNightly) ? gitShortHash : `v${appVersion}`
const commitLink = `https://github.com/alessandrojean/tankobon/commit/${gitShortHash}`
const releaseLink = `https://github.com/alessandrojean/tankobon/releases/tag/v${appVersion}`

const breakpoints = useBreakpoints(breakpointsTailwind)
const lgAndLarger = breakpoints.greaterOrEqual('lg')
</script>

<template>
  <aside
    class="box-content bg-gray-50 dark:bg-gray-900 border-r border-gray-200 dark:border-gray-800 h-full overflow-hidden shadow motion-safe:transition-all"
    :class="[
      collapsed && lgAndLarger ? 'w-16' : 'w-72',
    ]"
  >
    <div class="flex flex-col min-h-0 h-full">
      <div class="flex-1 overflow-y-auto overflow-x-hidden fancy-scrollbar">
        <div
          class="px-3.5 flex justify-between items-center h-16 w-[18rem] box-border motion-safe:transition-transform origin-right"
          :class="[
            collapsed && lgAndLarger ? '-translate-x-[13.85rem]' : '',
          ]"
        >
          <slot name="logo">
            <Logo :label="t('dashboard.title')" />
          </slot>

          <Button
            v-if="collapsible"
            class="w-10 h-10"
            kind="ghost"
            icon-only
            :aria-controls="$attrs.id"
            :aria-expanded="!(collapsed && lgAndLarger)"
            :title="
              collapsed && lgAndLarger
                ? t('common-actions.expand')
                : t('common-actions.collapse')
            "
            @click="collapsed = !collapsed"
          >
            <span />
            <ChevronDoubleLeftIcon
              class="w-5 h-5 collapse-icon"
              :class="[{ collapsed: collapsed && lgAndLarger }]"
            />
          </Button>
        </div>
        <nav class="mt-6 px-3">
          <ul
            class="space-y-1.5"
            :class="[
              collapsed && lgAndLarger ? 'flex flex-col items-center' : '',
            ]"
          >
            <li v-for="item in allowedItems" :key="item.key" class="w-full">
              <RouterLink
                v-slot="{ href, isActive, isExactActive, navigate, route }"
                custom
                :to="item.to"
              >
                <AsideButton
                  :item="item"
                  :href="href"
                  :target="item.external ? '_blank' : undefined"
                  :active="
                    active(item.active, item.exact, isExactActive, isActive)
                  "
                  @click="
                    item.external
                      ? navigate($event)
                      : handleNavigation(route, $event)
                  "
                />
              </RouterLink>
            </li>
          </ul>
        </nav>
      </div>

      <div v-if="$slots.footer" class="shrink-0">
        <slot name="footer" :collapsed="collapsed && lgAndLarger" :collapsible="collapsible" />
      </div>

      <div
        class="p-3.5 text-xs flex flex-col gap-1 w-44 box-border text-gray-700 dark:text-gray-400 motion-safe:transition-transform origin-right"
        :class="[collapsed && lgAndLarger ? '-translate-x-44' : '']"
      >
        <a
          :href="(isDev || isNightly) ? commitLink : releaseLink"
          target="_blank"
          :class="[
            'flex items-center gap-1.5 w-fit rounded',
            'focus:outline-none focus-visible:ring-2',
            'motion-safe:transition',
            'hocus:text-primary-600 dark:hocus:text-primary-500 hocus:underline',
            'focus-visible:ring-black dark:focus-visible:ring-white/90',
          ]"
          :title="isDev ? $t('aside-menu.commit-link') : $t('aside-menu.release-link')"
          :tabindex="collapsed && lgAndLarger ? '-1' : undefined"
        >
          <span>{{ versionString }}</span>
          <ArrowTopRightOnSquareIcon class="w-3 h-3" />
        </a>
        <p>&copy; 2023 Alessandro Jean</p>
      </div>
    </div>
  </aside>
</template>

<style lang="postcss">
.collapse-icon {
  & > path {
    @apply motion-safe:transition-[d] motion-safe:delay-150 motion-safe:duration-300;
  }

  &.collapsed > path {
    d: path('M11.25 4.5l7.5 7.5-7.5 7.5m-6-15l7.5 7.5-7.5 7.5');
  }
}
</style>
