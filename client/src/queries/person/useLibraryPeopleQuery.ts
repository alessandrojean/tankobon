import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import { PersonEntity } from '@/types/tankobon-person'
import { getAllPeopleByLibrary, GetAllPeopleByLibraryParameters } from '@/services/tankobon-people'
import { MaybeRefDeep } from '@/types/reactivity'

type UseLibraryPeopleQueryOptions<S = PaginatedResponse<PersonEntity>> =
  UseQueryOptions<PaginatedResponse<PersonEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllPeopleByLibraryParameters>

type ErrorResponse = TankobonApiError | Error

export default function useLibraryPeopleQuery<S = PaginatedResponse<PersonEntity>>(
  options: UseLibraryPeopleQueryOptions<S>
) {
  return useQuery<PaginatedResponse<PersonEntity>, ErrorResponse, S>({
    queryKey: [
      'people',
      {
        libraryId: options.libraryId,
        search: options.search,
        page: options.page,
        sort: options.sort,
        size: options.size,
        includes: options.includes,
      }
    ],
    queryFn: async () => {
      return await getAllPeopleByLibrary({ 
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
