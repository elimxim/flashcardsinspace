<template>
  <div class="code-hud">
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
            'dot-indicator--active': i > attempts,
            'dot-indicator--locked': i <= attempts
        }"></div>
      </div>
      <div class="hud-display" :class="`hud-display--${status.toLowerCase()}`">
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
          <div class="code-progress-bar" :style="{ width: (input.length * 25) + '%' }"/>
          <div class="code-digits-overlay">
            <span class="code-digit">{{ input[0] || '*' }}</span>
            <span class="code-digit">{{ input[1] || '*' }}</span>
            <span class="code-digit">{{ input[2] || '*' }}</span>
            <span class="code-digit">{{ input[3] || '*' }}</span>
          </div>
        </div>
        <div class="display-footer-tag">AUTO-SUBMIT ENABLED</div>
      </div>
    </div>

    <div class="hud-keypad-grid">
      <button
        v-for="n in 9"
        :key="n"
        class="hud-grid-key"
        :class="{ 'hud-grid-key--locked': status !== 'IDLE' }"
        @click="handleKeyPress(n.toString())"
      >
        {{ n }}
      </button>

      <button
        class="hud-grid-key hud-clear-key"
        :class="{ 'hud-grid-key--locked': status !== 'IDLE' }"
        @click="handleKeyPress('C')"
      >
        C
      </button>
      <button
        class="hud-grid-key"
        :class="{ 'hud-grid-key--locked': status !== 'IDLE' }"
        @click="handleKeyPress('0')"
      >
        0
      </button>
      <button
        class="hud-grid-key hud-resend-key"
        :class="{ 'hud-grid-key--locked': isResetLocked }"
        @click="handleKeyPress('R')"
      >
        ↻
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineExpose, nextTick, computed } from 'vue'
import { UXConfig } from '@/utils/device-utils.ts'

const props = defineProps<{
  attempts: number
  verifyCode: (code: string) => Promise<void>
  resendCode: () => Promise<void>
}>()

const KEY_ANIMATION_DURATION = 200

type HudStatus = 'IDLE' | 'SYNCING' | 'ERROR' | 'SUCCESS' | 'OFF' | 'LOCKED'

const input = ref<string>('')
const dirty = ref(false)
const status = ref<HudStatus>('IDLE')
const animatingOnTap = ref(false)
const animationDurationMs = computed(() => `${KEY_ANIMATION_DURATION}ms`)

let successTimeout: ReturnType<typeof setTimeout> | null = null
let failureTimeout: ReturnType<typeof setTimeout> | null = null

const isResetLocked = computed(() => {
  return status.value === 'SUCCESS' || status.value !== 'OFF' && dirty.value || status.value === 'LOCKED'
})

const handleKeyPress = async (key: string) => {
  if (UXConfig().showAnimationOnTap) {
    await startTapAnimation(key)
  } else {
    await processKeyPress(key)
  }
}

const startTapAnimation = async (key: string) => {
  animatingOnTap.value = true
  setTimeout(async () => {
    animatingOnTap.value = false
    await processKeyPress(key)
  }, KEY_ANIMATION_DURATION)
}

const processKeyPress = async (key: string) => {
  if (/^\d$/.test(key)) await pressKey(key)
  if (key === 'C') clearInput()
  if (key === 'R') await resendCode()
}

const pressKey = async (digit: string) => {
  dirty.value = true
  if (status.value !== 'IDLE') return
  if (input.value.length < 4) {
    input.value += digit
    if (input.value.length === 4) {
      status.value = 'SYNCING'
      await nextTick()
      await props.verifyCode(input.value)
    }
  }
}

const clearInput = () => {
  input.value = ''
}

const resendCode = async () => {
  if (status.value !== 'OFF' && dirty.value) return
  status.value = 'SYNCING'
  await nextTick()
  await props.resendCode()
  clearInput()
}

const triggerIdle = () => status.value = 'IDLE'

const switchOff = () => status.value = 'OFF'

const lock = () => status.value = 'LOCKED'

const triggerSuccess = async (): Promise<void> => {
  return new Promise((resolve) => {
    status.value = 'SUCCESS'

    if (successTimeout) {
      clearTimeout(successTimeout)
      successTimeout = null
    }

    successTimeout = setTimeout(() => {
      successTimeout = null
      resolve()
    }, 500)
  })
}

