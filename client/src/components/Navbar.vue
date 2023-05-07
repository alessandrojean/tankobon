<script lang="ts" setup>
import { Bars3Icon } from '@heroicons/vue/20/solid'
import { BuildingLibraryIcon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
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

const librarySelectorOpen = ref(false)

function showLibrarySelector() {
  librarySelectorOpen.value = true
}

function closeLibrarySelector() {
  librarySelectorOpen.value = false
}

const userStore = useUserStore()
const libraryStore = useLibraryStore()

const library = computed(() => libraryStore.library)
const { data: userHasAtLeastTwoLibraries } = useUserLibrariesByUserQuery({
  userId: computed(() => userStore.me!.id),
  select: libraries => libraries.length > 1,
  initialData: [],
})
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
          v-if="userHasAtLeastTwoLibraries"
          class="lg:hidden -ml-1 mr-2 w-10 h-10"
          :kind="transparent && !isScrolling ? 'navbar-light' : 'navbar-dark'"
          size="mini"
          rounded="full"
          :title="$t('common-actions.open-menu')"
          @click="showAsideDialog()"
        >
          <span class="sr-only">
            {{ $t('common-actions.open-menu') }}
          </span>
          <Bars3Icon class="h-6 w-6" />
        </Button>

        <Logo
          class="ml-1 lg:hidden"
          label="Tankobon"
          dark
          icon-only
        />

        <FadeTransition>
          <Button
            v-if="userHasAtLeastTwoLibraries"
            class="h-8 -ml-2 hidden lg:flex"
            :kind="transparent && !isScrolling ? 'navbar-light' : 'navbar-dark-elevated'"
            size="mini"
            rounded="full"
            :title="$t('libraries.select')"
            @click="showLibrarySelector"
          >
            <BuildingLibraryIcon class="w-5 h-5" />
            <div>
              <span class="sr-only">{{ $t('libraries.selected') }}</span>
              {{ library?.attributes?.name }}
            </div>
          </Button>
        </FadeTransition>

        <div class="ml-auto inline-flex items-center">
          <SearchButton
            class="hidden lg:block mr-3"
            :transparent="transparent && !isScrolling"
          />

          <Button
            class="h-8 w-8 mr-2 lg:hidden"
            :kind="transparent && !isScrolling ? 'navbar-light' : 'navbar-dark'"
            size="mini"
            rounded="full"
            :title="$t('common-actions.search-collection')"
          >
            <div class="sr-only">
              {{ $t('common-actions.search-collection') }}
            </div>
            <MagnifyingGlassIcon class="w-6 h-6" />
          </Button>

          <FadeTransition>
            <Button
              v-if="userHasAtLeastTwoLibraries"
              class="h-8 w-8 mr-2 lg:hidden"
              :kind="transparent && !isScrolling ? 'navbar-light' : 'navbar-dark'"
              size="mini"
              rounded="full"
              :title="$t('libraries.select')"
              @click="showLibrarySelector"
            >
              <div class="sr-only">
                <span>{{ $t('libraries.selected') }}</span>
                {{ library?.attributes?.name }}
              </div>
              <BuildingLibraryIcon class="w-6 h-6" />
            </Button>
          </FadeTransition>

          <ThemeToggle :transparent="transparent && !isScrolling" />
          <ProfileMenu :transparent="transparent && !isScrolling" />
        </div>
      </div>
    </div>

    <LibrarySelectorDialog
      v-if="userHasAtLeastTwoLibraries"
      :is-open="librarySelectorOpen"
      @close="closeLibrarySelector"
    />
  </nav>
</template>
