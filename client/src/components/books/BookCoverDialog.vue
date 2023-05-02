<script lang="ts" setup>
import { XMarkIcon } from '@heroicons/vue/20/solid'
import { Dialog as HeadlessUiDialog } from '@headlessui/vue'

export interface BookCoverDialogProps {
  coverUrl: string
  open?: boolean
}

const props = withDefaults(defineProps<BookCoverDialogProps>(), { open: false })

const emit = defineEmits<{ (e: 'close'): void }>()

const { open } = toRefs(props)

onBeforeRouteLeave(() => {
  if (!open.value)
    return true

  emit('close')
  return false
})
</script>

<template>
  <TransitionRoot :show="open" as="template">
    <HeadlessUiDialog as="template" @close="$emit('close')">
      <div class="dialog">
        <TransitionChild
          as="template"
          enter="motion-reduce:transition-none duration-300 ease-out"
          enter-from="opacity-0"
          enter-to="opacity-100"
          leave="motion-reduce:transition-none duration-200 ease-in"
          leave-from="opacity-100"
          leave-to="opacity-0"
        >
          <slot name="overlay" :close="() => $emit('close')">
            <div
              class="hidden sm:block fixed inset-0 bg-gray-700/75 dark:bg-gray-900/90 motion-safe:transition-opacity"
              @click="$emit('close')"
            />
          </slot>
        </TransitionChild>

        <TransitionChild
          as="template"
          enter="motion-reduce:transition-none duration-300 ease-out"
          enter-from="opacity-0 scale-95"
          enter-to="opacity-100 scale-100"
          leave="motion-reduce:transition-none duration-200 ease-in"
          leave-from="opacity-100 scale-100"
          leave-to="opacity-0 scale-95"
        >
          <DialogPanel class="dialog-content">
            <DialogTitle as="h3" class="sr-only">
              {{ $t('common-actions.zoom') }}
            </DialogTitle>

            <img
              :src="coverUrl"
              alt=""
              class="min-w-0 max-w-full min-h-0 max-h-full rounded-lg"
            >

            <div class="hidden sm:block absolute w-10 h-10 -right-12 -top-1">
              <button
                type="button"
                class="close-button"
                :title="$t('common-actions.close')"
                @click="$emit('close')"
              >
                <span aria-hidden="true">
                  <XMarkIcon class="w-6 h-6" />
                </span>
                <span class="sr-only">
                  {{ $t('common-actions.close') }}
                </span>
              </button>
            </div>
          </DialogPanel>
        </TransitionChild>
      </div>
    </HeadlessUiDialog>
  </TransitionRoot>
</template>

<style lang="postcss" scoped>
.dialog {
  @apply fixed inset-0 z-20 p-6 md:p-12 lg:p-24
    flex flex-col items-center justify-center;
}

.dialog-content {
  @apply relative flex flex-col items-center align-middle
    min-w-0 max-w-full min-h-0 max-h-full overflow-visible
    text-left bg-white dark:bg-gray-800 w-fit h-fit
    shadow-xl rounded-xl ring-1 ring-black/5;
}

.close-button {
  @apply p-1 text-white opacity-80 rounded-md motion-safe:transition-shadow;
}

.close-button:hover,
.close-button:focus {
  @apply opacity-100;
}

.close-button:focus {
  @apply outline-none;
}

.close-button:focus-visible {
  @apply ring-2 ring-primary-100 dark:ring-primary-500;
}
</style>
