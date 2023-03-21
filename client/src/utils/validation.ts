import { helpers, email } from '@vuelidate/validators'
import { checkEmailAvailability } from '@/services/tankobon-users'

export const emailIsAvailable = helpers.withAsync(async (value: string) => {
  if (value.length === 0 || !email.$validator(value, null, null)) {
    return true
  }

  return await checkEmailAvailability(value)
})

export const emailIsAvailableIfNotSame = (current: string) => helpers.withAsync(async (value: string) => {
  if (value.length === 0 || !email.$validator(value, null, null)) {
    return true
  }

  if (current === value) {
    return true
  }

  return await checkEmailAvailability(value)
})


export const maxFileSize = (maxSize: number, sizeString: string) =>
  helpers.withParams(
    { sizeString },
    (file: File | null) => {
      if (!file) {
        return true
      }
    
      return file.size <= maxSize
    }
  )

export const isbn = (isbn: string) => {
  return isbn13(isbn) || isbn10(isbn)
}

export const isbn13 = (isbn13: string) => {
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

export const isbn10 = (isbn10: string) => {
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