import type { Notification } from '@/App.vue'

export type ShowAsideDialog = () => void

export const ShowAsideDialogKey: InjectionKey<ShowAsideDialog> =
  Symbol('showAsideDialog')

export type Notificate = (notification: Notification) => Promise<void>

export const NotificateKey: InjectionKey<Notificate> = Symbol('notificate')
