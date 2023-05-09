import { type UseQueryOptions, useQuery } from '@tanstack/vue-query'
import type { MaybeRef } from '@vueuse/core'
import type { TankobonApiError } from '@/types/tankobon-response'
import type { Metric, Tag } from '@/types/tankobon-metrics'
import { type MetricKey, getMetric } from '@/services/tankobon-metrics'

export type MetricMap<K extends MetricKey> = Record<K, Metric>

interface UseMetricsQueryOptions<K extends MetricKey, S = MetricMap<K>> extends UseQueryOptions<MetricMap<K>, ErrorResponse, S> {
  metrics: MaybeRef<K[]>
  tags?: MaybeRef<Tag[]>
}

type ErrorResponse = TankobonApiError | Error

function isFullfilled<T>(result: PromiseSettledResult<T>): result is PromiseFulfilledResult<T> {
  return result.status === 'fulfilled'
}

export default function useMetricsQuery<K extends MetricKey, S = MetricMap<K>>(
  options: UseMetricsQueryOptions<K, S>,
) {
  return useQuery<MetricMap<K>, ErrorResponse, S>({
    queryKey: ['metrics', { metrics: options.metrics, tags: options.tags }],
    queryFn: async () => {
      const keys = unref(options.metrics)
      const tags = unref(options.tags)
      const metrics = await Promise.allSettled(
        keys.map(key => getMetric({ metric: key, tags })),
      )

      return Object.fromEntries(
        metrics
          .filter(isFullfilled)
          .map(metric => [metric.value.name, metric.value]),
      ) as MetricMap<K>
    },
    ...options,
  })
}
