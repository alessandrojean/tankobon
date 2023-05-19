import type { TankobonApiError } from '@/types/tankobon-response'

interface UseUserPreferenceOptions {
  onError?: (error: Error | TankobonApiError) => void
}

export default function useUserPreference<PValue>(key: string, defaultValue: PValue, options: UseUserPreferenceOptions = {}) {
  function encodeValue(value: PValue): string {
    if (typeof value === 'object') {
      return JSON.stringify(value)
    } else {
      return String(value)
    }
  }

  function decodeValue(value: string): PValue {
    try {
      return JSON.parse(value)
    } catch (_) {
      return value as PValue
    }
  }

  const { data: preference } = useUserPreferencesQuery({
    select: preferences => preferences[key] ? decodeValue(preferences[key]) : defaultValue,
    onError: error => options?.onError?.(error),
  })

  const { mutate: setPreferences } = useSetPreferencesMutation()

  return {
    preference: computed<PValue>({
      get: () => preference.value,
      set: newValue => setPreferences({ [key]: encodeValue(newValue) }),
    }),
  }
}
