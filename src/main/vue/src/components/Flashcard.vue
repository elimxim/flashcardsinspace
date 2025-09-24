<template>
  <div
    class="flashcard"
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
        <div class="flashcard__strip">
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
        <div class="flashcard__strip">
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
        <div class="flashcard__strip">
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
        <div class="flashcard__strip">
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
  editFormWasOpened?: boolean
  onEdit?: Function
}>(), {
  stage: undefined,
  frontSide: undefined,
  backSide: undefined,
  viewedTimes: undefined,
  textOnly: false,
  unflippable: false,
  transparent: false,
  editFormWasOpened: false,
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
}

.flashcard__face--front {
  transform: rotateY(0deg);
}

.flashcard__face--back {
  transform: rotateY(180deg);
}

.flashcard__face--front--style {
  background-color: #fdfdfd;
  background-image: linear-gradient(0deg, rgba(34, 34, 34, 0.15) 1px, transparent 1px);
  background-size: 100% 28px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.flashcard__face--back--style {
  background-color: #fdfdfd;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.flashcard__face--transparent {
  background: none;
  cursor: default;
  perspective: none;
}

.flashcard__strip {
  height: fit-content;
  font-size: clamp(1rem, 2vw, 1.2rem);
  color: #9f9f9f;
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
  font-size: clamp(1.2rem, 2vw, 1.8rem);
  color: #686868;
  text-align: center;
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
  color: #686868;
}
</style>
