<template>
  <div ref="mainPanel" class="main-panel">
    <div class="main-panel-grid" :style="{ '--main-panel--tracks': layout.cols }">
      <div
        v-for="cell in cells"
        :key="cell.widget.id"
        class="main-panel-cell"
        :flip-key="cell.widget.id"
        :style="cell.style"
      >
        <component v-bind="cell.widget.props" :is="cell.widget.component" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import GalleryWidget from '@/components/control-panel/widgets/GalleryWidget.vue'
import FlashcardWidget from '@/components/control-panel/widgets/FlashcardWidget.vue'
import CalendarWidget from '@/components/control-panel/widgets/CalendarWidget.vue'
import ReviewInfoWidget from '@/components/control-panel/widgets/ReviewInfoWidget.vue'
import SpecialStageWidget from '@/components/control-panel/widgets/SpecialStageWidget.vue'
import DayStreakWidget from '@/components/control-panel/widgets/DayStreakWidget.vue'
import LaunchWidget from '@/components/control-panel/widgets/LaunchWidget.vue'
import QuizWidget from '@/components/control-panel/widgets/QuizWidget.vue'
import {
  type Component,
  computed,
  type ComputedRef,
  onMounted,
  onUnmounted,
  ref,
  shallowRef,
  watch,
} from 'vue'
import { specialStages } from '@/core-logic/stage-logic.ts'
import { useFlip } from '@/utils/flip.ts'
import { reviewIcons, ReviewSessionType } from '@/core-logic/review-logic.ts'
import {
  type GridLayout,
  type GridMetrics,
  layoutWidgets,
  type WidgetSizing,
} from '@/utils/widget-grid.ts'

const GRID_METRICS: GridMetrics = {
  // Lets a 360px phone fit 3 cells
  minCellWidth: 104,
  gap: 12,
  // Going from 3 cells to 2 always takes more than 1.5 (1.5 * (w - 12) / (w - 24)),
  // so phones keep 3 cells while desktops may fall back far enough to fill the rows
  maxCellGrowth: 1.5,
}

/** Read by the CSS below via v-bind() */
const minCellCss = `${GRID_METRICS.minCellWidth}px`
const gapCss = `${GRID_METRICS.gap}px`

const SQUARE = { colSpans: [1], rowSpans: [1] }
const STRETCHING_SQUARE = { colSpans: [1, 2], rowSpans: [1] }

const mainPanel = ref<HTMLElement>()
const mainPanelWidth = ref(0)

const { setupAuto } = useFlip(mainPanel)

let resizeObserver: ResizeObserver | null = null

setupAuto()

interface Widget {
  id: string
  component: Component
  colSpans: number[]
  rowSpans: number[]
  maxMoveUp?: number
  props?: Record<string, unknown>
  hidden: ComputedRef<boolean>
}

/** In display order */
const widgets: Widget[] = [
  {
    id: 'gallery',
    component: GalleryWidget,
    ...SQUARE,
    hidden: computed(() => false),
  },
  {
    id: 'unknown',
    component: SpecialStageWidget,
    ...SQUARE,
    props: {
      stage: specialStages.UNKNOWN,
      icon: reviewIcons.get(ReviewSessionType.UNKNOWN),
    },
    hidden: computed(() => false),
  },
  {
    id: 'flashcard',
    component: FlashcardWidget,
    ...SQUARE,
    hidden: computed(() => false),
  },
  {
    id: 'calendar',
    component: CalendarWidget,
    ...SQUARE,
    hidden: computed(() => false),
  },
  {
    id: 'review-info',
    component: ReviewInfoWidget,
    colSpans: [2],
    rowSpans: [1],
    hidden: computed(() => false),
  },
  {
    id: 'day-streak',
    component: DayStreakWidget,
    ...STRETCHING_SQUARE,
    hidden: computed(() => false),
  },
  {
    id: 'launch',
    component: LaunchWidget,
    colSpans: [2],
    rowSpans: [1, 2],
    maxMoveUp: 2,
    hidden: computed(() => false),
  },
  {
    id: 'attempted',
    component: SpecialStageWidget,
    ...SQUARE,
    props: {
      stage: specialStages.ATTEMPTED,
      icon: reviewIcons.get(ReviewSessionType.ATTEMPTED),
    },
    hidden: computed(() => false),
  },
  {
    id: 'quiz',
    component: QuizWidget,
    ...STRETCHING_SQUARE,
    hidden: computed(() => false),
  },
]

const visibleWidgets = computed(() => widgets.filter((widget) => !widget.hidden.value))

const computedLayout = computed<GridLayout>(() => {
  const sizings: WidgetSizing[] = visibleWidgets.value.map(
    ({ id, colSpans, rowSpans, maxMoveUp }) => ({
      id,
      colSpans,
      rowSpans,
      maxMoveUp,
    }),
  )
  return layoutWidgets(sizings, mainPanelWidth.value, GRID_METRICS)
})

/**
 * Replaced only when the placements actually change, so resizing within the
 * same layout doesn't re-render (and FLIP) on every observed width
 */
const layout = shallowRef<GridLayout>(computedLayout.value)

watch(computedLayout, (next) => {
  if (JSON.stringify(next) !== JSON.stringify(layout.value)) {
    layout.value = next
  }
})

const cells = computed(() => {
  const byId = new Map(visibleWidgets.value.map((widget) => [widget.id, widget]))
  return layout.value.placements.flatMap((placement) => {
    const widget = byId.get(placement.id)
    if (!widget) return []
    return [
      {
        widget,
        style: {
          gridColumn: `${placement.col} / span ${placement.colSpan}`,
          gridRow: `${placement.row} / span ${placement.rowSpan}`,
        },
      },
    ]
  })
})

function updateMainPanelWidth() {
  if (mainPanel.value) {
    mainPanelWidth.value = mainPanel.value.offsetWidth
  }
}

onMounted(() => {
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
  --main-panel--min-cell: v-bind(minCellCss);
  --main-panel--max-row: 124px;
  --main-panel--gap: v-bind(gapCss);
  container-type: inline-size;
  border-radius: 4px;
}

.main-panel-grid {
  --main-panel--cell-width: calc(
    (100cqw - (var(--main-panel--tracks) - 1) * var(--main-panel--gap)) / var(--main-panel--tracks)
  );
  display: grid;
  grid-template-columns: repeat(var(--main-panel--tracks), minmax(0, 1fr));
  /* Square cells, but not taller than max-row once there are few columns */
  grid-auto-rows: clamp(
    var(--main-panel--min-cell),
    var(--main-panel--cell-width),
    var(--main-panel--max-row)
  );
  gap: var(--main-panel--gap);
}

.main-panel-cell {
  min-width: 0;
  min-height: 0;
}
</style>
