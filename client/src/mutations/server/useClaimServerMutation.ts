import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { claimAdmin } from '@/services/tankobon-claim'
import type { UserEntity } from '@/types/tankobon-user'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { ClaimAdmin } from '@/types/tankobon-claim'

type ErrorResponse = TankobonApiError | Error

export default function useClaimServerMutation() {
  const queryClient = useQueryClient()

  return useMutation<UserEntity, ErrorResponse, ClaimAdmin>({
    mutationFn: claimAdmin,
    onSuccess() {
      queryClient.setQueryData(['claim-status'], { isClaimed: true })
    },
  })
}
