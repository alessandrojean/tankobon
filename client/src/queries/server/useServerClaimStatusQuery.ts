import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import { getClaimStatus } from '@/services/tankobon-claim'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { ClaimStatus } from '@/types/tankobon-claim'

type ErrorResponse = TankobonApiError | Error

export default function useServerClaimStatusQuery(options?: UseQueryOptions<ClaimStatus, ErrorResponse>) {
  return useQuery<ClaimStatus, ErrorResponse>({
    queryKey: ['claim-status'],
    queryFn: getClaimStatus,
    initialData: { isClaimed: false },
    ...options,
  })
}
