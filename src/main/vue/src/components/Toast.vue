<template>
  <transition name="toast-fade">
    <div v-if="toast.show" class="toast">
      <span class="toast-message">
        {{ toast.message }}
      </span>
      <button
        class="toast-close-btn"
        @click="toast.hideToast">
        <font-awesome-icon icon="fa-solid fa-xmark"/>
      </button>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { useToastStore } from '@/stores/toast-store'

const toast = useToastStore()
</script>

<style scoped>
.toast {
  position: fixed;
  top: calc(var(--navbar-height) + 1rem);
  right: 1rem;
  display: flex;
  flex-direction: row;
  width: fit-content;
  max-width: 350px;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  color: var(--toast--color);
  background-color: var(--toast--bg-color);
  z-index: 1050;
}

.modal-top-nav button {
  border: none;
  outline: none;
  background: none;
  cursor: pointer;
  font-size: 1.5em;
  color: #c5c5c5;
}

.modal-top-nav button:hover {
  color: #737373;
}

.toast-message {
  flex-grow: 1;
  overflow-wrap: break-word;
  font-family: var(--error-font-family);
  font-size: 0.9rem;
}

/* Close button styling */
.toast-close-btn {
  background: transparent;
  border: none;
  color: var(--toast--color, white);
  cursor: pointer;
  font-size: 1.5rem;
  line-height: 1;
  padding: 0;
  opacity: 0.7;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0; /* Prevent the button from shrinking */
}

.toast-close-btn:hover {
  background-color: rgba(0, 0, 0, 0.1);
  opacity: 1;
}

.toast-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.toast-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.6, -0.28, 0.735, 0.045);
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateX(calc(100% + 1rem)) scale(0.9);
}
</style>
