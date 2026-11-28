<template>
  <SideBar ref="sidebar" v-model:expanded="expanded" title="FILTERS">
    <template #left>
      <AwesomeButton
        icon="fa-regular fa-circle-xmark"
        class="control-bar-button"
        tooltip="Clear Filters"
        tooltip-position="bottom-right"
        :on-click="clear"
      />
    </template>
    <div class="gallery-filter">
      <template v-if="UXConfig().showZoomSlider">
        <div class="gallery-filter-zoom">
          <font-awesome-icon icon="fa-solid fa-magnifying-glass-minus" />
          <SliderLine v-model="zoom" class="gallery-filter-zoom-slider" label="Zoom" />
          <font-awesome-icon icon="fa-solid fa-magnifying-glass-plus" />
        </div>
        <div class="gallery-filter-divider" />
      </template>
      <template v-if="selectedCount > 0">
        <div class="gallery-filter-row">
          <Toggle v-model="selected" class="gallery-filter-toggle" size="M">
            <span class="gallery-filter-label">Selected</span>
          </Toggle>
          <span class="cp-count-box">{{ selectedCount }}</span>
        </div>
        <div class="gallery-filter-divider" />
      </template>
      <div class="gallery-filter-row">
        <Toggle v-model="unknown" class="gallery-filter-toggle" size="M">
          <span class="gallery-filter-label">{{ specialStages.UNKNOWN.displayName }}</span>
        </Toggle>
        <span class="cp-count-box">{{ countOf(specialStages.UNKNOWN) }}</span>
      </div>
      <div class="gallery-filter-row">
        <Toggle v-model="attempted" class="gallery-filter-toggle" size="M">
          <span class="gallery-filter-label">{{ specialStages.ATTEMPTED.displayName }}</span>
        </Toggle>
        <span class="cp-count-box">{{ countOf(specialStages.ATTEMPTED) }}</span>
      </div>
      <div class="gallery-filter-divider" />
      <div v-for="stage in learningStageArray" :key="stage.name" class="gallery-filter-row">
        <SmartCheckbox
          class="gallery-filter-checkbox"
          :model-value="filter.stages.includes(stage.name)"
          :label="stage.displayName"
          @update:model-value="(checked: boolean) => setStage(stage, checked)"
        />
        <span class="cp-count-box">{{ countOf(stage) }}</span>
      </div>
      <div class="gallery-filter-divider" />
      <div class="gallery-filter-row">
        <Toggle v-model="outerSpace" class="gallery-filter-toggle" size="M">
          <span class="gallery-filter-label">{{ specialStages.OUTER_SPACE.displayName }}</span>
        </Toggle>
        <span class="cp-count-box">{{ countOf(specialStages.OUTER_SPACE) }}</span>
      </div>
    </div>
  </SideBar>
</template>

<script setup lang="ts">
import SideBar from '@/components/common/SideBar.vue'
import AwesomeButton from '@/components/common/AwesomeButton.vue'
import Toggle from '@/components/common/Toggle.vue'
import SmartCheckbox from '@/components/common/SmartCheckbox.vue'
import SliderLine from '@/components/common/SliderLine.vue'
import { UXConfig } from '@/utils/device-utils.ts'
import { computed, ref } from 'vue'
import { type Stage, learningStageArray, specialStages } from '@/core-logic/stage-logic.ts'
import { type GalleryFilter } from '@/components/gallery/FlashcardsViewer.vue'

const filter = defineModel<GalleryFilter>('filter', { required: true })
const expanded = defineModel<boolean>('expanded', { required: true })
const zoom = defineModel<number>('zoom', { required: true })

const props = defineProps<{
  counts: Map<string, number>
  selectedCount: number
}>()

const sidebar = ref<InstanceType<typeof SideBar>>()
const isOverlay = computed(() => sidebar.value?.isOverlay ?? false)

const selected = computed({
  get: () => filter.value.selected,
  set: (value: boolean) => (filter.value = { ...filter.value, selected: value }),
})

const unknown = computed({
  get: () => filter.value.unknown,
  set: (value: boolean) => (filter.value = { ...filter.value, unknown: value }),
})

const attempted = computed({
  get: () => filter.value.attempted,
  set: (value: boolean) => (filter.value = { ...filter.value, attempted: value }),
})

const outerSpace = computed({
  get: () => filter.value.outerSpace,
  set: (value: boolean) => (filter.value = { ...filter.value, outerSpace: value }),
})

function setStage(stage: Stage, checked: boolean) {
  const others = filter.value.stages.filter((name) => name !== stage.name)
  filter.value = { ...filter.value, stages: checked ? [...others, stage.name] : others }
}

function countOf(stage: Stage): number {
  return props.counts.get(stage.name) ?? 0
}

function clear() {
  filter.value = {
    selected: false,
    unknown: false,
    attempted: false,
    stages: [],
    outerSpace: false,
  }
}

function toggle() {
  sidebar.value?.toggle()
}

defineExpose({
  toggle,
  isOverlay,
})
</script>

<style scoped>
.gallery-filter {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px 16px;
}

.gallery-filter-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.gallery-filter-zoom {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  color: #808daa;
}

.gallery-filter-zoom-slider {
  flex: 1;
  --slider-line--color-track: #575e68;
  --slider-line--color-fill: #8a9cff;
  --slider-line--color-thumb: #e6e8f0;
}

.gallery-filter-toggle {
  --toggle--color-on: #8a9cff;
  --toggle--color-border-on: #8a9cff;
  --toggle--color-off: #575e68;
  --toggle--color-border-off: #575e68;
}

.gallery-filter-checkbox {
  --smart-checkbox--size: 22px;
  --smart-checkbox--color-unchecked: #808daa;
  --smart-checkbox--color-unchecked--hover: #b6c1ff;
  --smart-checkbox--color-checked: #8a9cff;
  --smart-checkbox--color-checked--hover: #b6c1ff;
  --smart-checkbox--label--color: #e6e8f0;
}

.gallery-filter-label {
  color: #e6e8f0;
  font-size: clamp(0.9rem, 2vh, 1rem);
}

.gallery-filter-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.15);
}
</style>
