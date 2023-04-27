import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { CollectionResponse, PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import { ContributorRoleEntity } from '@/types/tankobon-contributor-role'
import { getAllContributorsByBook, GetAllContributorsByBookParameters } from '@/services/tankobon-book-contributors'
import { MaybeRefDeep } from '@/types/reactivity'
import { ContributorEntity } from '@/types/tankobon-contributor'

type UseBookContributorsQueryOptions<S = CollectionResponse<ContributorEntity>> =
  UseQueryOptions<CollectionResponse<ContributorEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllContributorsByBookParameters>

type ErrorResponse = TankobonApiError | Error

export default function useBookContributorsQuery<S = CollectionResponse<ContributorEntity>>(
  options: UseBookContributorsQueryOptions<S>
) {
  return useQuery<CollectionResponse<ContributorEntity>, ErrorResponse, S>({
    queryKey: [
      'book-contributors',
      {
        bookId: options.bookId,
        includes: options.includes,
      }
    ],
    queryFn: async () => {
      return await getAllContributorsByBook({ 
        bookId: unref(options.bookId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
