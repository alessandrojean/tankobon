import { api } from '@/modules/api'
import type { Metric, Tag } from '@/types/tankobon-metrics'
import { type ErrorResponse, TankobonApiError } from '@/types/tankobon-response'
import { isAxiosError } from 'axios'

export type MetricKey = 'disk.free' | 'disk.total' | 'system.cpu.count' |
  'system.cpu.usage' | 'tankobon.books' | 'tankobon.books.covers' | 
  'tankobon.libraries' | 'tankobon.users.avatars' | 'jvm.info' |
  'application.ready.time' | 'application.started.time' | 'tankobon.users'

export interface GetMetricParams {
  metric: MetricKey,
  tags?: Tag[],
}

export async function getMetric({ metric, tags }: GetMetricParams): Promise<Metric> {
  try {
    const { data } = await api.get<Metric>(`actuator/metrics/${metric}`, {
      params: {
        tag: tags?.map(t => `${t.key}:${t.value}`)
      },
      paramsSerializer: { indexes: false },
    })

    return data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
