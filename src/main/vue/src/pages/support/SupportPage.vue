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
    <div class="support-content">
      <GlassCard
        class="emotional-support"
        text="Emotional Support"
        @click.stop="goToEmotionalSupport"
      />
      <GlassCard
        class="website-support"
        text="Website Support"
        @click.stop="goToWebsiteSupport"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
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
  flex-direction: row;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 40px;
  padding: clamp(10px, 5vw, 20px) clamp(20px, 5vw, 40px);
}

.emotional-support {
  background: linear-gradient(135deg, rgba(198, 58, 237, 0.6) 0%, rgba(247, 85, 147, 0.6) 100%);
}

.website-support {
  background: linear-gradient(135deg, rgba(58, 100, 237, 0.6) 0%, rgba(85, 142, 247, 0.6) 100%);
}

</style>
