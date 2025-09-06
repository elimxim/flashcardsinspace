<template>
  <div class="space-container">
    <transition name="toast-transition">
    <div
      v-if="show"
      class="space-toast space-toast-theme"
      :toast-type="toast?.type"
      @mouseenter="toaster.pause()"
      @mouseleave="toaster.resume()"
    >
      <Starfield twinkle verticalDrift="2px"/>
      <div class="space-toast__mixin"/>
      <div class="space-toast__content">
        <div class="space-toast__content__icon-box">
          <font-awesome-icon v-if="toast?.type === ToastType.SUCCESS" icon="fa-solid fa-circle-check"/>
          <font-awesome-icon v-else-if="toast?.type === ToastType.ERROR" icon="fa-solid fa-circle-exclamation"/>
          <font-awesome-icon v-else-if="toast?.type === ToastType.INFO" icon="fa-solid fa-info-circle"/>
          <font-awesome-icon v-else-if="toast?.type === ToastType.WARNING" icon="fa-solid fa-triangle-exclamation"/>
          <font-awesome-icon v-else icon="fa-solid fa-circle-question"/>
        </div>
        <div class="tb-body">
          <div class="space-toast__content__title">
            {{ toast?.title }}
          </div>
          <div class="space-toast__content__msg">
            {{ toast?.message }}
          </div>
        </div>
        <button class="space-toast__content__button" @click="toaster.dismiss()">
          <font-awesome-icon icon="fa-solid fa-xmark"/>
        </button>
      </div>
      <div v-if="toast !== null && !toast.persistent" class="space-toast__progress">
        <Progressbar
          height="8px"
          :duration="toast?.duration"
          :paused="paused"
        />
      </div>
    </div>
  </transition>
  </div>
</template>

