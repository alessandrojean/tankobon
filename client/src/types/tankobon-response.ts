export interface EntityResponse<Entity> {
  result: 'OK'
  response: 'ENTITY'
  data: Entity
}

export interface CollectionResponse<Entity> {
  result: 'OK'
  response: 'COLLECTION'
  data: Entity[]
}

export interface PaginatedResponse<Entity> {
  result: 'OK'
  response: 'COLLECTION'
  data: Entity[]
  pagination: Pagination
}

export interface ErrorResponse {
  result: 'ERROR'
  errors: TankobonError[]
}

export interface TankobonError {
  id: string
  status: number
  title: string
  details: string
  stackTrace: string | undefined
}

export interface Pagination {
  currentPage: number
  totalElements: number
  totalPages: number
}

export type TankobonResponse<T> = EntityResponse<T>
| CollectionResponse<T>
| PaginatedResponse<T>
| ErrorResponse

export class TankobonApiError extends Error {
  errors: TankobonError[]

  constructor(response: ErrorResponse) {
    super(response.errors[0].title)
    this.errors = response.errors
    Object.setPrototypeOf(this, TankobonApiError.prototype)
  }
}
