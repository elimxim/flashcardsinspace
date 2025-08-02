<template>
  <div id="app" class="app-container">
    <nav class="navbar">
      <div class="logo-container">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-image"/>
        <div class="title-text">Flashcards in Space</div>
      </div>

      <ul class="nav-items">
        <li :hidden="!isAuthenticated">
          <router-link :to="{ name: 'flashcards' }">
            Flashcards
          </router-link>
        </li>
        <li>
          <router-link :to="{ name: 'home' }">
            Home
          </router-link>
        </li>
        <li>
          <router-link :to="{ name: 'leitner' }">
            Leitner
          </router-link>
        </li>
        <li>
          <router-link :to="{ name: 'support' }">
            Support
          </router-link>
        </li>
        <li class="nav-user">
          <router-link :to="{ name: 'signup' }">
            <font-awesome-icon icon="fa-solid fa-circle-user"/>
          </router-link>
        </li>
      </ul>
    </nav>
    <router-view></router-view>
  </div>
</template>

<script setup lang="ts">
import '@/assets/main.css'
import { useFlashcardDataStore } from '@/stores/flashcard-data.ts'
import { useLanguageDataStore } from '@/stores/language-data.ts'
import { useCalendarDataStore } from '@/stores/calendar-data.ts'
import { useAuthStore } from '@/stores/auth-state.ts'
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'

const authStore = useAuthStore()
const flashcardDataStore = useFlashcardDataStore()
const languageDataStore = useLanguageDataStore()
const calendarDataStore = useCalendarDataStore()

const { isAuthenticated } = storeToRefs(authStore)

onMounted(() => {
  flashcardDataStore.loadData()
  languageDataStore.loadData()
  calendarDataStore.loadData()
})
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.navbar {
  display: flex;
  justify-content: space-between;
  background-color: #707070;
  padding: 10px;
}

.nav-items {
  display: flex;
  list-style-type: none;
  align-items: center;
  margin: 0;
  padding: 0;
}

.nav-items li {
  margin-right: 15px;
}

.nav-items a {
  color: white;
  text-decoration: none;
}

.nav-items a.router-link-active {
  font-weight: bold;
  text-decoration: underline;
}

.nav-user {
  font-size: 1.6em;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo-image {
  height: 2em;
  margin-right: 10px;
}

.title-text {
  font-size: 1.5em;
  color: white;
  font-weight: bold;
}

</style>
