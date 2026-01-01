<template>
  <div ref="swipeTape" class="swipe-tape">
    <div
      ref="trackRef"
      class="tape-track"
      :style="tapeStyle"
    >
      <div
        v-for="(frame, index) in frames"
        :key="index"
        class="tape-frame"
      >
        <slot :frame="frame" :index="index"/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" generic="T">
import { computed, ref } from 'vue'
import { useTape } from '@/utils/use-tape.ts'

const props = withDefaults(defineProps<{
  frames: T[]
  snapDuration?: number
  threshold?: number
  frameGap?: number
  frameKey?: (frame: T, index: number) => string,
  onSwipeLeft?: () => void
  onSwipeRight?: () => void
}>(), {
  snapDuration: 200,
  threshold: 100,
  frameGap: 10,
  frameKey: (_: T, index: number) => `${index}`,
  onSwipeLeft: () => {
  },
  onSwipeRight: () => {
  },
})

const swipeTape = ref<HTMLElement>()
const trackRef = ref<HTMLElement>()
const frameGapPx = computed(() => `${props.frameGap}px`)
const doubleFrameGapPx = computed(() => `${props.frameGap * 2}px`)

const frameWidth = computed(() => swipeTape.value?.offsetWidth ?? 300)

const {
  tapeStyle,
  triggerSwipe,
} = useTape({
  element: trackRef,
  threshold: props.threshold,
  snapDuration: props.snapDuration,
  pageWidth: frameWidth,
  pageGap: props.frameGap,
  onSwipeLeft: props.onSwipeLeft,
  onSwipeRight: props.onSwipeRight,
})

defineExpose({
  triggerSwipe,
})

</script>

<style scoped>
.swipe-tape {
  flex: 1;
  position: relative;
  min-height: 0;
}

.tape-track {
  position: absolute;
  inset: 0;
  display: flex;
  gap: v-bind(frameGapPx);
  width: calc(300% + v-bind(doubleFrameGapPx));
  will-change: transform;
}

.tape-frame {
  flex: 1;
  min-width: 0;
}

</style>

