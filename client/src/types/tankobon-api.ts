import { CollectionResponse, PaginatedResponse } from './tankobon-response'

export type SortDirection = 'asc' | 'desc'

export interface Sort<T> {
  property: T,
  direction: SortDirection,
}

export interface Paginated<T> {
  page?: number,
  size?: number,
  sort?: Sort<T>[],
  unpaged?: boolean,
}

export type PaginatedOrNot<T extends Paginated<any>, Entity> = 
  T['unpaged'] extends true ? CollectionResponse<Entity> : PaginatedResponse<Entity>

