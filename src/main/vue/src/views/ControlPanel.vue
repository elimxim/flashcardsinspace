<template>
  <div class="control-panel control-panel--theme">
    <div class="control-panel-layout">
      <FlashcardSetSideBar ref="sidebar"/>
      <div class="main-panel">
        <div class="topbar">
          <AwesomeButton
            :on-click="sidebar?.toggle"
            :hidden="sidebar?.isExpanded()"
            :disabled="sidebar?.isExpanded()"
            icon="fa-solid fa-bars"
          />
          <div class="topbar-text">
            {{ flashcardSetName }}
          </div>
          <AwesomeButton
            icon="fa-solid fa-gear"
            :on-click="openFlashcardSetSettings"
            :disabled="!flashcardSet"
          />
        </div>
        <div class="launch-panel">
          <div class="calendar-panel">
            <div class="calendar-panel__main">
              <AwesomeButton
                icon="fa-solid fa-calendar-days"
                class="calendar-panel__main__button"
                :disabled="!flashcardSet"
                :on-click="modalStore.toggleCalendar"
              />
              <div class="calendar-panel__main__day">
                <div class="calendar-panel__main__day__text">
                  Day
                </div>
                <div v-if="isCurrDayOff" class="calendar-panel__main__day__vacation">
                  🌴
                </div>
                <div v-else class="calendar-panel__main__day__number">
                  400
                </div>
              </div>
            </div>
            <div class="calendar-panel__stages">
              <div class="calendar-stage">
                <div class="calendar-stage__label">Stage 2</div>
                <div class="calendar-stage__value">20</div>
              </div>
              <div class="calendar-stage">
                <div class="calendar-stage__label">Stage 1</div>
                <div class="calendar-stage__value">200</div>
              </div>
              <div class="calendar-stage">
                <div class="calendar-stage__label">Total</div>
                <div class="calendar-stage__value">220</div>
              </div>
            </div>
          </div>
          <LaunchButton
            label="Start Review"
            :on-click="onLaunch"
            :disabled="false"
          />
        </div>
      </div>
    </div>
  </div>
  <FlashcardSetSettingsModal/>
  <FlashcardSetCreationModal/>
  <FlashcardModificationModal/>
  <CalendarModal/>
</template>

<script setup lang="ts">
import FlashcardSetSideBar from '@/components/control-panel/FlashcardSetSideBar.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import LaunchButton from '@/components/control-panel/LaunchButton.vue'
import FlashcardSetSettingsModal from '@/views/modal/FlashcardSetSettingsModal.vue'
import FlashcardSetCreationModal from '@/views/modal/FlashcardSetCreationModal.vue'
import FlashcardModificationModal from '@/views/modal/FlashcardModificationModal.vue'
import CalendarModal from '@/views/modal/CalendarModal.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref } from 'vue'
import { loadFlashcardSetStore } from '@/shared/stores.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'
import { chronodayStatuses } from '@/core-logic/chrono-logic.ts';

const flashcardStore = useFlashcardStore()
const chronoStore = useChronoStore()
const modalStore = useModalStore()

const { flashcardSet } = storeToRefs(flashcardStore)
const { currDay } = storeToRefs(chronoStore)

const sidebar = ref<InstanceType<typeof FlashcardSetSideBar>>()

const flashcardSetName = computed(() => flashcardSet.value?.name || '')
const isCurrDayOff = computed(() => currDay.value?.status === chronodayStatuses.OFF)
const dayNumber = computed(() => isCurrDayOff.value ? '🌴' : currDay.value?.seqNumber ?? '?')

interface StageReview {
  stage: string
  count: number
}

const stageReviews = computed(() => {
  if (!currDay.value) return []

})

function openFlashcardSetSettings() {
  if (flashcardSet.value) {
    modalStore.toggleFlashcardSetSettings()
  }
}

const onLaunch = () => console.log('🚀 launch!')

onMounted(() => {
  loadFlashcardSetStore()
})
</script>

<style scoped>
.control-panel--theme {
  font-family: var(--main-font-family);
  --panel--bg-color: var(--control-panel--bg-color, #f5f5f5);
  --panel--topbar--bg-color: #f5f5f5;
  --panel--topbar--text-color: #333333;
  --panel--topbar--shadow-color: rgba(0, 0, 0, 0.4);
  --panel--text-color: #333333;
  --panel--border-color: rgba(0, 0, 0, 0.4);
}

.control-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: calc(100vh - var(--navbar-height));
  background-color: var(--panel--bg-color);
}

.control-panel-layout {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
}

.main-panel {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.topbar {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  padding: 10px;
  background-color: var(--panel--topbar--bg-color);
  box-shadow: -4px 0 4px 0 var(--panel--topbar--shadow-color);
  width: 100%;
  height: 40px;
}

.topbar-text {
  font-size: clamp(1rem, 1.8vw, 1.2rem);
  color: var(--panel--topbar--text-color);
  font-weight: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.launch-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px;
  border: 2px solid #0d124a;
  border-radius: 4px;
  margin: 4px;
}

.calendar-panel {
  display: flex;
  padding: 4px;
  gap: 10px;
}

.calendar-panel__main {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 4px;
}

.calendar-panel__main__button {
  flex: 1;
  border: 2px solid #0d124a;
  border-radius: 6px;
  --awesome-button--font-size: clamp(40px, 6vw, 50px);
  --awesome-button--color: #0d124a;
}

.calendar-panel__main__day {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 4px;
}

.calendar-panel__main__day__text {
  font-size: clamp(0.8rem, 1.8vw, 1rem);
}

.calendar-panel__main__day__number {
  font-size: clamp(0.8rem, 1.8vw, 0.9rem);
  font-weight: 600;
  border: 1px solid var(--panel--border-color);
  color: var(--panel--text-color);
  border-radius: 3px;
  padding: 2px;
}

.calendar-panel__main__day__vacation {
  font-size: clamp(0.8rem, 1.8vw, 1rem);
  padding: 2px;
}

.calendar-panel__stages {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  padding: 4px;
  border: 2px solid #0d124a;
  border-radius: 4px;
  gap: 4px;
}

.calendar-stage {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  border: 2px solid #0d124a;
  border-radius: 4px;
  width: 100%;
}
</style>

