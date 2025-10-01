<template>
  <div
    v-if="visible"
    ref="modalOverlay"
    class="modal-overlay"
    role="dialog"
    tabindex="-1"
    @keydown="handleKeydown"
  >
    <div class="modal-window">
      <div class="modal-top-control">
        <slot name="control"/>
        <AwesomeButton
          icon="fa-solid fa-circle-xmark"
          :onClick="onPressExit"
        />
      </div>
      <div class="modal-title" v-if="title">
        {{ title }}
      </div>
      <div class="modal-body">
        <slot ref="slot"/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { nextTick, ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  visible: boolean
  title?: string
  focusable?: boolean
  onPressExit?: () => void
  onPressEnter?: () => void
  onPressDelete?: () => void
}>(), {
  title: '',
  focusable: false,
  onPressExit: () => {
  },
  onPressEnter: () => {
  },
  onPressDelete: () => {
  },
})

function onPressExit() {
  props.onPressExit()
}

function onPressEnter() {
  props.onPressEnter()
}

function onPressDelete() {
  props.onPressDelete()
}

const slot = ref<HTMLElement>()

watch(() => props.visible, (newVal) => {
  if (props.focusable && newVal) {
    nextTick().then(() => {
      slot.value?.focus()
    })
  }
})

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    event.preventDefault()
    event.stopPropagation()
    onPressExit()
  } else if (event.key === 'Enter' && !event.ctrlKey) {
    if (event.target !instanceof HTMLInputElement) {
      event.preventDefault()
      event.stopPropagation()
      onPressEnter()
    }
  } else if (event.key === 'Enter' && event.ctrlKey) {
    event.preventDefault()
    event.stopPropagation()
    onPressEnter()
  } else if (event.key === 'Delete') {
    event.preventDefault()
    event.stopPropagation()
    onPressDelete()
  }
}

</script>

<style scoped>
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: grid;
  place-items: center;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  z-index: 900;
}

.modal-window {
  background: #fff;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  width: clamp(240px, 92vw, 540px);
  min-width: 240px;
  height: clamp(360px, 90vh, 480px);
  min-height: 360px;
  padding: 10px;
  margin: 10px 10px clamp(10px, 10vh, 100px);
  user-select: none;
  resize: none;
  overflow: hidden;
}

.modal-top-control {
  display: flex;
  align-items: center;
  justify-content: right;
  gap: 8px;
}

.modal-title {
  text-align: center;
  font-size: 1.5em;
  font-weight: bold;
}

.modal-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
  gap: 10px;
}
</style>

<style>
.modal-main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
}

.modal-main-area--inner {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.modal-error-text {
  color: var(--text--error--color);
  font-size: clamp(0.8rem, 1.6vw, 0.9rem);
  margin: 0;
  padding: 0;
}

.modal-control-buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 10px;
}

.modal-input {
  font-size: clamp(0.9rem, 2vh, 1rem);
}
</style>
