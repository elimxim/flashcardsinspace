<template>
  <div class="control-bar control-bar--theme">
    <slot name="left"/>
    <div
      class="control-bar-title"
      :class="{ 'flex-grow': !centerTitle }"
    >
      {{ title }}
    </div>
    <slot name="right"/>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  centerTitle?: boolean
}>(), {
  centerTitle: false,
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  padding: 10px;
  background-color: var(--c-bar--bg-color);
  box-shadow: 0 2px 3px 2px var(--c-bar--shadow-color);
  width: 100%;
  height: 40px;
}

.control-bar-title {
  color: var(--c-bar--title--color);
  font-size:var(--c-bar--title--font-size);
  font-weight: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.control-bar-title.flex-grow {
  flex: 1;
}
</style>
