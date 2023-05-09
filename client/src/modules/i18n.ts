import type { DefaultDateTimeFormatSchema, DefaultNumberFormatSchema } from 'vue-i18n'
import { createI18n } from 'vue-i18n'

const messages = Object.fromEntries(
  Object
    .entries(
      import.meta.glob<{ default: any }>('../../locales/*.y(a)?ml', { eager: true }),
    )
    .map(([key, value]) => {
      const yaml = key.endsWith('.yaml')
      return [key.slice(14, yaml ? -5 : -4), value.default]
    }),
)

const numberFormats = {
  currency: {
    style: 'currency',
    notation: 'standard',
  },
  integer: {
    style: 'decimal',
    maximumFractionDigits: 0,
  },
  decimal: {
    style: 'decimal',
    maximumFractionDigits: 2,
    useGrouping: false,
  },
  dimension: {
    style: 'decimal',
    minimumFractionDigits: 0,
    maximumFractionDigits: 1,
    useGrouping: false,
  },
  percent: {
    style: 'percent',
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
    useGrouping: false,
  },
} satisfies DefaultNumberFormatSchema

const dateTimeFormats = {
  short: {
    day: '2-digit',
    month: '2-digit',
    year: '2-digit',
  },
  dateTime: {
    day: '2-digit',
    month: '2-digit',
    year: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  },
} satisfies DefaultDateTimeFormatSchema

function createConfigForLang<
  T = DefaultNumberFormatSchema | DefaultDateTimeFormatSchema,
>(schema: T): Record<string, T> {
  return Object.fromEntries(
    Object.keys(messages)
      .map(lang => [lang, schema]),
  )
}

export const i18n = createI18n({
  legacy: false,
  locale: 'en-US',
  messages,
  numberFormats: createConfigForLang(numberFormats),
  datetimeFormats: createConfigForLang(dateTimeFormats),
})
