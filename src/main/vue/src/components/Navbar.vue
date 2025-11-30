<template>
  <nav class="navbar navbar--theme">
    <Starfield twinkle :density="100" :star-size="1.6" vertical-drift="2px"/>
    <div class="logo-container">
      <img src="@/assets/rocket.svg" alt="Logo" class="logo-image"/>
      <div
        class="nav-text nav-text--title"
        @click="navigateToControlPanel"
      >
        Flashcards in Space
      </div>
    </div>

    <div class="nav-items">
      <div v-if="showIcons" class="nav-item-icon">
        <AwesomeButton
          class="navigation-button"
          icon="fa-solid fa-rocket"
          :disabled="!isAuthenticated"
          :on-click="navigateToControlPanel"
          :scale-factor="1.2"
          fill-space
          square
        />
      </div>
      <div v-else class="nav-item-text">
        <div class="nav-text nav-text--item" @click="navigateToControlPanel">
          Control Panel
        </div>
      </div>
      <div v-if="!showIcons" class="nav-item-partition">
        <font-awesome-icon icon="fa-solid fa-circle"/>
      </div>
      <div v-if="showIcons" class="nav-item-icon">
        <AwesomeButton
          class="navigation-button"
          icon="fa-solid fa-house"
          :on-click="navigateToHome"
          :scale-factor="1.2"
          fill-space
          square
        />
      </div>
      <div v-else class="nav-item-text">
        <div class="nav-text nav-text--item" @click="navigateToHome">
          Home
        </div>
      </div>
      <div v-if="!showIcons" class="nav-item-partition">
        <font-awesome-icon icon="fa-solid fa-circle"/>
      </div>
      <div v-if="showIcons" class="nav-item-icon">
        <AwesomeButton
          class="navigation-button"
          icon="fa-solid fa-heart-pulse"
          :on-click="navigateToSupport"
          :scale-factor="1.2"
          fill-space
          square
        />
      </div>
      <div v-else class="nav-item-text">
        <div class="nav-text nav-text--item" @click="navigateToSupport">
          Support
        </div>
      </div>
      <div v-if="!showIcons" class="nav-item-partition">
        <font-awesome-icon icon="fa-solid fa-circle"/>
      </div>
      <div v-if="showIcons" class="nav-item-icon">
        <AwesomeButton
          class="navigation-button"
          icon="fa-solid fa-user-astronaut"
          :on-click="navigateToUser"
          :scale-factor="1.2"
          fill-space
          square
        />
      </div>
      <div v-else class="nav-item-text">
        <div class="nav-text nav-text--item" @click="navigateToUser">
          User
        </div>
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
import { onMounted, onUnmounted, ref } from 'vue'

const ICONS_SWITCH_BREAKPOINT = 800

const authStore = useAuthStore()
const router = useRouter()

const { isAuthenticated } = storeToRefs(authStore)

const showIcons = ref(window.innerWidth <= ICONS_SWITCH_BREAKPOINT)

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

function updateShowIcons() {
  showIcons.value = window.innerWidth <= ICONS_SWITCH_BREAKPOINT
}

onMounted(() => {
  window.addEventListener('resize', updateShowIcons)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateShowIcons)
})
</script>

<style scoped>
.navbar--theme {
  --nav--font-family: var(--navbar--font-family);
  --nav--bg: transparent;
  --nav--text--color: #FAF9F6;
  --nav--text--color--hover: #dad0f8;
  --starfield--star--color: #dad0f8;
}

.navbar {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-between;
  background: var(--nav--bg);
  padding: 10px;
  height: var(--navbar-height);
  min-height: var(--navbar-height);
  max-height: var(--navbar-height);
  z-index: 1000;
}

.nav-items {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 4px;
}

.nav-item-text {
  width: fit-content;
}

.nav-item-icon {
  width: 40px;
  height: 40px;
}

.nav-item-partition {
  font-size: 0.4rem;
  color: var(--nav--text--color);
}

.nav-text {
  font-family: var(--nav--font-family);
  font-weight: 600;
  letter-spacing: 0.1rem;
  word-spacing: 0.1rem;
  text-transform: uppercase;
  color: var(--nav--text--color);
  cursor: pointer;
  transition: color 0.3s ease-in-out;
}

.nav-text:hover {
  color: var(--nav--text--color--hover);
}

.nav-text--title {
  font-size: clamp(1rem, 2vw, 1.5rem);
}

.nav-text--item {
  font-size: 0.8rem;
  text-align: center;
  padding: 4px;
  border-radius: 9999px;
  transition: background 0.3s ease-in-out;
}

.nav-text--item:hover {
  background: rgba(250, 249, 246, 0.15);
}

.logo-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.logo-image {
  height: 60px;
  z-index: 1001;
}

</style>
