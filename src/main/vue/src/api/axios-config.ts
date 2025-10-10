import type { AxiosInstance } from 'axios'

/**
 * Recursively transform ISO timestamp strings to Date objects in API responses
 * Only transforms strings matching ISO 8601 timestamp format (with time component)
 * Leaves LocalDate strings (yyyy-MM-dd) unchanged
 */
function transformResponseDates(data: any): any {
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
    const result: any = {}
    for (const key in data) {
      result[key] = transformResponseDates(data[key])
    }
    return result
  }

  return data
}

/**
 * Recursively transform Date objects to ISO strings in API requests
 * Converts Date objects to UTC ISO strings
 */
function transformRequestDates(data: any): any {
  if (data === null || data === undefined) return data

  if (data instanceof Date) {
    return data.toISOString()
  }

  if (Array.isArray(data)) {
    return data.map(transformRequestDates)
  }

  if (typeof data === 'object') {
    const result: any = {}
    for (const key in data) {
      result[key] = transformRequestDates(data[key])
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
      if (config.data) {
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
      response.data = transformResponseDates(response.data)
      return response
    },
    (error) => {
      if (error.response?.data) {
        error.response.data = transformResponseDates(error.response.data)
      }
      return Promise.reject(error)
    }
  )
}
