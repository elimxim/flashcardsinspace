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
      <div class="modal-top-nav">
        <button @click="onPressExit">
          <font-awesome-icon icon="fa-solid fa-xmark"/>
        </button>
      </div>
      <div class="modal-title" v-if="title !== null">{{ title }}</div>
      <div class="modal-content">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    required: true,
  },
  focusable: {
    type: Boolean,
    required: false,
    default: true,
  },
  title: {
    type: String,
    required: false,
    default: null,
  },
  onPressExit: {
    type: Function,
    required: false,
    default: () => {
    },
  },
  onPressEnter: {
    type: Function,
    required: false,
    default: () => {
    },
  },
  onPressDelete: {
    type: Function,
    required: false,
    default: () => {
    },
  }
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

const modalOverlay = ref<HTMLElement>()

watch(() => props.visible, (newValue) => {
  if (props.focusable && newValue) {
    nextTick(() => {
      modalOverlay.value?.focus()
    })
  }
})

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    event.preventDefault()
    event.stopPropagation()
    onPressExit()
  } else if (event.key === 'Enter') {
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
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  width: 100vw;
  height: 100vh;
}

.modal-window {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 10%;
  display: flex;
  flex-direction: column;
  padding: 2px;
  resize: both;
  max-width: 90vw;
  width: fit-content;
  min-width: fit-content;
  max-height: 90vh;
  height: fit-content;
  min-height: fit-content;
  overflow: auto;
  user-select: none;
}

.modal-top-nav {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: right;
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

.modal-title {
  flex: 1;
  text-align: center;
  font-size: 1.5em;
  font-weight: bold;
  padding-left: 20px;
  padding-right: 20px;
}

.modal-content {
  flex: 100;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

</style>
