<template>
  <div :class="[
    'page',
    'flex-column',
    'scrollbar-hidden',
    'home-page',
    'home-page--theme',
  ]">
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
          text="Signup"
          class="what-action-button what-action-button--signup"
          :on-click="navigateToSignup"
          rounded
        />
        <SmartButton
          text="Login"
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
            <div class="schedule-stage" v-for="stage in 7" :key="stage">
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
            <font-awesome-icon icon="fa-solid fa-chart-line"/>
          </div>
          <h3 class="feature-title">Mission Progress</h3>
          <p class="feature-description">
            Track your learning journey with detailed statistics,
            day streaks, and stage progression analytics.
          </p>
          <div class="progress-demo">
            <div class="progress-bar">
              <div class="progress-fill" style="width: 68%"></div>
            </div>
            <span class="progress-text">68% to Outer Space</span>
          </div>
        </div>
      </div>
    </section>

    <!-- How It Works Section -->
    <section class="how-it-works-section">
      <div class="how-it-works-container">
        <div class="section-header">
          <h2 class="section-header">Mission Protocol</h2>
          <p class="section-subtitle">
            From rookie astronaut to space commander in 7 stages
          </p>
        </div>

        <div class="mission-timeline">
          <div class="timeline-step" v-for="(step, index) in missionSteps" :key="index">
            <div class="step-connector" v-if="index < missionSteps.length - 1"></div>
            <div class="step-icon" :class="step.iconClass">
              <font-awesome-icon :icon="step.icon"/>
            </div>
            <div class="step-content">
              <h3 class="step-title">{{ step.title }}</h3>
              <p class="step-description">{{ step.description }}</p>
              <div class="step-timing" v-if="step.timing">
                <GlowLamp :on="true" :size="12" :intensity="0.6"/>
                <span>{{ step.timing }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="mission-summary">
          <div class="summary-card">
            <div class="summary-icon">
              <font-awesome-icon icon="fa-solid fa-graduation-cap"/>
            </div>
            <div class="summary-content">
              <h3>The Science Behind the Mission</h3>
              <p>
                Spaced repetition leverages the psychological spacing effect -
                the phenomenon where information is better retained when
                learning sessions are spaced out over time rather than massed together.
                Modern meta-analyses confirm what Ebbinghaus discovered: distributed
                practice is significantly more effective than cramming.
              </p>
              <div class="science-stats">
                <div class="science-stat">
                  <span class="stat-number">1885</span>
                  <span class="stat-label">First discovered by Hermann Ebbinghaus</span>
                </div>
                <div class="science-stat">
                  <span class="stat-number">140+</span>
                  <span class="stat-label">Years of continuous research validation</span>
                </div>
                <div class="science-stat">
                  <span class="stat-number">2-3x</span>
                  <span class="stat-label">More effective than cramming</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Call to Action Section -->
    <section class="cta-section">
      <div class="cta-container">
        <div class="cta-content">
          <div class="cta-badge">
            <GlowLamp :on="true" :size="14" :intensity="0.9" class="cta-badge-lamp"/>
            <span class="cta-badge-text">READY FOR LAUNCH</span>
          </div>

          <h2 class="cta-title">
            Begin Your Mission to
            <span class="cta-title-highlight">Outer Space</span>
          </h2>

          <p class="cta-description">
            Join thousands of space cadets who've successfully launched their knowledge into
            permanent orbit.
            <strong>No crash landings on exam day guaranteed!</strong>
          </p>

          <div class="cta-actions">
            <SmartButton
              text="START FREE MISSION"
              class="cta-primary"
              :on-click="navigateToSignup"
              rounded
            />
            <SmartButton
              text="EXISTING ASTRONAUT?"
              class="cta-secondary"
              :on-click="navigateToLogin"
              rounded
            />
          </div>

          <div class="cta-features">
            <div class="cta-feature">
              <font-awesome-icon icon="fa-solid fa-check" class="feature-check"/>
              <span>Free forever for basic missions</span>
            </div>
            <div class="cta-feature">
              <font-awesome-icon icon="fa-solid fa-check" class="feature-check"/>
              <span>No credit card required for liftoff</span>
            </div>
            <div class="cta-feature">
              <font-awesome-icon icon="fa-solid fa-check" class="feature-check"/>
              <span>Instant mission deployment</span>
            </div>
          </div>
        </div>

        <div class="cta-visual">
          <div class="rocket-animation">
            <div class="rocket">
              <font-awesome-icon icon="fa-solid fa-rocket"/>
            </div>
            <div class="rocket-trail"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="home-footer">
      <div class="footer-content">
        <p class="footer-text">
          Made with 🚀 for space explorers everywhere
        </p>
        <div class="footer-links">
          <router-link :to="{ name: routeNames.support }" class="footer-link">
            Mission Support
          </router-link>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import Starfield from '@/components/Starfield.vue'
import GlowLamp from '@/components/control-panel/GlowLamp.vue'
import SmartButton from '@/components/SmartButton.vue'
import VoiceRecorder from '@/components/VoiceRecorder.vue'
import { useRouter } from 'vue-router'
import { routeNames } from '@/router'
import { onUnmounted, ref } from 'vue'

const router = useRouter()

const audioDemo = ref<Blob | undefined>()

const missionSteps = [
  {
    title: "Launch Preparation",
    description: "Create your flashcards and prepare for liftoff. Add text, images, and audio to both sides.",
    icon: "fa-solid fa-rocket",
    iconClass: "step-icon--launch",
    timing: "Stage S1 • Daily"
  },
  {
    title: "Initial Orbit",
    description: "Begin your learning journey. Review new cards frequently as your brain establishes neural pathways.",
    icon: "fa-solid fa-satellite",
    iconClass: "step-icon--orbit",
    timing: "Stage S2-S3 • Every 2-4 days"
  },
  {
    title: "Stable Trajectory",
    description: "Knowledge stabilizes in medium-term memory. Review intervals increase as retention strengthens.",
    icon: "fa-solid fa-globe",
    iconClass: "step-icon--stable",
    timing: "Stage S4-S5 • Weekly"
  },
  {
    title: "Deep Space Mission",
    description: "Information reaches long-term memory. Reviews become less frequent but maintain knowledge orbit.",
    icon: "fa-solid fa-space-shuttle",
    iconClass: "step-icon--deep",
    timing: "Stage S6-S7 • Monthly"
  },
  {
    title: "Outer Space Mastery",
    description: "Congratulations, Commander! Knowledge has achieved permanent orbit in your long-term memory.",
    icon: "fa-solid fa-star",
    iconClass: "step-icon--mastery",
    timing: "Outer Space • Mastered"
  }
]

function navigateToSignup() {
  router.push({ name: routeNames.signup })
}

function navigateToLogin() {
  router.push({ name: routeNames.login })
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

.stat-number {
  font-family: var(--day-streak--font-family);
  font-size: 2rem;
  font-weight: 800;
  color: #00d4ff;
  line-height: 1;
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
  --smart-button--width: 180px;
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

.section-subtitle {
  font-size: clamp(1rem, 2vw, 1.2rem);
  color: #94a3b8;
  max-width: 600px;
  margin: 0 auto;
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
  font-size: 1.5rem;
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

.progress-demo {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #00d4ff 0%, #7c3aed 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  color: #94a3b8;
  font-size: 0.9rem;
  text-align: center;
}

/* How It Works Section */
.how-it-works-section {
  position: relative;
  z-index: 1;
  padding: 4rem clamp(1rem, 5vw, 4rem);
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.8) 0%, rgba(30, 41, 59, 0.6) 100%);
}

.how-it-works-container {
  max-width: 1000px;
  margin: 0 auto;
}

.mission-timeline {
  position: relative;
  margin: 3rem 0;
}

.timeline-step {
  display: flex;
  align-items: flex-start;
  gap: 2rem;
  margin-bottom: 3rem;
  position: relative;
}

.timeline-step:last-child {
  margin-bottom: 0;
}

.step-connector {
  position: absolute;
  left: 30px;
  top: 60px;
  width: 2px;
  height: calc(100% + 3rem);
  background: linear-gradient(180deg, rgba(0, 212, 255, 0.5) 0%, rgba(124, 58, 237, 0.3) 100%);
}

.step-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: #ffffff;
  position: relative;
  z-index: 2;
  flex-shrink: 0;
}

.step-icon--launch {
  background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.4);
}

