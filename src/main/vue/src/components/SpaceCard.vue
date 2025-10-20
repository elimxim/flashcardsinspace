<template>
  <div
    class="space-card space-card--theme"
    :class="{ 'space-card--flipped': flipped }"
    @click="flip"
  >
    <div class="space-card-flipper">
      <div
        class="space-card-face space-card-face--front"
        :class="{
          'space-card-face--front--style': !transparent,
          'space-card-face--transparent': transparent,
        }"
      >
        <div class="space-card-strip select-none">
          <span v-if="!textOnly" class="space-card-strip-text">
            {{ stage }}
          </span>
          <AwesomeButton
            v-if="!textOnly"
            icon="fa-solid fa-pen-to-square"
            class="space-card-edit-button"
            :on-click="onEdit"
          />
        </div>
        <div class="space-card-body">
          {{ frontSide }}
        </div>
        <div class="space-card-strip select-none">
          <span v-if="!textOnly">
            <font-awesome-icon icon="fa-regular fa-eye"/>
            {{ viewedTimes }}
          </span>
        </div>
      </div>
      <div
        class="space-card-face space-card-face--back"
        :class="{
          'space-card-face--back--style': !transparent,
          'space-card-face--transparent': transparent,
        }"
      >
        <div class="space-card-strip select-none">
          <span v-if="!textOnly" class="space-card-strip-text">
            {{ stage }}
          </span>
          <AwesomeButton
            v-if="!textOnly"
            icon="fa-solid fa-pen-to-square"
            class="space-card-edit-button"
            :on-click="onEdit"
          />
        </div>
        <div class="space-card-body">
          {{ backSide }}
        </div>
        <div class="space-card-strip select-none">
          <span v-if="!textOnly">
            <font-awesome-icon icon="fa-regular fa-eye"/>
            {{ viewedTimes }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AwesomeButton from '@/components/AwesomeButton.vue'
import { ref } from 'vue'

const props = withDefaults(defineProps<{
  stage?: string
  frontSide?: string
  backSide?: string
  viewedTimes?: number
  textOnly?: boolean
  unflippable?: boolean
  transparent?: boolean
  onEdit?: () => void
}>(), {
  stage: undefined,
  frontSide: undefined,
  backSide: undefined,
  viewedTimes: undefined,
  textOnly: false,
  unflippable: false,
  transparent: false,
  onEdit: () => {
  },
})

const flipped = ref(false)

function flip() {
  if (!props.unflippable) {
    flipped.value = !flipped.value
  }
}

function flipToFront() {
  if (!props.unflippable) {
    flipped.value = false
  }
}

async function flipToFrontAndWait(): Promise<void> {
  return new Promise((resolve) => {
    if (flipped.value && !props.unflippable) {
      flipped.value = false
      setTimeout(() => resolve(void 0), 500)
    } else {
      resolve(void 0)
    }
  })
}

defineExpose({
  flip,
  flipToFront,
  flipToFrontAndWait,
})

</script>

<style scoped>
.space-card--theme {
  --card--color: var(--space-card--color, #686868);
  --card--color--strip: var(--space-card--color--strip, #9f9f9f);
  --card--color--strip--hover: var(--space-card--color--strip--hover, #686868);
  --card--border-color: var(--space-card--border-color, none);
  --card--box-shadow: var(--space-card--box-shadow, 0 8px 12px rgba(0, 0, 0, 0.15));
  --card--box-shadow--hover: var(--flashcard--box-shadow--hover, 0 12px 16px rgba(0, 0, 0, 0.2));
  --card--front--bg-color: var(--space-card--front--bg-color, white);
  --card--front--bg-image: var(--space-card--front--bg-image, none);
  --card--front--bg-size: var(--space-card--front--bg-size, none);
  --card--back--bg-color: var(--space-card--back--bg-color, white);
}

.space-card {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  will-change: transform;
  transform-style: preserve-3d;
  perspective: 1000px;
  z-index: 10;
}

.space-card--flipped .space-card-flipper {
  transform: rotateY(180deg);
}

.space-card-flipper {
  flex: 1;
  transition: transform 0.5s cubic-bezier(0.25, 1, 0.5, 1);
  transform-style: preserve-3d;
  position: relative;
  will-change: transform;
}

.space-card-face {
  position: absolute;
  width: 100%;
  height: 100%;
  padding: 0.4rem;
  backface-visibility: hidden;
  display: flex;
  flex-direction: column;
  border-radius: 24px;
  overflow-wrap: break-word;
  border-color: var(--card--border-color);
  border-style: solid;
  border-width: 1px;
}

.space-card-face--front {
  transform: rotateY(0deg);
}

.space-card-face--back {
  transform: rotateY(180deg);
}

.space-card-face--front--style {
  background-color: var(--card--front--bg-color);
  background-image: var(--card--front--bg-image);
  background-size: var(--card--front--bg-size);
  box-shadow: var(--card--box-shadow);
  transition: box-shadow 0.2s ease-in-out;
}

.space-card-face--back--style {
  background-color: var(--card--back--bg-color);
  box-shadow: var(--card--box-shadow);
  transition: box-shadow 0.2s ease-in-out;
}

.space-card:hover .space-card-face--front--style,
.space-card:hover .space-card-face--back--style {
  box-shadow: var(--card--box-shadow--hover);
}

.space-card-face--transparent {
  background: none;
  border: none;
  cursor: default;
  perspective: none;
}

.space-card-strip {
  height: fit-content;
  font-size: clamp(1.1rem, 2vw, 1.2rem);
  color: var(--card--color--strip);
  display: flex;
  justify-content: space-between;
  padding-left: 4px;
  padding-right: 4px;
  gap: 10px;
}

.space-card-body {
  flex: 1;
  width: 100%;
  height: 100%;
  font-size: clamp(1.4rem, 2vw, 1.8rem);
  color: var(--card--color);
  text-align: center;
  white-space: pre-wrap;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.space-card-strip-text {
  background: none;
  font-size: inherit;
  color: inherit;
}

.space-card-edit-button {
  --awesome-button--icon--size: inherit;
  --awesome-button--icon--color: var(--card--color--strip);
  --awesome-button--icon--color--hover: var(--card--color--strip--hover);
}

</style>
