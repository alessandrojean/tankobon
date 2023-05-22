import { describe, expect, it } from 'vitest'
import { getFlagScriptCode, getLanguageName } from '@/utils/language'

it('Should compute the correct script code for the flag', () => {
  expect(getFlagScriptCode('ja')).toBe('oriental')
  expect(getFlagScriptCode('ja-Latn-JP')).toBe('romanized')
  expect(getFlagScriptCode('ja-Japn')).toBe('oriental')
  expect(getFlagScriptCode('en-US')).toBe(undefined)
})

describe('Language name', () => {
  it('Should return the correct language name', () => {
    const expectedResults = {
      'ja-Latn-JP': 'Japanese (Romanized)',
      'ja-JP': 'Japanese',
      'zh-Latn-TW': 'Chinese (Romanized, Taiwan)',
      'en-US': 'English (US)',
      'pt-PT': 'Portuguese',
      'pt-BR': 'Portuguese (Brazil)',
      'zz-AA': 'zz (AA)',
      'undefined': 'Unknown',
    }

    const results = Object.keys(expectedResults)
      .map(lang => getLanguageName({
        language: lang === 'undefined' ? undefined : lang,
        locale: 'en-US',
        romanizedLabel: 'Romanized',
        unknownLabel: 'Unknown',
      }))

    expect(results).toStrictEqual(Object.values(expectedResults))
  })
})
