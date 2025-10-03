<template>
  <div
    class="flashcard flashcard--theme"
    :class="{ 'flashcard--flipped': flipped }"
    @click="flip"
  >
    <div class="flashcard__flipper">
      <div
        class="flashcard__face flashcard__face--front"
        :class="{
          'flashcard__face--front--style': !transparent,
          'flashcard__face--transparent': transparent,
        }"
      >
        <div class="flashcard__strip select-none">
          <span v-if="!textOnly" class="flashcard__corner-text">
            {{ stage }}
          </span>
          <button
            v-if="!textOnly"
            class="flashcard__edit-button"
            @click.stop="onEdit()"
          >
            <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
          </button>
        </div>
        <div class="flashcard__body">
          {{ frontSide }}
        </div>
        <div class="flashcard__strip select-none">
          <span v-if="!textOnly">
            <font-awesome-icon icon="fa-regular fa-eye"/>
            {{ viewedTimes }}
          </span>
        </div>
      </div>
      <div
        class="flashcard__face flashcard__face--back"
        :class="{
          'flashcard__face--back--style': !transparent,
          'flashcard__face--transparent': transparent,
        }"
      >
        <div class="flashcard__strip select-none">
          <span v-if="!textOnly" class="flashcard__corner-text">
            {{ stage }}
          </span>
          <button
            v-if="!textOnly"
            class="flashcard__edit-button"
            @click.stop="onEdit()"
          >
            <font-awesome-icon icon="fa-solid fa-pen-to-square"/>
          </button>
        </div>
        <div class="flashcard__body">
          {{ backSide }}
        </div>
        <div class="flashcard__strip select-none">
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
.flashcard {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  will-change: transform;
  transform-style: preserve-3d;
  perspective: 1000px;
}

.flashcard--theme {
  --default-flashcard__front--bg-color: white;
  --default-flashcard__back--bg-color: white;
  --default-flashcard--color: #686868;
  --default-flashcard--color--strip: #9f9f9f;
  --default-flashcard--color--strip--hover: #686868;
  --default-flashcard--box-shadow: 0 8px 12px rgba(0, 0, 0, 0.15);
  --default-flashcard--box-shadow--hover: 0 12px 16px rgba(0, 0, 0, 0.2);
}

.flashcard--flipped .flashcard__flipper {
  transform: rotateY(180deg);
}

.flashcard__flipper {
  flex: 1;
  transition: transform 0.5s cubic-bezier(0.25, 1, 0.5, 1);
  transform-style: preserve-3d;
  position: relative;
  will-change: transform;
}

.flashcard__face {
  position: absolute;
  width: 100%;
  height: 100%;
  padding: 0.4rem;
  backface-visibility: hidden;
  display: flex;
  flex-direction: column;
  border-radius: 24px;
  overflow-wrap: break-word;
  border-color: var(--flashcard--border-color);
  border-style: solid;
  border-width: 1px;
}

.flashcard__face--front {
  transform: rotateY(0deg);
}

.flashcard__face--back {
  transform: rotateY(180deg);
}

.flashcard__face--front--style {
  background-color: var(--flashcard__front--bg-color, var(--default-flashcard__front--bg-color));
  background-image: var(--flashcard__front--bg-image);
  background-size: var(--flashcard__front--bg-size);
  box-shadow: var(--flashcard--box-shadow, var(--default-flashcard--box-shadow));
  transition: box-shadow 0.2s ease-in-out;
}

.flashcard__face--back--style {
  background-color: var(--flashcard__back--bg-color, var(--default-flashcard__back--bg-color));
  box-shadow: var(--flashcard--box-shadow, var(--default-flashcard--box-shadow));
  transition: box-shadow 0.2s ease-in-out;
}

.flashcard:hover .flashcard__face--front--style,
.flashcard:hover .flashcard__face--back--style {
  box-shadow: var(--flashcard--box-shadow--hover, var(--default-flashcard--box-shadow--hover));
}

.flashcard__face--transparent {
  background: none;
  border: none;
  cursor: default;
  perspective: none;
}

.flashcard__strip {
  height: fit-content;
  font-size: clamp(1.1rem, 2vw, 1.2rem);
  color: var(--flashcard--color--strip, var(--default-flashcard--color--strip));
  display: flex;
  justify-content: space-between;
  padding-left: 4px;
  padding-right: 4px;
  gap: 10px;
}

.flashcard__body {
  flex: 1;
  width: 100%;
  height: 100%;
  font-size: clamp(1.4rem, 2vw, 1.8rem);
  color: var(--flashcard--color, var(--default-flashcard--color));
  text-align: center;
  white-space: pre-wrap;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.flashcard__corner-text {
  background: none;
  font-size: inherit;
  color: inherit;
}

.flashcard__edit-button {
  border: none;
  outline: none;
  background: none;
  cursor: pointer;
  font-size: inherit;
  color: inherit;
}

.flashcard__edit-button:hover {
  color: var(--flashcard--color--strip--hover, var(--default-flashcard--color--strip--hover));
}
</style>
