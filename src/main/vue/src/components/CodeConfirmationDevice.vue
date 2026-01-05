<template>
  <div class="hud hud--theme">
    <div class="glass-glare"/>
    <div class="top-left-edge-highlight"/>
    <div class="bottom-right-edge-highlight"/>

    <div class="hud-display-wrapper">
      <div class="dots-container">
        <div
          v-for="i in 3"
          :key="i"
          class="dot-indicator"
          :class="{
          'active': attempts >= i,
          'locked': attempts < i
        }"></div>
      </div>
      <div class="hud-display" :class="status.toLowerCase()">
        <div class="display-header">
          <div class="display-nav-titles">
            <span class="display-title-sub">AUTHORIZATION</span>
            <span class="display-title-main">CODE CONTROL</span>
          </div>
          <div class="signal-flow">
          <span
            v-for="s in 3"
            :key="s"
            class="chevron"
            :class="{ 'pulse': status === 'SYNCING' }"
          >
            ›
          </span>
          </div>
        </div>

        <div class="code-field">
          <div class="fill-bar" :style="{ width: (input.length * 50) + '%' }"></div>
          <div class="digits-overlay">
            <span class="digit">{{ input[0] || '*' }}</span>
            <span class="digit">{{ input[1] || '*' }}</span>
          </div>
        </div>
        <div class="footer-tag">AUTO-SUBMIT ENABLED</div>
      </div>
    </div>

    <div class="keypad-grid" :class="{ 'locked': status !== 'IDLE' }">
      <button v-for="n in 9" :key="n" @click="pressKey(n.toString())" class="grid-key">
        {{ n }}
      </button>

      <button @click="clearInput" class="grid-key clear-key">C</button>
      <button @click="pressKey('0')" class="grid-key">0</button>
      <div class="empty-slot"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineEmits, defineExpose } from 'vue';

type HUDStatus = 'IDLE' | 'SYNCING' | 'ERROR' | 'SUCCESS' | 'LOCKED';

const emit = defineEmits<{
  (e: 'verify', code: string): void;
}>();

const input = ref<string>('');
const attempts = ref<number>(3);
const status = ref<HUDStatus>('IDLE');

const pressKey = (val: string) => {
  if (status.value !== 'IDLE') return;
  if (input.value.length < 2) {
    input.value += val;
    if (input.value.length === 2) {
      status.value = 'SYNCING';
      triggerFailure()
      setTimeout(() => emit('verify', input.value), 800);
    }
  }
};

const clearInput = () => {
  input.value = '';
};

const triggerSuccess = () => status.value = 'SUCCESS';

const triggerFailure = () => {
  attempts.value--;
  status.value = 'ERROR';
  setTimeout(() => {
    if (attempts.value <= 0) {
      status.value = 'LOCKED';
    } else {
      input.value = '';
      status.value = 'IDLE';
    }
  }, 1200);
};

defineExpose({ triggerSuccess, triggerFailure });
</script>

<style scoped>
.hud--theme {
  --cyan: #00f2ff;
  --glass: rgba(255, 255, 255, 0.04);
}

.hud {
  container-type: size;
  position: relative;
  height: 100%;
  aspect-ratio: 8 / 15;
  padding: 6cqb 4cqb;
  background: var(--glass);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 44px;
  overflow: hidden;
  font-family: 'Rajdhani', sans-serif;
  color: white;
  box-shadow: 0 3cqb 3cqb rgba(0, 0, 0, 0.4);
}

.glass-glare {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    140deg,
    rgba(255, 255, 255, 0.05) 25%,
    rgba(255, 255, 255, 0.02) 40%,
    transparent 41%,
    transparent 69%,
    rgba(255, 255, 255, 0.02) 70%,
    rgba(255, 255, 255, 0.05) 85%
  );
  pointer-events: none;
  z-index: 5;
}

