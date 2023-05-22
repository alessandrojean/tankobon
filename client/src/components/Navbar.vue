<script lang="ts" setup>
import { Bars3Icon } from '@heroicons/vue/20/solid'
import { MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import { ShowAsideDialogKey } from '@/symbols'
import { injectStrict } from '@/utils/injetion'

export interface NavbarProps {
  transparent?: boolean
}

const props = withDefaults(defineProps<NavbarProps>(), {
  transparent: false,
})

const { transparent } = toRefs(props)

const { y: scrollY } = useScroll(window)
const isScrolling = computed(() => scrollY.value > 0)

const showAsideDialog = injectStrict(ShowAsideDialogKey)
</script>

<template>
  <nav
    class="z-20 bg-gray-900 supports-[backdrop-filter]:bg-gray-900/95 backdrop-blur sm:backdrop-filter-none md:backdrop-blur transition duration-300 ease-in-out sm:left-16 md:left-0 dark:border-b dark:border-gray-800 motion-safe:transition-colors motion-safe:duration-300"
    :class="[
      transparent && !isScrolling
        ? '!bg-transparent !backdrop-blur-none !border-transparent'
        : '',
    ]"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6">
      <div class="flex items-center h-16">
        <Button
          class="lg:hidden -ml-1 mr-2 w-8 h-8"
          :kind="transparent && !isScrolling ? 'navbar-light' : 'navbar-dark-elevated'"
          size="mini"
          :title="$t('common-actions.open-menu')"
          @click="showAsideDialog()"
        >
          <span class="sr-only">
            {{ $t('common-actions.open-menu') }}
          </span>
          <Bars3Icon class="h-5 w-5" />
        </Button>

        <Logo
          class="ml-1 lg:hidden"
          label="Tankobon"
          dark
          icon-only
        />

        <LibrarySelector :transparent="transparent && !isScrolling" />

        <div class="ml-auto inline-flex items-center">
          <SearchButton
            class="hidden lg:block mr-3"
            :transparent="transparent && !isScrolling"
          />

          <Button
            class="h-8 w-8 mr-2 lg:hidden"
            :kind="transparent && !isScrolling ? 'navbar-light' : 'navbar-dark-elevated'"
            size="mini"
            :title="$t('common-actions.search-collection')"
          >
            <div class="sr-only">
              {{ $t('common-actions.search-collection') }}
            </div>
            <MagnifyingGlassIcon class="w-5 h-5" />
          </Button>

          <ThemeToggle class="ml-1" :transparent="transparent && !isScrolling" />
          <ProfileMenu class="-mr-1" :transparent="transparent && !isScrolling" />
        </div>
      </div>
    </div>
  </nav>
</template>
