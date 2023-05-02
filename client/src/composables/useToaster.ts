import type { Toast } from '@/App.vue'
import { ShowToastKey } from '@/symbols'

type SimpleToast = Omit<Toast, 'type'>

export function useToaster() {
  const showToast = inject(ShowToastKey)!

  async function toastSimple(notification: SimpleToast, type: Toast['type']) {
    await showToast({ ...notification, type })
  }

  return {
    showToast,
    success: async (notification: SimpleToast) => {
      await toastSimple(notification, 'success')
    },
    failure: async (notification: SimpleToast) => {
      await toastSimple(notification, 'failure')
    },
    info: async (notification: SimpleToast) => {
      await toastSimple(notification, 'info')
    },
    warning: async (notification: SimpleToast) => {
      await toastSimple(notification, 'warning')
    },
  }
}
