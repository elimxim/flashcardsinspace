<template>
  <div :class="[
    'page',
    'flex-column',
    'padding-auto',
    'scrollbar-hidden',
    'lightspeed-page',
  ]">
    <Starfield
      :density="120"
      :star-size="1.8"
      twinkle
      vertical-drift="3px"
    />

    <h2 class="lightspeed-header">
      <span class="lightspeed-title">
        Lightspeed Schedule
      </span>
    </h2>

    <p class="lightspeed-description">
      <strong>What is spaced repetition?</strong><br/>
      Spaced repetition is a science-based method of learning new concepts or vocabulary
      in a manageable amount of time. Instead of spending hours at a desk studying the
      same words repeatedly, you can use spaced repetition to learn the same concepts
      in a fraction of the time. It gives you a straightforward way to know what to study
      and when to study it. All you have to do is create flashcards and follow the spaced
      repetition schedule.
    </p>

    <div class="learning-timeline">
      <div class="timeline-stage" v-for="(stage, index) in stages" :key="index">
        <div class="stage-connector" v-if="index < stages.length - 1"></div>
        <div class="stage-icon" :class="stage.iconClass">
          <font-awesome-icon :icon="stage.icon"/>
        </div>
        <div class="stage-content">
          <h3 class="stage-title">{{ stage.title }}</h3>
          <p class="step-description">{{ stage.description }}</p>
          <div class="step-timing" v-if="stage.timing">
            <span>{{ stage.timing }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="summary-card">
      <div class="summary-header">
        <div class="summary-icon">
          <font-awesome-icon icon="fa-solid fa-graduation-cap"/>
        </div>
        <h3 class="summary-title">
          The Science Behind the Mission
        </h3>
      </div>
      <div class="summary-content">
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
</template>

<script setup lang="ts">
import Starfield from '@/components/Starfield.vue';

const stages = [
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

</script>

<style scoped>
.lightspeed-page {
  padding: 20px 40px;
}

.lightspeed-header {
  margin: 0;
  padding: 0;
}

.lightspeed-title {
  font-family: var(--h-page--font-family);
  display: block;
  font-size: clamp(2rem, 6vw, 3rem);
  font-weight: 800;
  line-height: 1.1;
  color: #a0c4ff;
  text-align: center;
}

.lightspeed-description {
  font-family: var(--h-page--font-family);
  font-size: clamp(1rem, 2vw, 1.2rem);
  line-height: 1.6;
  color: #e2e8f0;
}

.lightspeed-description strong {
  color: #ffd700;
}

.lightspeed-description em {
  color: #00d4ff;
  font-style: normal;
  font-weight: 600;
}

.learning-timeline {
  position: relative;
  margin: 3rem 0;
}

.timeline-stage {
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 2rem;
  margin-bottom: 3rem;
}

.timeline-stage:last-child {
  margin-bottom: 0;
}

.stage-connector {
  position: absolute;
  left: 30px;
  top: 60px;
  width: 2px;
  height: calc(100% + 3rem);
  background: linear-gradient(180deg, rgba(0, 212, 255, 0.5) 0%, rgba(124, 58, 237, 0.3) 100%);
}

.stage-icon {
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

.stage-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  margin-top: 1rem;
}

.stage-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
  padding: 0;
}

.step-description {
  color: #cbd5e1;
  line-height: 1.6;
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

.summary-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 2.5rem;
  backdrop-filter: blur(10px);
}

.summary-header {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 2rem;
}

.summary-title {
  font-size: 2rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
}

.summary-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #00d4ff 0%, #7c3aed 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  color: #ffffff;
  flex-shrink: 0;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.summary-content p {
  color: #cbd5e1;
  line-height: 1.6;
  margin: 0;
}

.science-stats {
  display: flex;
  justify-content: space-evenly;
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

</style>
