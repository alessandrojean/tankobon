import axios, { type AxiosInstance } from 'axios'

export const API_BASE_URL = 'http://localhost:8080'

function createInstance() {
  const instance: AxiosInstance = axios.create({
    baseURL: `${API_BASE_URL}/api/v1`,
    withCredentials: true,
    headers: {
      'X-Requested-With': 'XMLHttpRequest',
    }
  })
  
  // Add the Accept-Language header to change the messages
  // language to follow the client language.
  instance.interceptors.request.use((config) => {
    if (config.headers['Accept-Language'] || !document.documentElement.lang) {
      return config
    }

    const documentLocale = document.documentElement.lang
    const [major] = documentLocale.split('-')
    const clientLocale = `${documentLocale},${major};q=0.8`
    const englishLocale = documentLocale !== 'en-US' ? ',en-US;q=0.5,en;q=0.3' : ''

    config.headers['Accept-Language'] = clientLocale + englishLocale

    return config
  })

  return instance
}

export const api = createInstance()

export type ImageCollection = 'avatars' | 'covers'
export interface GetFullImageUrlParams {
  collection: ImageCollection,
  fileName?: string,
  timeHex?: string,
}

export function getFullImageUrl({ collection, fileName, timeHex }: GetFullImageUrlParams) {
  if (!fileName || fileName.length === 0) {
    return null
  }

  return `${API_BASE_URL}/images/${collection}/${fileName}?${timeHex}`
}
