<template>
  <div
    :class="[
      'page',
      'flex-column',
      'scrollbar-hidden',
      'home-page',
      'home-page--theme',
    ]"
  >
    <Starfield
      :density="120"
      :star-size="1.8"
      twinkle
      vertical-drift="3px"
    />
    <section class="what-section">
      <h1 class="section-header">
        <span class="section-title section-title--main">
          Welcome to<br/>
          Flashcards In Space
        </span>
        <span class="section-title section-title--sub">
          Launch Learning Into Outer Space
        </span>
      </h1>

      <p class="section-description section-description--short">
        <strong>What is Flashcards in Space?</strong><br>
        Flashcards in Space is a super fun and efficient spaced repetition system
        that helps you learn vocabulary or any other concept you can put into flashcards.
      </p>

      <div class="what-actions">
        <SmartButton
          text="New to the galaxy?"
          class="what-action-button what-action-button--signup"
          :on-click="navigateToSignup"
          rounded
        />
        <SmartButton
          text="Been abducted by us before?"
          class="what-action-button what-action-button--login"
          :on-click="navigateToLogin"
          rounded
        />
      </div>

      <div class="what-tagline">
        <span class="what-tagline-text">Spacing Out Never Felt So Smart</span>
      </div>
    </section>

    <section class="why-section">
      <h2 class="section-header">
        <span class="section-title section-title--centered">
          Why Flashcards in Space?
        </span>
      </h2>

      <div class="section-description">
        <p class="description-paragraph">
          Flashcards in Space was born from a simple dream: we envisioned a spaced repetition
          system that we'd genuinely love to use every single day. We searched through already
          existing options like Anki, Quizlet, SuperMemo, and countless other apps, but couldn't
          find anything that sparked our imagination or made learning feel truly exciting.
          It was then that we realized our dream system didn't exist yet.
        </p>
        <p class="description-paragraph">
          Instead of settling for outdated interfaces and boring experiences, we decided
          to create our own system, complete with our own <em>Lightspeed Schedule</em> to help
          you learn new concepts at lightspeed. With fun visuals and graphics, streak counters,
          and motivation pages, plus a load of other exciting and one-of-a-kind features,
          you are bound to have a unique and engaging experience at Flashcards in Space.
        </p>
        <p class="description-paragraph">
          And, just like space explorers, we are always excited to push beyond the limits
          to create new and improved experiences for you and all the other learners out there.
        </p>
      </div>

      <div class="features-grid">
        <div class="feature-card feature-card--primary">
          <div class="feature-icon">
            <font-awesome-icon icon="fa-solid fa-rocket"/>
          </div>
          <h3 class="feature-title">Lightspeed Schedule</h3>
          <p class="feature-description">
            We created our own spaced repetition algorithm based on Leitner's System
            (thus giving us the name Lightspeed) that accelerates learning through 7
            optimized stages, launching knowledge into outer space.
          </p>
          <div class="schedule-stages">
            <div v-for="stage in 7" :key="stage" class="schedule-stage">
              <font-awesome-icon :icon="`fa-solid fa-${stage}`"/>
            </div>
            <div class="schedule-stage schedule-stage--transparent">
              <font-awesome-icon icon="fa-solid fa-arrow-right-long"/>
            </div>
            <div class="schedule-stage schedule-stage--final">
              <font-awesome-icon icon="fa-solid fa-infinity"/>
            </div>
          </div>
        </div>

        <div class="feature-card">
          <div class="feature-icon">
            <font-awesome-icon icon="fa-solid fa-brain"/>
          </div>
          <h3 class="feature-title">Scientific Method</h3>
          <p class="feature-description">
            Thanks to the brilliant minds who pioneered spaced repetition - Hermann Ebbinghaus,
            Cecil Mace, Sebastian Leitner, and others - our work builds on their foundation.
            Their groundbreaking work proved that spaced learning delivers remarkable results.
          </p>
          <div class="feature-stats">
            <div class="stat">
              <span class="stat-value">80%</span>
              <span class="stat-label">Better Retention</span>
            </div>
            <div class="stat">
              <span class="stat-value">50%</span>
              <span class="stat-label">Less Time</span>
            </div>
          </div>
        </div>

        <div class="feature-card">
          <div class="feature-icon">
            <font-awesome-icon icon="fa-solid fa-volume-high"/>
          </div>
          <h3 class="feature-title">Audio Recording</h3>
          <p class="feature-description">
            Record and play audio for both sides of your flashcards.
            Perfect for language learning and pronunciation practice.
          </p>
          <div class="feature-demo">
            <span class="demo-text">Try the audio feature</span>
            <VoiceRecorder v-model="audioDemo" expanded no-trash/>
          </div>
        </div>

        <div class="feature-card">
          <div class="feature-icon">
            <font-awesome-icon icon="fa-solid fa-palette"/>
          </div>
          <h3 class="feature-title">Engaging Visual Interface</h3>
          <p class="feature-description">
            Experience learning through our thoughtfully crafted interface that makes everything
            feel like piloting a spaceship. With intuitive visual elements and interactive features,
            every element is designed to be simple, fun, and engaging, transforming your study
            sessions into an enjoyable adventure.
          </p>
        </div>
      </div>
    </section>

    <section class="how-it-works-section">
      <h2 class="section-header">
          <span class="section-title section-title--centered">
            How does Flashcards in Space work?
          </span>
      </h2>

      <div class="section-description">
        <p class="description-paragraph">
          Flashcards in Space uses spaced repetition to help you learn new concepts
          without wasting time studying things you don't need to. We make it super simple
          for you to get started and to keep going so that you can learn exactly what you need,
          when you need it.
        </p>
        <p class="description-paragraph">
          Here's how your learning journey unfolds: You create flashcards for whatever you
          want to master, then simply follow our <em>Lightspeed Schedule</em>. Cards you struggle
          with come back sooner, while ones you nail get spaced out further. The magic happens
          behind the scenes as your cards automatically move through our 7-stage system,
          graduating from daily reviews all the way to long-term memory. No cramming, no
          guesswork - just consistent progress as you watch your knowledge launch into orbit!
        </p>
        <p class="description-paragraph">
          If you are a new astronaut looking to improve your vocabulary or memory skills,
          fly on over to the "support" page to learn exactly how to use Flashcards in Space!
        </p>
      </div>

      <SmartButton
        text="Go to Support Page"
        class="help-action-button"
        :on-click="navigateToSupport"
        rounded
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import Starfield from '@/components/Starfield.vue'
import SmartButton from '@/components/SmartButton.vue'
import VoiceRecorder from '@/components/VoiceRecorder.vue'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'
import { onUnmounted, ref } from 'vue'

