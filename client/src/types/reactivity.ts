import { MaybeRef } from '@vueuse/core'

export type MaybeRefDeep<T extends object> = {
  [Property in keyof T]: MaybeRef<T[Property]>
}
