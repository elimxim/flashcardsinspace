import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
})

// todo move api calls here

export default apiClient

export async function sendRemoveFlashcardRequest(setId: number, id: number) {
  console.log(`Removing flashcard ${id} from set ${setId}`)
  return await apiClient.delete('/flashcard-sets/' + setId + '/flashcards/' + id)
}
