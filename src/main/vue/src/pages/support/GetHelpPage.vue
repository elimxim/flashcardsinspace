<template>
  <div
    :class="[
      'page',
      'flex-column',
      'flex-center',
      'padding-auto',
    ]"
  >
    <AuroraWaves
      :colors="['#c63aed', '#5864ed', '#00d2ff']"
      :min-length="2"
      :max-length="3"
      :min-thickness="3"
      :max-thickness="6"
      :pulse-peak="1.4"
      :min-duration="20"
      :max-duration="30"
      :min-amplitude="0.4"
      :max-amplitude="0.9"
      :spawn-interval="100"
      :max-lines="8"
      :blur-amount="0.85"
    />
    <div class="help-content">
      <div class="help-header">
        <h1>I need...</h1>
      </div>
      <div class="help-cards-container">
        <div class="help-card-wrapper">
          <GlassCard
            class="help-card help-card--warm"
            text="Emotional Support"
            :on-click="goToEmotionalSupport"
          />
          <div class="help-card-description">
            <p>Find your spark with inspiring words</p>
          </div>
        </div>
        <div class="help-card-wrapper">
          <GlassCard
            class="help-card support-card--cold"
            text="Website Guide"
            :on-click="goToWebsiteGuide"
          />
          <div class="help-card-description">
            <p>Learn how to navigate our features</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AuroraWaves from '@/components/AuroraWaves.vue'
import GlassCard from '@/components/GlassCard.vue'
import { routeNames } from '@/router'
import { useRouter } from 'vue-router'

const router = useRouter()

function goToEmotionalSupport() {
  router.push({ name: routeNames.emotionalSupport })
}

function goToWebsiteGuide() {
  router.push({ name: routeNames.websiteGuide })
}

</script>

<style scoped>
.help-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 40px;
  padding: clamp(10px, 5vw, 20px) clamp(20px, 5vw, 40px);
  z-index: 1;
}

.help-header h1 {
  font-family: var(--site-content--font-family);
  font-size: 2.5rem;
  font-weight: 200;
  letter-spacing: 0.05rem;
  word-spacing: 0.1rem;
  color: #ffffff;
}

.help-cards-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 40px;
}

.help-card-wrapper {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.help-card {
  --glass-card--width: 240px;
  --glass-card--height: 160px;
  --glass-card--font-size: 2rem;
}

.help-card--warm {
  background: linear-gradient(135deg, rgba(198, 58, 237, 0.6) 0%, rgba(247, 85, 147, 0.6) 100%);
}

.support-card--cold {
  background: linear-gradient(135deg, rgba(58, 100, 237, 0.6) 0%, rgba(85, 142, 247, 0.6) 100%);
}

.help-card-description {
  width: 320px;
  height: 40px;
  text-align: center;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.help-card-description p {
  font-family: var(--site-content--font-family);
  font-size: 1rem;
  font-weight: 200;
  letter-spacing: 0.02rem;
  word-spacing: 0.04rem;
  color: #ffffff;
  margin: 0;
  padding: 0;
}

@media (max-width: 400px) {
  .help-header h1 {
    font-size: 2rem;
  }

  .help-card {
    --glass-card--width: 180px;
    --glass-card--height: 120px;
    --glass-card--font-size: 1.5rem;
  }

  .help-card-description {
    width: 280px
  }

  .help-card-description p {
    font-size: 0.9rem;
  }
}

</style>
