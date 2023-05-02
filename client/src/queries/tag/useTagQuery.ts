import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { MaybeRef } from '@vueuse/core'
import type { TankobonApiError } from '@/types/tankobon-response'
import { getOneTag } from '@/services/tankobon-tags'
import type { TagEntity, TagIncludes } from '@/types/tankobon-tag'

interface UseTagQueryOptions<S = TagEntity> extends UseQueryOptions<TagEntity, ErrorResponse, S> {
  tagId: MaybeRef<string>
  includes?: MaybeRef<TagIncludes[]>
}

type ErrorResponse = TankobonApiError | Error

export default function useTagQuery<S = TagEntity>(
  options: UseTagQueryOptions<S>,
) {
  return useQuery<TagEntity, ErrorResponse, S>({
    queryKey: ['tag', { id: options.tagId }],
    queryFn: async () => {
      return await getOneTag({
        tagId: unref(options.tagId),
        includes: unref(options.includes),
      })
    },
    ...options,
  })
}
