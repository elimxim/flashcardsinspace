<template>
  <div class="control-panel control-panel--theme">
    <div class="control-panel-layout">
      <SideBar ref="sidebar"/>
      <div class="control-panel-content">
        <ControlBar :title="flashcardSetName">
          <template #left>
            <AwesomeButton
              icon="fa-solid fa-bars"
              class="control-bar-button"
              :on-click="sidebar?.toggle"
              :hidden="isSidebarExpanded"
              :disabled="isSidebarExpanded"
            />
          </template>
          <template #right>
            <AwesomeButton
              icon="fa-solid fa-gear"
              class="control-bar-button"
              :on-click="openFlashcardSetSettings"
              :disabled="!flashcardSet"
            />
          </template>
        </ControlBar>
        <FlashcardInfoBar :hidden="!flashcardSet || isSidebarExpanded"/>
        <div ref="mainPanel" class="main-panel">
          <div
            v-for="row in widgetRows"
            :key="row.id"
            class="main-panel-row"
          >
            <div
              v-for="widget in row.widgets"
              :key="widget.id"
              :flip-key="widget.id"
              :class="widget.className"
            >
              <component :is="widget.component" v-bind="widget.props"/>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <FlashcardSetSettingsModal/>
</template>

<script setup lang="ts">
import SideBar from '@/components/control-panel/SideBar.vue'
import ControlBar from '@/components/ControlBar.vue'
import FlashcardInfoBar from '@/components/control-panel/FlashcardInfoBar.vue'
import FlashcardWidget from '@/components/control-panel/FlashcardWidget.vue'
import CalendarWidget from '@/components/control-panel/CalendarWidget.vue'
import ReviewInfoWidget from '@/components/control-panel/ReviewInfoWidget.vue'
import SpecialStageWidget from '@/components/control-panel/SpecialStageWidget.vue'
import DayStreakWidget from '@/components/control-panel/DayStreakWidget.vue'
import LaunchWidget from '@/components/control-panel/LaunchWidget.vue'
import AwesomeButton from '@/components/AwesomeButton.vue'
import FlashcardSetSettingsModal from '@/views/modal/FlashcardSetSettingsModal.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useToggleStore } from '@/stores/toggle-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, onUnmounted, ref, watch, type Component } from 'vue'
import { loadFlashcardSetStore } from '@/shared/stores.ts'
import { useControlStore } from '@/stores/control-store.ts'
import { specialStages } from '@/core-logic/stage-logic.ts'
import { useFlip } from '@/utils/flip.ts'

const flashcardStore = useFlashcardStore()
const toggleStore = useToggleStore()
const controlStore = useControlStore()

const { flashcardSet } = storeToRefs(flashcardStore)
const { isSidebarExpanded } = storeToRefs(controlStore)

const sidebar = ref<InstanceType<typeof SideBar>>()
const mainPanel = ref<HTMLElement>()
const flashcardSetName = computed(() => flashcardSet.value?.name || '')
const mainPanelWidth = ref(0)

const { setupAuto } = useFlip(mainPanel)

setupAuto()

interface Widget {
  id: string
  component: Component
  className: string
  props?: Record<string, any>
}

const widgets: Record<string, Widget> = {
  flashcard: {
    id: 'flashcard',
    component: FlashcardWidget,
    className: 'main-panel-widget',
    props: {}
  },
  calendar: {
    id: 'calendar',
    component: CalendarWidget,
    className: 'main-panel-widget',
    props: {}
  },
  reviewInfo: {
    id: 'review-info',
    component: ReviewInfoWidget,
    className: 'main-panel-stretching-widget',
    props: {}
  },
  launch: {
    id: 'launch',
    component: LaunchWidget,
    className: 'main-panel-stretching-widget',
    props: {}
  },
  unknown: {
    id: 'unknown',
    component: SpecialStageWidget,
    className: 'main-panel-stretching-widget',
    props: { stage: specialStages.UNKNOWN, icon: 'fa-regular fa-circle-question' }
  },
  attempted: {
    id: 'attempted',
    component: SpecialStageWidget,
    className: 'main-panel-stretching-widget',
    props: { stage: specialStages.ATTEMPTED, icon: 'fa-solid fa-rotate-right' }
  },
  dayStreak: {
    id: 'day-streak',
    component: DayStreakWidget,
    className: 'main-panel-widget',
    props: {}
  },
}

const widgetRows = computed(() => {
  const width = mainPanelWidth.value

  if (width < 580) {
    return [
      {
        id: 'row-1',
        widgets: [widgets.flashcard, widgets.calendar, widgets.dayStreak],
      },
      {
        id: 'row-2',
        widgets: [widgets.reviewInfo, widgets.launch],
      },
      {
        id: 'row-3',
        widgets: [widgets.unknown, widgets.attempted],
      },
    ]
  }

  if (width < 1000) {
    return [
      {
        id: 'row-1',
        widgets: [widgets.flashcard, widgets.calendar, widgets.reviewInfo, widgets.launch],
      },
      {
        id: 'row-2',
        widgets: [widgets.unknown, widgets.attempted, widgets.dayStreak],
      },
    ]
  }

  return [
    {
      id: 'row-1',
      widgets: [
        widgets.flashcard,
        widgets.calendar,
        widgets.reviewInfo,
        widgets.launch,
        widgets.unknown,
        widgets.attempted,
        widgets.dayStreak,
      ],
    },
  ]
})

function openFlashcardSetSettings() {
  if (flashcardSet.value) {
    toggleStore.toggleFlashcardSetSettings()
  }
}

function updateMainPanelWidth() {
  if (mainPanel.value) {
    mainPanelWidth.value = mainPanel.value.offsetWidth
  }
}

function handleResize() {
  updateMainPanelWidth()
}

watch(isSidebarExpanded, () => {
  setTimeout(() => {
    updateMainPanelWidth()
  }, 300) // Match sidebar transition duration
})

onMounted(() => {
  loadFlashcardSetStore()
  updateMainPanelWidth()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.control-panel--theme {
  --c-panel--text-color: #333333;
  --c-panel--bg-color: var(--control-panel--bg-color, #f5f5f5);
}

.control-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: calc(100vh - var(--navbar-height));
  background-color: var(--c-panel--bg-color);
}

.control-panel-layout {
  position: relative;
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 100%;
}

.control-panel-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-panel {
  display: flex;
  flex-direction: column;
  margin-top: 10px;
  padding: 6px 16px;
  gap: 16px;
  border-radius: 4px;
}

.main-panel-row {
  display: flex;
  justify-content: flex-start;
  gap: 16px;
  height: 106px;
}

.main-panel-widget {
  flex: 0 0 auto;
}

.main-panel-stretching-widget {
  flex: 1 1 auto;
  max-width: 200px;
}

</style>

