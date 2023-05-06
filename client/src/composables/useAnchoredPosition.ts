/**
 * Adapted from @primer/react package.
 */
import { getAnchoredPosition } from '@primer/behaviors'
import type { AnchorAlignment, AnchorPosition, PositionSettings } from '@primer/behaviors'

export interface UseAnchoredPositionOptions extends Partial<Omit<PositionSettings, 'align'>> {
  align?: MaybeRef<AnchorAlignment>
  floatingElementRef?: Ref<HTMLElement>
  anchorElementRef?: Ref<HTMLElement>
}

export function useAnchoredPosition(options?: UseAnchoredPositionOptions) {
  const floatingElementRef = options?.floatingElementRef ?? ref<HTMLElement>()
  const anchorElementRef = options?.anchorElementRef ?? ref<HTMLElement>()
  const position = ref<AnchorPosition>()

  const updatePosition = () => {
    if (
      floatingElementRef.value instanceof Element
      && anchorElementRef.value instanceof Element
    ) {
      const finalOptions: Partial<PositionSettings> = {
        ...options,
        align: unref(options?.align),
      }

      position.value = getAnchoredPosition(
        floatingElementRef.value,
        anchorElementRef.value,
        finalOptions,
      )
    } else {
      position.value = undefined
    }
  }

  useResizeObserver(
    [floatingElementRef, anchorElementRef, document.documentElement],
    updatePosition,
  )
  watchEffect(updatePosition)

  return {
    floatingElementRef,
    anchorElementRef,
    position,
  }
}
