export interface TankobonSuccessEntityResponse<Entity> {
  result: 'OK',
  response: 'ENTITY',
  data: Entity,
}

export interface TankobonSuccessCollectionResponse<Entity> {
  result: 'OK',
  response: 'COLLECTION',
  data: Entity[],
}

export interface TankobonSuccessPaginatedCollectionResponse<Entity> {
  result: 'OK',
  response: 'COLLECTION',
  data: Entity[],
  pagination: TankobonPagination,
}

export interface TankobonErrorResponse {
  result: 'ERROR',
  errors: TankobonError[]
}

export interface TankobonError {
  id: string,
  status: number,
  title: string,
  details: string,
  stackTrace: string | undefined,
}

export interface TankobonPagination {
  currentPage: number,
  totalElements: number,
  totalPages: number,
}

export type TankobonResponse<T> = TankobonSuccessEntityResponse<T>
  | TankobonSuccessCollectionResponse<T>
  | TankobonSuccessPaginatedCollectionResponse<T>
  | TankobonErrorResponse

export class TankobonApiError extends Error {
  errors: TankobonError[]

  constructor (response: TankobonErrorResponse) {
    super(response.errors[0].title)
    this.errors = response.errors
    Object.setPrototypeOf(this, TankobonApiError.prototype)
  }
}
