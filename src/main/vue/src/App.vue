<template>
  <div id="app" class="app-container">
    <nav class="navbar">
      <div class="logo-container">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-image"/>
        <div class="title-text">Flashcards in Space</div>
      </div>

      <ul class="nav-items">
        <li :hidden="!isAuthenticated">
          <router-link :to="{ name: routeNames.flashcards }">
            Flashcards
          </router-link>
        </li>
        <li>
          <router-link :to="{ name: routeNames.home }">
            Home
          </router-link>
        </li>
        <li>
          <router-link :to="{ name: routeNames.leitner }">
            Leitner
          </router-link>
        </li>
        <li>
          <router-link :to="{ name: routeNames.support }">
            Support
          </router-link>
        </li>
        <li class="nav-user">
          <router-link :to="{ name: routeNames.user }">
            <font-awesome-icon icon="fa-solid fa-circle-user"/>
          </router-link>
        </li>
      </ul>
    </nav>
    <router-view></router-view>
  </div>
</template>

<script setup lang="ts">
import '@/assets/css/base/themes.css'
import '@/assets/css/base/reset.css'
import { useLanguageStore } from '@/stores/language-store.ts'
import { useAuthStore } from '@/stores/auth-store.ts'
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { routeNames } from '@/router'

const authStore = useAuthStore()
const languageStore = useLanguageStore()

const { isAuthenticated } = storeToRefs(authStore)

onMounted(() => {
  languageStore.loadData()
})

</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-between;
  background-color: #707070;
  padding: 1vh;
  height: var(--navbar-height);
  min-height: var(--navbar-height);
  max-height: var(--navbar-height);
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
  height: 5em;
  margin-right: 10px;
}

.title-text {
  font-size: 1.5em;
  color: white;
  font-weight: bold;
}

</style>