.step-icon--orbit {
  background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%);
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.4);
}

.step-icon--stable {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.4);
}

.step-icon--deep {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.4);
}

.step-icon--mastery {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  box-shadow: 0 0 20px rgba(251, 191, 36, 0.4);
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 0 20px rgba(251, 191, 36, 0.4);
  }
  50% {
    box-shadow: 0 0 30px rgba(251, 191, 36, 0.6);
  }
}

.step-content {
  flex: 1;
  padding-top: 0.5rem;
}

.step-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 0.75rem;
}

.step-description {
  color: #cbd5e1;
  line-height: 1.6;
  margin-bottom: 1rem;
  max-width: 500px;
}

.step-timing {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #00d4ff;
  font-weight: 500;
}

.step-timing .lamp {
  --glow-lamp--color: #00d4ff;
}

.mission-summary {
  margin-top: 4rem;
}

.summary-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 2.5rem;
  backdrop-filter: blur(10px);
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.summary-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #00d4ff 0%, #7c3aed 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #ffffff;
  flex-shrink: 0;
}

.summary-content h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 1rem;
}

.summary-content p {
  color: #cbd5e1;
  line-height: 1.6;
  margin-bottom: 2rem;
}

.science-stats {
  display: flex;
  gap: 3rem;
}

.science-stat {
  text-align: center;
}

