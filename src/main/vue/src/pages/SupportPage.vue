<template>
  <div class="page page--auto-padded support-page support-page--theme">
    <Starfield
      :density="80"
      :star-size="2"
      twinkle
      vertical-drift="4px"
    />
    <div class="support-content">
      <div class="support-header">
        <h1 class="support-title">Mission Control Center</h1>
        <h2 class="support-subtitle">Support Page Under Construction</h2>
      </div>
      <div class="support-message">
        <div class="message-panel">
          <div class="panel-indicator">
            <div class="indicator-light indicator-light--warning"></div>
            <span class="indicator-label">SYSTEM STATUS</span>
          </div>

          <div class="message-content">
            <p class="primary-message">
              Our engineering crew is currently calibrating the support communication arrays
              and fine-tuning the help navigation systems.
            </p>

            <p class="secondary-message">
              While our support bay is being upgraded, you can still reach mission control
              through the standard communication channels. All critical systems remain operational.
            </p>

            <div class="status-indicators">
              <div class="status-item">
                <div class="status-light status-light--green"></div>
                <span>Core Systems: Online</span>
              </div>
              <div class="status-item">
                <div class="status-light status-light--green"></div>
                <span>Learning Modules: Operational</span>
              </div>
              <div class="status-item">
                <div class="status-light status-light--amber"></div>
                <span>Support Bay: Under Construction</span>
              </div>
            </div>
          </div>
        </div>

        <div class="construction-info">
          <font-awesome-icon icon="fa-solid fa-tools" class="construction-icon"/>
          <span class="construction-text">Estimated completion: Next mission cycle</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Starfield from '@/components/Starfield.vue'
import AwesomeContainer from '@/components/AwesomeContainer.vue'
</script>

<style scoped>
.support-page--theme {
  --support--bg: linear-gradient(135deg,
  rgb(25, 30, 60) 0%,
  rgb(45, 55, 85) 30%,
  rgb(65, 75, 105) 70%,
  rgb(85, 95, 125) 100%
  );
  --support--panel--bg: rgba(255, 255, 255, 0.95);
  --support--panel--border: rgba(128, 128, 128, 0.3);
  --support--title--color: rgba(255, 255, 255, 0.95);
  --support--subtitle--color: rgba(255, 255, 255, 0.8);
  --support--text--color: #2d3748;
  --support--text--secondary--color: #4a5568;
  --support--indicator--bg: rgba(0, 0, 0, 0.05);
  --support--construction--color: rgba(255, 255, 255, 0.95);
}

.support-page {
  align-items: center;
  justify-content: center;
  background: var(--support--bg);
}

.support-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
  max-width: 800px;
  width: 100%;
}

.support-header {
  text-align: center;
}

.support-title {
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 700;
  color: var(--support--title--color);
  margin: 0 0 0.5rem 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.support-subtitle {
  font-size: clamp(1.2rem, 2.5vw, 1.5rem);
  font-weight: 400;
  color: var(--support--subtitle--color);
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.support-message {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  width: 100%;
}

.message-panel {
  background: var(--support--panel--bg);
  border: 1px solid var(--support--panel--border);
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
}

.panel-indicator {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  padding: 0.5rem;
  background: var(--support--indicator--bg);
  border-radius: 4px;
}

.indicator-light {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  position: relative;
}

.indicator-light--warning {
  background: #f59e0b;
  box-shadow: 0 0 8px rgba(245, 158, 11, 0.6);
}

.indicator-light--warning::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.3) 0%, transparent 70%);
  animation: pulse 2s ease-in-out infinite;
}

.indicator-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--support--text--color);
  letter-spacing: 0.05em;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.primary-message {
  font-size: 1.1rem;
  line-height: 1.6;
  color: var(--support--text--color);
  margin: 0;
}

.secondary-message {
  font-size: 1rem;
  line-height: 1.6;
  color: var(--support--text--secondary--color);
  margin: 0;
}

.status-indicators {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1rem;
  padding: 1rem;
  background: var(--support--indicator--bg);
  border-radius: 4px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.95rem;
  color: var(--support--text--color);
}

.status-light {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: relative;
}

.status-light--green {
  background: #10b981;
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.5);
}

.status-light--amber {
  background: #f59e0b;
  box-shadow: 0 0 6px rgba(245, 158, 11, 0.5);
}

.construction-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  backdrop-filter: blur(5px);
}

.construction-icon {
  font-size: 1.2rem;
  color: var(--support--construction--color);
}

.construction-text {
  font-size: 0.95rem;
  color: var(--support--construction--color);
  font-weight: 500;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .support-page {
    padding: 1rem;
  }

  .status-indicators {
    gap: 0.5rem;
  }

  .status-item {
    font-size: 0.9rem;
  }
}
</style>
