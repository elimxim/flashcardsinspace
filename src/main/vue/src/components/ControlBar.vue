<template>
  <div
    class="control-bar control-bar--theme"
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
withDefaults(defineProps<{
  title: string
  centerTitle?: boolean
  shadow?: boolean
}>(), {
  centerTitle: false,
  shadow: false,
})

</script>

<style scoped>
.control-bar--theme {
  --c-bar--bg-color: var(--control-bar--bg-color, #f5f5f5);
  --c-bar--title--color: var(--control-bar--title--color, #333333);
  --c-bar--title--font-size: var(--control-bar--title--font-size, clamp(1rem, 1.8vw, 1.2rem));
  --c-bar--shadow-color: var(--control-bar--shadow-color, rgba(0, 0, 0, 0.15));
}

.control-bar {
  position: relative;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background-color: var(--c-bar--bg-color);
  width: 100%;
  height: 40px;
}

.control-bar.shadow {
  box-shadow: 0 3px 3px 0 var(--c-bar--shadow-color);
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
  color: var(--c-bar--title--color);
  font-size: var(--c-bar--title--font-size);
  font-weight: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.control-bar-title.centered {
  grid-column: 1 / -1;
  text-align: center;
}
</style>
