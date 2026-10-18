<template>
  <div class="widget-review-badge">
    <Tooltip :text="tooltipText" position="bottom-left">
      <font-awesome-icon
        icon="fa-solid fa-play"
        class="review-badge"
        :class="{ 'review-badge--reversed': reversed }"
      />
    </Tooltip>
  </div>
</template>

<script setup lang="ts">
import Tooltip from '@/components/common/Tooltip.vue'
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    reversed?: boolean
  }>(),
  {
    reversed: false,
  },
)

const tooltipText = computed(() => (props.reversed ? 'Reversed' : ''))
</script>

<style scoped>
.widget-review-badge {
  position: absolute;
  top: 0;
  right: 4px;
}

.review-badge {
  font-size: 0.7rem;
  color: var(--cp--badge--color);
  pointer-events: none;
  transition: transform 0.4s ease-in-out;
}

.review-badge--reversed {
  transform: rotateY(180deg);
}
</style>
