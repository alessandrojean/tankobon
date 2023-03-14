import axios, { type AxiosInstance } from 'axios'

export const API_BASE_URL = 'http://localhost:8080'

export const api: AxiosInstance = axios.create({
  baseURL: `${API_BASE_URL}/api/v1`,
  withCredentials: true,
  headers: {
    'X-Requested-With': 'XMLHttpRequest',
  }
})
