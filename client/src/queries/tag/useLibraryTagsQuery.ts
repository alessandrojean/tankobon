import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { PaginatedResponse, TankobonApiError } from '@/types/tankobon-response'
import type { TagEntity } from '@/types/tankobon-tag'
import type { GetAllTagsByLibraryParameters } from '@/services/tankobon-tags'
import { getAllTagsByLibrary } from '@/services/tankobon-tags'
import type { MaybeRefDeep } from '@/types/reactivity'

type UseLibraryTagsQueryOptions<S = PaginatedResponse<TagEntity>> =
  UseQueryOptions<PaginatedResponse<TagEntity>, ErrorResponse, S> &
  MaybeRefDeep<GetAllTagsByLibraryParameters>

type ErrorResponse = TankobonApiError | Error

export default function useLibraryTagsQuery<S = PaginatedResponse<TagEntity>>(
  options: UseLibraryTagsQueryOptions<S>,
) {
  return useQuery<PaginatedResponse<TagEntity>, ErrorResponse, S>({
    queryKey: [
      'tags',
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
      return await getAllTagsByLibrary({
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
