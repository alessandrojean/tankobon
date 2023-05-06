import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import {
  TankobonApiError,
} from '@/types/tankobon-response'
import type {
  EntityResponse,
  ErrorResponse,
  PaginatedResponse,
} from '@/types/tankobon-response'
import type {
  PersonCreation,
  PersonEntity,
  PersonIncludes,
  PersonSort,
  PersonUpdate,
} from '@/types/tankobon-person'
import type { Paginated } from '@/types/tankobon-api'

type PersonOnly = EntityResponse<PersonEntity>
type PersonPaginated = PaginatedResponse<PersonEntity>

export interface GetAllPeopleByLibraryParameters extends Paginated<PersonSort> {
  libraryId: string
  search?: string
  includes?: PersonIncludes[]
}

export async function getAllPeopleByLibrary(options: GetAllPeopleByLibraryParameters): Promise<PersonPaginated> {
  const { libraryId, includes, page, size, sort, search, unpaged } = options
  const searchOrUndefined = (search && search.length > 2) ? search : undefined

  try {
    const { data: people } = await api.get<PersonPaginated>(`libraries/${libraryId}/people`, {
      params: {
        search: searchOrUndefined,
        includes: includes?.join(','),
        unpaged: unpaged ?? false,
        page,
        size,
        sort: sort?.map(({ property, direction }) => {
          return `${property},${direction}`
        }),
      },
      paramsSerializer: {
        indexes: null,
      },
    })

    return people
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOnePerson(person: PersonCreation): Promise<PersonEntity> {
  try {
    const { data } = await api.post<PersonOnly>('people', person)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetOnePersonParameters {
  personId?: string
  includes?: PersonIncludes[]
}

export async function getOnePerson({ personId, includes }: GetOnePersonParameters): Promise<PersonEntity> {
  try {
    const { data: person } = await api.get<PersonOnly>(`people/${personId}`, {
      params: {
        includes: includes?.join(','),
      },
    })

    return person.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deletePersonPicture(personId: string): Promise<void> {
  try {
    await api.delete(`people/${personId}/picture`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface UploadPersonPictureOptions {
  personId: string
  picture: File
}

export async function uploadPersonPicture(options: UploadPersonPictureOptions): Promise<void> {
  try {
    await api.postForm(`people/${options.personId}/picture`, {
      picture: options.picture,
    })
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function updateOnePerson(person: PersonUpdate): Promise<void> {
  try {
    await api.put(`people/${person.id}`, person)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deleteOnePerson(personId: string): Promise<void> {
  try {
    await api.delete(`people/${personId}`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
