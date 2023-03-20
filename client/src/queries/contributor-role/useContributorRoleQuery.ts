import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { MaybeRef } from '@vueuse/core'
import { Includes } from '@/types/tankobon-entity'
import { getOneContributorRole } from '@/services/tankobon-contributor-roles'
import { ContributorRoleEntity } from '@/types/tankobon-contributor-role'

interface UseContributorRoleQueryOptions<S = ContributorRoleEntity> extends UseQueryOptions<ContributorRoleEntity, ErrorResponse, S> {
  contributorRoleId: MaybeRef<string>,
  includes?: MaybeRef<Includes>,
}

type ErrorResponse = TankobonApiError | Error

export default function useContributorRoleQuery<S = ContributorRoleEntity>(
  options: UseContributorRoleQueryOptions<S>
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
