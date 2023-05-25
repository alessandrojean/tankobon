import type { MassUnit } from './tankobon-unit'

export interface Weight {
  value: number
  unit: MassUnit
}

export interface WeightString {
  value: string
  unit: MassUnit
}
