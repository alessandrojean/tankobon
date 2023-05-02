import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { MaybeRef } from '@vueuse/core'
import type { TankobonApiError } from '@/types/tankobon-response'
import { getOneContributorRole } from '@/services/tankobon-contributor-roles'
import type { ContributorRoleEntity, ContributorRoleIncludes } from '@/types/tankobon-contributor-role'

interface UseContributorRoleQueryOptions<S = ContributorRoleEntity> extends UseQueryOptions<ContributorRoleEntity, ErrorResponse, S> {
  contributorRoleId: MaybeRef<string>
  includes?: MaybeRef<ContributorRoleIncludes[]>
}

type ErrorResponse = TankobonApiError | Error

export default function useContributorRoleQuery<S = ContributorRoleEntity>(
  options: UseContributorRoleQueryOptions<S>,
) {
  return useQuery<ContributorRoleEntity, ErrorResponse, S>({
    queryKey: ['contributor-role', { id: options.contributorRoleId }],
    queryFn: async () => {
      return await getOneContributorRole({
        contributorRoleId: unref(options.contributorRoleId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
