import type { Entity } from './tankobon-entity'

export type ReadProgressEntity = Entity<ReadProgressAttributes> & {
  type: 'READ_PROGRESS'
}

export interface ReadProgressAttributes {
  finishedAt: string | null
  isCompleted: boolean
  page: number
  startedAt: string | null
  createdAt: string
}

export interface ReadProgressCreation extends Omit<ReadProgressAttributes, 'createdAt'> {
  book: string
}

export interface ReadProgressUpdate extends Omit<ReadProgressAttributes, 'createdAt'> {
  id: string
}

export type ReadProgressSort = 'createdAt' | 'startedAt' | 'finishedAt'
export type ReadProgressIncludes = 'user' | 'book'
