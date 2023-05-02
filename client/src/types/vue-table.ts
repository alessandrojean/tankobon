import type { RowData } from '@tanstack/vue-table'

declare module '@tanstack/vue-table' {
  // eslint-disable-next-line unused-imports/no-unused-vars
  interface ColumnMeta<TData extends RowData, TValue> {
    cellClass?: string
    headerClass?: string
    headerContainerClass?: string
    tabular?: boolean
  }
}
