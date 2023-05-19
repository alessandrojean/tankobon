/**
 * Suggestions to the language fields.
 * New languages can be added later.
 * The country part is mandatory.
 */
export const BCP47_OPTIONS = [
  'ja-JP',
  'ja-Latn-JP',
  'ko-KR',
  'ko-Latn-KR',
  'zh-Hans-CN',
  'zh-Hant-TW',
  'zh-Hant-HK',
  'zh-Latn-CN',
  'zh-Latn-TW',
  'zh-Latn-HK',
  'en-US',
  'en-UK',
  'en-SG',
  'en-PH',
  'fr-FR',
  'de-DE',
  'hr-HR',
  'it-IT',
  'no-NO',
  'sv-SE',
  'pt-PT',
  'pt-BR',
  'es-ES',
  'es-419',
]

export function getRegionCode(language: string) {
  const parts = language.split('-')

  return parts[parts.length - 1]
}

export const ORIENTAL_LANGUAGES = ['ja', 'ko', 'zh']

export function getFlagScriptCode(language: string) {
  if (language.includes('Latn')) {
    return 'romanized'
  }

  const [languageCode] = language.split('-')

  return ORIENTAL_LANGUAGES.includes(languageCode) ? 'oriental' : undefined
}

export interface GetLanguageNameOptions {
  language: string | null | undefined
  locale: string
  romanizedLabel: string
  unknownLabel: string
}

const omitRegionExceptions = [
  { language: 'ja', region: 'JP' },
  { language: 'ko', region: 'KR' },
  { language: 'zh', region: 'CN' },
  { language: 'sv', region: 'SE' },
]

// The tag will always have the language and country code.
// The script code is optional.
export function getLanguageName({ language, locale, romanizedLabel, unknownLabel }: GetLanguageNameOptions) {
  if (!language) {
    return unknownLabel
  }

  const parts = language.split('-')
  const displayNames = {
    language: new Intl.DisplayNames(locale, { type: 'language', style: 'short' }),
    region: new Intl.DisplayNames(locale, { type: 'region', style: 'short' }),
  }

  // Japanese, Korean and Chinese case.
  if (parts.includes('Latn')) {
    const [languageCode, _, regionCode] = parts
    const languageName = displayNames.language.of(languageCode)
    const region = displayNames.region.of(regionCode)
    const omitRegion = omitRegionExceptions.find((e) => {
      return e.language === languageCode && e.region === regionCode
    })

    return (languageCode === regionCode.toLowerCase() || omitRegion)
      ? `${languageName} (${romanizedLabel})`
      : `${languageName} (${romanizedLabel}, ${region})`
  }

  if (parts.length === 3) {
    const [languageCode, scriptCode, regionCode] = parts
    const languageName = displayNames.language.of(`${languageCode}-${scriptCode}`)
    const region = displayNames.region.of(regionCode)
    const omitRegion = omitRegionExceptions.find((e) => {
      return e.language === languageCode && e.region === regionCode
    })

    return (languageCode !== regionCode.toLowerCase() && !omitRegion)
      ? `${languageName} (${region})`
      : (languageName ?? language)
  }

  if (parts.length === 2) {
    const [languageCode, regionCode] = parts
    const languageName = displayNames.language.of(languageCode)
    const region = displayNames.region.of(regionCode)
    const omitRegion = omitRegionExceptions.find((e) => {
      return e.language === languageCode && e.region === regionCode
    })

    return (languageCode !== regionCode.toLowerCase() && !omitRegion)
      ? `${languageName} (${region})`
      : (languageName ?? language)
  }

  return displayNames.language.of(language) ?? language
}
