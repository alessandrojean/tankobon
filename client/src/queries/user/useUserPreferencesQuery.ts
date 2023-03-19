import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import { getMyPreferences } from '@/services/tankobon-preferences'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { Preferences } from '@/types/tankobon-preference'

type ErrorResponse = TankobonApiError | Error

export default function useUserPreferencesQuery<Select = Preferences>(
  options?: UseQueryOptions<Preferences, ErrorResponse, Select>
) {
  return useQuery<Preferences, ErrorResponse, Select>({
    queryKey: ['preferences'],
    queryFn: async () => {
      try {
        const preferences = await getMyPreferences()

        return Object.fromEntries(
          preferences.map(({ attributes }) => [attributes.key, attributes.value])
        )
      } catch (_) {
        return {}
      }
    },
    ...options,
  })
}
