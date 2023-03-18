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
