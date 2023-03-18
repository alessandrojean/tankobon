import type { Notification } from '@/App.vue'
import { NotificateKey } from '@/symbols'

type SimpleNotification = Omit<Notification, 'type'>

export function useNotificator() {
  const notificate = inject(NotificateKey)!

  async function notificateSimple(notification: SimpleNotification, type: Notification['type']) {
    await notificate({ ...notification, type })
  }

  return {
    notificate,
    success: async (notification: SimpleNotification) => {
      await notificateSimple(notification, 'success')
    },
    failure: async (notification: SimpleNotification) => {
      await notificateSimple(notification, 'failure')
    },
    info: async (notification: SimpleNotification) => {
      await notificateSimple(notification, 'info')
    },
    warning: async (notification: SimpleNotification) => {
      await notificateSimple(notification, 'warning')
    }
  }
}
