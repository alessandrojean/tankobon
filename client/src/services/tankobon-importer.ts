import { api } from '@/modules/api'
import { BookEntity } from '@/types/tankobon-book'
import { ExternalBookEntity, ExternalBookIncludes } from '@/types/tankobon-external-book'
import { ImporterSourceEntity, ImporterSources, ImportOneBook } from '@/types/tankobon-importer-source'
import { CollectionResponse, EntityResponse, ErrorResponse, TankobonApiError } from '@/types/tankobon-response'
import { isAxiosError } from 'axios'

type ImporterColletion = CollectionResponse<ImporterSourceEntity>
type ExternalBookCollection = CollectionResponse<ExternalBookEntity>
type BookSingle = EntityResponse<BookEntity>

export async function getAllSources(): Promise<ImporterSourceEntity[]> {
  try {
    const { data: sources } = await api.get<ImporterColletion>('importer/sources')

    return sources.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface SearchByIsbnOptions {
  isbn: string,
  sources?: ImporterSources[],
  includes?: ExternalBookIncludes[],
}

export async function searchByIsbn(options: SearchByIsbnOptions): Promise<ExternalBookEntity[]> {
  const { sources, includes, isbn } = options

  try {
    const { data: results } = await api.get<ExternalBookCollection>(`importer/search/${isbn}`, {
      params: {
        sources: sources?.join(','),
        includes: includes?.join(','),
      }
    })

    return results.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}


export async function importOneBook(book: ImportOneBook): Promise<BookEntity> {
  try {
    const { data } = await api.post<BookSingle>('importer/import', book)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
