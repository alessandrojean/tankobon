import type { TankobonApiError } from '@/types/tankobon-response'

interface UseUserPreferenceOptions {
  onError?: (error: Error | TankobonApiError) => void
}

export default function useUserPreference<PValue>(key: string, defaultValue: PValue, options: UseUserPreferenceOptions = {}) {
  const localPreference = useLocalStorage<PValue>(key, defaultValue)

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
    select: preferences => decodeValue(preferences[key]) ?? localPreference.value,
    onError: (error) => options?.onError?.(error),
  })

  const { mutateAsync: setPreferences } = useSetPreferencesMutation()

  watch(localPreference, async (newValue) => {
    if (encodeValue(newValue) !== encodeValue(preference.value)) {
      await setPreferences({ [key]: encodeValue(newValue) })
    }
  })

  watch(preference, (newValue) => {
    localPreference.value = newValue
  })

  return { preference: localPreference }
}