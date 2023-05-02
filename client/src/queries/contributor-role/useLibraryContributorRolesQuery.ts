import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { ContributorRoleEntity } from '@/types/tankobon-contributor-role'
import type { GetAllContributorRolesByLibraryParameters } from '@/services/tankobon-contributor-roles'
import { getAllContributorRolesByLibrary } from '@/services/tankobon-contributor-roles'
import type { MaybeRefDeep } from '@/types/reactivity'

type UseLibraryContributorRolesQueryOptions<S = PaginatedResponse<ContributorRoleEntity>> =
  UseQueryOptions<PaginatedResponse<ContributorRoleEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllContributorRolesByLibraryParameters>

type ErrorResponse = TankobonApiError | Error

export default function useLibraryContributorRolesQuery<S = PaginatedResponse<ContributorRoleEntity>>(
  options: UseLibraryContributorRolesQueryOptions<S>,
) {
  return useQuery<PaginatedResponse<ContributorRoleEntity>, ErrorResponse, S>({
    queryKey: [
      'contributor-roles',
      {
        libraryId: options.libraryId,
        search: options.search,
        page: options.page,
        sort: options.sort,
        size: options.size,
        includes: options.includes,
      },
    ],
    queryFn: async () => {
      return await getAllContributorRolesByLibrary({
        libraryId: unref(options.libraryId),
        search: unref(options.search),
        page: unref(options.page),
        sort: unref(options.sort),
        size: unref(options.size),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
