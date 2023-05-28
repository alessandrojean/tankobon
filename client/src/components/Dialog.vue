<script lang="ts" setup>
import { Dialog as HeadlessUiDialog } from '@headlessui/vue'
import { XMarkIcon } from '@heroicons/vue/20/solid'
import { SetDialogOpenKey } from '@/symbols'
import { injectStrict } from '@/utils/injetion'

export interface DialogProps {
  as?: string
  bodyPaddingless?: boolean
  description?: string
  dialogClass?: string
  fullHeight?: boolean
  isOpen: boolean
  title?: string
}

const props = withDefaults(defineProps<DialogProps>(), {
  as: 'div',
  bodyPaddingless: false,
  dialogClass: 'max-w-3xl',
  fullHeight: true,
})

const emit = defineEmits<{ (e: 'close'): void }>()

const { dialogClass, fullHeight, isOpen } = toRefs(props)

onBeforeRouteLeave(() => {
  if (!isOpen.value) {
    return true
  }

  emit('close')
  return false
})

const setDialogOpen = injectStrict(SetDialogOpenKey)

watch(isOpen, open => setDialogOpen(open))
</script>

<script lang="ts">
export default { inheritAttrs: false }
</script>

<template>
  <TransitionRoot
    appear
    as="template"
    :show="isOpen"
    @close="$emit('close')"
  >
    <HeadlessUiDialog
      static
      :open="isOpen"
      @close="$emit('close')"
    >
      <div
        class="fixed z-30 inset-0 flex flex-col items-center px-4 sm:py-6 sm:px-6 md:px-0 md:py-12 lg:py-16"
        :class="[
          { 'justify-center': !fullHeight },
        ]"
      >
        <TransitionChild
          as="template"
          enter="motion-safe:transition-opacity duration-200 ease-in-out-primer"
          enter-from="opacity-0"
          enter-to="opacity-100"
          leave="motion-safe:transition-opacity duration-50 ease-in"
          leave-from="opacity-100"
          leave-to="opacity-0"
        >
          <slot name="overlay" :close="() => $emit('close')">
            <div
              class="fixed inset-0 bg-gray-700/75 dark:bg-gray-950/80 motion-safe:transition-opacity"
              @click="$emit('close')"
            />
          </slot>
        </TransitionChild>

        <TransitionChild
          :as="as"
          class="relative flex flex-col w-full will-change-transform overflow-hidden text-left bg-white dark:bg-gray-900 shadow-xl rounded-lg ring-1 ring-black/5"
          :class="[
            dialogClass,
            { 'h-full': fullHeight, 'max-h-full': !fullHeight },
          ]"
          v-bind="$attrs"
          enter="motion-reduce:transition-none duration-200 ease-in-out-primer"
          enter-from="opacity-0 scale-75"
          enter-to="opacity-100 scale-100"
          leave="motion-reduce:transition-none duration-50 ease-in"
          leave-from="opacity-100"
          leave-to="opacity-0"
        >
          <div
            class="relative overflow-hidden bg-gray-50 dark:bg-gray-900 shrink-0 px-4 md:px-6 py-4 md:py-5 border-b border-gray-200 dark:border-gray-700"
          >
            <slot name="header">
              <div class="flex items-center">
                <div v-if="title" class="flex-grow">
                  <DialogTitle
                    class="text-xl font-medium font-display-safe leading-6"
                  >
                    {{ title }}
                  </DialogTitle>
                  <DialogDescription
                    v-if="description"
                    class="hidden sm:block mt-0.5 text-sm text-gray-700 dark:text-gray-300"
                  >
                    {{ description }}
                  </DialogDescription>
                </div>

                <Button
                  class="w-10 -mr-2"
                  kind="ghost-alt"
                  :title="$t('common-actions.close')"
                  @click="$emit('close')"
                >
                  <span class="sr-only">{{ $t('common-actions.close') }}</span>
                  <XMarkIcon class="w-5 h-5" />
                </Button>
              </div>
            </slot>
          </div>

          <div
            class="flex-grow overflow-y-auto"
            :class="[
              !bodyPaddingless ? 'p-4 md:p-6' : '',
            ]"
          >
            <slot />
          </div>

          <div
            v-if="$slots.footer"
            class="shrink-0 border-t border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 px-4 md:px-6 py-3 md:py-4"
          >
            <slot name="footer" />
          </div>
        </TransitionChild>
      </div>
    </HeadlessUiDialog>
  </TransitionRoot>
</template>
