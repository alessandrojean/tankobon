<script lang="ts" setup>
export interface SimpleHeaderProps {
  loading?: boolean
  subtitle?: string
  title: string
}

withDefaults(defineProps<SimpleHeaderProps>(), {
  loading: false
})
</script>

<template>
  <header>
    <div class="max-w-7xl mx-auto px-4 sm:px-6">
      <div
        :class="[
          'border-b dark:border-gray-700',
          $slots.tabs ? 'pt-6' : 'py-6'
        ]"
      >
        <div class="md:flex md:items-center md:justify-between">
          <div class="flex-1 flex items-center space-x-4">
            <slot name="avatar" />
            <div>
              <div v-if="loading" class="skeleton h-7 w-56"></div>
              <slot name="title" v-else :title="title">
                <h1
                  class="text-xl lg:text-2xl font-display font-semibold text-gray-900 dark:text-gray-100"
                >
                  {{ title }}
                </h1>
              </slot>

              <p
                class="text-gray-500 dark:text-gray-400"
                v-if="subtitle && !$slots.subtitle"
              >
                {{ subtitle }}
              </p>
              <slot
                name="subtitle"
                v-else
                :subtitle="subtitle"
                subtitleClasses="text-gray-500 dark:text-gray-400"
              />
            </div>
          </div>
          <div v-if="$slots.actions" class="mt-5 md:mt-0 md:ml-4">
            <slot name="actions" />
          </div>
        </div>

        <div v-if="$slots.tabs" class="mt-6">
          <slot name="tabs" />
        </div>
      </div>
    </div>
  </header>
</template>