const triggerFailure = async (): Promise<void> => {
  return new Promise((resolve) => {
    status.value = 'ERROR'
    if (failureTimeout) {
      clearTimeout(failureTimeout)
      failureTimeout = null
    }

    failureTimeout = setTimeout(() => {
      if (props.attempts >= 3) {
        status.value = 'OFF'
      } else {
        clearInput()
        status.value = 'IDLE'
      }
      failureTimeout = null
      resolve()
    }, 500)
  })
}

defineExpose({
  triggerIdle,
  triggerSuccess,
  triggerFailure,
  switchOff,
  lock,
})

</script>

<style scoped>
.code-hud {
  container-type: size;
  position: relative;
  height: 100%;
  aspect-ratio: 8 / 15;
  padding: 6cqb 4cqb;
  background: rgba(255, 255, 255, 0.04);
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
  mask-image: linear-gradient(to right, white 80%, transparent), linear-gradient(to bottom, white 80%, transparent);
  mask-composite: intersect;
  -webkit-mask-image: linear-gradient(to right, white 80%, transparent), linear-gradient(to bottom, white 80%, transparent);
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
  mask-image: linear-gradient(to left, white 80%, transparent), linear-gradient(to top, white 80%, transparent);
  mask-composite: intersect;
  -webkit-mask-image: linear-gradient(to left, white 80%, transparent), linear-gradient(to top, white 80%, transparent);
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

.dot-indicator--active {
  background: #00f2ff;
  box-shadow: 0 0 2cqb #00f2ff;
}

.dot-indicator--locked {
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

.hud-display--syncing {
  pointer-events: none;
}

.hud-display--success {
  border-color: rgba(74, 255, 68, 0.8);
}

.hud-display--error {
  border-color: rgba(255, 68, 68, 0.8);
  animation: shake 0.4s ease-in-out;
  pointer-events: none;
  filter: blur(1px);
}

.hud-display--off {
  opacity: 0.5;
  pointer-events: none;
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
  color: #00f2ff;
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

.code-progress-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 242, 255, 0.18);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.code-digits-overlay {
  width: 100%;
  display: flex;
  flex-direction: row;
  gap: 1.2cqb;
  align-items: center;
  justify-content: center;
  font-size: 4.4cqb;
  font-weight: 700;
  color: #00f2ff;
  text-shadow: 0 0 1.5cqb rgba(0, 242, 255, 0.5);
  z-index: 1;
}

.code-digit {
  width: 4cqb;
  text-align: center;
}

.display-footer-tag {
  font-size: 1.5cqb;
  opacity: 0.4;
  margin-top: 1.5cqb;
  text-align: center;
  font-weight: 500;
  letter-spacing: 0.2cqb;
}

.hud-keypad-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2.8cqb;
}

.hud-grid-key {
  aspect-ratio: 1;
  background: transparent;
  border: 2px solid rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  color: rgba(255, 255, 255, 0.8);
  font-size: 3.5cqb;
  cursor: pointer;
  transition: all v-bind(animationDurationMs) cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hud-grid-key--locked {
  color: rgba(255, 255, 255, 0.15);
  border-width: 1px;
  pointer-events: none;
}

@media (hover: hover) {
  .hud-grid-key:not(.hud-clear-key):not(.hud-resend-key):hover {
    border-color: rgba(255, 255, 255, 0.6);
    background: rgba(255, 255, 255, 0.05);
    color: white;
  }
}

.hud-grid-key:not(.hud-clear-key):not(.hud-resend-key):active {
  transform: scale(0.9);
  background: #00f2ff;
  border-color: #00f2ff;
  color: black;
  box-shadow: 0 0 3cqb #00f2ff;
}

.hud-clear-key:not(.hud-grid-key--locked) {
  color: #ff9d00;
  border-color: rgba(255, 157, 0, 0.2);
}

@media (hover: hover) {
  .hud-clear-key:hover {
    border-color: #ff9d00;
    background: rgba(255, 255, 255, 0.02);
  }
}

.hud-clear-key:active {
  transform: scale(0.9);
  background: #ff9d00;
  border-color: #ff9d00;
  color: black;
  box-shadow: 0 0 3cqb #ff9d00;
}

.hud-resend-key:not(.hud-grid-key--locked) {
  color: #ff0000;
  border-color: rgba(255, 0, 0, 0.2);
}

@media (hover: hover) {
  .hud-resend-key:hover {
    border-color: #ff0000;
    background: rgba(255, 255, 255, 0.02);
  }
}

.hud-resend-key:active {
  transform: scale(0.9);
  background: #ff0000;
  border-color: #ff0000;
  color: black;
  box-shadow: 0 0 3cqb #ff0000;
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
