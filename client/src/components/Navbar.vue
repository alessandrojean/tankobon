<script lang="ts" setup>
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
</script>

<template>
  <nav
    :class="[
      'z-20 bg-gray-800 supports-backdrop-blur:bg-gray-800/95',
      'backdrop-blur sm:backdrop-filter-none md:backdrop-blur',
      'transition duration-300 ease-in-out sm:left-16 md:left-0',
      'dark:border-b dark:bordery-gray-700',
      'motion-safe:transition-colors motion-safe:duration-300',
      transparent && !isScrolling 
        ? '!bg-transparent !backdrop-blur-none !border-transparent'
        : '',
    ]"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6">
      <div class="flex items-center h-16">
        <Logo
          class="ml-1 lg:hidden"
          label="Tankobon"
          dark
          icon-only
        />

        <div class="ml-auto inline-flex">
          <ProfileMenu :transparent="transparent && !isScrolling" />
        </div>
      </div>
    </div>
  </nav>
</template>