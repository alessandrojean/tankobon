import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { addOneContributorRole } from '@/services/tankobon-contributor-roles'
import type { ContributorRoleCreation, ContributorRoleEntity } from '@/types/tankobon-contributor-role'
import type { TankobonApiError } from '@/types/tankobon-response'

type ErrorResponse = TankobonApiError | Error

export default function useCreateContributorRoleMutation() {
  const queryClient = useQueryClient()

  return useMutation<ContributorRoleEntity, ErrorResponse, ContributorRoleCreation>({
    mutationFn: addOneContributorRole,
    onSuccess(_, creation) {
      queryClient.invalidateQueries(['contributor-roles', { libraryId: creation.library }])
    },
  })
}
