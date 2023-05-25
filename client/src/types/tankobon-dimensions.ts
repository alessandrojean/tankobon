import type { LengthUnit } from './tankobon-unit'

export interface Dimensions {
  width: number
  height: number
  depth: number
  unit: LengthUnit
}

export interface DimensionsString {
  width: string
  height: string
  depth: string
  unit: LengthUnit
}
