<template>
  <div
    :class="[
      'page',
      'flex-column',
      'flex-center',
      'padding-auto',
      'support-page',
    ]"
  >
    <AuroraWaves
      :colors="['#c63aed', '#5864ed', '#00d2ff']"
      :minLength="2"
      :maxLength="3"
      :minThickness="3"
      :maxThickness="6"
      :pulsePeak="1.4"
      :minDuration="20"
      :maxDuration="30"
      :minAmplitude="0.4"
      :maxAmplitude="0.9"
      :spawnInterval="100"
      :maxLines="8"
      :blurAmount="0.85"
    />
    <div class="support-content">
      <div class="support-header">
        <h1>I need...</h1>
      </div>
      <div class="cards-container">
        <GlassCard
          class="support-card support-card--emotional"
          text="Emotional Support"
          @click.stop="goToEmotionalSupport"
        />
        <GlassCard
          class="support-card support-card--website"
          text="Website Support"
          @click.stop="goToWebsiteSupport"
        />
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

function goToWebsiteSupport() {
  router.push({ name: routeNames.websiteSupport })
}

</script>

<style scoped>
.support-content {
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

.support-header h1 {
  font-family: var(--site-content--font-family);
  font-size: 2.5rem;
  font-weight: 200;
  letter-spacing: 0.05rem;
  word-spacing: 0.1rem;
  color: #ffffff;
}

.cards-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 40px;
}

.support-card {
  --glass-card--width: 240px;
  --glass-card--height: 160px;
  --glass-card--font-size: 2rem;
}

.support-card--emotional {
  background: linear-gradient(135deg, rgba(198, 58, 237, 0.6) 0%, rgba(247, 85, 147, 0.6) 100%);
}

.support-card--website {
  background: linear-gradient(135deg, rgba(58, 100, 237, 0.6) 0%, rgba(85, 142, 247, 0.6) 100%);
}

@media(max-width: 400px) {
  .support-header h1 {
    font-size: 1.5rem;
  }

  .support-card {
    --glass-card--width: 180px;
    --glass-card--height: 120px;
    --glass-card--font-size: 1.5rem;
  }
}

</style>
