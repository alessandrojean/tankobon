import { isAxiosError } from 'axios'
import { api } from '@/modules/api'
import type { Includes } from '@/types/tankobon-entity'
import { 
  type ErrorResponse,
  type EntityResponse,
  TankobonApiError,
PaginatedResponse,
} from '@/types/tankobon-response'
import type {
  ContributorRoleCreation,
  ContributorRoleEntity,
  ContributorRoleIncludes,
  ContributorRoleSort,
  ContributorRoleUpdate
} from '@/types/tankobon-contributor-role'
import { Paginated } from '@/types/tankobon-api'

type ContributorRoleOnly = EntityResponse<ContributorRoleEntity>
type ContributorRolePaginated = PaginatedResponse<ContributorRoleEntity>

export interface GetAllContributorRolesByLibraryParameters extends Paginated<ContributorRoleSort> {
  libraryId: string,
  search?: string,
  includes?: ContributorRoleIncludes[]
}

export async function getAllContributorRolesByLibrary(options: GetAllContributorRolesByLibraryParameters): Promise<ContributorRolePaginated> {
  const { libraryId, includes, page, size, sort, search } = options
  const searchOrUndefined = search && search.length > 2 ? search : undefined

  try {
    const { data: contributorRoles } = await api.get<ContributorRolePaginated>(`libraries/${libraryId}/contributor-roles`, {
      params: {
        search: searchOrUndefined,
        includes: includes?.join(','),
        page,
        size,
        sort: sort?.map(({ property, direction }) => {
          return `${property},${direction}`
        })
      },
      paramsSerializer: {
        indexes: null,
      },
    })

    return contributorRoles
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function addOneContributorRole(contributorRole: ContributorRoleCreation): Promise<ContributorRoleEntity> {
  try {
    const { data } = await api.post<ContributorRoleOnly>('contributor-roles', contributorRole)

    return data.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export interface GetOneContributorRoleParameters {
  contributorRoleId?: string,
  includes?: Includes
}

export async function getOneContributorRole({ contributorRoleId, includes }: GetOneContributorRoleParameters): Promise<ContributorRoleEntity> {
  try {
    const { data: contributorRole } = await api.get<ContributorRoleOnly>(`contributor-roles/${contributorRoleId}`, {
      params: {
        includes: includes?.join(','),
      }
    })

    return contributorRole.data
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function updateOneContributorRole(contributorRole: ContributorRoleUpdate): Promise<void> {
  try {
    await api.put(`contributor-roles/${contributorRole.id}`, contributorRole)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}

export async function deleteOneContributorRole(contributorRoleId: string): Promise<void> {
  try {
    await api.delete(`contributor-roles/${contributorRoleId}`)
  } catch (e) {
    if (isAxiosError<ErrorResponse>(e) && e.response?.data) {
      throw new TankobonApiError(e.response.data)
    }

    throw e
  }
}
