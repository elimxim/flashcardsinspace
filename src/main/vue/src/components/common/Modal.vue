<template>
  <Teleport to="#app">
    <component :is="frame" :visible="visible" :focus-on="focusOn" v-bind="frameProps">
      <template #header>
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
      </template>
      <div v-if="title" class="modal-title select-text">
        {{ title }}
      </div>
      <div class="modal-body" :style="{ overflow }">
        <slot />
      </div>
    </component>
  </Teleport>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import SmartButton from '@/components/common/SmartButton.vue'
import ModalSheet from '@/components/common/ModalSheet.vue'
import ModalWindow from '@/components/common/ModalWindow.vue'
import { onUnmounted, onMounted } from 'vue'
import { UXConfig } from '@/utils/device-utils.ts'

const props = withDefaults(
  defineProps<{
    visible: boolean
    title?: string
    focusOn?: Pick<HTMLElement, 'focus'>
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

const isSheet = UXConfig().showSheetModals
const frame = isSheet ? ModalSheet : ModalWindow
const frameProps = isSheet ? { onDismiss: pressExit } : {}

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
.modal-topline {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
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
}
</style>
