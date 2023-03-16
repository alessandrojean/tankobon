import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import { TankobonApiError, type ErrorResponse } from '@/types/tankobon-response'
import type {
  PreferenceEntity,
  PreferenceCollectionResponse,
  Preferences
} from '@/types/tankobon-preference'

export async function getMyPreferences(): Promise<PreferenceEntity[]> {
  try {
    const { data: me } = await api.get<PreferenceCollectionResponse>('users/me/preferences')

    return me.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function setMyPreferencesValuesByKeys(preferences: Preferences) {
  try {
    const { data } = await api.post<PreferenceCollectionResponse>(
      'users/me/preferences/batch',
      preferences,
    )

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
