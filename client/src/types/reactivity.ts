import type { MaybeRef } from '@vueuse/core'

export type MaybeRefDeep<T extends object> = {
  [Property in keyof T]: MaybeRef<T[Property]>
}

export type Unref<T> = T extends Ref<infer U> ? U : T

export type UnrefDeep<T extends object> = {
  [Property in keyof T]: Unref<T[Property]>
}