.top-left-edge-highlight {
  position: absolute;
  filter: blur(1px);
  top: 0;
  left: 0;
  right: 15%;
  bottom: 45%;
  border-top: 2px solid rgba(255, 255, 255, 0.6);
  border-left: 2px solid rgba(255, 255, 255, 0.6);
  border-top-left-radius: 44px;
  mask-image: linear-gradient(to right, white 80%, transparent),
  linear-gradient(to bottom, white 80%, transparent);
  mask-composite: intersect;
  -webkit-mask-image: linear-gradient(to right, white 80%, transparent),
  linear-gradient(to bottom, white 80%, transparent);
  -webkit-mask-composite: source-in;
  pointer-events: none;
}

.bottom-right-edge-highlight {
  position: absolute;
  filter: blur(1px);
  top: 60%;
  left: 15%;
  right: 0;
  bottom: 0;
  border-bottom: 2px solid rgba(255, 255, 255, 0.6);
  border-right: 2px solid rgba(255, 255, 255, 0.6);
  border-bottom-right-radius: 44px;
  pointer-events: none;
  mask-image: linear-gradient(to left, white 80%, transparent),
  linear-gradient(to top, white 80%, transparent);
  mask-composite: intersect;
  -webkit-mask-image: linear-gradient(to left, white 80%, transparent),
  linear-gradient(to top, white 80%, transparent);
  -webkit-mask-composite: source-in;
}

.hud-display-wrapper {
  display: flex;
  flex-direction: column;
  gap: 1.5cqb;
}

.dots-container {
  display: flex;
  flex-direction: row;
  gap: 1.2cqb;
  justify-content: center;
  align-items: center;
  align-self: flex-end;
  margin-right: 2.5cqb;
}

.dot-indicator {
  width: 1.1cqb;
  height: 1.1cqb;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.4s ease;
}

.dot-indicator.active {
  background: #00f2ff;
  box-shadow: 0 0 2cqb #00f2ff;
}

.dot-indicator.locked {
  background: red;
  box-shadow: 0 0 2cqb darkred;
}

.hud-display {
  flex: 1;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 4cqb;
  padding: 3cqb;
  margin-bottom: 5.5cqb;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.display-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.8cqb;
}

.display-nav-titles {
  display: flex;
  flex-direction: column;
}

.display-title-sub {
  font-size: 1.5cqb;
  opacity: 0.4;
  letter-spacing: 0.2cqb;
}

.display-title-main {
  font-size: 2.7cqb;
  font-weight: 600;
  letter-spacing: 0.2cqb;
}

.signal-flow {
  display: flex;
  gap: 0.5cqb;
  color: var(--cyan);
  font-size: 5cqb;
}

.chevron.pulse {
  animation: pulse 0.6s infinite;
}

.code-field {
  height: 8cqb;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 2cqb;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fill-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 242, 255, 0.18);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.digits-overlay {
  z-index: 1;
  font-size: 4.4cqb;
  font-weight: 700;
  letter-spacing: 2.4cqb;
  color: var(--cyan);
  text-shadow: 0 0 1.5cqb rgba(0, 242, 255, 0.5);
}

.footer-tag {
  font-size: 1.5cqb;
  opacity: 0.4;
  margin-top: 1.5cqb;
  text-align: center;
  font-weight: 500;
  letter-spacing: 0.2cqb;
}

.keypad-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2.8cqb;
}

.grid-key {
  aspect-ratio: 1;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  color: rgba(255, 255, 255, 0.8);
  font-size: 3.5cqb;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  align-items: center;
  justify-content: center;
}

.grid-key:hover {
  border-color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.05);
  color: white;
}

.grid-key:active {
  transform: scale(0.9);
  background: var(--cyan);
  border-color: var(--cyan);
  color: black;
  box-shadow: 0 0 3cqb var(--cyan);
}

.clear-key {
  border-color: rgba(255, 255, 255, 0.1);
  color: #ff9d00;
  opacity: 0.8;
}

.locked {
  opacity: 0.5;
  pointer-events: none;
}

.error {
  border-color: #ff4444;
  animation: shake 0.4s ease-in-out;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 1;
  }
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  20% {
    transform: translateX(-1cqb);
  }
  40% {
    transform: translateX(1cqb);
  }
  60% {
    transform: translateX(-1cqb);
  }
  80% {
    transform: translateX(1cqb);
  }
}
</style>
