<template>
  <div
    class="control-bar"
    :class="{ 'shadow': shadow }"
  >
    <div class="left-controls">
      <slot name="left"/>
    </div>
    <div
      class="control-bar-title"
      :class="{ 'centered': centerTitle }"
    >
      {{ title }}
    </div>
    <div class="right-controls">
      <slot name="right"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  title: string
  centerTitle?: boolean
  centerTitlePadding?: number
  shadow?: boolean
}>(), {
  centerTitle: false,
  centerTitlePadding: 0,
  shadow: false,
})

const centerTitlePaddingPx = computed(() => `${props.centerTitlePadding}px`)

</script>

<style scoped>
.control-bar {
  position: relative;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 4px 10px;
  background-color: var(--control-bar--bg-color);
  width: 100%;
  height: 40px;
}

.control-bar.shadow {
  box-shadow: 0 3px 3px 0 var(--control-bar--shadow-color);
}

.left-controls {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
  grid-column: 1;
  grid-row: 1;
  z-index: 1;
  height: 100%;
}

.right-controls {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
  grid-column: 3;
  grid-row: 1;
  z-index: 1;
  height: 100%;
}

.control-bar-title {
  grid-column: 2;
  grid-row: 1;
  text-align: left;
  color: var(--control-bar--title--color);
  font-size: var(--control-bar--title--font-size);
  font-weight: 300;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.control-bar-title.centered {
  grid-column: 1 / -1;
  text-align: center;
  padding-inline: v-bind(centerTitlePaddingPx);
}
</style>