<script setup lang="ts">
import Starfield from './Starfield.vue'
import Progressbar from '@/components/Progressbar.vue'
import { onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { ToastType, useSpaceToaster } from '@/stores/toast-store.ts'

const toaster = useSpaceToaster()
const { toast, show, paused } = storeToRefs(toaster)

onUnmounted(() => toaster.reset())
</script>

<style scoped>
.space-container {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: calc(100vh - var(--navbar-height));
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1rem;
  padding: 1rem;
  z-index: 1000;
  pointer-events: none;
}

.space-toast-theme {
  --default-color-from: #000000;
  --default-color-via: #000000;
  --default-color-to: #000000;
  --default-color-glow: rgba(0, 0, 0, 0.3);
  --default-toast--bg: rgba(0, 0, 0, 0.8);
  --default-icon-box--color: rgba(255, 255, 255, 0.8);
  --default-icon-box--bg: rgba(0, 0, 0, 0.1);
  --default-icon-box--glow: rgba(255, 255, 255, 0.10);
  --default-title--color: rgba(0, 0, 0, 0.9);
  --default-msg--color: rgba(226, 232, 240, 0.9);
  --default-button--color: rgba(0, 0, 0, 0.9);
  --default-button--hover--color: white;
  --default-button--hover--bg: rgba(255, 255, 255, 0.1);
  --default-progressbar--bg-color: none;
  --default-starfield__star--color: white;
}

.space-toast-theme[toast-type="success"] {
  --color-from: var(--success-toast--from, var(--default-color-from));
  --color-via: var(--success-toast--via, var(--default-color-via));
  --color-to: var(--success-toast--to, var(--default-color-to));
  --color-glow: var(--success-toast--glow, var(--default-color-glow));
  --toast--bg: var(--space-toast--bg, var(--default-toast--bg));
  --icon-box--color: var(--space-toast--icon-box--color, var(--default-icon-box--color));
  --icon-box--bg: var(--space-toast--icon-box--bg, var(--default-icon-box--bg));
  --icon-box--glow: var(--space-toast--icon-box--glow, var(--default-icon-box--glow));
  --title--color: var(--space-toast--title--color, var(--default-title--color));
  --msg--color: var(--space-toast--msg--color, var(--default-msg--color));
  --button--color: var(--space-toast--button--color, var(--default-button--color));
  --button--hover--color: var(--space-toast--button--hover--color, var(--default-button--hover--color));
  --button--hover--bg: var(--space-toast--button--hover--bg, var(--default-button--hover--bg));
  --progressbar--bg-color: var(--space-toast--progressbar--bg-color, var(--default-progressbar--bg-color));
  --starfield__star--color: var(--space-toast--starfield__star--color, var(--default-starfield__star--color));
}

.space-toast-theme[toast-type="error"] {
  --color-from: var(--error-toast--from);
  --color-via: var(--error-toast--via);
  --color-to: var(--error-toast--to);
  --color-glow: var(--error-toast--glow);
  --toast--bg: var(--space-toast--bg, var(--default-toast--bg));
  --icon-box--color: var(--space-toast--icon-box--color, var(--default-icon-box--color));
  --icon-box--bg: var(--space-toast--icon-box--bg, var(--default-icon-box--bg));
  --icon-box--glow: var(--space-toast--icon-box--glow, var(--default-icon-box--glow));
  --title--color: var(--space-toast--title--color, var(--default-title--color));
  --msg--color: var(--space-toast--msg--color, var(--default-msg--color));
  --button--color: var(--space-toast--button--color, var(--default-button--color));
  --button--hover--color: var(--space-toast--button--hover--color, var(--default-button--hover--color));
  --button--hover--bg: var(--space-toast--button--hover--bg, var(--default-button--hover--bg));
  --progressbar--bg-color: var(--space-toast--progressbar--bg-color, var(--default-progressbar--bg-color));
  --starfield__star--color: var(--space-toast--starfield__star--color, var(--default-starfield__star--color));
}

.space-toast-theme[toast-type="info"] {
  --color-from: var(--info-toast--from);
  --color-via: var(--info-toast--via);
  --color-to: var(--info-toast--to);
  --color-glow: var(--info-toast--glow);
  --toast--bg: var(--space-toast--bg, var(--default-toast--bg));
  --icon-box--color: var(--space-toast--icon-box--color, var(--default-icon-box--color));
  --icon-box--bg: var(--space-toast--icon-box--bg, var(--default-icon-box--bg));
  --icon-box--glow: var(--space-toast--icon-box--glow, var(--default-icon-box--glow));
  --title--color: var(--space-toast--title--color, var(--default-title--color));
  --msg--color: var(--space-toast--msg--color, var(--default-msg--color));
  --button--color: var(--space-toast--button--color, var(--default-button--color));
  --button--hover--color: var(--space-toast--button--hover--color, var(--default-button--hover--color));
  --button--hover--bg: var(--space-toast--button--hover--bg, var(--default-button--hover--bg));
  --progressbar--bg-color: var(--space-toast--progressbar--bg-color, var(--default-progressbar--bg-color));
  --starfield__star--color: var(--space-toast--starfield__star--color, var(--default-starfield__star--color));
}

.space-toast-theme[toast-type="warning"] {
  --color-from: var(--warning-toast--from);
  --color-via: var(--warning-toast--via);
  --color-to: var(--warning-toast--to);
  --color-glow: var(--warning-toast--glow);
  --toast--bg: var(--space-toast--bg, var(--default-toast--bg));
  --icon-box--color: var(--space-toast--icon-box--color, var(--default-icon-box--color));
  --icon-box--bg: var(--space-toast--icon-box--bg, var(--default-icon-box--bg));
  --icon-box--glow: var(--space-toast--icon-box--glow, var(--default-icon-box--glow));
  --title--color: var(--space-toast--title--color, var(--default-title--color));
  --msg--color: var(--space-toast--msg--color, var(--default-msg--color));
  --button--color: var(--space-toast--button--color, var(--default-button--color));
  --button--hover--color: var(--space-toast--button--hover--color, var(--default-button--hover--color));
  --button--hover--bg: var(--space-toast--button--hover--bg, var(--default-button--hover--bg));
  --progressbar--bg-color: var(--space-toast--progressbar--bg-color, var(--default-progressbar--bg-color));
  --starfield__star--color: var(--space-toast--starfield__star--color, var(--default-starfield__star--color));
}

.space-toast {
  position: relative;
  width: 360px;
  border-radius: 1rem;
  overflow: hidden;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  background: var(--toast--bg);
  box-shadow: 0 0 0 0 var(--color-glow);
  pointer-events: auto;
}

.space-toast__mixin {
  position: absolute;
  inset: -1px;
  border-radius: 1rem;
  opacity: 0.40;
  pointer-events: none;
  background-image: linear-gradient(
    45deg,
    var(--color-from),
    var(--color-via),
    var(--color-to)
  );
}

.space-toast__content {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  margin: 1rem;
  z-index: 10;
}

.space-toast__content__icon-box {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 36px;
  max-height: 36px;
  min-width: 36px;
  max-width: 36px;
  font-size: 1.25rem;
  border-radius: 10px;
  color: var(--icon-box--color);
  background: var(--icon-box--bg);
  box-shadow: inset 0 0 0 1px var(--icon-box--glow);
}

.space-toast__content__title {
  color: var(--title--color);
  font-weight: 600;
  line-height: 1.5;
}

.space-toast__content__msg {
  color: var(--msg--color);
  line-height: 1.5;
}

.space-toast__content__msg:empty {
  display: none;
}

.space-toast__content__button {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 24px;
  max-height: 24px;
  min-width: 24px;
  max-width: 24px;
  border-radius: 9999px;
  padding: 0.375rem;
  font-size: 0.9rem;
  color: var(--button--color);
  opacity: 1;
  background: transparent;
  border: none;
}

.space-toast__content__button:hover {
  color: var(--button--hover--color);
  background: var(--button--hover--bg);
}

.space-toast__progress {
  position: relative;
  margin-top: 0.75rem;
  --progressbar--from: var(--color-from);
  --progressbar--via: var(--color-via);
  --progressbar--to: var(--color-to);
  --progressbar--bg-color: var(--default-progressbar--bg-color);
}

/* transition> */

.toast-transition-enter-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.toast-transition-leave-active {
  transition: all 0.4s cubic-bezier(0.6, -0.28, 0.735, 0.045);
}

.toast-transition-enter-from,
.toast-transition-leave-to {
  transform: translateX(calc(100% + 1rem)) scale(0.9);
}

/* <transition */

</style>
