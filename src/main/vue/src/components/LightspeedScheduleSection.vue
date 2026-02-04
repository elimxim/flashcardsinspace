<template>
  <div class="lightspeed-schedule">
    <h1 class="section-header">
    <span class="section-title section-title--centered">
      Lightspeed Schedule
    </span>
    </h1>

    <div class="section-description">
      <strong>What is spaced repetition?</strong>
      <p class="section-description">
        Spaced repetition is a science-based method of learning new concepts or vocabulary
        in a manageable amount of time. Instead of spending hours at a desk studying the
        same words repeatedly, you can use spaced repetition to learn the same concepts
        in a fraction of the time. It gives you a straightforward way to know what to study
        and when to study it. All you have to do is create flashcards and follow the spaced
        repetition schedule.
      </p>
      <strong>How does Lightspeed Schedule work?</strong><br/>
      <p class="section-description">
        Our <em>Lightspeed Schedule</em> is a custom spaced repetition
        algorithm that we created based on Leitner's System. It consists of 7 stages which
        are designed to accelerate learning by optimizing the timing of reviews. The schedule
        is designed to be followed daily, with reviews becoming less frequent as you progress
        through the stages. When you remember a flashcard, you complete the stage and the
        flashcard moves to the next one. But when you forget a card, it goes back to the
        first stage and its journey starts over. When you complete the last stage, the flashcard
        moves to the <strong>Outer Space</strong> stage, which means that you have mastered
        the cards and they are now permanently stored in your long-term memory. You can still
        review cards though if you want to reinforce your memory.
      </p>
    </div>

    <div class="learning-timeline">
      <Starfield
        :density="120"
        :star-size="1.8"
        vertical-drift="20px"
        twinkle
      />
      <div v-for="(stage, index) in stages" :key="index" class="timeline-stage">
        <div v-if="index < stages.length - 1" class="stage-connector"></div>
        <div class="stage-icon" :class="stage.iconClass">
          <font-awesome-icon :icon="stage.icon"/>
        </div>
        <div class="stage-content">
          <h3 class="stage-title">{{ stage.title }}</h3>
          <p class="stage-description">{{ stage.description }}</p>
          <div v-if="stage.timing" class="stage-timing">
            <span>{{ stage.timing }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="section-description">
      <p class="section-description">
        Additionally, our schedule has two special stages: <em>Unknown</em> and <em>Attempted</em>.
        We added them to give you more control over your learning process. You can review them
        at any time. You can even create a custom review session for them.
      </p>
      <strong>What are Unknown and Attempted stages?</strong><br/>
      <p class="section-description">
        The <em>Unknown</em> stage contains flashcards that you have just created. You can review
        them an unlimited number of times before they move to the first stage on the next day.
      </p>
      <p class="section-description">
        The <em>Attempted</em> stage contains flashcards that you have already seen
        but forgot. Before going back to the first stage, they move to the <em>Attempted</em>
        stage. But just for a day. Like the <em>Unknown</em> stage, you can review them
        an unlimited number of times.
      </p>
    </div>

    <div class="feature-card">
      <div class="feature-icon">
        <font-awesome-icon icon="fa-solid fa-graduation-cap"/>
      </div>
      <h3 class="feature-title">
        The Science Behind the Mission
      </h3>
      <p class="feature-description">
        Spaced repetition leverages the psychological spacing effect -
        the phenomenon where information is better retained when
        learning sessions are spaced out over time rather than massed together.
        Modern meta-analyses confirm what Ebbinghaus discovered: distributed
        practice is significantly more effective than cramming.
      </p>
      <div class="feature-stats">
        <div class="feature-stat">
          <span class="feature-stat-title">1885</span>
          <span class="feature-stat-description">First discovered by Hermann Ebbinghaus</span>
        </div>
        <div class="feature-stat">
          <span class="feature-stat-title">140+</span>
          <span class="feature-stat-description">Years of continuous research validation</span>
        </div>
        <div class="feature-stat">
          <span class="feature-stat-title">2-3x</span>
          <span class="feature-stat-description">More effective than cramming</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Starfield from '@/components/Starfield.vue'

const stages = [
  {
    title: "Launch Preparation",
    description: "Create your flashcards and prepare for liftoff. Add text, and audio to both sides.",
    icon: "fa-solid fa-rocket",
    iconClass: "stage-icon--launch",
    timing: "Stage S1 • Daily"
  },
  {
    title: "Initial Orbit",
    description: "Begin your learning journey. Review new cards frequently as your brain establishes neural pathways.",
    icon: "fa-solid fa-satellite",
    iconClass: "stage-icon--orbit",
    timing: "Stage S2-S3 • Every 2-4 days"
  },
  {
    title: "Stable Trajectory",
    description: "Knowledge stabilizes in medium-term memory. Review intervals increase as retention strengthens.",
    icon: "fa-solid fa-globe",
    iconClass: "stage-icon--stable",
    timing: "Stage S4-S5 • Every 1-2 weeks"
  },
  {
    title: "Deep Space Mission",
    description: "Information reaches long-term memory. Reviews become less frequent but maintain knowledge orbit.",
    icon: "fa-solid fa-space-shuttle",
    iconClass: "stage-icon--deep",
    timing: "Stage S6-S7 • Every 1-2 months"
  },
  {
    title: "Outer Space Mastery",
    description: "After competing Stage S7, information is permanently stored in long-term memory. No further reviews needed.",
    icon: "fa-solid fa-star",
    iconClass: "stage-icon--mastery",
    timing: "Outer Space • Mastered"
  }
]

</script>

<style scoped>
.lightspeed-schedule {
  width: 100%;
  height: 100%;
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

.stage-icon--launch {
  background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.4);
}

.stage-icon--orbit {
  background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%);
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.4);
}

.stage-icon--stable {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.4);
}

.stage-icon--deep {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.4);
}

.stage-icon--mastery {
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
  font-family: var(--site-content--font-family);
  font-size: clamp(1.2rem, 3vw, 1.4rem);
  font-weight: 600;
  color: #ffffff;
  margin: 0;
  padding: 0;
}

.stage-description {
  font-family: var(--site-content--font-family);
  font-size: clamp(1rem, 3vw, 1.2rem);
  color: #cbd5e1;
  line-height: 1.6;
  max-width: 500px;
}

.stage-timing {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: var(--site-content--font-family);
  font-size: clamp(0.9rem, 3vw, 1rem);
  color: #00d4ff;
  font-weight: 500;
}

</style>
