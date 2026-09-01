<template>
  <div v-if="visible" ref="modalOverlay" class="modal-overlay" role="dialog" tabindex="-1">
    <div ref="modalWindow" class="modal-window touch-none" tabindex="-1">
      <div class="modal-topline">
        <div class="modal-controls modal-controls--left">
          <slot name="controls-left" />
        </div>
        <div class="modal-controls modal-controls--center">
          <slot name="controls-center" />
        </div>
        <div class="modal-controls modal-controls--right">
          <slot name="controls-right" />
          <AwesomeButton icon="fa-solid fa-circle-xmark" :on-click="pressExit" />
        </div>
      </div>
      <div v-if="title" class="modal-title select-text">
        {{ title }}
      </div>
      <div class="modal-body">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import SmartButton from '@/components/common/SmartButton.vue'
import { nextTick, ref, watch, onUnmounted, onMounted } from 'vue'

const props = withDefaults(
  defineProps<{
    visible: boolean
    title?: string
    focusOn?: HTMLElement
    overflow?: string
    exitButton?: InstanceType<typeof SmartButton>
    enterButton?: InstanceType<typeof SmartButton>
    deleteButton?: InstanceType<typeof SmartButton>
    onExit?: () => void
  }>(),
  {
    title: '',
    focusOn: undefined,
    overflow: 'auto',
    exitButton: undefined,
    enterButton: undefined,
    deleteButton: undefined,
    onExit: () => {},
  },
)

const modalWindow = ref<HTMLDivElement>()

function pressExit() {
  props.exitButton?.click()
  props.onExit()
}

function pressEnter() {
  props.enterButton?.click()
}

function holdDelete(event: Event) {
  props.deleteButton?.hold(event)
}

function releaseDelete() {
  props.deleteButton?.release()
}

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      nextTick().then(() => {
        if (props.focusOn) {
          props.focusOn.focus()
        } else {
          modalWindow.value?.focus()
        }
      })
    }
  },
)

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('keyup', handleKeyUp)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('keyup', handleKeyUp)
})

function handleKeydown(event: KeyboardEvent) {
  if (!props.visible) return

  if (event.key === 'Escape') {
    event.preventDefault()
    event.stopPropagation()
    pressExit()
  } else if (event.key === 'Enter') {
    if (event.ctrlKey || !(event.target instanceof HTMLTextAreaElement)) {
      event.preventDefault()
      event.stopPropagation()
      pressEnter()
    }
  } else if (event.key === 'Delete') {
    event.preventDefault()
    event.stopPropagation()
    holdDelete(event)
  }
}

function handleKeyUp(event: KeyboardEvent) {
  if (!props.visible) return

  if (event.key === 'Delete') {
    event.preventDefault()
    event.stopPropagation()
    releaseDelete()
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
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 900;
}

.modal-window {
  position: relative;
  background: #fff;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  width: clamp(240px, 92vw, 540px);
  min-width: 240px;
  height: clamp(360px, 90vh, 480px);
  min-height: 360px;
  padding: 8px;
  margin: 10px 10px clamp(10px, 10vh, 100px);
  user-select: none;
  resize: none;
  outline: none;
}

.modal-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 2px;
}

.modal-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.modal-controls--left {
  justify-content: left;
}

.modal-controls--center {
  justify-content: center;
}

.modal-controls--right {
  justify-content: right;
}

.modal-title {
  text-align: center;
  font-size: 1.5em;
  font-weight: bold;
  margin-bottom: 10px;
}

.modal-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  min-width: 0;
  padding: 2px;
  overflow: v-bind(overflow);
}
</style>
