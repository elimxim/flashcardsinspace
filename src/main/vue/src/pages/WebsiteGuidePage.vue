<template>
  <div :class="['page', 'flex-column', 'padding-auto', 'scrollbar-hidden', 'website-guide-page']">
    <div class="tutorial-section select-text">
      <h1 class="section-header">
        <span class="section-title section-title--centered"> In-Depth Tutorial </span>
      </h1>
      <div class="section-description">
        A complete, step-by-step guide to mastering Flashcards in Space. Learn how to get the most
        out of your Flashcards in Space experience.
      </div>
      <div class="video-wrapper">
        <div class="video-container">
          <iframe
            src="https://www.youtube.com/embed/nvnR1z9cUTs"
            title="Flashcards in Space In-Depth Tutorial"
            allow="
              accelerometer;
              autoplay;
              clipboard-write;
              encrypted-media;
              gyroscope;
              picture-in-picture;
              web-share;
            "
            allowfullscreen
          >
          </iframe>
        </div>
      </div>
    </div>
    <div class="schedule-section">
      <LightspeedScheduleSection />
    </div>
  </div>
</template>

<script setup lang="ts">
import LightspeedScheduleSection from '@/components/LightspeedScheduleSection.vue'
</script>

<style scoped>
.website-guide-page {
  padding: clamp(10px, 5vw, 20px) clamp(20px, 5vw, 40px);
  gap: 40px;
}

.tutorial-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.video-wrapper {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  flex-shrink: 0;
}

.video-container {
  position: relative;
  padding-bottom: 56.25%; /* Calculates height based on the .video-wrapper's width */
  height: 0;
  overflow: hidden;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  background-color: #1a1f2e;
}

.video-container iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: none;
}

.schedule-section {
  flex: 1;
  width: 100%;
}
</style>
