export interface Tag {
  key: string
  value: string
}

export interface Metric {
  name: string
  description?: string
  baseUnit?: string
  measurements: Measurement[]
  availableTags: Tag[]
}

export interface Measurement {
  statistic: string
  value: number
}

export interface TagRequest {
  tag: string
  values: string[]
}
