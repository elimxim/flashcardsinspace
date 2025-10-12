<template>
  <div
    class="sidebar sidebar--theme"
    :class="{ 'sidebar--collapsed': !expanded }"
  >
    <div class="sidebar-header">
      <div class="sidebar-header__title">
        Flashcard Sets
      </div>
      <AwesomeButton
        ref="toggleButton"
        icon="fa-solid fa-chevron-left"
        :on-click="toggle"
      />
    </div>
    <div
      class="sidebar-content"
      :class="{ 'sidebar__content--scrollable': expanded && !animating }"
    >
      <div
        v-for="set in flashcardSets"
        :key="set.id"
        class="sidebar-item"
        :class="{ 'sidebar-flashcard-set--active': set.id === flashcardSet?.id }"
        @click="selectFlashcardSet(set.id)"
      >
        <div class="sidebar-item__name">{{ set.name }}</div>
        <div class="sidebar-item__count">{{ getFlashcardCount(set.id) }}</div>
      </div>
    </div>
  </div>
  <SpaceToast/>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import SpaceToast from '@/components/SpaceToast.vue'
import { useFlashcardStore } from '@/stores/flashcard-store.ts'
import { useFlashcardSetStore } from '@/stores/flashcard-set-store.ts'
import { useModalStore } from '@/stores/modal-store.ts'
import { storeToRefs } from 'pinia'
import { computed, onMounted, ref } from 'vue'
import {
  loadFlashcardAndChronoStores,
  loadFlashcardSetStore
} from '@/shared/stores.ts'

const flashcardStore = useFlashcardStore()
const flashcardSetStore = useFlashcardSetStore()
const modalStore = useModalStore()

const { flashcardSet } = storeToRefs(flashcardStore)
const { flashcardSets } = storeToRefs(flashcardSetStore)

let animationTimeout: number | undefined
const animating = ref(false)
const expanded = ref(true)

function toggle() {
  if (animationTimeout) {
    window.clearTimeout(animationTimeout)
  }
  animating.value = true
  expanded.value = !expanded.value
  animationTimeout = window.setTimeout(() => {
    animating.value = false
  }, 400)
}

async function selectFlashcardSet(setId: number) {
  const set = flashcardSetStore.findSet(setId)
  if (set) {
    await loadFlashcardAndChronoStores(set)
  }
}

function getFlashcardCount(setId: number): number {
  return 0
}

defineExpose({
  toggle,
  expanded: computed(() => expanded.value),
})

onMounted(() => {
  loadFlashcardSetStore()
})
</script>


<style scoped>
.sidebar--theme {
  font-family: var(--main-font-family);
  --bar--bg-color: var(--sidebar--bg-color, #fafafa);
  --bar--border-color: var(--sidebar--border-color, #e0e0e0);
  --bar--text-color: var(--sidebar--text-color, #333333);
  --bar--shadow-color: var(--sidebar--shadow-color, rgba(0, 0, 0, 0.1));
  --bar--item--bg-color: var(--sidebar--item--bg-color, white);
  --bar--item--bg-color--hover: var(--sidebar--item--bg-color--hover, #eeeeee);
  --bar--item--bg-color--active: var(--sidebar--item--bg-color--active, #e3f2fd);
  --bar--item--border-color: var(--sidebar--item--border-color, #e0e0e0);
  --bar--item--border-color--active: var(--sidebar--item--border-color--active, #90caf9);
  --bar--item--text-color: var(--sidebar--item--text-color, #333333);
  --bar--item--count-color: var(--sidebar--item--count-color, #666666);
}

.sidebar {
  display: flex;
  flex-direction: column;
  width: 280px;
  height: 100%;
  background-color: var(--bar--bg-color);
  border-right: 1px solid var(--bar--border-color);
  box-shadow: 2px 0 8px var(--bar--shadow-color);
  transition: width 0.35s ease;
  overflow: hidden;
}

.sidebar--collapsed {
  width: 0;
  box-shadow: none;
  border-right: none;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: var(--bar--bg-color);
  box-shadow: 0 2px 4px var(--bar--shadow-color);
  flex-shrink: 0;
}

.sidebar-header__title {
  font-size: clamp(0.9rem, 1.5vw, 1.1rem);
  color: var(--bar--text-color);
  font-weight: normal;
  white-space: nowrap;
}

.sidebar-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  flex: 1;
  overflow: hidden;
}

.sidebar__content--scrollable {
  overflow-y: auto;
}

.sidebar-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  cursor: pointer;
  background-color: var(--bar--item--bg-color);
  border: 1px solid var(--bar--item--border-color);
  border-radius: 6px;
  transition: all 0.1s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.sidebar-item:hover {
  background-color: var(--bar--item--bg-color--hover);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.sidebar-flashcard-set--active {
  background-color: var(--bar--item--bg-color--active);
  border-color: var(--bar--item--border-color--active);
}

.sidebar-item__name {
  flex: 1;
  color: var(--bar--item--text-color);
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 0.5rem;
}

.sidebar-item__count {
  color: var(--bar--item--count-color);
  font-size: 0.85rem;
  font-weight: 500;
  min-width: 30px;
  text-align: right;
}
</style>
