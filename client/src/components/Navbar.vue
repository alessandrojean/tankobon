<script lang="ts" setup>
import { ShowAsideDialogKey } from '@/symbols'
import { injectStrict } from '@/utils/injetion'
import { Bars3Icon } from '@heroicons/vue/20/solid'

export interface NavbarProps {
  transparent?: boolean
}

const props = withDefaults(defineProps<NavbarProps>(), {
  transparent: false
})

const { transparent } = toRefs(props)

const isScrolling = ref(false)

function handleScroll() {
  isScrolling.value = window.scrollY > 0
}

onMounted(() => window.addEventListener('scroll', handleScroll))
onUnmounted(() => window.removeEventListener('scroll', handleScroll))

const showAsideDialog = injectStrict(ShowAsideDialogKey)
</script>

<template>
  <nav
    :class="[
      'z-20 bg-gray-800 supports-backdrop-blur:bg-gray-800/95',
      'backdrop-blur sm:backdrop-filter-none md:backdrop-blur',
      'transition duration-300 ease-in-out sm:left-16 md:left-0',
      'dark:border-b dark:border-gray-700',
      'motion-safe:transition-colors motion-safe:duration-300',
      transparent && !isScrolling 
        ? '!bg-transparent !backdrop-blur-none !border-transparent'
        : '',
    ]"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6">
      <div class="flex items-center h-16">
        <button
          @click="showAsideDialog()"
          class="lg:hidden p-1 mr-2 rounded-full text-gray-300 hover:text-white transition-shadow motion-reduce:transition-none focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-primary-500 focus-visible:ring-offset-gray-800"
        >
          <span class="sr-only">
            {{ $t('common-actions.open-menu') }}
          </span>
          <span aria-hidden="true">
            <Bars3Icon class="h-6 w-6" />
          </span>
        </button>

        <Logo
          class="ml-1 lg:hidden"
          label="Tankobon"
          dark
          icon-only
        />

        <div class="ml-auto inline-flex">
          <ThemeToggle :transparent="transparent && !isScrolling" />
          <ProfileMenu :transparent="transparent && !isScrolling" />
        </div>
      </div>
    </div>
  </nav>
</template>