import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
})

// todo move api calls here

export default apiClient
