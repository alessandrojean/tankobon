import type { InjectionKey } from 'vue'
import type { Toast } from '@/App.vue'

export type ShowAsideDialog = () => void

export const ShowAsideDialogKey: InjectionKey<ShowAsideDialog>
  = Symbol('showAsideDialog')

export type ShowToast = (toast: Toast) => Promise<void>

export const ShowToastKey: InjectionKey<ShowToast> = Symbol('showToast')

export type ShowSearchPalette = () => void

export const ShowSearchPaletteKey: InjectionKey<ShowSearchPalette>
  = Symbol('showSearchPalette')
