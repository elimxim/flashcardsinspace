<template>
  <nav class="navbar navbar--theme">
    <Starfield twinkle :density="200" :star-size="1.6" vertical-drift="2px"/>
    <div class="logo-container">
      <img src="@/assets/logo.svg" alt="Logo" class="logo-image"/>
      <div class="title-text" @click="navigateToControlPanel">
        Flashcards in Space
      </div>
    </div>

    <div class="nav-controls">
      <div class="nav-control-item">
        <AwesomeButton
          class="navigation-button"
          icon="fa-solid fa-rocket"
          :disabled="!isAuthenticated"
          :on-click="navigateToControlPanel"
          tooltip="Control Panel"
          :tooltip-delay="50"
          tooltip-position="bottom"
          :scale-factor="1.2"
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
          :tooltip-delay="50"
          tooltip-position="bottom"
          :scale-factor="1.2"
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
          :tooltip-delay="50"
          tooltip-position="bottom"
          :scale-factor="1.2"
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
          :tooltip-delay="50"
          tooltip-position="bottom-left"
          :scale-factor="1.2"
          fill-space
          square
        />
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import Starfield from '@/components/Starfield.vue'
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
.navbar--theme {
  --starfield--star--color: #b1b176;
}

.navbar {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-between;
  background-color: #0C0404;
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
  gap: 4px;
}

.nav-control-item {
  width: 36px;
  height: 36px;
}

.logo-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.logo-image {
  height: 40px;
  z-index: 1001;
}

.title-text {
  font-size: clamp(1rem, 2vw ,1.5rem);
  font-weight: 600;
  letter-spacing: 0.05rem;
  word-spacing: 0.05rem;
  text-transform: uppercase;
  color: #FAF9F6;
  cursor: pointer;
  transition: color 0.3s ease-in-out;
}

.title-text:hover {
  color: #dad0f8;
}
</style>
