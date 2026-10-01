<template>
  <div class="launch-button launch-button--theme" :class="{ 'launch-button--disabled': disabled }">
    <Starfield
      class="launch-button-starfield"
      :star-size="4"
      :density="16"
      vertical-drift="6px"
      twinkle
    />
    <AwesomeButton
      v-bind="$attrs"
      icon="fa-solid fa-rocket"
      :disabled="disabled"
      :animation-duration="600"
      :hold-time="REVERSE_HOLD_TIME"
      :held="reversed"
      :on-hold="toggleReversed"
      fill-space
    />
    <div class="curtain">
      <div class="curtain-text">
        <template v-if="!disabled"> Start Review </template>
        <template v-else> Nothing to Review </template>
      </div>
    </div>
    <WidgetReviewBadge :reversed="reversed" />
  </div>
</template>

<script setup lang="ts">
import Starfield from '@/components/common/Starfield.vue'
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import { REVERSE_HOLD_TIME, useReverseMode } from '@/utils/reverse-mode.ts'
import { ReviewSessionType } from '@/core-logic/review-logic.ts'
import WidgetReviewBadge from '@/components/WidgetReviewBadge.vue'

withDefaults(
  defineProps<{
    disabled?: boolean
  }>(),
  {
    disabled: false,
  },
)

const { reversed, toggle: toggleReversed } = useReverseMode(ReviewSessionType.LIGHTSPEED)
</script>

<style scoped>
.launch-button--theme {
  --awesome-button--icon--size: 70px;
  --awesome-button--icon--color: var(--cp--text--color);
  --awesome-button--icon--color--hover: var(--cp--text--color--active);
  --awesome-button--icon--color--disabled: var(--cp--text--color);
  --awesome-button--bg: var(--cp--launch--color);
  --awesome-button--bg--hover: var(--cp--widget--color--active);
  --awesome-button--bg--disabled: var(--cp--widget--color--inactive);
  --awesome-button--bg--held: var(--cp--launch--color--reversed);
  --awesome-button--bg--held--hover: var(--cp--widget--color--reversed--active);
  --awesome-button--border: 1px solid var(--cp--border-color);
  --awesome-button--border-radius: 6px;
}

.launch-button-starfield {
  z-index: 1;
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
  font-size: 16px;
  color: #e2e8f0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.05rem;
  text-transform: uppercase;
  text-align: center;
}

.launch-button:hover .curtain {
  transform: translateY(-100%);
}

.launch-button--disabled:hover .curtain {
  transform: none;
}
</style>
