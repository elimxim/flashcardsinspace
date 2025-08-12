import axios from 'axios'

const publicApiClient = axios.create({
  baseURL: '/api-public',
  withCredentials: false,
})

export default publicApiClient
