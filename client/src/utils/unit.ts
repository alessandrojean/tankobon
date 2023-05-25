import type { LengthUnit, MassUnit } from '@/types/tankobon-unit'

export const unitAbbreviation: Record<MassUnit | LengthUnit, string> = {
  CENTIMETER: 'cm',
  MILLIMETER: 'mm',
  INCH: 'in',
  KILOGRAM: 'kg',
  GRAM: 'g',
  POUND: 'lb',
  OUNCE: 'oz',
}

export function isLengthUnit(unit: MassUnit | LengthUnit): unit is LengthUnit {
  return ['CENTIMETER', 'MILLIMETER', 'INCH'].includes(unit)
}

export function isMassUnit(unit: MassUnit | LengthUnit): unit is MassUnit {
  return !isLengthUnit(unit)
}
