import type { AxiosInstance } from 'axios'

type JsonValue = string | number | boolean | null | JsonObject | JsonArray
type JsonObject = { [key: string]: JsonValue }
type JsonArray = JsonValue[]

/**
 * Recursively transform ISO timestamp strings to Date objects in API responses
 * Only transforms strings matching ISO 8601 timestamp format
 * Leaves LocalDate strings (yyyy-MM-dd) unchanged
 */
function transformResponseDates(data: unknown): unknown {
  if (data === null || data === undefined) return data

  if (typeof data === 'string') {
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(data)) {
      return new Date(data)
    }
    return data
  }

  if (Array.isArray(data)) {
    return data.map(transformResponseDates)
  }

  if (typeof data === 'object') {
    const result: Record<string, unknown> = {}
    for (const key in data) {
      const value = (data as Record<string, unknown>)[key]
      if (value !== null) {
        result[key] = transformResponseDates(value)
      }
    }
    return result
  }

  return data
}

/**
 * Recursively transform Date objects to ISO strings in API requests
 * Converts Date objects to UTC ISO strings
 */
function transformRequestDates(data: unknown): unknown {
  if (data === null || data === undefined) return data

  if (data instanceof Date) {
    return data.toISOString()
  }

  if (Array.isArray(data)) {
    return data.map(transformRequestDates)
  }

  if (typeof data === 'object') {
    const result: Record<string, unknown> = {}
    for (const key in data) {
      result[key] = transformRequestDates((data as Record<string, unknown>)[key])
    }
    return result
  }

  return data
}

/**
 * Configure axios instance with date transformation interceptors
 */
export function configureDateTransformers(axiosInstance: AxiosInstance) {
  axiosInstance.interceptors.request.use(
    (config) => {
      // Skip transformation for FormData (used in file uploads)
      if (config.data && !(config.data instanceof FormData)) {
        config.data = transformRequestDates(config.data)
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  axiosInstance.interceptors.response.use(
    (response) => {
      // Skip transformation for Blob responses (binary data like audio files)
      if (!(response.data instanceof Blob)) {
        response.data = transformResponseDates(response.data)
      }
      return response
    },
    (error) => {
      if (error.response?.data && !(error.response.data instanceof Blob)) {
        error.response.data = transformResponseDates(error.response.data)
      }
      return Promise.reject(error)
    }
  )
}
