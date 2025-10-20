<template>
  <div
    v-if="visible"
    ref="modalOverlay"
    class="modal-overlay"
    role="dialog"
    tabindex="-1"
  >
    <div ref="modalWindow" class="modal-window" tabindex="-1">
      <div class="modal-top-control">
        <slot name="control"/>
        <AwesomeButton
          icon="fa-solid fa-circle-xmark"
          :on-click="pressExit"
        />
      </div>
      <div v-if="title" class="modal-title">
        {{ title }}
      </div>
      <div class="modal-body">
        <slot/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { nextTick, ref, watch, onUnmounted, onMounted } from 'vue'

const props = withDefaults(defineProps<{
  visible: boolean
  title?: string
  focusOn?: HTMLElement
  onPressExit?: () => void
  onPressEnter?: () => void
  onPressDelete?: () => void
}>(), {
  title: '',
  focusOn: undefined,
  onPressExit: () => {
  },
  onPressEnter: () => {
  },
  onPressDelete: () => {
  },
})

const modalWindow = ref<HTMLDivElement>()

function pressExit() {
  props.onPressExit()
}

function pressEnter() {
  props.onPressEnter()
}

function pressDelete() {
  props.onPressDelete()
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    nextTick().then(() => {
      if (props.focusOn) {
        props.focusOn.focus()
      } else {
        modalWindow.value?.focus()
      }
    })
  }
})

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event: KeyboardEvent) {
  if (!props.visible) return

  if (event.key === 'Escape') {
    event.preventDefault()
    event.stopPropagation()
    pressExit()
  } else if (event.key === 'Enter' && !event.ctrlKey) {
    if (!(event.target instanceof HTMLTextAreaElement)) {
      event.preventDefault()
      event.stopPropagation()
      pressEnter()
    }
  } else if (event.key === 'Enter' && event.ctrlKey) {
    event.preventDefault()
    event.stopPropagation()
    pressEnter()
  } else if (event.key === 'Delete') {
    event.preventDefault()
    event.stopPropagation()
    pressDelete()
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
  padding: 10px;
  margin: 10px 10px clamp(10px, 10vh, 100px);
  user-select: none;
  resize: none;
  outline: none;
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
  margin-bottom: 10px;
}

.modal-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  min-width: 0;
}
</style>
