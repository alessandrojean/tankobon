import { describe, expect, it } from 'vitest'

import { isbn, positiveDecimal } from '@/utils/validation'

describe('ISBN', () => {
  it('Should pass on a valid ISBN', () => {
    expect(isbn('9788545702870')).toBe(true)
    expect(isbn('978-85-4570-232-0')).toBe(true)
    expect(isbn('8576161877')).toBe(true)
  })

  it('Should fail on a invalid ISBN', () => {
    expect(isbn('1234')).toBe(false)
    expect(isbn('978-85-4570-288-9')).toBe(false)
  })
})

describe('Positive decimal number', () => {
  it('Should pass on a valid number', () => {
    expect(positiveDecimal('2,90')).toBe(true)
    expect(positiveDecimal('2.9')).toBe(true)
    expect(positiveDecimal('2')).toBe(true)
  })

  it('Should fail on an invalid number', () => {
    expect(positiveDecimal('-2')).toBe(false)
    expect(positiveDecimal('+2')).toBe(false)
    expect(positiveDecimal('2,')).toBe(false)
    expect(positiveDecimal('2,A')).toBe(false)
    expect(positiveDecimal('ABC')).toBe(false)
    expect(positiveDecimal('.2')).toBe(false)
  })
})
