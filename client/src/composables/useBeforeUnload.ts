import type { Fn } from '@vueuse/core'

export interface UseBeforeUnloadOptions {
  enabled: MaybeRef<boolean>
}

function preventUnload(event: BeforeUnloadEvent) {
  event.preventDefault()
  event.returnValue = ''
}

export default function useBeforeUnload(options?: UseBeforeUnloadOptions) {
  const cleanup = ref<Fn>()
  const enabled = (options?.enabled !== undefined && isRef(options.enabled))
    ? options.enabled
    : ref(true)

  watchEffect((onCleanup) => {
    if (enabled.value) {
      cleanup.value = useEventListener(window, 'beforeunload', preventUnload)

      onCleanup(() => cleanup.value?.())
    } else {
      cleanup.value?.()
    }
  })
}
