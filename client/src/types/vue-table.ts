import type { RowData } from '@tanstack/table-core'

declare module '@tanstack/table-core' {
  interface ColumnMeta<TData extends RowData, TValue> {
    cellClass?: string,
    headerClass?: string,
    headerContainerClass?: string,
    tabular?: boolean,
  }
}
