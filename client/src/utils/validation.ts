import { email, helpers, url } from '@vuelidate/validators'
import { checkEmailAvailability } from '@/services/tankobon-users'

export const positiveDecimal = helpers.regex(/^\d+([,.]\d+)?$/)

export const emailIsAvailable = helpers.withAsync(async (value: string) => {
  if (value.length === 0 || !email.$validator(value, null, null)) {
    return true
  }

  return await checkEmailAvailability(value)
})

export function emailIsAvailableIfNotSame(current: string) {
  return helpers.withAsync(async (value: string) => {
    if (value.length === 0 || !email.$validator(value, null, null)) {
      return true
    }

    if (current === value) {
      return true
    }

    return await checkEmailAvailability(value)
  })
}

export function maxFileSize(maxSize: number, sizeString: string) {
  return helpers.withParams(
    { sizeString },
    (file: File | null) => {
      if (!file) {
        return true
      }

      return file.size <= maxSize
    },
  )
}

export function allowedHosts(hosts: string[]) {
  const hostsParsed = hosts.map(h => h.toLowerCase().replace(/^www\./, ''))

  return (value: string) => {
    if (value.length === 0 || !url.$validator(value, null, null)) {
      return true
    }

    try {
      const urlParsed = new URL(value)
      const host = urlParsed.hostname.toLowerCase().replace(/^www./, '')

      return hostsParsed.includes(host)
    } catch (_) {
      return false
    }
  }
}

export function isbn(isbn: string) {
  return isbn13(isbn) || isbn10(isbn)
}

export function isbn13(isbn13: string) {
  if (isbn13.length === 0) {
    return true
  }

  const digits = isbn13.replace(/[^\dX]/g, '')

  if (digits.length !== 13) {
    return false
  }

  const sum = digits.split('')
    .reduce((acm, crr, i) => acm + parseInt(crr, 10) * (i % 2 === 0 ? 1 : 3), 0)

  return sum % 10 === 0
}

export function isbn10(isbn10: string) {
  if (isbn10.length === 0) {
    return true
  }

  const digits = isbn10.replace(/[^\dX]/g, '')

  if (digits.length !== 10) {
    return false
  }

  const partialSum = digits
    .slice(0, -1)
    .split('')
    .reduce((acm, crr, i) => acm + parseInt(crr, 10) * (10 - i), 0)

  const sum = partialSum + (digits[9] === 'X' ? 10 : parseInt(digits[9], 10))

  return sum % 11 === 0
}
