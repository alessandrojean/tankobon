import { api } from '@/modules/api'
import type { Metric, Tag } from '@/types/tankobon-metrics'
import { type ErrorResponse, TankobonApiError } from '@/types/tankobon-response'
import { isAxiosError } from 'axios'

export interface GetMetricParams {
  metric: string,
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
