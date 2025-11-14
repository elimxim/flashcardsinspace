<template>
  <nav class="navbar">
    <div class="logo-container">
      <img src="@/assets/logo.svg" alt="Logo" class="logo-image"/>
      <div class="title-text">Flashcards in Space</div>
    </div>

    <div class="nav-controls">
      <div class="nav-control-item">
        <AwesomeButton
          class="navigation-button"
          icon="fa-solid fa-shuttle-space"
          :disabled="!isAuthenticated"
          :on-click="navigateToControlPanel"
          tooltip="Control Panel"
          :tooltip-delay="10"
          tooltip-position="bottom"
          fill-space
          square
        />
      </div>
      <div class="nav-control-item">
        <AwesomeButton
          class="navigation-button"
          icon="fa-solid fa-house"
          :on-click="navigateToHome"
          tooltip="Home"
          :tooltip-delay="10"
          tooltip-position="bottom"
          fill-space
          square
        />
      </div>
      <div class="nav-control-item">
        <AwesomeButton
          class="navigation-button"
          icon="fa-solid fa-heart-pulse"
          :on-click="navigateToSupport"
          tooltip="Support"
          :tooltip-delay="10"
          tooltip-position="bottom"
          fill-space
          square
        />
      </div>
      <div class="nav-control-item">
        <AwesomeButton
          class="navigation-button"
          icon="fa-solid fa-user-astronaut"
          :on-click="navigateToUser"
          tooltip="User"
          :tooltip-delay="10"
          tooltip-position="bottom-left"
          fill-space
          square
        />
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { useAuthStore } from '@/stores/auth-store.ts'
import { storeToRefs } from 'pinia'
import { routeNames } from '@/router'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const { isAuthenticated } = storeToRefs(authStore)

function navigateToControlPanel() {
  router.push({ name: routeNames.controlPanel })
}

function navigateToHome() {
  router.push({ name: routeNames.home })
}

function navigateToSupport() {
  router.push({ name: routeNames.support })
}

function navigateToUser() {
  router.push({ name: routeNames.user })
}
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-between;
  background-color: #707070;
  padding: 10px;
  height: var(--navbar-height);
  min-height: var(--navbar-height);
  max-height: var(--navbar-height);
  z-index: 1000;
}

.nav-controls {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-items: center;
  gap: 10px;
}

.nav-control-item {
  width: 36px;
  height: 36px;
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
