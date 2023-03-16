export type SortDirection = 'asc' | 'desc'

export interface Sort<T> {
  property: T,
  direction: SortDirection,
}

export interface Paginated<T> {
  page?: number,
  size?: number,
  sort?: Sort<T>[],
}
