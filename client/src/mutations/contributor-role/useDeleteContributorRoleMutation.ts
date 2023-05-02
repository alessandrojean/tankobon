import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { deleteOneContributorRole } from '@/services/tankobon-contributor-roles'

type ErrorResponse = TankobonApiError | Error

export default function useDeleteContributorRoleMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, string>({
    mutationFn: deleteOneContributorRole,
    onSuccess(_, contributorRoleId) {
      queryClient.invalidateQueries(['contributor-roles'])
      queryClient.invalidateQueries(['contributor-role', { id: contributorRoleId }])
    },
  })
}
