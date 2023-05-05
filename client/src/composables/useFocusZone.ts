/**
 * Adapted from @primer/react package.
 */
import { focusZone } from '@primer/behaviors'
import type { FocusZoneSettings } from '@primer/behaviors'

export interface UseFocusZoneOptions extends Omit<FocusZoneSettings, 'activeDescendantControl'> {
  containerRef?: Ref<HTMLElement | undefined>
  activeDescendantFocus?: boolean | Ref<HTMLElement | undefined>
  disabled?: Ref<boolean>
}

export function useFocusZone(options: UseFocusZoneOptions = {}) {
  const containerRef = options.containerRef ?? ref<HTMLElement>()
  const useActiveDescendant = !!options.activeDescendantFocus
  const passedActiveDescendantRef
    = (typeof options.activeDescendantFocus === 'boolean' || !options.activeDescendantFocus)
      ? undefined
      : options.activeDescendantFocus
  const activeDescendantControlRef = passedActiveDescendantRef
  const disabled = options.disabled ?? ref(false)
  const abortController = ref<AbortController>()

  watchEffect((onCleanup) => {
    if (
      containerRef.value instanceof HTMLElement
      && (!useActiveDescendant || activeDescendantControlRef?.value instanceof HTMLElement)
    ) {
      if (!disabled.value && containerRef.value) {
        const vanillaSettings: FocusZoneSettings = {
          ...options,
          activeDescendantControl: activeDescendantControlRef?.value ?? undefined,
        }
        abortController.value = focusZone(containerRef.value, vanillaSettings)

        onCleanup(() => {
          abortController.value?.abort()
        })
      } else {
        abortController.value?.abort()
      }
    }
  })

  return { containerRef, activeDescendantControlRef }
}
