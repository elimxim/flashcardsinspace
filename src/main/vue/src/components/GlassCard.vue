<template>
  <div class="glass-rim glass-card--theme">
    <div class="glass-face">
      <div class="glass-text">
        {{ text }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  text?: string,
}>(), {
  text: '',
})
</script>

<style scoped>
.glass-card--theme {
  --gc--width: var(--glass-card--width, 240px);
  --gc--height: var(--glass-card--height, 160px);
  --gc--border-radius: var(--glass-card--border-radius, 16px);
  --gc--rim-size: var(--glass-card--rim-size, 8px);
  --gc--bg: var(--glass-card--bg, none);
  --gc--font-family: var(--glass-card--font-family, 'Quicksand', sans-serif);
  --gc--font-size: var(--glass-card--font-size, 2rem);
  --gc--font-weight: var(--glass-card--font-weight, 600);
}

.glass-rim {
  position: relative;
  width: var(--gc--width);
  height: var(--gc--height);
  border-radius: var(--gc--border-radius);
  background: var(--gc--bg);
  box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.3),
  inset 0 0 0 1px rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--gc--rim-size);
  will-change: transform;
  transform: translateZ(0);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.glass-face {
  width: 100%;
  height: 100%;
  position: relative;
  border-radius: calc(var(--gc--border-radius) - 2px);
  background: radial-gradient(120% 120% at 0% 0%, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0) 100%),
  linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  backdrop-filter: blur(10px) saturate(1.5);
  -webkit-backdrop-filter: blur(10px) saturate(1.5);
  box-shadow: inset 1px 1px 0 0 rgba(255, 255, 255, 0.5),
  inset -1px -1px 0 0 rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
  overflow: hidden;
}

.glass-rim:hover {
  transform: scale(1.02) translateZ(0);
  box-shadow: 0 20px 50px -10px rgba(0, 0, 0, 0.4),
  inset 0 0 0 1px rgba(255, 255, 255, 0.2);
}

.glass-rim:hover .glass-face {
  box-shadow: inset 1px 1px 0 0 rgba(255, 255, 255, 0.8),
  inset -1px -1px 0 0 rgba(0, 0, 0, 0.1),
  inset 0 0 20px rgba(255, 255, 255, 0.2);
}

.glass-face::after {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0) 20%,
    rgba(255, 255, 255, 0.3) 30%,
    rgba(255, 255, 255, 0.3) 70%,
    rgba(255, 255, 255, 0) 80%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  pointer-events: none;
}

.glass-rim:hover .glass-face::after {
  left: 150%;
  transition: left 0.8s ease-in-out;
}

.glass-text {
  font-family: var(--gc--font-family);
  font-size: var(--gc--font-size);
  font-weight: var(--gc--font-weight);
  text-transform: uppercase;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0.9) 0%,
    rgba(255, 255, 255, 0.8) 100%
  );
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  transform: translateZ(0);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  opacity: 0.9;
}
</style>