const router = useRouter()

const audioDemo = ref<Blob | undefined>()

function navigateToSignup() {
  router.push({ name: routeNames.signup })
}

function navigateToLogin() {
  router.push({ name: routeNames.login })
}

function navigateToSupport() {
  router.push({ name: routeNames.support })
}

onUnmounted(() => {
  audioDemo.value = undefined
})
</script>

<style scoped>
.home-page--theme {
  --h-page--font-family: var(--home-page--font-family);
}

.section-header {
  margin: 0;
  padding: 0;
}

.section-title {
  font-family: var(--h-page--font-family);
  display: block;
  font-size: clamp(2rem, 6vw, 3rem);
  font-weight: 800;
  line-height: 1.1;
  color: #a0c4ff;
}

.section-title--main {
  font-size: clamp(2.5rem, 6vw, 4rem);
}

.section-title--sub {
  font-size: clamp(1rem, 2.5vw, 1.5rem);
  font-weight: 300;
  font-style: italic;
  line-height: 1;
  color: #b0c4de;
}

.section-title--centered {
  text-align: center;
}

.section-description {
  font-family: var(--h-page--font-family);
  font-size: clamp(1rem, 2vw, 1.2rem);
  line-height: 1.6;
  color: #e2e8f0;
}

.section-description--short {
  max-width: 500px;
  text-indent: unset;
}

.section-description strong {
  color: #ffd700;
}

.section-description em {
  color: #00d4ff;
  font-style: normal;
  font-weight: 600;
}

.description-paragraph {
  font-family: var(--h-page--font-family);
  text-indent: 2rem;
  text-align: justify;
}

.what-section {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-content: center;
  padding: 20px 40px;
  gap: 20px;
  color: #ffffff;
}

.stat-label {
  font-size: 0.8rem;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-top: 0.25rem;
}

