import { useQuery, type UseQueryOptions } from '@tanstack/vue-query'
import type { TankobonApiError } from '@/types/tankobon-response'
import { MaybeRef } from '@vueuse/core'
import { Metric } from '@/types/tankobon-metrics'
import { getMetric, type MetricKey } from '@/services/tankobon-metrics'

export type MetricMap<K extends MetricKey> = Record<K, Metric>

interface UseMetricsQueryOptions<K extends MetricKey, S = MetricMap<K>> extends UseQueryOptions<MetricMap<K>, ErrorResponse, S> {
  metrics: MaybeRef<K[]>,
}

type ErrorResponse = TankobonApiError | Error

function isFullfilled<T>(result: PromiseSettledResult<T>): result is PromiseFulfilledResult<T> {
  return result.status === 'fulfilled'
}

export default function useMetricsQuery<K extends MetricKey, S = MetricMap<K>>(
  options: UseMetricsQueryOptions<K, S>
) {
  return useQuery<MetricMap<K>, ErrorResponse, S>({
    queryKey: ['metrics', { metrics: options.metrics }],
    queryFn: async () => {
      const keys = unref(options.metrics)
      const metrics = await Promise.allSettled(
        keys.map((key) => getMetric({ metric: key }))
      )

      return Object.fromEntries(
        metrics
          .filter(isFullfilled)
          .map((metric) => [metric.value.name, metric.value])
      ) as MetricMap<K>
    },
    ...options,
  })
}
