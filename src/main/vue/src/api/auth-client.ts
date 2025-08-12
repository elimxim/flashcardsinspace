import axios from 'axios'

const authClient = axios.create({
  baseURL: '/auth',
  withCredentials: false,
})

export default authClient
