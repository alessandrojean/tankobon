import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { claimAdmin } from '@/services/tankobon-claim'

export default function useClaimServerMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: claimAdmin,
    onSuccess() {
      queryClient.setQueryData(['claim-status'], { isClaimed: true })
    }
  })
}
