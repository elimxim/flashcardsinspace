import axios from 'axios';
import { useRouter } from 'vue-router';

const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
});

// apiClient.interceptors.response.use(
//   (response) => response,
//   async (error) => {
//     const originalRequest = error.config;
//
//     if (error.response.status === 401 && !originalRequest._retry) {
//       originalRequest._retry = true;
//
//       try {
//         await apiClient.post('/auth/refresh-token');
//         return apiClient(originalRequest);
//       } catch (refreshError) {
//         console.error("Session expired. Please log in again.", refreshError);
//         const router = useRouter();
//         await router.push({ name: 'login' });
//         return Promise.reject(refreshError);
//       }
//     }
//
//     return Promise.reject(error);
//   }
// );


export default apiClient;
