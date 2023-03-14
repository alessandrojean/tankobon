import axios, { type AxiosInstance } from 'axios'

export const api: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  withCredentials: true,
  headers: {
    'X-Requested-With': 'XMLHttpRequest',
  }
})