.what-actions {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.what-action-button {
  --smart-button--title--color: #ffffff;
  --smart-button--border-color: rgba(255, 255, 255, 0.2);
  --smart-button--width: 200px;
  --smart-button--height: 50px;
  --smart-button--title--font-size: 0.9rem;
  --smart-button--title--letter-spacing: 0.05em;
  transition: all 0.3s ease;
}

.what-action-button:hover {
  transform: translateY(-2px);
}

.what-action-button--signup {
  --smart-button--bg: linear-gradient(135deg, #7c3aed 0%, #a855f7 100%);
  --smart-button--bg--hover: linear-gradient(135deg, #8b5cf6 0%, #c084fc 100%);
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.3);
}

.what-action-button--signup:hover {
  box-shadow: 0 8px 30px rgba(124, 58, 237, 0.4);
}

.what-action-button--login {
  --smart-button--bg: linear-gradient(135deg, #3a64ed 0%, #558ef7 100%);
  --smart-button--bg--hover: linear-gradient(135deg, #5cbbf6 0%, #84a0fc 100%);
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.3);
}

.what-action-button--login:hover {
  box-shadow: 0 8px 30px rgba(58, 121, 237, 0.4);
}

.what-tagline {
  text-align: center;
  margin-top: 1rem;
}

.what-tagline-text {
  font-family: var(--h-page--font-family);
  font-size: 0.9rem;
  color: #94a3b8;
  font-style: italic;
}

.why-section {
  position: relative;
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  padding: 20px 40px;
  gap: 20px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 2rem;
}

.feature-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 2rem;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.5), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  border-color: rgba(0, 212, 255, 0.3);
  box-shadow: 0 10px 40px rgba(0, 212, 255, 0.1);
}

.feature-card:hover::before {
  opacity: 1;
}

.feature-card--primary {
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.1) 0%, rgba(168, 85, 247, 0.1) 100%);
  border-color: rgba(124, 58, 237, 0.3);
}

.feature-card--primary:hover {
  border-color: rgba(124, 58, 237, 0.5);
  box-shadow: 0 10px 40px rgba(124, 58, 237, 0.2);
}

.feature-card--primary::before {
  background: linear-gradient(90deg, transparent, rgba(200, 0, 255, 0.5), transparent);
}

.feature-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #00d4ff 0%, #7c3aed 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.5rem;
  font-size: 34px;
  color: #ffffff;
}

.feature-card--primary .feature-icon {
  background: linear-gradient(135deg, #7c3aed 0%, #a855f7 100%);
}

.feature-title {
  font-family: var(--h-page--font-family);
  font-size: 1.3rem;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 1rem;
}

.feature-description {
  font-family: var(--h-page--font-family);
  color: #cbd5e1;
  line-height: 1.6;
}

.schedule-stages {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.schedule-stage {
  width: 32px;
  height: 32px;
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid rgba(0, 212, 255, 0.4);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 600;
  color: #00d4ff;
}

.schedule-stage--final {
  background: linear-gradient(135deg, #7c3aed 0%, #a855f7 100%);
  border-color: #7c3aed;
  color: #ffffff;
}

.schedule-stage--transparent {
  background: none;
  border: none;
}

.feature-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: auto;
  align-items: center;
  text-align: center;
  gap: 20px;
}

.stat-value {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: #00d4ff;
}

.stat-label {
  font-size: 0.8rem;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.feature-demo {
  display: flex;
  flex-direction: column;
  align-items: start;
  gap: 1rem;

  --voice-recorder--time--color: #ffffff;
  --voice-recorder--time--bg: rgba(0, 212, 255, 0.2);
  --voice-recorder--controls--bg: rgba(0, 212, 255, 0.15);
  --voice-recorder--button--color: #00d4ff;
  --voice-recorder--button--color--hover: #ffffff;
  --voice-recorder--button--color--active: #ffffff;
  --voice-recorder--button--color--disabled: #94a3b8;
  --voice-recorder--mic-button--bg--hover: rgba(0, 212, 255, 0.2);
  --voice-recorder--mic-button--bg--active: rgba(0, 212, 255, 0.2);
  --voice-recorder--play-button--bg--hover: rgba(0, 212, 255, 0.2);
  --voice-recorder--play-button--bg--active: rgba(0, 212, 255, 0.2);
  --voice-recorder--warning-text--color: #cbd5e1;
}

.demo-text {
  color: #94a3b8;
  font-size: 0.9rem;
}

.how-it-works-section {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px 40px;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.1) 0%, rgba(0, 212, 255, 0.05) 100%);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.help-action-button {
  --smart-button--bg: linear-gradient(135deg, #00f7ff 0%, #00d4ff 100%);
  --smart-button--bg--hover: linear-gradient(135deg, #00ffff 0%, #3a49ed 100%);
  --smart-button--title--color: #000000;
  --smart-button--title--color--hover: #ffffff;
  --smart-button--width: 200px;
  --smart-button--height: 50px;
  --smart-button--title--font-size: 0.9rem;
  --smart-button--title--letter-spacing: 0.05em;
  font-weight: 700;
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.3);
  transition: all 0.3s ease;
  align-self: center;
}

.help-action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(58, 121, 237, 0.4);
}

@media (max-width: 1000px) {
  .features-grid {
    display: grid;
    grid-template-columns: repeat(1, 1fr);
    grid-template-rows: repeat(4, 1fr);
    gap: 2rem;
  }
}

</style>
