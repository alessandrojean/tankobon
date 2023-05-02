<script lang="ts" setup>
import { Dialog as HeadlessUiDialog } from '@headlessui/vue'
import type { AsideMenuProps } from '@/components/AsideMenu.vue'

export interface DashboardAsideDialogProps extends AsideMenuProps {
  isOpen?: boolean
  isAdmin?: boolean
}

const props = withDefaults(defineProps<DashboardAsideDialogProps>(), {
  isAdmin: false,
  isOpen: false,
  libraryGroups: () => [],
})

const emit = defineEmits<{ (e: 'close'): void }>()

const { isOpen, isAdmin } = toRefs(props)
</script>

<script lang="ts">
export default { inheritAttrs: false }
</script>

<template>
  <TransitionRoot as="template" :show="isOpen">
    <HeadlessUiDialog class="relative z-50" @close="emit('close')">
      <TransitionChild
        as="template"
        enter="motion-reduce:transition-none ease-out duration-300"
        enter-from="opacity-0"
        enter-to="opacity-100"
        leave="motion-reduce:transition-none ease-in duration-200"
        leave-from="opacity-100"
        leave-to="opacity-0"
      >
        <slot name="overlay" :close="() => $emit('close')">
          <div
            class="fixed inset-0 bg-gray-700/75 dark:bg-gray-950/90 motion-safe:transition-opacity"
            @click="$emit('close')"
          />
        </slot>
      </TransitionChild>

      <TransitionChild
        as="template"
        enter="motion-safe:transition ease-out duration-500"
        enter-from="opacity-0 -translate-x-full"
        enter-to="opacity-100 translate-x-0"
        leave="motion-safe:transition ease-in duration-500"
        leave-from="opacity-100 translate-x-0"
        leave-to="opacity-0 -translate-x-full"
      >
        <div
          class="fixed inset-0 w-72 max-w-full rounded-r-2xl overflow-hidden shadow-xl  ring-1 ring-black/5"
        >
          <DialogPanel class="w-full h-full overflow-y-auto overflow-x-hidden">
            <AsideMenu
              :is-admin="isAdmin"
              v-bind="$attrs"
              @navigate="$emit('close')"
            >
              <template #logo>
                <DialogTitle>
                  <Logo label="Tankobon" />
                </DialogTitle>
              </template>

              <template v-if="$slots.footer" #footer>
                <slot name="footer" />
              </template>
            </AsideMenu>
          </DialogPanel>
        </div>
      </TransitionChild>
    </HeadlessUiDialog>
  </TransitionRoot>
</template>
