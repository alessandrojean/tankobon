import { Dimensions } from './tankobon-dimensions'
import { Entity } from './tankobon-entity'
import { MonetaryAmount } from './tankobon-monetary'

export type ExternalBookEntity = Entity<ExternalBookAttributes> & {
  type: 'IMPORTER_SOURCE'
}

export interface ExternalBookAttributes {
  isbn: string,
  title: string,
  contributors: ExternalBookContributor[],
  publisher: string,
  synopsis: string,
  dimensions: Dimensions[],
  labelPrice: MonetaryAmount,
  pageCount: number,
  url: string,
  coverUrl?: string,
}

export interface ExternalBookContributor {
  name: string,
  role: string,
}

export type ExternalBookIncludes = 'importer_source'