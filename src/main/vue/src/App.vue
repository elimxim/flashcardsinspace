<template>
  <div id="app" class="app-container">
    <nav class="navbar">
      <div class="logo-container">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-image" />
        <div class="title-text">Flashcards in Space</div>
      </div>

      <ul class="nav-items">
        <li><router-link :to="{ name: 'home' }">Home</router-link></li>
        <li><router-link :to="{ name: 'flashcards' }">Flashcards</router-link></li>
        <li><router-link :to="{ name: 'leitner' }">Leitner</router-link></li>
        <li><router-link :to="{ name: 'support' }">Support</router-link></li>
      </ul>
    </nav>
    <router-view></router-view>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

/* Style the navbar */
.navbar {
  display: flex;
  justify-content: space-between; /* Spread items to sides */
  align-items: center;
  background-color: #333;
  padding: 10px;
}

/* Navigation items aligned to the left, but in this layout, they stay on the left by default */
.nav-items {
  display: flex;
  list-style-type: none;
  margin: 0;
  padding: 0;
}

/* Style links */
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

.logo-container {
  display: flex;
  align-items: center;
}

.logo-image {
  height: 50px; /* Adjust size as needed */
  margin-right: 10px; /* Space between logo and text */
}

.title-text {
  font-size: 2em; /* Make it bigger */
  color: white;
  font-weight: bold;
}


</style>
