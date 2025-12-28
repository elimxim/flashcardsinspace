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
import QuizWidget from '@/components/control-panel/QuizWidget.vue'
import {
  computed,
  onMounted,
  onUnmounted,
  ref,
  type Component,
  ComputedRef,
} from 'vue'
import { loadFlashcardSetStore } from '@/utils/stores.ts'
import { specialStages } from '@/core-logic/stage-logic.ts'
import { useFlip } from '@/utils/flip.ts'

const FIRST_REARRANGE_BREAKPOINT = 1000
const SECOND_REARRANGE_BREAKPOINT = 490
const THIRD_REARRANGE_BREAKPOINT = 342

const mainPanel = ref<HTMLElement>()
const mainPanelWidth = ref(0)
const rowHeight = ref(106)
const rowHeightPx = computed(() => `${rowHeight.value}px`)

const { setupAuto } = useFlip(mainPanel, { duration: 1 })

let resizeObserver: ResizeObserver | null = null

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
    hidden: computed(() => false),
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
    className: 'main-panel-square-widget',
    props: {
      stage: specialStages.UNKNOWN,
      icon: 'fa-regular fa-circle-question',
    },
    hidden: computed(() => false),
  },
  attempted: {
    id: 'attempted',
    component: SpecialStageWidget,
    className: 'main-panel-square-widget',
    props: {
      stage: specialStages.ATTEMPTED,
      icon: 'fa-solid fa-rotate-right',
    },
    hidden: computed(() => false),
  },
  dayStreak: {
    id: 'day-streak',
    component: DayStreakWidget,
    className: 'main-panel-square-widget',
    props: {},
    hidden: computed(() => false),
  },
  quiz: {
    id: 'quiz-widget',
    component: QuizWidget,
    className: 'main-panel-square-widget',
    props: {},
    hidden: computed(() => false),
  },
}

const widgetRows = computed(() => {
  const width = mainPanelWidth.value

  if (width < THIRD_REARRANGE_BREAKPOINT) {
    return [
      {
        id: 'row-1',
        widgets: [
          widgets.flashcard,
          widgets.calendar,
        ],
      },
      {
        id: 'row-2',
        widgets: [
          widgets.reviewInfo,
          widgets.dayStreak,
        ],
      },
      {
        id: 'row-3',
        widgets: [
          widgets.launch,
          widgets.quiz,
        ],
      },
      {
        id: 'row-4',
        widgets: [
          widgets.unknown,
          widgets.attempted,
        ],
      },

    ]
  }

  if (width < SECOND_REARRANGE_BREAKPOINT) {
    return [
      {
        id: 'row-1',
        widgets: [
          widgets.flashcard,
          widgets.calendar,
          widgets.dayStreak,
        ],
      },
      {
        id: 'row-2',
        widgets: [
          widgets.reviewInfo,
          widgets.launch,
        ],
      },
      {
        id: 'row-3',
        widgets: [
          widgets.unknown,
          widgets.attempted,
          widgets.quiz,
        ],
      },
    ]
  }

  if (width < FIRST_REARRANGE_BREAKPOINT) {
    return [
      {
        id: 'row-1',
        widgets: [
          widgets.flashcard,
          widgets.calendar,
          widgets.dayStreak,
          widgets.reviewInfo,
        ],
      },
      {
        id: 'row-2',
        widgets: [
          widgets.unknown,
          widgets.attempted,
          widgets.quiz,
          widgets.launch,
        ],
      },
    ]
  }

  return [
    {
      id: 'row-1',
      widgets: [
        widgets.flashcard,
        widgets.calendar,
        widgets.dayStreak,
        widgets.reviewInfo,
        widgets.launch,
        widgets.unknown,
        widgets.attempted,
        widgets.quiz,
      ],
    },
  ]
})

function updateMainPanelWidth() {
  if (mainPanel.value) {
    mainPanelWidth.value = mainPanel.value.offsetWidth
  }
}

onMounted(() => {
  loadFlashcardSetStore()
  updateMainPanelWidth()

  resizeObserver = new ResizeObserver(() => {
    updateMainPanelWidth()
  })

  if (mainPanel.value) {
    resizeObserver.observe(mainPanel.value)
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
})
</script>

<style scoped>
.main-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  border-radius: 4px;
}

.main-panel-row {
  display: flex;
  justify-content: flex-start;
  gap: 12px;
  height: v-bind(rowHeightPx);
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
