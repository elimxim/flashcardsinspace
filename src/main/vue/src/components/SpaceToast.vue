<template>
  <div
    v-if="show"
    class="tb pointer-events-auto"
    role="status"
    aria-live="polite"
    :data-variant="toast?.type"
    :data-persistent="toast?.persistent ? 'true' : 'false'"
    :style="{ '--dur': toast?.duration + 'ms' }"
    @mouseenter="toaster.pause()"
    @mouseleave="toaster.resume()"
  >
    <div class="tb-card">
      <div class="tb-ring"/>
      <Starfield twinkle verticalDrift="2px"/>
      <div class="tb-content">
        <div class="tb-icon">
        </div>

        <div class="tb-body">
          <div class="tb-title">{{ toast?.title }}</div>
          <div class="tb-msg">{{ toast?.message }}</div>
        </div>

        <button class="btn-icon" aria-label="Dismiss" @click="toaster.dismiss()">
        </button>
      </div>

      <div v-if="toast !== null && !toast.persistent"
        class="tb-progress-track">
        <Progressbar
          height="6px"
          :duration="toast?.duration"
          :paused="paused"
          trackRounded
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Starfield from './Starfield.vue'
import Progressbar from '@/components/Progressbar.vue'
import { onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useSpaceToaster } from '@/stores/toast-store.ts'

const toaster = useSpaceToaster()
const { toast, show, paused } = storeToRefs(toaster)

onUnmounted(() => toaster.reset())
</script>

<style scoped>

:root {
  --tb-from: #60a5fa;
  --tb-via: #3b82f6;
  --tb-to: #2563eb;
  --tb-glow: rgba(96, 165, 250, .30);
}

.tb[data-variant="success"] {
  --tb-from: #34d399;
  --tb-via: #6ee7b7;
  --tb-to: #10b981;
  --tb-glow: rgba(52, 211, 153, .30);
}

.tb[data-variant="error"] {
  --tb-from: #fb7185;
  --tb-via: #fda4af;
  --tb-to: #f43f5e;
  --tb-glow: rgba(251, 113, 133, .30);
}

.tb[data-variant="info"] {
  --tb-from: #38bdf8;
  --tb-via: #7dd3fc;
  --tb-to: #0ea5e9;
  --tb-glow: rgba(56, 189, 248, .30);
}

.tb[data-variant="warning"] {
  --tb-from: #fbbf24;
  --tb-via: #fcd34d;
  --tb-to: #f59e0b;
  --tb-glow: rgba(251, 191, 36, .30);
}


.tb {
  position: relative;
  width: 360px;
  max-width: 90vw;
  border-radius: 1rem;
  padding: 1px;
  background-image: linear-gradient(135deg, rgba(255, 255, 255, 0.10), rgba(255, 255, 255, 0.05), transparent);
  -webkit-backdrop-filter: blur(6px);
  backdrop-filter: blur(6px);
  box-shadow: 0 0 0 0 var(--tb-glow);
}

.tb-card {
  position: relative;
  border-radius: 1rem;
  padding: 12px 12px;
  background: rgba(11, 16, 32, 0.80);
  overflow: hidden;
}

.tb-ring {
  position: absolute;
  inset: -1px;
  border-radius: 1rem;
  opacity: 0.20;
  pointer-events: none;
  background-image: linear-gradient(
    45deg,
    var(--tb-from),
    var(--tb-via),
    var(--tb-to)
  );
}

.tb-content {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.tb-icon {
  display: grid;
  place-items: center;
  height: 36px;
  width: 36px;
  border-radius: 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.10);
  color: white;
}

.icon {
  display: none;
  width: 20px;
  height: 20px;
}

.tb-title {
  font-weight: 600;
  line-height: 1.5;
}

.tb-msg {
  color: rgba(226, 232, 240, 0.9);
  line-height: 1.5;
}

.tb-msg:empty {
  display: none;
}

.btn-ghost {
  margin-top: .5rem;
  display: inline-flex;
  align-items: center;
  gap: .25rem;
  padding: .375rem .625rem;
  font-size: .75rem;
  font-weight: 600;
  border-radius: .5rem;
  border: 1px solid rgba(255, 255, 255, .2);
  background: rgba(255, 255, 255, .1);
  color: white;
}

.btn-ghost:hover {
  background: rgba(255, 255, 255, .15);
}

/* hide button entirely when no action label provided */
.tb[data-has-action="false"] .btn-ghost {
  display: none;
}

.btn-icon {
  border-radius: .5rem;
  padding: .375rem;
  color: rgba(226, 232, 240, .8);
}

.btn-icon:hover {
  color: white;
  background: rgba(255, 255, 255, .1);
}

.tb-progress-track {
  position: relative;
  margin-top: 0.75rem;
  --progressbar--from: var(--tb-from);
  --progressbar--via: var(--tb-via);
  --progressbar--to: var(--tb-to);
  --progressbar--bg-color: var(--toast__progressbar--bg-color);
}

</style>
