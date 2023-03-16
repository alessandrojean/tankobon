import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import { TankobonApiError, type TankobonErrorResponse } from '@/types/tankobon-response'
import type {
  TankobonPreferenceEntity,
  TankobonPreferenceCollectionResponse,
  Preferences
} from '@/types/tankobon-preference'

export async function getMyPreferences(): Promise<TankobonPreferenceEntity[]> {
  try {
    const { data: me } = await api.get<TankobonPreferenceCollectionResponse>('users/me/preferences')

    return me.data
  } catch (e) {
    if (isAxiosError<TankobonErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function setMyPreferencesValuesByKeys(preferences: Preferences) {
  try {
    const { data } = await api.post<TankobonPreferenceCollectionResponse>(
      'users/me/preferences/batch',
      preferences,
    )

    return data.data
  } catch (e) {
    if (isAxiosError<TankobonErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