.science-stat .stat-number {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  color: #00d4ff;
  margin-bottom: 0.5rem;
}

.science-stat .stat-label {
  font-size: 0.9rem;
  color: #94a3b8;
  line-height: 1.3;
}



/* Call to Action Section */
.cta-section {
  position: relative;
  z-index: 1;
  padding: 4rem clamp(1rem, 5vw, 4rem);
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.1) 0%, rgba(0, 212, 255, 0.05) 100%);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.cta-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 4rem;
}

.cta-content {
  flex: 1;
  max-width: 600px;
}

.cta-badge {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  padding: 0.5rem 1rem;
  background: rgba(0, 255, 136, 0.1);
  border: 1px solid rgba(0, 255, 136, 0.2);
  border-radius: 25px;
  backdrop-filter: blur(10px);
  width: fit-content;
}

.cta-badge-lamp {
  --glow-lamp--color: #00ff88;
}

.cta-badge-text {
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #00ff88;
}

.cta-title {
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 1.5rem;
  line-height: 1.2;
}

.cta-title-highlight {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.cta-description {
  font-size: clamp(1rem, 2vw, 1.2rem);
  line-height: 1.6;
  color: #cbd5e1;
  margin-bottom: 2.5rem;
}

.cta-description strong {
  color: #fbbf24;
}

.cta-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.cta-primary {
  --smart-button--bg: linear-gradient(135deg, #00ff88 0%, #00d4ff 100%);
  --smart-button--bg--hover: linear-gradient(135deg, #00ff88 0%, #7c3aed 100%);
  --smart-button--title--color: #000000;
  --smart-button--title--color--hover: #ffffff;
  --smart-button--width: 200px;
  --smart-button--height: 50px;
  --smart-button--title--font-size: 0.9rem;
  --smart-button--title--letter-spacing: 0.05em;
  font-weight: 700;
  box-shadow: 0 4px 20px rgba(0, 255, 136, 0.3);
  transition: all 0.3s ease;
}

.cta-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 255, 136, 0.4);
}

.cta-secondary {
  --smart-button--bg: rgba(255, 255, 255, 0.1);
  --smart-button--bg--hover: rgba(255, 255, 255, 0.2);
  --smart-button--title--color: #ffffff;
  --smart-button--border-color: rgba(255, 255, 255, 0.2);
  --smart-button--width: 200px;
  --smart-button--height: 50px;
  --smart-button--title--font-size: 0.9rem;
  border: 1px solid var(--smart-button--border-color);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.cta-secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(255, 255, 255, 0.1);
}

.cta-features {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.cta-feature {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #94a3b8;
  font-size: 0.9rem;
}

.feature-check {
  color: #00ff88;
  font-size: 0.8rem;
}

.cta-visual {
  flex: 0 0 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.rocket-animation {
  position: relative;
  width: 200px;
  height: 200px;
}

.rocket {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-45deg);
  font-size: 3rem;
  color: #00d4ff;
  animation: rocket-float 3s ease-in-out infinite;
  z-index: 2;
}

.rocket-trail {
  position: absolute;
  top: 60%;
  left: 60%;
  width: 100px;
  height: 4px;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 187, 36, 0.8) 50%, transparent 100%);
  border-radius: 2px;
  transform: rotate(-45deg);
  animation: trail-pulse 3s ease-in-out infinite;
}

@keyframes rocket-float {
  0%, 100% {
    transform: translate(-50%, -50%) rotate(-45deg) translateY(0px);
  }
  50% {
    transform: translate(-50%, -50%) rotate(-45deg) translateY(-10px);
  }
}

@keyframes trail-pulse {
  0%, 100% {
    opacity: 0.6;
    transform: rotate(-45deg) scaleX(1);
  }
  50% {
    opacity: 1;
    transform: rotate(-45deg) scaleX(1.2);
  }
}

/* Footer */
.home-footer {
  position: relative;
  z-index: 1;
  padding: 2rem clamp(1rem, 5vw, 4rem);
  background: rgba(0, 0, 0, 0.3);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.footer-text {
  color: #94a3b8;
  font-size: 0.9rem;
}

.footer-links {
  display: flex;
  gap: 2rem;
}

.footer-link {
  color: #00d4ff;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.3s ease;
}

.footer-link:hover {
  color: #ffffff;
}

</style>
