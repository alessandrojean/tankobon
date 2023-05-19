import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { setMyPreferencesValuesByKeys } from '@/services/tankobon-preferences'
import type { Preferences } from '@/types/tankobon-preference'

type ErrorResponse = TankobonApiError | Error

export default function useSetPreferencesMutation() {
  const queryClient = useQueryClient()

  return useMutation<Preferences, ErrorResponse, Preferences>({
    mutationFn: async (preferences: Preferences) => {
      const persisted = await setMyPreferencesValuesByKeys(preferences)

      return Object.fromEntries(
        persisted.map(({ attributes }) => [attributes.key, attributes.value]),
      )
    },
    onSuccess(_, preferences) {
      queryClient.setQueryData<Preferences>(['preferences'], old => ({
        ...old,
        ...preferences,
      }))
    },
  })
}
