<template>
  <button
    type="button"
    class="grid-tile"
    :class="{
      'grid-tile--selecting': selecting,
      'grid-tile--selected': selected,
    }"
    @click="onClick(flashcard)"
  >
    <span class="grid-tile-text">{{ flashcard.frontSide }}</span>
    <font-awesome-icon v-if="selected" icon="fa-solid fa-circle-check" class="grid-tile-check" />
  </button>
</template>

<script setup lang="ts">
import { type Flashcard } from '@/model/flashcard.ts'

withDefaults(
  defineProps<{
    flashcard: Flashcard
    selecting?: boolean
    selected?: boolean
    onClick?: (flashcard: Flashcard) => void
  }>(),
  {
    selecting: false,
    selected: false,
    onClick: () => {},
  },
)
</script>

<style scoped>
.grid-tile {
  position: relative;
  aspect-ratio: 4 / 3;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border: none;
  border-radius: 10px;
  background: white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  container-type: inline-size;
  outline: none;
  cursor: zoom-in;
  transition: box-shadow 0.2s ease-in-out;
}

@media (hover: hover) {
  .grid-tile:hover {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  }
}

.grid-tile--selecting {
  cursor: pointer;
}

.grid-tile--selected {
  background: #f5f5f8;
}

.grid-tile--selected .grid-tile-text {
  opacity: 0.7;
}

.grid-tile-check {
  position: absolute;
  right: 6px;
  bottom: 6px;
  font-size: clamp(14px, 12cqi, 24px);
  color: #007bff;
  background: white;
  border-radius: 50%;
}

.grid-tile-text {
  max-height: 100%;
  overflow: hidden;
  color: #2f3340;
  font-size: clamp(0.5rem, 11cqi, 1.3rem);
  line-height: 1.25;
  text-align: center;
  overflow-wrap: anywhere;
}
</style>
