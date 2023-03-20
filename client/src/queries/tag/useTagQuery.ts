import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { MaybeRef } from '@vueuse/core'
import { Includes } from '@/types/tankobon-entity'
import { getOneTag } from '@/services/tankobon-tags'
import { TagEntity } from '@/types/tankobon-tag'

interface UseTagQueryOptions<S = TagEntity> extends UseQueryOptions<TagEntity, ErrorResponse, S> {
  tagId: MaybeRef<string>,
  includes?: MaybeRef<Includes>,
}

type ErrorResponse = TankobonApiError | Error

export default function useTagQuery<S = TagEntity>(
  options: UseTagQueryOptions<S>
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
