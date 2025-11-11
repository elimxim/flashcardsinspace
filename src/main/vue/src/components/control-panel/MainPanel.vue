<template>
  <div ref="mainPanel" class="main-panel">
    <div
      v-for="row in widgetRows"
      :key="row.id"
      class="main-panel-row"
    >
      <template
        v-for="widget in row.widgets"
        :key="widget.id"
      >
        <div
          v-if="!widget.hidden.value"
          :class="widget.className"
          :flip-key="widget.id"
        >
          <component v-bind="widget.props" :is="widget.component"/>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import FlashcardWidget from '@/components/control-panel/FlashcardWidget.vue'
import CalendarWidget from '@/components/control-panel/CalendarWidget.vue'
import ReviewInfoWidget from '@/components/control-panel/ReviewInfoWidget.vue'
import SpecialStageWidget from '@/components/control-panel/SpecialStageWidget.vue'
import DayStreakWidget from '@/components/control-panel/DayStreakWidget.vue'
import LaunchWidget from '@/components/control-panel/LaunchWidget.vue'
import { storeToRefs } from 'pinia'
import {
  computed,
  onMounted,
  onUnmounted,
  ref,
  watch,
  type Component,
  ComputedRef,
} from 'vue'
import { loadFlashcardSetStore } from '@/shared/stores.ts'
import { useControlStore } from '@/stores/control-store.ts'
import { specialStages } from '@/core-logic/stage-logic.ts'
import { useFlip } from '@/utils/flip.ts'
import { useChronoStore } from '@/stores/chrono-store.ts'

const controlStore = useControlStore()
const chronoStore = useChronoStore()

const { isSidebarExpanded } = storeToRefs(controlStore)
const { isInitialDay, isDayOff } = storeToRefs(chronoStore)

const mainPanel = ref<HTMLElement>()
const mainPanelWidth = ref(0)

const { setupAuto } = useFlip(mainPanel)

setupAuto()

interface Widget {
  id: string
  component: Component
  className: string
  props?: Record<string, unknown>
  hidden: ComputedRef<boolean>
}

const widgets: Record<string, Widget> = {
  flashcard: {
    id: 'flashcard',
    component: FlashcardWidget,
    className: 'main-panel-square-widget',
    props: {},
    hidden: computed(() => false),
  },
  calendar: {
    id: 'calendar',
    component: CalendarWidget,
    className: 'main-panel-square-widget',
    props: {},
    hidden: computed(() => false),
  },
  reviewInfo: {
    id: 'review-info',
    component: ReviewInfoWidget,
    className: 'main-panel-stretching-widget',
    props: {},
    hidden: computed(() => isInitialDay.value || isDayOff.value),
  },
  launch: {
    id: 'launch',
    component: LaunchWidget,
    className: 'main-panel-stretching-widget',
    props: {},
    hidden: computed(() => false),
  },
  unknown: {
    id: 'unknown',
    component: SpecialStageWidget,
    className: 'main-panel-stretching-widget',
    props: {
      stage: specialStages.UNKNOWN,
      icon: 'fa-regular fa-circle-question',
    },
    hidden: computed(() => false),
  },
  attempted: {
    id: 'attempted',
    component: SpecialStageWidget,
    className: 'main-panel-stretching-widget',
    props: {
      stage: specialStages.ATTEMPTED,
      icon: 'fa-solid fa-rotate-right',
    },
    hidden: computed(() => false),
  },
  dayStreak: {
    id: 'day-streak',
    component: DayStreakWidget,
    className: 'main-panel-widget',
    props: {},
    hidden: isInitialDay,
  },
}

const widgetRows = computed(() => {
  const width = mainPanelWidth.value

  if (width < 540) {
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
.main-panel {
  display: flex;
  flex-direction: column;
  margin-top: 10px;
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
  height: 100%;
  width: fit-content;
}

.main-panel-square-widget {
  flex: 0 0 auto;
  height: 100%;
  aspect-ratio: 1 / 1; /* Fallback for old browsers that miscalculate the width */
}

.main-panel-stretching-widget {
  flex: 1 1 auto;
  height: 100%;
  max-width: 200px;
}
</style>
