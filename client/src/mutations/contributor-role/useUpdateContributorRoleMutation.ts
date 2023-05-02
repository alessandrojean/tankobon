import { useMutation, useQueryClient } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { ContributorRoleUpdate } from '@/types/tankobon-contributor-role'
import { updateOneContributorRole } from '@/services/tankobon-contributor-roles'

type ErrorResponse = TankobonApiError | Error

export default function useUpdateContributorRoleMutation() {
  const queryClient = useQueryClient()

  return useMutation<void, ErrorResponse, ContributorRoleUpdate>({
    mutationFn: updateOneContributorRole,
    async onSuccess(_, { id }) {
      queryClient.invalidateQueries(['contributor-roles'])
      queryClient.invalidateQueries(['contributor-role', { id }])
    },
  })
}
