<template>
  <div class="launch-button launch-button--theme">
    <Starfield
      twinkle
      :star-size="4"
      :density="16"
      vertical-drift="6px"
    />
    <AwesomeButton
      v-bind="$attrs"
      icon="fa-solid fa-rocket"
      fill-space
    />
    <div class="curtain">
      <div class="curtain-text">Start Review</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Starfield from '@/components/Starfield.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'

</script>

<style scoped>
.launch-button--theme {
  --l-btn--text-color: var(--launch-button--text-color, #e2e8f0);
  --l-btn--text-shadow-color: var(--launch-button--text-shadow-color, rgba(0, 0, 0, 0.3));
  --awesome-button--font-size: clamp(40px, 10vw, 60px);
  --awesome-button--color: var(--launch-button--color);
  --awesome-button--color--hover: var(--launch-button--color--hover);
  --awesome-button--bg: var(--launch-button--bg);
  --awesome-button--bg--hover: var(--launch-button--bg--hover);
  --awesome-button--bg--disabled: var(--launch-button--bg--disabled);
  --awesome-button--border-radius: 6px;
}

.launch-button {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 6px;
}

.curtain {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: transparent;
  backdrop-filter: blur(3px);
  border: 1px solid #1a202c;
  border-radius: 6px;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.6s ease-in-out;
  z-index: 2;
}

.curtain-text {
  font-size: clamp(12px, 2vw ,16px);
  color: var(--l-btn--text-color);
  text-shadow: 0 2px 4px var(--l-btn--text-shadow-color);
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
}

.launch-button:hover .curtain {
  transform: translateY(-100%);
}
</style>
