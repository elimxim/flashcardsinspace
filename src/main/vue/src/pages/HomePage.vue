<template>
  <div
    :class="[
      'page',
      'flex-column',
      'scrollbar-hidden',
      'select-text',
    ]"
  >
    <section class="what-section">
      <Starfield
        :density="120"
        :star-size="1.8"
        twinkle
        vertical-drift="3px"
      />
      <h1 class="section-header">
        <span class="section-title section-title--main">
          Welcome to<br/>
          Flashcards In Space
        </span>
        <span class="section-title section-title--sub">
          Launch Your Learning Into Outer Space
        </span>
      </h1>

      <p class="section-description section-description--short">
        <strong>What is Flashcards in Space?</strong><br>
        Flashcards in Space is a super fun and efficient spaced repetition system
        that helps you learn vocabulary or any other concept you can put into flashcards.
      </p>

      <div class="what-actions">
        <SmartButton
          text="Ready to start your journey?"
          class="action-button action-button--signup"
          :on-click="navigateToSignup"
          rounded
        />
        <SmartButton
          text="Experienced space traveller?"
          class="action-button action-button--login"
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
        <p class="section-description section-description--paragraph">
          Flashcards in Space was born from a simple dream: we envisioned a spaced repetition
          system that we'd genuinely love to use every single day. We searched through already
          existing options like Anki, Quizlet, SuperMemo, and countless other apps, but couldn't
          find anything that sparked our imagination or made learning feel truly exciting.
          It was then that we realized our dream system didn't exist yet.
        </p>
        <p class="section-description section-description--paragraph">
          Instead of settling for outdated interfaces and boring experiences, we decided
          to create our own system, complete with our own <em>Lightspeed Schedule</em> to help
          you learn new concepts at lightspeed. With fun visuals and graphics, streak counters,
          and motivation pages, plus a load of other exciting and one-of-a-kind features,
          you are bound to have a unique and engaging experience at Flashcards in Space.
        </p>
        <p class="section-description section-description--paragraph">
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
            <div class="feature-stat">
              <span class="feature-stat-title">80%</span>
              <span class="feature-stat-description">Better retention</span>
            </div>
            <div class="feature-stat">
              <span class="feature-stat-title">50%</span>
              <span class="feature-stat-description">Less time</span>
            </div>
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

        <div class="feature-card">
          <div class="feature-icon">
            <font-awesome-icon icon="fa-solid fa-chart-line"/>
          </div>
          <h3 class="feature-title">Mission Progress</h3>
          <p class="feature-description">
            We focus on what works. Our system currently features an adaptive <b>Quiz Mode</b>
            for mastering tough cards, <b>Audio support</b>, and a <b>Progress Calendar</b>.
            We’re serious about the future of your learning, and this is just the beginning
            of what we’re building together.
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
        <p class="section-description section-description--paragraph">
          Flashcards in Space uses spaced repetition to help you learn new concepts
          without wasting time studying things you don't need to. We make it super simple
          for you to get started and to keep going so that you can learn exactly what you need,
          when you need it.
        </p>
        <p class="section-description section-description--paragraph">
          Here's how your learning journey unfolds: You create flashcards for whatever you
          want to master, then simply follow our <em>Lightspeed Schedule</em>. Cards you struggle
          with come back sooner, while ones you nail get spaced out further. The magic happens
          behind the scenes as your cards automatically move through our 7-stage system,
          graduating from daily reviews all the way to long-term memory. No cramming, no
          guesswork - just consistent progress as you watch your knowledge launch into orbit!
        </p>
        <p class="section-description section-description--paragraph">
          If you are a new astronaut looking to improve your vocabulary or memory skills,
          fly on over to the "Get Help" page to learn exactly how to use Flashcards in Space!
        </p>
      </div>

      <SmartButton
        text="Learn More"
        class="action-button action-button--help"
        :on-click="navigateToSupport"
        rounded
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import Starfield from '@/components/Starfield.vue'
import SmartButton from '@/components/SmartButton.vue'
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
  router.push({ name: routeNames.getHelp })
}

onUnmounted(() => {
  audioDemo.value = undefined
})
</script>

<style scoped>
.what-section {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-content: center;
  padding: clamp(10px, 5vw ,20px) clamp(20px, 5vw, 40px);
  gap: 20px;
  color: #ffffff;
}

.what-actions {
  display: flex;
  gap: 20px;
}

@media (max-width: 470px) {
  .what-actions {
    flex-direction: column;
    align-items: center;
  }
}

.action-button {
  --smart-button--title--color: #ffffff;
  --smart-button--border-color: rgba(255, 255, 255, 0.2);
  --smart-button--width: 200px;
  --smart-button--height: 50px;
  --smart-button--title--font-size: 0.9rem;
  --smart-button--title--letter-spacing: 0.05em;
  transition: all 0.3s ease;
}

.action-button--signup {
  --smart-button--bg: linear-gradient(135deg, #7c3aed 0%, #a855f7 100%);
  --smart-button--bg--hover: linear-gradient(135deg, #8b5cf6 0%, #c084fc 100%);
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.3);
}

.action-button--login {
  --smart-button--bg: linear-gradient(135deg, #3a64ed 0%, #558ef7 100%);
  --smart-button--bg--hover: linear-gradient(135deg, #5cbbf6 0%, #84a0fc 100%);
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.3);
}

@media (hover: hover) {
  .action-button:hover {
    transform: translateY(-2px);
  }

  .action-button--signup:hover {
    box-shadow: 0 8px 30px rgba(124, 58, 237, 0.4);
  }

  .action-button--login:hover {
    box-shadow: 0 8px 30px rgba(58, 121, 237, 0.4);
  }
}

.what-tagline {
  text-align: center;
  margin-top: 1rem;
}

.what-tagline-text {
  font-family: var(--site-content--font-family);
  font-size: 0.9rem;
  color: #94a3b8;
  font-style: italic;
}

.why-section {
  position: relative;
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.1);
  padding: clamp(10px, 5vw ,20px) clamp(20px, 5vw, 40px);
  gap: 20px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 2rem;
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

.how-it-works-section {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: clamp(10px, 5vw ,20px) clamp(20px, 5vw, 40px);
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.1) 0%, rgba(0, 212, 255, 0.05) 100%);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.action-button--help {
  --smart-button--bg: linear-gradient(135deg, #3a64ed 0%, #558ef7 100%);
  --smart-button--bg--hover: linear-gradient(135deg, #5cbbf6 0%, #84a0fc 100%);
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.3);
  align-self: center;
}

@media (hover: hover) {
  .action-button--help:hover {
    box-shadow: 0 8px 30px rgba(58, 121, 237, 0.4);
  }
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
