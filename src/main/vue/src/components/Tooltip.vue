<template>
  <div
    class="tooltip tooltip--theme"
    @mouseenter="show"
    @mouseleave="hide"
  >
    <slot/>
    <template v-if="isHoverSupported">
      <transition name="tooltip-fade">
        <div
          v-if="visible && text"
          :class="['tooltip-message', `tooltip--${position}`]"
        >
          {{ text }}
          <div class="tooltip-arrow"/>
        </div>
      </transition>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { isHoverSupported } from '@/utils/utils.ts'

const props = withDefaults(defineProps<{
  text?: string
  delay?: number
  position?: 'top' | 'bottom' | 'left' | 'right' | 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right'
}>(), {
  text: undefined,
  delay: 1000,
  position: 'top',
})

const visible = ref(false)
let timeout: ReturnType<typeof setTimeout> | null = null

function show() {
  timeout = setTimeout(() => {
    visible.value = true
  }, props.delay)
}

function hide() {
  if (timeout) {
    clearTimeout(timeout)
    timeout = null
  }
  visible.value = false
}
</script>

<style scoped>
.tooltip--theme {
  --tooltip--color: rgba(51, 51, 51, 0.9);
  --tooltip--bg-color: rgba(255, 255, 255, 0.5);
  --tooltip--border-color: rgb(158, 158, 158);
  --tooltip--padding: 0.75rem 1rem;
  --tooltip--border-radius: 12px;
  --tooltip--font-size: 0.9rem;
  --tooltip--box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  --tooltip--arrow-size: 6px;
}

.tooltip {
  position: relative;
  display: inline-block;
}

.tooltip-message {
  position: absolute;
  color: var(--tooltip--color);
  background-color: var(--tooltip--bg-color);
  border: 1px solid var(--tooltip--border-color);
  padding: var(--tooltip--padding);
  border-radius: var(--tooltip--border-radius);
  font-size: var(--tooltip--font-size);
  box-shadow: var(--tooltip--box-shadow);
  text-wrap: nowrap;
  pointer-events: none;
  backdrop-filter: blur(2px);
  z-index: 1000;
}

.tooltip-arrow {
  position: absolute;
  width: 0;
  height: 0;
  border-style: solid;
}

.tooltip--top {
  bottom: calc(100% + 10px);
  left: 50%;
  transform: translateX(-50%);
}

.tooltip--top .tooltip-arrow {
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-width: 8px 8px 0 8px;
  border-color: var(--tooltip--border-color) transparent transparent transparent;
}

.tooltip--bottom {
  top: calc(100% + 10px);
  left: 50%;
  transform: translateX(-50%);
}

.tooltip--bottom .tooltip-arrow {
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-width: 0 8px 8px 8px;
  border-color: transparent transparent var(--tooltip--border-color) transparent;
}

.tooltip--left {
  right: calc(100% + 10px);
  top: 50%;
  transform: translateY(-50%);
}

.tooltip--left .tooltip-arrow {
  left: 100%;
  top: 50%;
  transform: translateY(-50%);
  border-width: 8px 0 8px 8px;
  border-color: transparent transparent transparent var(--tooltip--border-color);
}

.tooltip--right {
  left: calc(100% + 10px);
  top: 50%;
  transform: translateY(-50%);
}

.tooltip--right .tooltip-arrow {
  right: 100%;
  top: 50%;
  transform: translateY(-50%);
  border-width: 8px 8px 8px 0;
  border-color: transparent var(--tooltip--border-color) transparent transparent;
}

.tooltip--top-left {
  bottom: calc(100% + 10px);
  right: 0;
}

.tooltip--top-left .tooltip-arrow {
  top: 100%;
  right: 10px;
  border-width: 8px 8px 0 8px;
  border-color: var(--tooltip--border-color) transparent transparent transparent;
}

.tooltip--top-right {
  bottom: calc(100% + 10px);
  left: 0;
}

.tooltip--top-right .tooltip-arrow {
  top: 100%;
  left: 10px;
  border-width: 8px 8px 0 8px;
  border-color: var(--tooltip--border-color) transparent transparent transparent;
}

.tooltip--bottom-left {
  top: calc(100% + 10px);
  right: 0;
}

.tooltip--bottom-left .tooltip-arrow {
  bottom: 100%;
  right: 10px;
  border-width: 0 8px 8px 8px;
  border-color: transparent transparent var(--tooltip--border-color) transparent;
}

.tooltip--bottom-right {
  top: calc(100% + 10px);
  left: 0;
}

.tooltip--bottom-right .tooltip-arrow {
  bottom: 100%;
  left: 10px;
  border-width: 0 8px 8px 8px;
  border-color: transparent transparent var(--tooltip--border-color) transparent;
}

.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: opacity 0.2s ease-in-out;
}

.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
}
</style>

