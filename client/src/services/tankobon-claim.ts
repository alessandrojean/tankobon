import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import {
  TankobonApiError,
  type TankobonErrorResponse,
  type TankobonSuccessEntityResponse
} from '@/types/tankobon-response'
import type { ClaimStatus, ClaimAdmin } from '@/types/tankobon-claim'
import type { TankobonUserEntity } from '@/types/tankobon-user'

export type ClaimResponse = TankobonSuccessEntityResponse<TankobonUserEntity>

export async function getClaimStatus(): Promise<ClaimStatus> {
  try {
    const { data } = await api.get<ClaimStatus>('claim')

    return data
  } catch (e) {
    if (isAxiosError<TankobonErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function claimAdmin(admin: ClaimAdmin): Promise<TankobonUserEntity> {
  try {
    const { data } = await api.post<ClaimResponse>('claim', admin)

    return data
  } catch (e) {
    if (isAxiosError<TankobonErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}