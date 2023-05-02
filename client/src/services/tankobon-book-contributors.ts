import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import {
  type CollectionResponse,
  type ErrorResponse,
  TankobonApiError,
} from '@/types/tankobon-response'
import type { ContributorEntity, ContributorIncludes } from '@/types/tankobon-contributor'

type ContributorCollection = CollectionResponse<ContributorEntity>

export interface GetAllContributorsByBookParameters {
  bookId: String
  includes?: ContributorIncludes[]
}

export async function getAllContributorsByBook(options: GetAllContributorsByBookParameters): Promise<ContributorCollection> {
  const { bookId, includes } = options

  try {
    const { data: contributors } = await api.get<ContributorCollection>(`books/${bookId}/contributors`, {
      params: {
        includes: includes?.join(','),
      },
    })

    return contributors
  }
  catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data)
      throw new TankobonApiError(e.response.data)

    throw e
  }
}
