<script lang="ts" setup>
import type { ComputedRef, FunctionalComponent } from 'vue'
import type { RouteLocation, RouteLocationRaw } from 'vue-router'

import {
  AcademicCapIcon,
  ArchiveBoxIcon,
  BookOpenIcon,
  BuildingOffice2Icon,
  ChevronDoubleLeftIcon,
  HomeIcon,
  QueueListIcon,
  Square2StackIcon,
  TagIcon,
  UserIcon,
} from '@heroicons/vue/24/outline'

export interface Item {
  key: string,
  label: string,
  icon?: FunctionalComponent,
  to: RouteLocationRaw,
  exact?: boolean,
  external?: boolean,
  active?: (() => boolean) | ComputedRef<boolean>,
  isAdminOnly?: boolean,
}

export interface AsideMenuProps {
  collapsible?: boolean,
  dark?: boolean,
  isAdmin?: boolean,
}

const props = withDefaults(defineProps<AsideMenuProps>(), {
  collapsible: false,
  isAdmin: false,
})

const { isAdmin } = toRefs(props)

const emit = defineEmits<{ (e: 'navigate', location: RouteLocation): void }>()

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()

const items = computed<Item[]>(() => [
  {
    key: 'dashboard',
    label: t('dashboard.title'),
    icon: HomeIcon,
    to: { name: 'index' },
    exact: true
  },
  {
    key: 'books',
    label: t('entities.books'),
    icon: BookOpenIcon,
    to: { name: 'welcome' },
  },
  {
    key: 'collections',
    label: t('entities.collections'),
    icon: ArchiveBoxIcon,
    to: { name: 'welcome' },
  },
  {
    key: 'series',
    label: t('entities.series'),
    icon: Square2StackIcon,
    to: { name: 'welcome' },
  },
  {
    key: 'publishers',
    label: t('entities.publishers'),
    icon: BuildingOffice2Icon,
    to: { name: 'welcome' },
  },
  {
    key: 'people',
    label: t('entities.people'),
    icon: AcademicCapIcon,
    to: { name: 'welcome' },
  },
  {
    key: 'contributor-roles',
    label: t('entities.contributor-roles'),
    icon: QueueListIcon,
    to: { name: 'welcome' },
  },
  {
    key: 'tags',
    label: t('entities.tags'),
    icon: TagIcon,
    to: { name: 'welcome' },
  },
  {
    key: 'users',
    label: t('entities.users'),
    icon: UserIcon,
    to: { name: 'users' },
    isAdminOnly: true,
    active: computed(() => {
      return String(router.currentRoute.value.name).includes('users')
    })
  },
])

function active(
  active: undefined | (() => boolean) | ComputedRef<boolean>,
  exact: boolean | undefined,
  isExactActive: boolean,
  isActive: boolean
): boolean {
  const activeResult =
    active && (typeof active === 'function' ? active() : active.value)

  return activeResult ?? ((exact && isExactActive) || (!exact && isActive))
}

async function handleNavigation(route: RouteLocation, event: MouseEvent) {
  event.preventDefault()
  await router.push(route)
  emit('navigate', route)
}

const collapsed = useLocalStorage('aside-collapsed', false)

const allowedItems = computed(() => {
  return items.value.filter((item) => item.isAdminOnly ? isAdmin.value : true)
})
</script>

<template>
  <aside
    :class="[
      'box-content bg-gray-50 dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 h-full overflow-hidden shadow',
      'motion-safe:transition-all',
      collapsed ? 'w-16' : 'w-72'
    ]"
  >
    <div class="flex flex-col min-h-0 h-full">
      <div class="flex-1 overflow-y-auto overflow-x-hidden fancy-scrollbar">
        <div
          :class="[
            'px-3.5 flex justify-between items-center h-16 w-[18rem] box-border',
            'motion-safe:transition-transform origin-right',
            collapsed ? '-translate-x-[13.85rem]' : ''
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
            :aria-expanded="!collapsed"
            :title="
              collapsed
                ? t('common-actions.expand')
                : t('common-actions.collapse')
            "
            @click="collapsed = !collapsed"
          >
            <span></span>
            <ChevronDoubleLeftIcon
              :class="['w-5 h-5 collapse-icon', { collapsed: collapsed }]"
            />
          </Button>
        </div>
        <nav class="mt-6 px-3">
          <ul
            :class="[
              'space-y-1.5',
              collapsed ? 'flex flex-col items-center' : ''
            ]"
          >
            <li v-for="item in allowedItems" :key="item.key" class="w-full">
              <RouterLink
                custom
                :to="item.to"
                v-slot="{ href, isActive, isExactActive, navigate, route }"
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
        <slot name="footer" :collapsed="collapsed" :collapsible="collapsible" />
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